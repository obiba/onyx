package org.obiba.onyx.engine;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.service.ActiveInterviewService;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class StageDependencyCondition extends AbstractEntity {

  public abstract Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService);

  public abstract boolean isDependentOn(String stageName);

}
