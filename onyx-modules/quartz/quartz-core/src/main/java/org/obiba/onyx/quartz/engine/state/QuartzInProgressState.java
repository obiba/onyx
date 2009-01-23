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

import java.util.Set;

import org.apache.wicket.Component;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.QuartzPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzInProgressState extends AbstractQuartzStageState {

  private static final Logger log = LoggerFactory.getLogger(QuartzInProgressState.class);

  private QuestionnaireBundleManager questionnaireBundleManager;

  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

  public String getName() {
    return "InProgress";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
    types.add(ActionType.INTERRUPT);
  }

  @Override
  protected void addSystemActions(Set<ActionType> types) {
    types.add(ActionType.COMPLETE);
  }

  public Component getWidget(String id) {
    return new QuartzPanel(id, getStage(), isResumingQuestionnaire());
  }

  @Override
  public void stop(Action action) {
    log.debug("Quartz Stage {} is stopping", super.getStage().getName());
    cancelQuestionnaireParticipant();
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void complete(Action action) {
    log.debug("Quartz Stage {} is completing", super.getStage().getName());
    questionnaireParticipantService.endQuestionnaireParticipant(activeInterviewService.getParticipant(), getStage().getName());
    castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

  @Override
  public void interrupt(Action action) {
    log.debug("Quartz Stage {} is interrupting", super.getStage().getName());
    castEvent(TransitionEvent.INTERRUPT);
  }

  private boolean isResumingQuestionnaire() {
    boolean resumingQuestionnaire = false;

    Participant participant = activeInterviewService.getParticipant();
    Questionnaire questionnaire = questionnaireBundleManager.getBundle(getStage().getName()).getQuestionnaire();

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
