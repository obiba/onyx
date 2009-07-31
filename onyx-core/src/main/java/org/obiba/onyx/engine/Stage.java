/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import org.obiba.onyx.core.domain.participant.Interview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * A <code>Stage</code> is a step of an {@link Interview}. They are contributed by {@link Module}s. A <code>Stage</code>
 * 's name must be unique throughout all contributed stages.
 * <p>
 * Some <code>Stage</code>s have dependencies on other <code>Stage</code>s. The actual dependency logic is encapsulated
 * in its {@link StageDependencyCondition} instance.
 * 
 * @see Module
 */
public class Stage {

  private static final String DESCRIPTION_KEY = ".description";

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(Stage.class);

  private String name;

  private String module;

  private StageDependencyCondition stageDependencyCondition;

  private boolean interviewConclusion;

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

  public MessageSourceResolvable getDescription() {
    // Codes are <module>.<name>.description, <name>.description
    return new DefaultMessageSourceResolvable(new String[] { getModule() + "." + getName() + DESCRIPTION_KEY, getName() + DESCRIPTION_KEY }, getName());
  }

  public StageDependencyCondition getStageDependencyCondition() {
    return stageDependencyCondition;
  }

  public void setStageDependencyCondition(StageDependencyCondition stageDependencyCondition) {
    this.stageDependencyCondition = stageDependencyCondition;
  }

  public void setInterviewConclusion(boolean interviewConclusion) {
    this.interviewConclusion = interviewConclusion;
  }

  public boolean isInterviewConclusion() {
    return interviewConclusion;
  }

  @Override
  public String toString() {
    return module + ":" + name;
  }

  private void validateConclusionStageDoesNotHaveDependencies() {
    if(interviewConclusion && stageDependencyCondition != null) {
      log.warn("The StageDependencyCondition [{}] was found for the conclusion stage [{}]. It will be ignored. Please remove the StageDependencyCondition from the [{}] configuration. Conclusion stages must not contain a StageDependencyCondition.", new Object[] { stageDependencyCondition, name, name });
    }
  }

  private Object readResolve() {
    validateConclusionStageDoesNotHaveDependencies();
    return this;
  }

}
