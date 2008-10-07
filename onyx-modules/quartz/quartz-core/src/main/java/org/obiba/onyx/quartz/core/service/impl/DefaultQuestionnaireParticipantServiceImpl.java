package org.obiba.onyx.quartz.core.service.impl;

import java.io.Serializable;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;

public abstract class DefaultQuestionnaireParticipantServiceImpl extends PersistenceManagerAwareService implements QuestionnaireParticipantService {

  public void deleteQuestionnaireParticipant(Serializable questionnaireParticipantId) {
    QuestionnaireParticipant questionnaireParticipant = loadQuestionnaireParticipant(questionnaireParticipantId);

    for(QuestionAnswer questionAnswer : questionnaireParticipant.getParticipantAnswers()) {
      CategoryAnswer template = new CategoryAnswer();
      template.setQuestionAnswer(questionAnswer);
      List<CategoryAnswer> categoryAnswerList = getPersistenceManager().match(template);

      for(CategoryAnswer categoryAnswer : categoryAnswerList) {
        getPersistenceManager().delete(categoryAnswer);
      }

      getPersistenceManager().delete(questionAnswer);
    }

    getPersistenceManager().delete(questionnaireParticipant);
  }

  public QuestionnaireParticipant loadQuestionnaireParticipant(Serializable questionnaireParticipantId) {
    QuestionnaireParticipant questionnaireParticipant = getPersistenceManager().get(QuestionnaireParticipant.class, questionnaireParticipantId);
    if(questionnaireParticipant == null) throw new IllegalArgumentException("Invalid quetsionnaire participant id");
    return questionnaireParticipant;
  }

}
