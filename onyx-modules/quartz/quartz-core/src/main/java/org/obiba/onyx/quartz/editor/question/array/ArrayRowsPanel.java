/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.array;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;
import org.obiba.onyx.quartz.editor.question.QuestionWindow;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner.CloneSettings;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public class ArrayRowsPanel extends Panel {

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final ModalWindow questionWindow;

  private final SortableList<Question> questionList;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<LocaleProperties> localePropertiesModel;

  public ArrayRowsPanel(String id, IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel, final IModel<LocaleProperties> localePropertiesModel, final FeedbackPanel feedbackPanel, final FeedbackWindow feedbackWindow) {
    super(id, model);
    this.questionnaireModel = questionnaireModel;
    this.localePropertiesModel = localePropertiesModel;
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;

    final Question questionParent = model.getObject().getElement();

    add(CSSPackageResource.getHeaderContribution(ArrayRowsPanel.class, "ArrayRowsPanel.css"));

    questionWindow = new ModalWindow("questionWindow");
    questionWindow.setCssClassName("onyx");
    questionWindow.setInitialWidth(950);
    questionWindow.setInitialHeight(550);
    questionWindow.setResizable(true);
    questionWindow.setTitle(new ResourceModel("Question"));
    add(questionWindow);

    final IModel<String> addQuestionModel = new Model<String>();

    List<ITab> tabs = new ArrayList<ITab>();
    tabs.add(new AbstractTab(new ResourceModel("Add.simple")) {
      @Override
      public Panel getPanel(String panelId) {
        return new SimpleAddPanel(panelId, addQuestionModel);
      }
    });
    tabs.add(new AbstractTab(new ResourceModel("Add.bulk")) {
      @Override
      public Panel getPanel(String panelId) {
        return new BulkAddPanel(panelId, addQuestionModel);
      }
    });
    add(new AjaxTabbedPanel("addTabs", tabs));

    questionList = new SortableList<Question>("questions", questionParent.getQuestions()) {

      public void onItemPopulation(Question question) {
        localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), question);
      }

      @Override
      public Component getItemTitle(@SuppressWarnings("hiding") String id, Question question) {
        return new Label(id, question.getName());
      }

      @Override
      public void editItem(final Question question, AjaxRequestTarget target) {
        final Question originalQuestion = QuestionnaireElementCloner.cloneQuestion(question, new CloneSettings(true, false));
        questionWindow.setContent(new QuestionWindow("content", new Model<EditedQuestion>(new EditedQuestion(question)), questionnaireModel, localePropertiesModel, questionWindow) {
          @Override
          protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, EditedQuestion editedQuestion) {

          }

          @Override
          protected void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target, EditedQuestion editedQuestion) {
            roolback(questionParent, question, originalQuestion);
          }
        });
        questionWindow.setCloseButtonCallback(new CloseButtonCallback() {
          @Override
          public boolean onCloseButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            roolback(questionParent, question, originalQuestion);
            return true;
          }
        });
        questionWindow.setWindowClosedCallback(new WindowClosedCallback() {
          @Override
          public void onClose(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            refreshList(target);
          }
        });
        questionWindow.show(target);
      }

      @Override
      public void deleteItem(Question question, AjaxRequestTarget target) {
        questionParent.getQuestions().remove(question);
        refreshList(target);
      }

      @Override
      public Button[] getButtons() {
        return null;
      }

    };
    add(questionList);

  }

  private class SimpleAddPanel extends Panel {

    public SimpleAddPanel(String id, IModel<String> model) {
      super(id, model);
      Form<String> form = new Form<String>("form", model);
      add(form);

      final TextField<String> questionName = new TextField<String>("question", model);
      questionName.setOutputMarkupId(true);
      questionName.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));
      questionName.setLabel(new ResourceModel("NewQuestion"));
      questionName.add(new AbstractValidator<String>() {
        @Override
        protected void onValidate(IValidatable<String> validatable) {
          Questionnaire questionnaire = questionnaireModel.getObject();
          questionnaire.setQuestionnaireCache(null); // invalidate cache
          QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
          if(questionnaireFinder.findQuestion(validatable.getValue()) != null) {
            error(validatable, "QuestionAlreadyExists");
          }
        }
      });
      form.add(questionName);
      form.add(new SimpleFormComponentLabel("questionLabel", questionName));

      AjaxSubmitLink simpleAddLink = new AjaxSubmitLink("link", form) {
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          addQuestion(questionName.getModelObject());
          questionName.setModelObject(null);
          target.addComponent(questionName);
          target.addComponent(questionList);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form1) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      simpleAddLink.add(new Image("img", Images.ADD).add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(simpleAddLink);
    }
  }

  private class BulkAddPanel extends Panel {

    public BulkAddPanel(String id, IModel<String> model) {
      super(id, model);
      Form<String> form = new Form<String>("form", model);
      add(form);
      final TextArea<String> questions = new TextArea<String>("questions", model);
      questions.setOutputMarkupId(true);
      questions.setLabel(new ResourceModel("NewQuestions"));
      form.add(questions);
      form.add(new SimpleFormComponentLabel("questionsLabel", questions));
      AjaxSubmitLink bulkAddLink = new AjaxSubmitLink("bulkAddLink") {
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          String[] names = StringUtils.split(questions.getModelObject(), ',');
          if(names == null) return;
          for(String name : names) {
            name = StringUtils.trimToNull(name);
            if(name == null) continue;
            if(QuartzEditorPanel.ELEMENT_NAME_PATTERN.matcher(name).matches()) addQuestion(name);
          }
          questions.setModelObject(null);
          target.addComponent(questions);
          target.addComponent(questionList);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form1) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      bulkAddLink.add(new Image("bulkAddImg", Images.ADD).add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(bulkAddLink);
    }
  }

  private boolean checkIfQuestionAlreadyExists(String name) {
    questionnaireModel.getObject().setQuestionnaireCache(null); // invalidate cache
    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
    if(questionnaireFinder.findQuestion(name) != null) {
      return true;
    }
    return false;
  }

  private void addQuestion(String name) {
    if(StringUtils.isNotBlank(name) && !checkIfQuestionAlreadyExists(name)) {
      Question parent = ((EditedQuestion) ArrayRowsPanel.this.getDefaultModelObject()).getElement();
      parent.addQuestion(new Question(name));
    }
  }

  private synchronized void roolback(Question questionParent, Question modified, Question original) {
    int index = questionParent.getQuestions().indexOf(modified);
    questionParent.removeQuestion(modified);
    questionParent.addQuestion(original, index);

    Questionnaire questionnaire = questionnaireModel.getObject();
    LocaleProperties localeProperties = localePropertiesModel.getObject();
    localePropertiesUtils.remove(localeProperties, questionnaire, modified);
    localePropertiesUtils.load(localeProperties, questionnaire, original);

    QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
  }

}
