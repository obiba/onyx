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

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * 
 */
public class DefaultVariablePathNamingStrategy implements IVariablePathNamingStrategy {

  private static final Logger log = LoggerFactory.getLogger(DefaultVariablePathNamingStrategy.class);

  public static final String PATH_SEPARATOR = "/";

  private String rootName;

  public String getPath(Variable entity) {
    String name = normalizeName(entity.getName());
    if(entity.getParent() != null) {
      return getPath(entity.getParent()) + getPathSeparator() + name;
    } else {
      return getPathSeparator() + name;
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

  public static DefaultVariablePathNamingStrategy getInstance(String rootName) {
    DefaultVariablePathNamingStrategy strategy = new DefaultVariablePathNamingStrategy();
    strategy.rootName = rootName;
    return strategy;
  }

  public String getPathSeparator() {
    return PATH_SEPARATOR;
  }

  public String normalizeName(String name) {
    return name.replace(' ', '_');
  }

  public void setRootName(String rootName) {
    this.rootName = rootName;
  }

  public String getRootName() {
    return rootName;
  }

  public Variable getVariable(Variable entity, String path) {
    if(entity == null) return null;
    // find the root
    Variable root = entity;
    while(root.getParent() != null) {
      root = root.getParent();
    }
    log.debug("root.name={}", root);

    // split the path
    List<String> entityNames = new ArrayList<String>();
    for(String str : path.split(getPathSeparator())) {
      if(str.length() > 0) {
        entityNames.add(str);
      }
    }
    log.debug("entityNames={}", entityNames);
    if(entityNames.size() == 0) return null;
    if(!normalizeName(root.getName()).equals(entityNames.get(0))) return null;

    Variable current = root;
    for(int i = 1; i < entityNames.size(); i++) {
      String entityName = entityNames.get(i);
      log.debug("entityName={}", entityName);
      log.debug("current.name={}", current);
      boolean found = false;
      for(Variable child : current.getVariables()) {
        log.debug("  child.name={}", child);
        if(normalizeName(child.getName()).equals(entityName)) {
          current = child;
          found = true;
          break;
        }
      }
      if(!found) {
        current = null;
        break;
      }
    }

    log.debug(">>> current.name={}", current);

    return current;
  }

}
