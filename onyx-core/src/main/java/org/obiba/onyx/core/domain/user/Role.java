/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class Role extends AbstractEntity implements Serializable, Comparable<Role> {

  private static final long serialVersionUID = -5985745491689725964L;

  public static final Role SYSTEM_ADMINISTRATOR = new Role("SYSTEM_ADMINISTRATOR");

  public static final Role PARTICIPANT_MANAGER = new Role("PARTICIPANT_MANAGER");

  public static final Role DATA_COLLECTION_OPERATOR = new Role("DATA_COLLECTION_OPERATOR");

  @Column(nullable = false)
  private String name = null;

  public Role() {
  }

  public Role(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String pName) {
    name = pName;
  }

  @Override
  public String toString() {
    return name;
  }

  public int compareTo(Role o) {
    return name.compareTo(o.name);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Role) {
      Role o = (Role) obj;
      return name.equals(o.name);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

}
