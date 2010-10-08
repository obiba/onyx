/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
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
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.editor.locale.model.LocaleChoiceRenderer;
import org.obiba.onyx.quartz.editor.locale.model.LocaleListModel;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.quartz.editor.utils.AJAXDownload;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public class QuestionnairePropertiesPanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  private final ListModel<Locale> listLocaleModel;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<EditedQuestionnaire> form;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public QuestionnairePropertiesPanel(String id, IModel<Questionnaire> model, final ModalWindow modalWindow) {
    super(id, new Model<EditedQuestionnaire>(new EditedQuestionnaire(model.getObject())));

    List<LocaleProperties> listLocaleProperties = new ArrayList<LocaleProperties>();
    final Questionnaire questionnaire = model.getObject();
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
      listLocaleProperties.add(localeProperties);
    }
    ListModel<LocaleProperties> localePropertiesModel = new ListModel<LocaleProperties>(listLocaleProperties);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<EditedQuestionnaire>("form", (IModel<EditedQuestionnaire>) getDefaultModel()));

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "element.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new AbstractValidator<String>() {

      @Override
      protected void onValidate(final IValidatable<String> validatable) {
        boolean isNewName = Iterables.all(questionnaireBundleManager.bundles(), new Predicate<QuestionnaireBundle>() {

          @Override
          public boolean apply(QuestionnaireBundle input) {
            return !input.getName().equals(validatable.getValue());
          }
        });
        if(!isNewName && !validatable.getValue().equals(questionnaire.getName())) {
          error(validatable, "NameNotAlreadyExistValidator");
        }
      }

    });
    form.add(name);
    form.add(new SimpleFormComponentLabel("nameLabel", name));

    TextField<String> version = new TextField<String>("version", new PropertyModel<String>(form.getModel(), "element.version"));
    version.setLabel(new ResourceModel("Version"));
    version.add(new RequiredFormFieldBehavior());
    version.add(new StringValidator.MaximumLengthValidator(20));
    form.add(version);
    form.add(new SimpleFormComponentLabel("versionLabel", version));

    CheckBox touchScreen = new CheckBox("touchScreen", new PropertyModel<Boolean>(form.getModel(), "touchScreen"));
    touchScreen.setLabel(new ResourceModel("TouchScreen"));
    form.add(touchScreen);
    form.add(new SimpleFormComponentLabel("touchScreenLabel", touchScreen));

    listLocaleModel = new ListModel<Locale>(new ArrayList<Locale>(questionnaire.getLocales()));

    final LocalesPropertiesAjaxTabbedPanel localesPropertiesAjaxTabbedPanel = new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", listLocaleModel, new PropertyModel<Questionnaire>(form.getModel(), "element"), localePropertiesModel);

    Palette<Locale> localesPalette = new Palette<Locale>("languages", listLocaleModel, LocaleListModel.getInstance(), LocaleChoiceRenderer.getInstance(), 7, false) {

      @Override
      protected Recorder<Locale> newRecorderComponent() {
        final Recorder<Locale> recorder = super.newRecorderComponent();
        recorder.add(new AjaxFormComponentUpdatingBehavior("onchange") {

          @Override
          protected void onUpdate(AjaxRequestTarget target) {
            localesPropertiesAjaxTabbedPanel.dependantModelChanged();
            target.addComponent(localesPropertiesAjaxTabbedPanel);
          }
        });
        return recorder;
      }
    };

    form.add(localesPalette, localesPropertiesAjaxTabbedPanel);

    final AJAXDownload download = new AJAXDownload() {

      @Override
      protected IResourceStream getResourceStream() {
        try {
          return new LocalePropertiesZipResource(questionnaireBundleManager.getBundle(questionnaire.getName()));
        } catch(IOException e) {
          log.error("Cannot persist questionnaire", e);
          return null;
        }
      }

      @Override
      protected String getFileName() {
        return questionnaire.getName() + "-locales.zip";
      }
    };
    form.add(download);

    form.add(new AjaxLink("downloadLocaleProperties") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        download.initiate(target);

      }
    });

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

  public void onSave(AjaxRequestTarget target, EditedQuestionnaire editedQuestionnaire) {
    Questionnaire questionnaire = editedQuestionnaire.getElement();
    questionnaire.getLocales().clear();
    for(Locale locale : listLocaleModel.getObject()) {
      questionnaire.addLocale(locale);
    }
    persist(target);
  }

  public void persist(AjaxRequestTarget target) {
    try {
      questionnairePersistenceUtils.persist(form.getModelObject().getElement(), form.getModelObject());
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }

}
