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

import java.util.ArrayList;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.form.AbstractQuestionnaireElementPanel;
import org.obiba.onyx.quartz.editor.locale.model.LocaleChoiceRenderer;
import org.obiba.onyx.quartz.editor.locale.model.LocaleListModel;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public class QuestionnairePropertiesPanel extends AbstractQuestionnaireElementPanel<Questionnaire> {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private ListModel<Locale> listLocaleModel;

  public QuestionnairePropertiesPanel(String id, IModel<Questionnaire> model, ModalWindow modalWindow) {
    super(id, model, model, modalWindow);
    createComponent();
  }

  public void createComponent() {
    // -------------------- Name --------------------
    TextField<String> nameTextField = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
    nameTextField.add(new RequiredFormFieldBehavior());
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
        if(!isNewName && !validatable.getValue().equals(getForm().getModelObject().getName())) {
          error(validatable);
        }
      }

      @Override
      protected String resourceKey() {
        return "NameNotAlreadyExistValidator";
      }
    });

    // -------------------- Version --------------------
    TextField<String> versionTextField = new TextField<String>("version", new PropertyModel<String>(form.getModel(), "version"));
    versionTextField.add(new RequiredFormFieldBehavior());
    versionTextField.add(new StringValidator.MaximumLengthValidator(20));

    // -------------------- Locales and Locales labels --------------------

    listLocaleModel = new ListModel<Locale>(new ArrayList<Locale>(form.getModelObject().getLocales()));

    final LocalesPropertiesAjaxTabbedPanel localesPropertiesAjaxTabbedPanel = new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", listLocaleModel, form.getModel(), localePropertiesModel);

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

    form.add(nameTextField, versionTextField, localesPalette, localesPropertiesAjaxTabbedPanel);
  }

  @Override
  public void onSave(AjaxRequestTarget target, Questionnaire questionnaire) {
    questionnaire.getLocales().clear();
    for(Locale locale : listLocaleModel.getObject()) {
      questionnaire.addLocale(locale);
    }
    saveToFiles();
  }
}
