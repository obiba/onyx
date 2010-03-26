/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.OpenAnswerDefinitionValidatorFactory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.behavior.InvalidFormFieldBehavior;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOpenAnswerDefinitionPanel extends AbstractOpenAnswerDefinitionPanel {

  private static final long serialVersionUID = 8950481253772691811L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultOpenAnswerDefinitionPanel.class);

  public static final String INPUT_SIZE_KEY = "size";

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DataField openField;

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public DefaultOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    super(id, questionModel, questionCategoryModel);
    initialize();
  }

  public DefaultOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    super(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);
    initialize();
  }

  @SuppressWarnings("serial")
  private void initialize() {
    setOutputMarkupId(true);

    OpenAnswer previousAnswer = activeQuestionnaireAdministrationService.findOpenAnswer(getQuestion(), getQuestionCategory().getCategory(), getOpenAnswerDefinition());
    if(previousAnswer != null) {
      setData(previousAnswer.getData());
    }

    QuestionnaireStringResourceModel openLabel = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), "label");
    QuestionnaireStringResourceModel unitLabel = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), "unitLabel");

    add(new Label("label", openLabel));

    if(getOpenAnswerDefinition().getDefaultValues().size() > 1) {
      openField = new DataField("open", new PropertyModel(this, "data"), getOpenAnswerDefinition().getDataType(), getOpenAnswerDefinition().getDefaultValues(), new IChoiceRenderer() {

        public Object getDisplayValue(Object object) {
          Data data = (Data) object;
          return (String) new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), data.getValueAsString()).getObject();
        }

        public String getIdValue(Object object, int index) {
          Data data = (Data) object;
          return data.getValueAsString();
        }

      }, unitLabel.getString());
    } else if(getOpenAnswerDefinition().getDefaultValues().size() > 0) {
      setData(getOpenAnswerDefinition().getDefaultValues().get(0));
      openField = new DataField("open", new PropertyModel(this, "data"), getOpenAnswerDefinition().getDataType(), unitLabel.getString());
    } else {
      openField = new DataField("open", new PropertyModel(this, "data"), getOpenAnswerDefinition().getDataType(), unitLabel.getString());
    }
    openField.getField().setOutputMarkupId(true);
    add(openField);

    // validators
    for(IValidator validator : OpenAnswerDefinitionValidatorFactory.getValidators(getOpenAnswerDefinitionModel(), activeQuestionnaireAdministrationService.getQuestionnaireParticipant().getParticipant())) {
      openField.add(validator);
    }

    // at least this validator for textual input
    if(getOpenAnswerDefinition().getDataType().equals(DataType.TEXT) && getOpenAnswerDefinition().getDefaultValues().size() == 0) {
      // see OpenAnswer.textValue column length
      openField.add(new DataValidator(new StringValidator.MaximumLengthValidator(2000), DataType.TEXT));
    }

    // UI arguments as attributes
    ValueMap arguments = getOpenAnswerDefinition().getUIArgumentsValueMap();
    if(arguments != null) {
      int size = arguments.getInt(INPUT_SIZE_KEY, -1);
      if(size > 0) {
        openField.add(new AttributeAppender("size", new Model(Integer.toString(size)), ""));
      }
    }

    // behaviors
    openField.add(new InvalidFormFieldBehavior());

    openField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        // persist data
        activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition(), getData());

        // clean a previous error message
        updateFeedback(target);

        fireQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), true);
      }

      @Override
      protected void onError(AjaxRequestTarget target, RuntimeException e) {
        super.onError(target, e);
        // display error messages
        updateFeedback(target);
      }
    });

    if(getOpenAnswerDefinition().getDefaultValues().size() == 0) {
      openField.add(new AjaxEventBehavior("onclick") {

        @Override
        protected void onEvent(AjaxRequestTarget target) {
          // persist data
          // do not fire event if category was already selected
          if(activeQuestionnaireAdministrationService.findAnswer(getQuestion(), getQuestionCategory().getCategory()) == null) {
            openField.focusField(target);

            // persist data for category
            activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory());

            fireQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), true);
          }
        }

      });
    }

    // set the label of the field
    openField.setLabel(QuestionnaireStringResourceModelHelper.getStringResourceModel(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition()));
  }

  /**
   * Refresh wizard feedback panel and input field.
   * @param target
   */
  private void updateFeedback(final AjaxRequestTarget target) {
    WizardForm wizard = (WizardForm) findParent(WizardForm.class);
    if(wizard != null && wizard.getFeedbackWindow() != null) {
      if(wizard.getFeedbackMessage() != null) wizard.getFeedbackWindow().show(target);
    }
    target.appendJavascript("Resizer.resizeWizard();");
    target.addComponent(openField.getField());
  }

  public void setFieldEnabled(boolean enabled) {
    openField.setFieldEnabled(enabled);
  }

  public boolean isFieldEnabled() {
    return openField.isFieldEnabled();
  }

  @Override
  public void resetField() {
    openField.setFieldModelObject(null);
    openField.getField().clearInput();
  }
}
