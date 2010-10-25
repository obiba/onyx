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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
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
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.utils.AJAXDownload;
import org.obiba.onyx.quartz.editor.utils.ZipResourceStream;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public abstract class QuestionnairePropertiesPanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final IModel<LocaleProperties> localePropertiesModel;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<Questionnaire> form;

  @SuppressWarnings("rawtypes")
  public QuestionnairePropertiesPanel(String id, IModel<Questionnaire> model, final ModalWindow modalWindow) {
    super(id, model);
    final Questionnaire questionnaire = model.getObject();

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<Questionnaire>("form", model));

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
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

    TextField<String> version = new TextField<String>("version", new PropertyModel<String>(form.getModel(), "version"));
    version.setLabel(new ResourceModel("Version"));
    version.add(new RequiredFormFieldBehavior());
    version.add(new StringValidator.MaximumLengthValidator(20));
    form.add(version);
    form.add(new SimpleFormComponentLabel("versionLabel", version));

    RadioGroup<String> uiType = new RadioGroup<String>("uiType", new PropertyModel<String>(form.getModel(), "uiType"));
    uiType.setLabel(new ResourceModel("UIType"));
    uiType.setRequired(true);
    form.add(uiType);

    Radio<String> standardUiType = new Radio<String>("standard", new Model<String>(Questionnaire.STANDARD_UI));
    standardUiType.setLabel(new ResourceModel("UIType.standard"));
    uiType.add(standardUiType);
    uiType.add(new SimpleFormComponentLabel("standardLabel", standardUiType));

    Radio<String> simplifiedUiType = new Radio<String>("simplified", new Model<String>(Questionnaire.SIMPLIFIED_UI));
    simplifiedUiType.setLabel(new ResourceModel("UIType.simplified"));
    uiType.add(simplifiedUiType);
    uiType.add(new SimpleFormComponentLabel("simplifiedLabel", simplifiedUiType));

    localePropertiesModel = new Model<LocaleProperties>(localePropertiesUtils.load(questionnaire, questionnaire));
    final LabelsPanel labelsPanel = new LabelsPanel("labels", localePropertiesModel, model, feedbackPanel, feedbackWindow);
    labelsPanel.setOutputMarkupId(true);
    form.add(labelsPanel);

    final Locale userLocale = Session.get().getLocale();
    IChoiceRenderer<Locale> renderer = new IChoiceRenderer<Locale>() {
      @Override
      public String getIdValue(Locale locale, int index) {
        return locale.toString();
      }

      @Override
      public Object getDisplayValue(Locale locale) {
        return locale.getDisplayLanguage(userLocale);
      }
    };

    IModel<List<Locale>> localeChoices = new LoadableDetachableModel<List<Locale>>() {
      @Override
      protected List<Locale> load() {
        List<Locale> locales = new ArrayList<Locale>();
        for(String language : Locale.getISOLanguages()) {
          locales.add(new Locale(language));
        }
        Collections.sort(locales, new Comparator<Locale>() {
          @Override
          public int compare(Locale locale1, Locale locale2) {
            return locale1.getDisplayLanguage(userLocale).compareTo(locale2.getDisplayLanguage(userLocale));
          }
        });
        return locales;
      }
    };

    final Palette<Locale> localesPalette = new Palette<Locale>("languages", new ListModel<Locale>(questionnaire.getLocales()), localeChoices, renderer, 5, false) {
      protected Recorder<Locale> newRecorderComponent() {
        Recorder<Locale> recorder = super.newRecorderComponent();
        recorder.add(new AjaxFormComponentUpdatingBehavior("onchange") {
          @Override
          protected void onUpdate(AjaxRequestTarget target) {
            LocaleProperties localeProperties = localePropertiesModel.getObject();
            Collection<Locale> selectedLocales = getModelCollection();
            @SuppressWarnings("unchecked")
            Collection<Locale> removedLocales = CollectionUtils.subtract(localeProperties.getLocales(), selectedLocales);
            for(Locale locale : removedLocales) {
              localeProperties.removeLocale(questionnaire, locale);
            }
            for(Locale locale : selectedLocales) {
              if(!localeProperties.getLocales().contains(locale)) {
                localeProperties.addLocale(questionnaire, locale);
              }
            }
            labelsPanel.onModelChange(target);
          }
        });
        return recorder;
      }
    };

    form.add(localesPalette);

    final AJAXDownload download = new AJAXDownload() {

      @Override
      protected IResourceStream getResourceStream() {
        try {
          QuestionnaireBundle bundle = questionnaireBundleManager.getBundle(questionnaire.getName());
          File tmpFile = File.createTempFile(bundle.getName() + "-locales", ".zip");
          tmpFile.deleteOnExit();

          OutputStream os = new FileOutputStream(tmpFile);
          ZipOutputStream zos = new ZipOutputStream(os);
          byte[] buffer = new byte[1024];
          for(Locale locale : bundle.getAvailableLanguages()) {
            ZipEntry zip = new ZipEntry("language_" + locale.getLanguage() + ".properties");
            zos.putNextEntry(zip);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Properties properties = bundle.getLanguage(locale);
            properties.store(out, "Languages properties for questionnaire " + bundle.getName() + " for " + locale.getDisplayLanguage());
            InputStream fis = new ByteArrayInputStream(out.toByteArray());
            int read = 0;
            while((read = fis.read(buffer)) != -1) {
              zos.write(buffer, 0, read);
            }
            zos.closeEntry();
          }
          zos.close();

          return new ZipResourceStream(tmpFile);
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

  /**
   * 
   * @param target
   * @param questionnaire
   */
  public abstract void onSave(AjaxRequestTarget target, Questionnaire questionnaire);

  public void persist(AjaxRequestTarget target) {
    try {
      questionnairePersistenceUtils.persist(form.getModelObject(), localePropertiesModel.getObject());
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }

}
