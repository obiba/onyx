/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.DataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageQuestionsProvider implements IDataProvider {

  private static final long serialVersionUID = 227294946626164090L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(PageQuestionsProvider.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private Page page;

  public PageQuestionsProvider(Page page) {
    InjectorHolder.getInjector().inject(this);
    this.page = page;
  }

  @SuppressWarnings("unchecked")
  public Iterator iterator(int first, int count) {
    List<Question> questionToAnswer = new ArrayList<Question>();
    for(Question question : page.getQuestions().subList(first, first + count)) {
      if(!answerQuestionIfDataSourceAvailable(question) && question.isToBeAnswered(activeQuestionnaireAdministrationService)) {
        questionToAnswer.add(question);
      }
    }
    return questionToAnswer.iterator();
  }

  public IModel model(Object object) {
    return new QuestionnaireModel((Question) object);
  }

  public int size() {
    return page.getQuestions().size();
  }

  public void detach() {
    // TODO Auto-generated method stub

  }

  /**
   * Answers the question using the data provided by any AnswerSource associated to its categories.
   * 
   * @param question Question to answer.
   * @return True, if question could be answered through AnswerSource.
   */
  private boolean answerQuestionIfDataSourceAvailable(Question question) {

    OpenAnswerDefinition openAnswer;
    DataSource dataSource;
    CategoryAnswer answer;
    boolean questionHasAnswers = false;

    // Search for AnswerSource by looping through question categories.
    List<QuestionCategory> categories = question.getQuestionCategories();
    for(QuestionCategory category : categories) {
      if((openAnswer = category.getCategory().getOpenAnswerDefinition()) != null) {

        // AnswerSource found.
        if((dataSource = openAnswer.getDataSource()) != null) {

          // Get data from AnswerSource and answer current question (if not already answered).
          answer = activeQuestionnaireAdministrationService.findAnswer(category);
          if(answer == null) {
            activeQuestionnaireAdministrationService.answer(category, category.getCategory().getOpenAnswerDefinition(), dataSource.getData(activeQuestionnaireAdministrationService));
          }
          questionHasAnswers = true;
        }
      }
    }

    return questionHasAnswers;
  }

}
