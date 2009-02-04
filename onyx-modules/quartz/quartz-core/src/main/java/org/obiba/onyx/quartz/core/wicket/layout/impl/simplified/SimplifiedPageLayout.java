/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.DataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;

/**
 * Simplified page layout implementation, has a unique question, presented in its self-provided UI, and is NOT able to
 * deal with in-page conditions resolution updates.
 */
public class SimplifiedPageLayout extends PageLayout {

  private static final long serialVersionUID = -1757316578083924986L;

  @SpringBean
  private QuestionPanelFactoryRegistry questionPanelFactoryRegistry;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private QuestionPanel questionPanel;

  /**
   * Constructor, given a questionnaire page.
   * @param id
   * @param pageModel
   */
  @SuppressWarnings("serial")
  public SimplifiedPageLayout(String id, IModel pageModel) {
    super(id, pageModel);
    setOutputMarkupId(true);

    Page page = (Page) getModelObject();
    Question question = getQuestion(page);

    if(question != null) {
      questionPanel = questionPanelFactoryRegistry.get(question.getUIFactoryName()).createPanel("question", new QuestionnaireModel(question));
      add(questionPanel);
    } else {
      add(new Label("question").setVisible(false));
    }

    // TODO get it from questionnaire bundle ?
    add(HeaderContributor.forCss("css/questionnaire-simplified.css"));
  }

  /**
   * Called when going next page, forward it to page questions.
   */
  public void onNext(AjaxRequestTarget target) {
    if(questionPanel != null) {
      questionPanel.onNext(target);
    }
  }

  /**
   * Called when going previous page, forward it to page questions.
   */
  public void onPrevious(AjaxRequestTarget target) {
    if(questionPanel != null) {
      questionPanel.onPrevious(target);
    }
  }

  private Question getQuestion(Page page) {
    List<Question> questionToAnswer = new ArrayList<Question>();
    for(Question question : page.getQuestions()) {
      if(!answerQuestionIfDataSourceAvailable(question) && question.isToBeAnswered(activeQuestionnaireAdministrationService)) {
        // if there are in-page conditions, make sure it is correctly resolved (case of cascading questions).
        activeQuestionnaireAdministrationService.setActiveAnswers(question, true);
        questionToAnswer.add(question);
      }
    }

    return questionToAnswer.size() > 0 ? questionToAnswer.get(0) : null;
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
