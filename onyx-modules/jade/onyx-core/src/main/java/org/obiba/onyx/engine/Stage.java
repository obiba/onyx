package org.obiba.onyx.engine;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;

/**
 * Stage is associated to a module, through its name.
 * 
 * @see Module
 * @author Yannick Marcon
 *
 */
@Entity
public class Stage extends AbstractEntity {

  private static final long serialVersionUID = 8309472904104798783L;

  private String name;

  private String module;

  private String description;

  private Integer displayOrder;

  @ManyToMany
  @JoinTable(name = "stage_dependencies", joinColumns = @JoinColumn(name = "stage_id"), inverseJoinColumns = @JoinColumn(name = "depends_on_stage_id"))
  private List<Stage> dependsOnStages;

  @ManyToMany(mappedBy = "dependsOnStages")
  private List<Stage> dependentStages;

  @ManyToMany(mappedBy = "stages")
  private List<Interview> interviews;

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

  public List<Stage> getDependsOnStages() {
    return dependsOnStages != null ? dependsOnStages : (dependsOnStages = new ArrayList<Stage>());
  }

  public List<Stage> getDependentStages() {
    return dependentStages != null ? dependentStages : (dependentStages = new ArrayList<Stage>());
  }

  public void addDependentStage(Stage stage) {
    if(!this.equals(stage)) {
      getDependentStages().add(stage);
      stage.getDependsOnStages().add(this);
    }
  }

  public List<Interview> getInterviews() {
    return interviews != null ? interviews : (interviews = new ArrayList<Interview>());
  }

  public void addInterview(Interview interview) {
    if(interview != null) {
      getInterviews().add(interview);
    }

  }

  @Override
  public String toString() {
    return module + ":" + name;// + ":{dependsOn=" + getDependsOnStages()+ "}";
  }

}
