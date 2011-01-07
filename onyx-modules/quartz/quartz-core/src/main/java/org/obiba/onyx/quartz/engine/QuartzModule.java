/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.engine;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.spring.BeanValueTableFactoryBean;
import org.obiba.magma.spring.ValueTableFactoryBean;
import org.obiba.magma.spring.ValueTableFactoryBeanProvider;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageManager;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.magma.CompositeVariableValueSourceFactory;
import org.obiba.onyx.magma.CustomVariablesRegistry;
import org.obiba.onyx.magma.PrebuiltVariableValueSourceFactory;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.magma.QuestionnaireBeanResolver;
import org.obiba.onyx.quartz.magma.QuestionnaireStageVariableSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class QuartzModule implements Module, ValueTableFactoryBeanProvider, ApplicationContextAware {

  public static final String MODULE_NAME = "quartz";

  private static final Logger log = LoggerFactory.getLogger(QuartzModule.class);

  private ApplicationContext applicationContext;

  private ActiveInterviewService activeInterviewService;

  private QuestionnaireParticipantService questionnaireParticipantService;

  private StageManager stageManager;

  private QuestionnaireBundleManager questionnaireBundleManager;

  private QuestionnaireBeanResolver beanResolver;

  private VariableEntityProvider variableEntityProvider;

  private CustomVariablesRegistry customVariablesRegistry;

  @Override
  public String getName() {
    return MODULE_NAME;
  }

  @Override
  public void initialize(WebApplication application) {
    log.info("initialize");
    for(Iterator<Stage> iter = getStages().iterator(); iter.hasNext();) {
      Stage stage = iter.next();
      QuestionnaireBundle bundle = questionnaireBundleManager.getBundle(stage.getName());
      if(bundle == null) {
        log.warn("Stage '{}' is configured, but no associated questionnaire could be found in the resources.", stage.getName());
        log.warn("Stage '{}' will not be registered.", stage.getName());
        iter.remove();
      }
    }
  }

  @Override
  public void shutdown(WebApplication application) {
    log.info("shutdown");
  }

  @Override
  public List<Stage> getStages() {
    return stageManager.getStages();
  }

  @Override
  public IStageExecution createStageExecution(Interview interview, Stage stage) {
    StageExecutionContext exec = (StageExecutionContext) applicationContext.getBean("stageExecutionContext");

    exec.setStage(stage);
    exec.setInterview(interview);

    AbstractStageState ready = (AbstractStageState) applicationContext.getBean("quartzReadyState");
    AbstractStageState waiting = (AbstractStageState) applicationContext.getBean("quartzWaitingState");
    AbstractStageState notApplicable = (AbstractStageState) applicationContext.getBean("quartzNotApplicableState");
    AbstractStageState skipped = (AbstractStageState) applicationContext.getBean("quartzSkippedState");
    AbstractStageState inProgress = (AbstractStageState) applicationContext.getBean("quartzInProgressState");
    AbstractStageState completed = (AbstractStageState) applicationContext.getBean("quartzCompletedState");
    AbstractStageState interrupted = (AbstractStageState) applicationContext.getBean("quartzInterruptedState");

    exec.addEdge(waiting, TransitionEvent.VALID, ready);
    exec.addEdge(waiting, TransitionEvent.NOTAPPLICABLE, notApplicable);

    exec.addEdge(ready, TransitionEvent.INVALID, waiting);
    exec.addEdge(ready, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(ready, TransitionEvent.SKIP, skipped);
    exec.addEdge(ready, TransitionEvent.START, inProgress);

    exec.addEdge(skipped, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(skipped, TransitionEvent.CANCEL, ready);

    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
    exec.addEdge(inProgress, TransitionEvent.INTERRUPT, interrupted);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);

    exec.addEdge(completed, TransitionEvent.CANCEL, ready);
    exec.addEdge(completed, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(completed, TransitionEvent.RESUME, inProgress);
    exec.addEdge(completed, TransitionEvent.INVALID, waiting);

    exec.addEdge(interrupted, TransitionEvent.CANCEL, ready);
    exec.addEdge(interrupted, TransitionEvent.RESUME, inProgress);
    exec.addEdge(interrupted, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(interrupted, TransitionEvent.INVALID, waiting);

    exec.addEdge(notApplicable, TransitionEvent.VALID, ready);
    exec.addEdge(notApplicable, TransitionEvent.INVALID, waiting);

    AbstractStageState initialState;
    if(stage.getStageDependencyCondition() == null) {
      initialState = ready;
    } else {
      Boolean condition = stage.getStageDependencyCondition().isDependencySatisfied(stage, activeInterviewService);
      if(condition == null) {
        initialState = waiting;
      } else if(condition == true) {
        initialState = ready;
      } else {
        initialState = notApplicable;
      }
    }
    exec.setInitialState(initialState);

    return exec;
  }

  @Override
  public Component getWorkstationPanel(String id) {
    return null;
  }

  @Override
  public Component getEditorPanel(String id) {
    return new QuartzEditorPanel(id);
  }

  @Override
  public boolean isInteractive() {
    return false;
  }

  @Override
  public void delete(Participant participant) {
    questionnaireParticipantService.deleteAllQuestionnairesParticipant(participant);
  }

  public Set<VariableValueSource> createSources() {
    ImmutableSet.Builder<VariableValueSource> sources = new ImmutableSet.Builder<VariableValueSource>();
    for(Stage stage : getStages()) {
      QuestionnaireBundle bundle = this.questionnaireBundleManager.getBundle(stage.getName());
      QuestionnaireStageVariableSourceFactory factory = new QuestionnaireStageVariableSourceFactory(stage, bundle);
      sources.addAll(factory.createSources());
    }
    return sources.build();
  }

  //
  // ValueTableFactoryBeanProvider Methods
  //

  @Override
  public Set<? extends ValueTableFactoryBean> getValueTableFactoryBeans() {
    Set<BeanValueTableFactoryBean> tableFactoryBeans = Sets.newHashSet();

    for(Stage stage : getStages()) {
      BeanValueTableFactoryBean b = new BeanValueTableFactoryBean();
      b.setValueTableName(stage.getName());
      b.setValueSetBeanResolver(beanResolver);
      b.setVariableEntityProvider(variableEntityProvider);

      QuestionnaireBundle bundle = this.questionnaireBundleManager.getBundle(stage.getName());
      QuestionnaireStageVariableSourceFactory questionnaireVariableFactory = new QuestionnaireStageVariableSourceFactory(stage, bundle);

      PrebuiltVariableValueSourceFactory customVariableFactory = new PrebuiltVariableValueSourceFactory();
      customVariableFactory.addVariableValueSources(customVariablesRegistry.getVariables(b.getValueTableName()));

      CompositeVariableValueSourceFactory compositeFactory = new CompositeVariableValueSourceFactory();
      compositeFactory.addFactory(questionnaireVariableFactory).addFactory(customVariableFactory);
      b.setVariableValueSourceFactory(compositeFactory);

      tableFactoryBeans.add(b);
    }

    return tableFactoryBeans;
  }

  @Override
  public StageManager getStageManager() {
    return stageManager;
  }

  //
  // Methods
  //
  @Required
  public void setBeanResolver(QuestionnaireBeanResolver beanResolver) {
    this.beanResolver = beanResolver;
  }

  @Required
  public void setVariableEntityProvider(VariableEntityProvider variableEntityProvider) {
    this.variableEntityProvider = variableEntityProvider;
  }

  @Required
  public void setCustomVariablesRegistry(CustomVariablesRegistry customVariablesRegistry) {
    this.customVariablesRegistry = customVariablesRegistry;
  }

  @Override
  @Required
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Required
  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  @Required
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

  @Required
  public void setQuestionnaireParticipantService(QuestionnaireParticipantService questionnaireParticipantService) {
    this.questionnaireParticipantService = questionnaireParticipantService;
  }

  @Required
  public void setStageManager(StageManager stageManager) {
    this.stageManager = stageManager;
  }

}
