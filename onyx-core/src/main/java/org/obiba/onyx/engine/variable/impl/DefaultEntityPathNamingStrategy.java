/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.impl;

import org.obiba.onyx.engine.variable.Entity;
import org.obiba.onyx.engine.variable.IEntityPathNamingStrategy;
import org.w3c.dom.Node;

/**
 * 
 */
public class DefaultEntityPathNamingStrategy implements IEntityPathNamingStrategy {

  public static final String PATH_SEPARATOR = "/";

  private String rootName;

  public String getPath(Entity entity) {
    if(entity.getParent() != null) {
      return getPath(entity.getParent()) + getPathSeparator() + entity.getName();
    } else {
      return getPathSeparator() + entity.getName();
    }
  }

  public String getPath(Node entityNode) {
    String name = getEntityName(entityNode);
    if(name != null) {
      String path = getPathSeparator() + name;
      if(entityNode.getParentNode() != null) {
        String parentPath = getPath(entityNode.getParentNode());
        if(parentPath != null) {
          return parentPath + path;
        }
      }
      return path;
    }
    return null;
  }

  private String getEntityName(Node node) {
    if(node.getAttributes() != null) {
      Node nameAttr = node.getAttributes().getNamedItem("name");
      if(nameAttr != null) {
        return nameAttr.getNodeValue();
      }
    }
    return null;
  }

  public static DefaultEntityPathNamingStrategy getInstance(String rootName) {
    DefaultEntityPathNamingStrategy strategy = new DefaultEntityPathNamingStrategy();
    strategy.rootName = rootName;
    return strategy;
  }

  protected String getPathSeparator() {
    return PATH_SEPARATOR;
  }

  protected String normalizeName(String name) {
    return name;
  }

  public void setRootName(String rootName) {
    this.rootName = rootName;
  }

  public String getRootName() {
    return rootName;
  }

}
