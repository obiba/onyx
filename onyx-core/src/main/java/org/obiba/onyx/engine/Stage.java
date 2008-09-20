package org.obiba.onyx.engine;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.obiba.core.domain.AbstractEntity;

/**
 * Stage is associated to a module, through its name.
 * 
 * @see Module
 */
@Entity
public class Stage extends AbstractEntity {

  private static final long serialVersionUID = 8309472904104798783L;

  private String name;

  private String module;

  private String description;

  private Integer displayOrder;

  @OneToOne(cascade = CascadeType.ALL)
  private StageDependencyCondition stageDependencyCondition;

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

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public StageDependencyCondition getStageDependencyCondition() {
    return stageDependencyCondition;
  }

  public void setStageDependencyCondition(StageDependencyCondition stageDependencyCondition) {
    this.stageDependencyCondition = stageDependencyCondition;
  }

  @Override
  public String toString() {
    return module + ":" + name;
  }
}
