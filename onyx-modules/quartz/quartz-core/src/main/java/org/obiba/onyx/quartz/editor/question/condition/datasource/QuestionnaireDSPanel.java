/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.condition.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireElementComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class QuestionnaireDSPanel extends Panel {

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final IModel<Question> questionModel;

  public QuestionnaireDSPanel(String id, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel, final ModalWindow dataSourceWindow) {
    super(id);
    this.questionModel = questionModel;

    Questionnaire currentQuestionnaire = questionnaireModel.getObject();

    IChoiceRenderer<IQuestionnaireElement> renderer = new IChoiceRenderer<IQuestionnaireElement>() {
      @Override
      public Object getDisplayValue(IQuestionnaireElement element) {
        return element.getName();
      }

      @Override
      public String getIdValue(IQuestionnaireElement element, int index) {
        return element.getName();
      }
    };

    List<Questionnaire> questionnaires = new ArrayList<Questionnaire>();
    for(QuestionnaireBundle bundle : questionnaireBundleManager.bundles()) {
      questionnaires.add(bundle.getQuestionnaire());
    }
    Collections.sort(questionnaires, new QuestionnaireElementComparator());

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    QuestionnaireDS dataSource = new QuestionnaireDS();
    dataSource.setQuestionnaire(currentQuestionnaire);
    Model<QuestionnaireDS> model = new Model<QuestionnaireDS>(dataSource);
    setDefaultModel(model);
    final Form<QuestionnaireDS> form = new Form<QuestionnaireDS>("form", model);
    add(form);

    final DropDownChoice<Questionnaire> questionnaireChoice = new DropDownChoice<Questionnaire>("questionnaire", new PropertyModel<Questionnaire>(form.getModel(), "questionnaire"), questionnaires, new IChoiceRenderer<Questionnaire>() {
      @Override
      public Object getDisplayValue(Questionnaire element) {
        return element.getName() + (questionnaireModel.getObject().getName().equals(element.getName()) ? " (" + new StringResourceModel("Current", QuestionnaireDSPanel.this, null).getString() + ")" : "");
      }

      @Override
      public String getIdValue(Questionnaire element, int index) {
        return element.getName();
      }
    });
    questionnaireChoice.setLabel(new ResourceModel("Questionnaire"));
    questionnaireChoice.setRequired(true);
    questionnaireChoice.setNullValid(false);
    form.add(questionnaireChoice);
    form.add(new SimpleFormComponentLabel("questionnaireLabel", questionnaireChoice));

    final DropDownChoice<Question> questionChoice = new DropDownChoice<Question>("question", new PropertyModel<Question>(form.getModel(), "question"), findQuestions(currentQuestionnaire), renderer);
    questionChoice.setLabel(new ResourceModel("Question"));
    questionChoice.setRequired(true);
    questionChoice.setNullValid(false);
    form.add(questionChoice);
    form.add(new SimpleFormComponentLabel("questionLabel", questionChoice));

    @SuppressWarnings("unchecked")
    final DropDownChoice<Category> categoryChoice = new DropDownChoice<Category>("category", new PropertyModel<Category>(form.getModel(), "category"), Collections.EMPTY_LIST, renderer);
    categoryChoice.setLabel(new ResourceModel("Category"));
    categoryChoice.setRequired(false);
    categoryChoice.setNullValid(true);
    categoryChoice.setEnabled(false);
    form.add(categoryChoice);
    form.add(new SimpleFormComponentLabel("categoryLabel", categoryChoice));

    @SuppressWarnings("unchecked")
    final DropDownChoice<OpenAnswerDefinition> openAnswerChoice = new DropDownChoice<OpenAnswerDefinition>("openAnswer", new PropertyModel<OpenAnswerDefinition>(form.getModel(), "openAnswerDefinition"), Collections.EMPTY_LIST, renderer);
    openAnswerChoice.setLabel(new ResourceModel("OpenAnswerDefinition"));
    openAnswerChoice.setRequired(false);
    openAnswerChoice.setNullValid(true);
    openAnswerChoice.setEnabled(false);
    form.add(openAnswerChoice);
    form.add(new SimpleFormComponentLabel("openAnswerLabel", openAnswerChoice));

    questionnaireChoice.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        questionChoice.setModelObject(null);
        questionChoice.setChoices(findQuestions(questionnaireChoice.getModelObject()));
        categoryChoice.setModelObject(null);
        categoryChoice.setEnabled(false);
        openAnswerChoice.setModelObject(null);
        openAnswerChoice.setEnabled(false);
        target.addComponent(questionChoice);
        target.addComponent(categoryChoice);
        target.addComponent(openAnswerChoice);
      }
    });
    questionChoice.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        boolean questionSelected = questionChoice.getModelObject() != null;
        categoryChoice.setModelObject(null);
        categoryChoice.setEnabled(questionSelected);
        openAnswerChoice.setModelObject(null);
        openAnswerChoice.setEnabled(questionSelected);
        if(questionSelected) {
          categoryChoice.setChoices(questionChoice.getModelObject().getCategories());
        }
        target.addComponent(categoryChoice);
        target.addComponent(openAnswerChoice);
      }
    });
    categoryChoice.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        boolean categorySelected = categoryChoice.getModelObject() != null;
        openAnswerChoice.setModelObject(null);
        openAnswerChoice.setEnabled(categorySelected);
        if(categorySelected) {
          openAnswerChoice.setChoices(findOpenAnswerDefinitions(categoryChoice.getModelObject()));
        }
        target.addComponent(openAnswerChoice);
      }
    });

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, form.getModelObject());
        dataSourceWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form2) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

    form.add(new AjaxButton("cancel", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        dataSourceWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }

  private List<Question> findQuestions(Questionnaire questionnaire) {
    if(questionnaire.getQuestionnaireCache() == null) {
      QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
    }
    Map<String, Question> questionCache = new HashMap<String, Question>(questionnaire.getQuestionnaireCache().getQuestionCache());
    questionCache.remove(questionModel.getObject().getName());
    List<Question> questions = new ArrayList<Question>(questionCache.values());
    Collections.sort(questions, new QuestionnaireElementComparator());
    return questions;
  }

  private List<OpenAnswerDefinition> findOpenAnswerDefinitions(Category category) {
    List<OpenAnswerDefinition> list = new ArrayList<OpenAnswerDefinition>();
    OpenAnswerDefinition parentOpenAnswer = category.getOpenAnswerDefinition();
    if(parentOpenAnswer != null) {
      if(parentOpenAnswer.getOpenAnswerDefinitions().isEmpty()) {
        list.add(parentOpenAnswer);
      } else {
        list.addAll(parentOpenAnswer.getOpenAnswerDefinitions());
        Collections.sort(list, new QuestionnaireElementComparator());
      }
    }
    return list;
  }

  /**
   * 
   * @param target
   * @param modelObject
   */
  public abstract void onSave(AjaxRequestTarget target, QuestionnaireDS modelObject);
}
