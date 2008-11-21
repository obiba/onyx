/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
/**
 * State inProgress for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: ready Possible forward states/actions/transitions: cancel, complete, interrupt On cancel
 * transition, current questionnaireParticipant is deleted from database (with its dependencies)
 */
package org.obiba.onyx.quartz.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.QuartzPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class QuartzInProgressState extends AbstractQuartzStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(QuartzInProgressState.class);

  private QuestionnaireBundleManager questionnaireBundleManager;

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("You may explain why you are cancelling this stage.").getActionDefinition());
    addAction(ActionDefinitionBuilder.create(ActionType.INTERRUPT, "Interrupt").getActionDefinition());
    addSystemAction(ActionDefinitionBuilder.COMPLETE_ACTION);
  }

  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

  public String getName() {
    return "InProgress";
  }

  public Component getWidget(String id) {
    return new QuartzPanel(id, getStage(), isResumingQuestionnaire());
  }

  @Override
  public void stop(Action action) {
    log.info("Quartz Stage {} is stopping", super.getStage().getName());
    cancelQuestionnaireParticipant();
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void complete(Action action) {
    log.info("Quartz Stage {} is completing", super.getStage().getName());
    castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

  @Override
  public void interrupt(Action action) {
    log.info("Quartz Stage {} is interrupting", super.getStage().getName());
    getActiveQuestionnaireAdministrationService().stopCurrentQuestionnaire();
    castEvent(TransitionEvent.INTERRUPT);
  }

  @Override
  public void onEntry(TransitionEvent event) {
    Questionnaire questionnaire = questionnaireBundleManager.getBundle(getStage().getName()).getQuestionnaire();
    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);

    if(isResumingQuestionnaire()) {
      Participant participant = activeInterviewService.getParticipant();
      activeQuestionnaireAdministrationService.resume(participant);
    }
  }

  private boolean isResumingQuestionnaire() {
    boolean resumingQuestionnaire = false;

    Participant participant = activeInterviewService.getParticipant();
    Questionnaire questionnaire = getActiveQuestionnaireAdministrationService().getQuestionnaire();

    if(participant != null && questionnaire != null) {
      QuestionnaireParticipant questionnaireParticipant = questionnaireParticipantService.getQuestionnaireParticipant(participant, questionnaire.getName());

      if(questionnaireParticipant != null) {
        if(questionnaireParticipant.getResumePage() != null) {
          resumingQuestionnaire = true;
        }
      }
    }

    return resumingQuestionnaire;
  }
}
