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
import org.obiba.onyx.engine.variable.EntityPathNamingStrategy;
import org.w3c.dom.Node;

/**
 * 
 */
public class DefaultPathEntityNamingStrategy implements EntityPathNamingStrategy {

  public static final String PATH_SEPARATOR = "/";

  public String getPath(Entity entity) {
    if(entity.getParent() != null) {
      return getPath(entity.getParent()) + PATH_SEPARATOR + entity.getName();
    } else {
      return PATH_SEPARATOR + entity.getName();
    }
  }

  public String getPath(Node entityNode) {
    String name = getEntityName(entityNode);
    if(name != null) {
      String path = PATH_SEPARATOR + name;
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

  public static DefaultPathEntityNamingStrategy getInstance() {
    return new DefaultPathEntityNamingStrategy();
  }

}
