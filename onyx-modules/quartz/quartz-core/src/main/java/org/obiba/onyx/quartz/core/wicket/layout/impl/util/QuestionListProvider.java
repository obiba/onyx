/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the children questions to be answered.
 */
public class QuestionListProvider extends AbstractDataListProvider<Question> {

  private static final long serialVersionUID = 227294946626164090L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionListProvider.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private Question parentQuestion;

  public QuestionListProvider(Question parentQuestion) {
    InjectorHolder.getInjector().inject(this);
    this.parentQuestion = parentQuestion;
  }

  @Override
  public List<Question> getDataList() {
    List<Question> questionToAnswer = new ArrayList<Question>();
    for(Question question : parentQuestion.getQuestions()) {
      if(question.isToBeAnswered(activeQuestionnaireAdministrationService)) {
        questionToAnswer.add(question);
      } else {
        activeQuestionnaireAdministrationService.setActiveAnswers(question, false);
      }
    }
    return questionToAnswer;
  }

  @Override
  public IModel model(Object object) {
    return new QuestionnaireModel((Question) object);
  }

}
