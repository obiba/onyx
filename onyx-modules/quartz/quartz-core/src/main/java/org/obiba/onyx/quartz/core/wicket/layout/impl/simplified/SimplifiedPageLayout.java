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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.wizard.QuestionnaireWizardForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplified page layout implementation, has a unique question, presented in its self-provided UI, and is NOT able to
 * deal with in-page conditions resolution updates.
 */
public class SimplifiedPageLayout extends PageLayout {

  private static final long serialVersionUID = -1757316578083924986L;

  private static final Logger log = LoggerFactory.getLogger(SimplifiedPageLayout.class);

  @SpringBean
  private QuestionPanelFactoryRegistry questionPanelFactoryRegistry;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

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
  }

  /**
   * Called when going next page, forward it to page questions.
   */
  public void onNext(AjaxRequestTarget target) {
    if(questionPanel != null) {
      questionPanel.onNext(target);
      enableModalFeedback(false);
    }
  }

  /**
   * Called when going previous page, forward it to page questions.
   */
  public void onPrevious(AjaxRequestTarget target) {
    if(questionPanel != null) {
      questionPanel.onPrevious(target);
      enableModalFeedback(false);
    }
  }

  @Override
  public void onStepInNext(AjaxRequestTarget target) {
    if(questionPanel != null) {
      enableModalFeedback(true);
      updateProgressBar();
    }
  }

  @Override
  public void onStepInPrevious(AjaxRequestTarget target) {
    if(questionPanel != null) {
      enableModalFeedback(true);
      updateProgressBar();
    }
  }

  private void updateProgressBar() {
    QuestionnaireWizardForm form = (QuestionnaireWizardForm) findParent(QuestionnaireWizardForm.class);
    form.updateProgressBar(form);
  }

  private void enableModalFeedback(boolean isEnable) {
    log.debug("Modal Feedback is enabled = {}", isEnable);
    QuestionnaireWizardForm form = (QuestionnaireWizardForm) findParent(QuestionnaireWizardForm.class);
    form.setModalFeedback(isEnable);
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
    if(questionToAnswer.size() > 1) {
      throw new UnsupportedOperationException("Simplified page layout does not support multiple questions to be answered.");
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
    IDataSource dataSource;
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
            activeQuestionnaireAdministrationService.answer(category, category.getCategory().getOpenAnswerDefinition(), dataSource.getData(activeInterviewService.getParticipant()));
          }
          questionHasAnswers = true;
        }
      }
    }

    return questionHasAnswers;
  }

}
