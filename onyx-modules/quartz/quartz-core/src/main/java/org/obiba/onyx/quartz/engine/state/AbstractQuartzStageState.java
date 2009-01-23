package org.obiba.onyx.quartz.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;

public abstract class AbstractQuartzStageState extends AbstractStageState {

  // TODO: Are there any issues related to making this protected?
  protected QuestionnaireParticipantService questionnaireParticipantService;

  public void setQuestionnaireParticipantService(QuestionnaireParticipantService questionnaireParticipantService) {
    this.questionnaireParticipantService = questionnaireParticipantService;
  }

  protected void cancelQuestionnaireParticipant() {
    QuestionnaireParticipant questionnaireParticipant = questionnaireParticipantService.getQuestionnaireParticipant(activeInterviewService.getParticipant(), super.getStage().getName());
    questionnaireParticipantService.deleteQuestionnaireParticipant(questionnaireParticipant);
  }

  protected void inactivateQuestionnaireParticipant() {
    QuestionnaireParticipant questionnaireParticipant = questionnaireParticipantService.getQuestionnaireParticipant(activeInterviewService.getParticipant(), super.getStage().getName());
    questionnaireParticipantService.inactivateQuestionnaireParticipant(questionnaireParticipant);
  }
}
