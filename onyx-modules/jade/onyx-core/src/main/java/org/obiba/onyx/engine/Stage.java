package org.obiba.onyx.engine;

import javax.persistence.MappedSuperclass;

import org.obiba.core.domain.AbstractEntity;

@MappedSuperclass
public class Stage extends AbstractEntity {

  private static final long serialVersionUID = 8309472904104798783L;

  private String name;

  private String module;

  private String description;
  
  private Integer pos;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getPos() {
    return pos;
  }

  public void setPos(Integer pos) {
    this.pos = pos;
  }

}
