/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableListCallback;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("serial")
public class CategoryWindow extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<EditedQuestionCategory> form;

  private final IModel<Questionnaire> questionnaireModel;

  private SortableList<OpenAnswerDefinition> openAnswerDefinitionList;

  public CategoryWindow(String id, IModel<QuestionCategory> model, final IModel<Questionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id);
    this.questionnaireModel = questionnaireModel;

    IModel<EditedQuestionCategory> editedModel = new Model<EditedQuestionCategory>(new EditedQuestionCategory(model.getObject()));
    setDefaultModel(editedModel);

    add(form = new Form<EditedQuestionCategory>("form", editedModel));

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    CategoryPanel categoryPanel = new CategoryPanel("categoryPanel", model, questionnaireModel);
    form.add(categoryPanel);

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, form.getModelObject());
        modalWindow.close(target);
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
        modalWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }

  /**
   * 
   * @param target
   * @param editedQuestionCategory
   */
  public void onSave(AjaxRequestTarget target, final EditedQuestionCategory editedQuestionCategory) {
    // editedQuestionCategory.setLocalePropertiesWithNamingStrategy(localeProperties.getObject());

    openAnswerDefinitionList.save(target, new SortableListCallback<OpenAnswerDefinition>() {

      @Override
      public void onSave(List<OpenAnswerDefinition> orderedItems, AjaxRequestTarget target1) {
        OpenAnswerDefinition currentOpenAnswer = editedQuestionCategory.getElement().getOpenAnswerDefinition();
        if(currentOpenAnswer != null && !CollectionUtils.isEmpty(currentOpenAnswer.getOpenAnswerDefinitions())) {
          currentOpenAnswer.getOpenAnswerDefinitions().clear();
          for(OpenAnswerDefinition openAnswerDefinition : orderedItems) {
            currentOpenAnswer.addOpenAnswerDefinition(openAnswerDefinition);
          }
        }
      }
    });
  }

  public void persist(AjaxRequestTarget target) {
    try {
      QuestionnaireBuilder builder = questionnairePersistenceUtils.createBuilder(questionnaireModel.getObject());
      questionnairePersistenceUtils.persist(form.getModelObject(), builder);
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }
}
