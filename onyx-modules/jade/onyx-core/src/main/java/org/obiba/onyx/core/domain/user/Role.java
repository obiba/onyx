package org.obiba.onyx.core.domain.user;

import java.io.Serializable;

public class Role implements Serializable, Comparable<Role> {

  private static final long serialVersionUID = -5985745491689725964L;

  public static final Role SYSTEM_ADMINISTRATOR = new Role("SYSTEM_ADMINISTRATOR");

  public static final Role PARTICIPANT_MANAGER = new Role("PARTICIPANT_MANAGER");

  public static final Role DATA_COLLECTION_OPERATOR = new Role("DATA_COLLECTION_OPERATOR");

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
