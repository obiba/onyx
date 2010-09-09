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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireCreator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.editor.QuestionnaireElementListPanel;
import org.obiba.onyx.quartz.editor.locale.model.LocaleChoiceRenderer;
import org.obiba.onyx.quartz.editor.locale.model.LocaleListModel;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class QuestionnairePropertiesPanel extends Panel {

  private static final long serialVersionUID = 1L;

  protected final ModalWindow modalWindow;

  @SpringBean
  protected QuestionnaireBundleManager questionnaireBundleManager;

  protected FeedbackPanel feedbackPanel;

  protected FeedbackWindow feedbackWindow;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  public QuestionnairePropertiesPanel(String id, IModel<Questionnaire> model, ModalWindow modalWindow) {
    super(id, model);
    this.modalWindow = modalWindow;
    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);
    add(new QuestionnaireForm("questionnaireForm", model));
  }

  public class QuestionnaireForm extends Form<Questionnaire> {

    private static final long serialVersionUID = 1L;

    /**
     * @param id
     * @param model
     */
    public QuestionnaireForm(String id, final IModel<Questionnaire> model) {
      super(id, model);

      // -------------------- Name --------------------
      TextField<String> nameTextField = createNameField();

      // -------------------- Version --------------------
      TextField<String> versionTextField = createVersionField();

      // -------------------- Locales and Locales labels --------------------

      final ListModel<LocaleProperties> localePropertiesModel = createLocalePropertiesModel();

      // make a copy of locales list because we don't want affect model when we manipulate Palette
      final ListModel<Locale> listLocaleModel = new ListModel<Locale>(new ArrayList<Locale>(getModelObject().getLocales()));

      final LocalesPropertiesAjaxTabbedPanel localesPropertiesAjaxTabbedPanel = new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", listLocaleModel, getModelObject(), localePropertiesModel);

      Palette<Locale> localesPalette = new Palette<Locale>("languages", listLocaleModel, LocaleListModel.getInstance(), LocaleChoiceRenderer.getInstance(), 7, false) {

        private static final long serialVersionUID = 1L;

        @Override
        protected Recorder<Locale> newRecorderComponent() {
          final Recorder<Locale> recorder = super.newRecorderComponent();
          recorder.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
              localesPropertiesAjaxTabbedPanel.dependantModelChanged();
              target.addComponent(localesPropertiesAjaxTabbedPanel);
            }
          });
          return recorder;
        }
      };

      // -------------------- Section --------------------

      QuestionnaireElementListPanel<Section> sectionListPanel = new QuestionnaireElementListPanel<Section>("sectionListPanel", new ListModel<Section>(getModelObject().getSections()), Section.class);

      // -------------------- Save --------------------
      AjaxButton saveButton = new AjaxButton("save", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          super.onSubmit();
          Questionnaire questionnaire = model.getObject();

          try {
            questionnaire.getLocales().clear();
            for(Locale locale : listLocaleModel.getObject()) {
              questionnaire.addLocale(locale);
            }

            log.info(questionnaire.getName() + " " + questionnaire.getVersion() + " " + questionnaire.getLocales().size());
            Map<Locale, Properties> extractedLocaleProperties = extractLocalePropertiesToMap(questionnaire);

            // FIXME to have same working directory (for QuestionnaireCreator and QuestionnaireBundleManager)
            File bundleRootDirectory = new File("target\\work\\webapp\\WEB-INF\\config\\quartz\\resources", "questionnaires");
            File bundleSourceDirectory = new File("src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "config" + File.separatorChar + "quartz" + File.separatorChar + "resources", "questionnaires");

            new QuestionnaireCreator(bundleRootDirectory, bundleSourceDirectory).createQuestionnaire(QuestionnaireBuilder.getInstance(questionnaire), extractedLocaleProperties);
          } catch(IOException e) {
            e.printStackTrace();
          }
        }

        /**
         * Create a Map<Locale, Properties> with localePropertiesModel object
         * @param affectedQuestionnaire
         * @return
         */
        private Map<Locale, Properties> extractLocalePropertiesToMap(Questionnaire affectedQuestionnaire) {
          DefaultPropertyKeyProviderImpl defaultPropertyKeyProviderImpl = new DefaultPropertyKeyProviderImpl();
          Map<Locale, Properties> mapLocaleProperties = new HashMap<Locale, Properties>();
          for(LocaleProperties localeProperties : localePropertiesModel.getObject()) {
            Properties properties = new Properties();
            for(int i = 0; i < localeProperties.getKeys().length; i++) {
              String key = localeProperties.getKeys()[i];
              String value = localeProperties.getValues()[i];
              String keyWithNamingStrategy = defaultPropertyKeyProviderImpl.getPropertyKey(affectedQuestionnaire, key);
              properties.setProperty(keyWithNamingStrategy, value != null ? value : "");
            }
            mapLocaleProperties.put(localeProperties.getLocale(), properties);
          }
          return mapLocaleProperties;
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      // -------------------- Cancel --------------------
      AjaxButton cancelButton = new AjaxButton("cancel", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          modalWindow.close(target);
        }
      };
      cancelButton.setDefaultFormProcessing(false);

      // add to UI
      add(nameTextField);
      add(versionTextField);
      add(localesPalette);
      add(localesPropertiesAjaxTabbedPanel);
      add(sectionListPanel);
      add(saveButton);
      add(cancelButton);
    }

    /**
     * Create model of LocaleProperties list using locale of questionnaire model and labels resources
     * @return
     */
    private ListModel<LocaleProperties> createLocalePropertiesModel() {
      List<LocaleProperties> list = new ArrayList<LocaleProperties>();
      for(Locale locale : getModelObject().getLocales()) {
        LocaleProperties localeProperties = new LocaleProperties(locale, getModelObject());
        List<String> values = new ArrayList<String>();
        for(String property : localeProperties.getKeys()) {
          QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(getModelObject().getName());
          values.add(QuestionnaireStringResourceModelHelper.getMessage(bundle, getModelObject(), property, new Object[0], locale));
        }
        localeProperties.setValues(values.toArray(new String[0]));
        list.add(localeProperties);
      }
      ListModel<LocaleProperties> localePropertiesModel = new ListModel<LocaleProperties>(list);
      return localePropertiesModel;
    }

    private TextField<String> createVersionField() {
      TextField<String> versionTextField = new TextField<String>("version", new PropertyModel<String>(getModel(), "version"));
      versionTextField.add(new RequiredFormFieldBehavior());
      versionTextField.add(new StringValidator.MaximumLengthValidator(20));
      return versionTextField;
    }

    private TextField<String> createNameField() {
      TextField<String> nameTextField = new TextField<String>("name", new PropertyModel<String>(getModel(), "name"));
      nameTextField.add(new RequiredFormFieldBehavior());
      nameTextField.add(new StringValidator.MaximumLengthValidator(100));
      nameTextField.add(new AbstractValidator<String>() {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onValidate(final IValidatable<String> validatable) {
          boolean isNewName = Iterables.all(questionnaireBundleManager.bundles(), new Predicate<QuestionnaireBundle>() {

            @Override
            public boolean apply(QuestionnaireBundle input) {
              return !input.getName().equals(validatable.getValue());
            }
          });
          if(!isNewName && !validatable.getValue().equals(getModelObject().getName())) {
            error(validatable);
          }
        }

        @Override
        protected String resourceKey() {
          return "NameNotAlreadyExistValidator";
        }
      });
      return nameTextField;
    }
  }
}
