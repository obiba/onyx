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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.quartz.editor.questionnaire.EditedQuestionnaire;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePersistenceUtils;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public class CategoryPropertiesPanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  private final VariableNamesPanel variableNamesPanel;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<EditedQuestionCategory> form;

  private final IModel<EditedQuestionnaire> questionnaireModel;

  private ListModel<LocaleProperties> localePropertiesModelCategory;

  private ListModel<LocaleProperties> localePropertiesModelQuestionCategory;

  public CategoryPropertiesPanel(String id, IModel<QuestionCategory> model, IModel<EditedQuestionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id, new Model<EditedQuestionCategory>(new EditedQuestionCategory(model.getObject())));
    this.questionnaireModel = questionnaireModel;

    Questionnaire questionnaire = questionnaireModel.getObject().getElement();

    List<LocaleProperties> listLocalePropertiesQuestionCategory = new ArrayList<LocaleProperties>();
    for(Locale locale : questionnaire.getLocales()) {
      LocaleProperties localeProperties = new LocaleProperties(locale, model);
      List<String> values = new ArrayList<String>();
      for(String property : localeProperties.getKeys()) {
        if(StringUtils.isNotBlank(model.getObject().getName())) {
          QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaire.getName());
          if(bundle != null) {
            values.add(QuestionnaireStringResourceModelHelper.getNonRecursiveResolutionMessage(bundle, model.getObject(), property, new Object[0], locale));
          }
        }
      }
      localeProperties.setValues(values.toArray(new String[localeProperties.getKeys().length]));
      listLocalePropertiesQuestionCategory.add(localeProperties);
    }
    localePropertiesModelQuestionCategory = new ListModel<LocaleProperties>(listLocalePropertiesQuestionCategory);

    List<LocaleProperties> listLocalePropertiesCategory = new ArrayList<LocaleProperties>();
    for(Locale locale : questionnaire.getLocales()) {
      LocaleProperties localeProperties = new LocaleProperties(locale, model);
      List<String> values = new ArrayList<String>();
      for(String property : localeProperties.getKeys()) {
        if(StringUtils.isNotBlank(model.getObject().getName())) {
          QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaire.getName());
          if(bundle != null) {
            values.add(QuestionnaireStringResourceModelHelper.getNonRecursiveResolutionMessage(bundle, model.getObject(), property, new Object[0], locale));
          }
        }
      }
      localeProperties.setValues(values.toArray(new String[localeProperties.getKeys().length]));
      listLocalePropertiesCategory.add(localeProperties);
    }
    localePropertiesModelCategory = new ListModel<LocaleProperties>(listLocalePropertiesCategory);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<EditedQuestionCategory>("form", (IModel<EditedQuestionCategory>) getDefaultModel()));

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "element.category.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    form.add(name);
    form.add(new SimpleFormComponentLabel("nameLabel", name));

    Category category = form.getModelObject().getElement().getCategory();

    TextField<String> exportName = new TextField<String>("exportName", new PropertyModel<String>(form.getModel(), "element.exportName"));
    exportName.setLabel(new ResourceModel("ExportName"));
    form.add(exportName);
    form.add(new SimpleFormComponentLabel("exportNameLabel", exportName));

    form.add(new LocalesPropertiesAjaxTabbedPanel("categoryLocalesPropertiesTabs", new PropertyModel<Category>(form.getModel(), "element.category"), localePropertiesModelCategory));

    form.add(new LocalesPropertiesAjaxTabbedPanel("questionCategoryLocalesPropertiesTabs", new PropertyModel<QuestionCategory>(form.getModel(), "element"), localePropertiesModelQuestionCategory));

    form.add(new CheckBox("escape", new PropertyModel<Boolean>(form.getModel(), "element.category.escape")));
    form.add(new CheckBox("noAnswer", new PropertyModel<Boolean>(form.getModel(), "element.category.noAnswer")));
    form.add(variableNamesPanel = new VariableNamesPanel("variableNamesPanel", category.getVariableNames()));

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
  public void onSave(AjaxRequestTarget target, EditedQuestionCategory editedQuestionCategory) {
    Iterable<LocaleProperties> localePropertiesIterable = Iterables.concat(localePropertiesModelCategory.getObject(), localePropertiesModelQuestionCategory.getObject());
    List<LocaleProperties> localePropertiesList = new ArrayList<LocaleProperties>(Arrays.asList(Iterables.toArray(localePropertiesIterable, LocaleProperties.class)));
    editedQuestionCategory.setLocalePropertiesWithNamingStrategy(localePropertiesList);
    for(Map.Entry<String, String> entries : variableNamesPanel.getNewMapData().entrySet()) {
      editedQuestionCategory.getElement().getCategory().addVariableName(entries.getKey(), entries.getValue());
    }
  }

  public void persist(AjaxRequestTarget target) {
    try {
      questionnairePersistenceUtils.persist(form.getModelObject(), questionnaireModel.getObject());
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }
}
