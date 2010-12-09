/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties.KeyValue;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner.CloneSettings;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner.ElementClone;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementNameRenderer;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class CopyQuestionPanel extends Panel {

  public static final String COPY_OF = "CopyOf";

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  private enum CategoryAction {
    COPY, SHARE;
  }

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private TextField<String> name;

  private RadioGroup<CategoryAction> categories;

  private final IModel<Questionnaire> questionnaireModel;

  public CopyQuestionPanel(String id, final IModel<Question> model, final IModel<Questionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id, model);
    this.questionnaireModel = questionnaireModel;

    final Questionnaire questionnaire = questionnaireModel.getObject();
    final QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
    questionnaire.setQuestionnaireCache(null);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    Form<?> form = new Form<Void>("form");
    form.setMultiPart(false);

    final Question question = model.getObject();
    String newName = new StringResourceModel(COPY_OF, CopyQuestionPanel.this, null, new Object[] { question.getName() }).getString();
    name = new TextField<String>("name", new Model<String>(newName));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(questionnaireFinder.findQuestion(validatable.getValue()) != null) {
          error(validatable, "QuestionAlreadyExists");
        }
      }
    });
    form.add(name);
    form.add(new SimpleFormComponentLabel("nameLabel", name));
    form.add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    categories = new RadioGroup<CategoryAction>("categories", new Model<CategoryAction>());
    categories.setLabel(new ResourceModel("Categories")).setRequired(true);
    form.add(categories);

    final Radio<CategoryAction> copyCategories = new Radio<CategoryAction>("copyCategories", new Model<CategoryAction>(CategoryAction.COPY));
    copyCategories.setLabel(new ResourceModel("Categories.Copy"));
    categories.add(copyCategories).add(new SimpleFormComponentLabel("copyCategoriesLabel", copyCategories));

    final Radio<CategoryAction> shareCategories = new Radio<CategoryAction>("shareCategories", new Model<CategoryAction>(CategoryAction.SHARE));
    shareCategories.setLabel(new ResourceModel("Categories.Share"));
    categories.add(shareCategories).add(new SimpleFormComponentLabel("shareCategoriesLabel", shareCategories));

    final DropDownChoice<Page> pageDropDown = new DropDownChoice<Page>("page", new Model<Page>(question.getPage()), questionnaire.getPages(), new QuestionnaireElementNameRenderer());
    pageDropDown.setRequired(true);
    pageDropDown.setNullValid(false);
    pageDropDown.setLabel(new ResourceModel("Page"));
    form.add(pageDropDown).add(new SimpleFormComponentLabel("pageLabel", pageDropDown));
    form.add(new HelpTooltipPanel("pageHelp", new ResourceModel("Page.Tooltip")));

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        LocaleProperties localeProperties = new LocaleProperties();
        Question copy = copyQuestion(localeProperties);
        pageDropDown.getModelObject().addQuestion(copy);
        try {
          questionnairePersistenceUtils.persist(questionnaire, localeProperties);
          questionnaire.setQuestionnaireCache(null);
          CopyQuestionPanel.this.onSave(target, copy);
          modalWindow.close(target);
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
          error(e.getClass() + ": " + e.getMessage());
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

    add(form);
  }

  private Question copyQuestion(LocaleProperties localeProperties) {
    Question question = (Question) getDefaultModelObject();

    localePropertiesUtils.load(localeProperties, questionnaireModel.getObject(), question);
    ElementClone<Question> questionCopy = QuestionnaireElementCloner.clone(question, new CloneSettings(false, true), localeProperties);
    questionCopy.getElement().setName(name.getModelObject());
    QuestionnaireElementCloner.addProperties(questionCopy, localeProperties);
    questionCopy.getElement().getQuestionCategories().clear();
    switch(categories.getModelObject()) {
    case COPY:
      for(QuestionCategory questionCategory : question.getQuestionCategories()) {
        localePropertiesUtils.load(localeProperties, questionnaireModel.getObject(), questionCategory);
        ElementClone<QuestionCategory> questionCategoryCopy = QuestionnaireElementCloner.clone(questionCategory, new CloneSettings(false, false, true), localeProperties);
        questionCopy.getElement().addQuestionCategory(questionCategoryCopy.getElement());
        QuestionnaireElementCloner.addProperties(questionCategoryCopy, localeProperties);

        for(final OpenAnswerDefinition openAnswer : questionCategory.getCategory().getOpenAnswerDefinitionsByName().values()) {
          String key = Iterables.find(questionCategoryCopy.getElement().getCategory().getOpenAnswerDefinitionsByName().keySet(), new Predicate<String>() {

            @Override
            public boolean apply(String inputKey) {
              return inputKey.startsWith("_" + openAnswer.getName() + "_");
            }

          });
          OpenAnswerDefinition openAnswerCopy = questionCategoryCopy.getElement().getCategory().getOpenAnswerDefinitionsByName().get(key);
          copyLabels(localeProperties, openAnswer, openAnswerCopy);
        }
      }
      break;

    case SHARE:
      for(QuestionCategory questionCategory : question.getQuestionCategories()) {
        QuestionCategory questionCategoryCopy = new QuestionCategory();
        questionCategoryCopy.setExportName(questionCategory.getExportName());
        questionCategoryCopy.setCategory(questionCategory.getCategory());
        questionCopy.getElement().addQuestionCategory(questionCategoryCopy);
        copyLabels(localeProperties, questionCategory, questionCategoryCopy);
      }
    }

    return questionCopy.getElement();
  }

  private void copyLabels(LocaleProperties localeProperties, IQuestionnaireElement original, IQuestionnaireElement copy) {
    localePropertiesUtils.load(localeProperties, questionnaireModel.getObject(), original, copy);
    for(Entry<Locale, KeyValue> entry : localeProperties.getElementLabels(original).entries()) {
      Locale locale = entry.getKey();
      final KeyValue originalKeyValue = entry.getValue();

      List<KeyValue> keyValues = localeProperties.getElementLabels(copy).get(locale);
      KeyValue keyValue = Iterables.find(keyValues, new Predicate<KeyValue>() {
        @Override
        public boolean apply(@SuppressWarnings("hiding") KeyValue keyValue) {
          return keyValue.getKey().equals(originalKeyValue.getKey());
        }
      });
      keyValue.setValue(originalKeyValue.getValue());
    }
  }

  protected abstract void onSave(AjaxRequestTarget target, Question newQuestion);

}
