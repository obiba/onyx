/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 
 */
@XStreamAlias("entity")
public class Entity implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final String SCHEME = "onyx";

  @XStreamAsAttribute
  private String name;

  @XStreamOmitField
  private Entity parent;

  @XStreamImplicit
  private List<Entity> entities;

  public Entity(String name) {
    this(name, null);
  }

  public Entity(String name, Entity parent) {
    super();
    this.name = name;
    this.parent = parent;
  }

  public List<Entity> getEntities() {
    return entities != null ? entities : (entities = new ArrayList<Entity>());
  }

  public Entity addEntity(Entity child) {
    if(child != null) {
      getEntities().add(child);
      child.setParent(this);
    }
    return child;
  }

  public Entity addEntity(String path, String separator) {
    String[] names = path.split(separator);
    Entity current = this;
    for(String name : names) {
      boolean found = false;
      for(Entity child : current.getEntities()) {
        if(child.getName().equals(name)) {
          current = child;
          found = true;
          break;
        }
      }
      if(!found) {
        current = current.addEntity(new Entity(name));
      }
    }
    return current;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Entity getParent() {
    return parent;
  }

  public void setParent(Entity parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    return name;
  }

}
