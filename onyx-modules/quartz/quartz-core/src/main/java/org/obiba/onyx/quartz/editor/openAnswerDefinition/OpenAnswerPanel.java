/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswerDefinition;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.quartz.editor.utils.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.data.IDataValidator;

/**
 *
 */
@SuppressWarnings("serial")
public class OpenAnswerPanel extends Panel {

  // TODO: localize date format
  public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private DropDownChoice<DataType> dataType;

  private WebMarkupContainer minMaxContainer;

  private TextField<String> minLength;

  private TextField<String> maxLength;

  private TextField<String> minNumeric;

  private TextField<String> maxNumeric;

  private TextField<String> beforeDate;

  private TextField<String> afterDate;

  private SimpleFormComponentLabel minimumLabel;

  private SimpleFormComponentLabel maximumLabel;

  private ListModel<LocaleProperties> localePropertiesModel;

  public OpenAnswerPanel(String id, final IModel<OpenAnswerDefinition> model, final IModel<Question> questionModel, IModel<Questionnaire> questionnaireModel) {
    super(id, model);

    localePropertiesModel = new ListModel<LocaleProperties>(localePropertiesUtils.loadLocaleProperties(model, questionnaireModel));

    Question question = questionModel.getObject();
    OpenAnswerDefinition openAnswerDefinition = model.getObject();

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        // TODO OpenAnswer name must be unique in his category
      }
    });
    add(name);
    add(new SimpleFormComponentLabel("nameLabel", name));

    TextField<String> variable = new TextField<String>("variable", new MapModel<String>(new PropertyModel<Map<String, String>>(model, "variableNames"), question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    variable.add(new StringValidator.MaximumLengthValidator(20));
    add(variable);
    add(new SimpleFormComponentLabel("variableLabel", variable));

    List<DataType> typeChoices = new ArrayList<DataType>(Arrays.asList(DataType.values()));
    typeChoices.remove(DataType.BOOLEAN);
    typeChoices.remove(DataType.DATA);
    dataType = new DropDownChoice<DataType>("dataType", new PropertyModel<DataType>(model, "dataType"), typeChoices, new IChoiceRenderer<DataType>() {
      @Override
      public Object getDisplayValue(DataType type) {
        return new StringResourceModel("DataType." + type, OpenAnswerPanel.this, null).getString();
      }

      @Override
      public String getIdValue(DataType type, int index) {
        return type.name();
      }
    });

    dataType.setLabel(new ResourceModel("DataType"));
    dataType.add(new RequiredFormFieldBehavior());
    dataType.setNullValid(false);

    add(new SimpleFormComponentLabel("dataTypeLabel", dataType));
    add(dataType);

    TextField<String> unit = new TextField<String>("unit", new PropertyModel<String>(model, "unit"));
    unit.setLabel(new ResourceModel("Unit"));
    add(unit);
    add(new SimpleFormComponentLabel("unitLabel", unit));

    add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", model, localePropertiesModel));

    CheckBox requiredCheckBox = new CheckBox("required", new PropertyModel<Boolean>(model, "required"));
    requiredCheckBox.setLabel(new ResourceModel("AnswerRequired"));
    add(requiredCheckBox);
    add(new SimpleFormComponentLabel("requiredLabel", requiredCheckBox));

    // min/max validators
    String maxValue = null, minValue = null;
    for(IDataValidator<?> dataValidator : openAnswerDefinition.getDataValidators()) {
      IValidator<?> validator = dataValidator.getValidator();
      if(validator instanceof RangeValidator<?>) {
        RangeValidator<?> rangeValidator = (RangeValidator<?>) validator;
        Object minimum = rangeValidator.getMinimum();
        Object maximum = rangeValidator.getMaximum();
        if(dataValidator.getDataType() == DataType.DATE) {
          if(minimum != null) minValue = DATE_FORMAT.format((Date) minimum);
          if(maximum != null) maxValue = DATE_FORMAT.format((Date) maximum);
        } else {
          if(minimum != null) minValue = String.valueOf(minimum);
          if(maximum != null) maxValue = String.valueOf(maximum);
        }
      } else if(validator instanceof StringValidator.MaximumLengthValidator) {
        int maximum = ((StringValidator.MaximumLengthValidator) validator).getMaximum();
        if(maximum > 0) maxValue = String.valueOf(maximum);
      } else if(validator instanceof MaximumValidator<?>) {
        Object maximum = ((MaximumValidator<?>) validator).getMaximum();
        if(dataValidator.getDataType() == DataType.DATE) {
          if(maximum != null) maxValue = DATE_FORMAT.format((Date) maximum);
        } else {
          if(maximum != null) maxValue = String.valueOf(maximum);
        }
      } else if(validator instanceof StringValidator.MinimumLengthValidator) {
        int minimum = ((StringValidator.MinimumLengthValidator) validator).getMinimum();
        if(minimum > 0) minValue = String.valueOf(minimum);
      } else if(validator instanceof MinimumValidator<?>) {
        Object minimum = ((MinimumValidator<?>) validator).getMinimum();
        if(dataValidator.getDataType() == DataType.DATE) {
          if(minimum != null) minValue = DATE_FORMAT.format((Date) minimum);
        } else {
          if(minimum != null) minValue = String.valueOf(minimum);
        }
      }
    }

    minMaxContainer = new WebMarkupContainer("minMaxContainer");
    minMaxContainer.setOutputMarkupId(true);
    add(minMaxContainer);

    minLength = new TextField<String>("minLength", new Model<String>(minValue), String.class);
    minLength.setLabel(new ResourceModel("Minimum.length"));
    minMaxContainer.add(minLength);

    maxLength = new TextField<String>("maxLength", new Model<String>(maxValue), String.class);
    maxLength.setLabel(new ResourceModel("Maximum.length"));
    minMaxContainer.add(maxLength);

    PatternValidator numericPatternValidator = new PatternValidator("[0-9]*");
    minNumeric = new TextField<String>("minNumeric", new Model<String>(minValue), String.class);
    minNumeric.setLabel(new ResourceModel("Minimum"));
    minNumeric.add(numericPatternValidator);
    minMaxContainer.add(minNumeric);

    maxNumeric = new TextField<String>("maxNumeric", new Model<String>(maxValue), String.class);
    maxNumeric.setLabel(new ResourceModel("Maximum"));
    maxNumeric.add(numericPatternValidator);
    minMaxContainer.add(maxNumeric);

    // TODO validate date
    // PatternValidator datePatternValidator = new PatternValidator("[0-9]4-[0-9]2-[0-9]2");
    beforeDate = new TextField<String>("beforeDate", new Model<String>(minValue), String.class);
    beforeDate.setLabel(new ResourceModel("Before"));
    // beforeDate.add(datePatternValidator);
    minMaxContainer.add(beforeDate);

    afterDate = new TextField<String>("afterDate", new Model<String>(maxValue), String.class);
    afterDate.setLabel(new ResourceModel("After"));
    // afterDate.add(datePatternValidator);
    minMaxContainer.add(afterDate);

    minMaxContainer.add(minimumLabel = new SimpleFormComponentLabel("minimumLabel", minLength));
    minMaxContainer.add(maximumLabel = new SimpleFormComponentLabel("maximumLabel", maxLength));

    setMinMaxLabels(dataType.getModelObject());

    // submit the whole form instead of just the dataType component
    dataType.add(new AjaxFormSubmitBehavior("onchange") {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        String value = dataType.getValue(); // use value because model is not set if validation error
        setMinMaxLabels(value == null ? null : DataType.valueOf(value));
        target.addComponent(minMaxContainer);
      }

      @Override
      protected void onError(AjaxRequestTarget target) {
        Session.get().getFeedbackMessages().clear(); // we don't want to validate fields now
        onSubmit(target);
      }
    });
  }

  /**
   * 
   * @param target
   * @param openAnswer
   */
  public void onSave(AjaxRequestTarget target, OpenAnswerDefinition openAnswer) {

  }

  @SuppressWarnings("incomplete-switch")
  private void setMinMaxLabels(DataType type) {
    if(type == null) {
      setMinimumLabel(minLength);
      setMaximumLabel(maxLength);
      minLength.setVisible(true).setEnabled(false);
      maxLength.setVisible(true).setEnabled(false);
      clearAndHide(minNumeric, maxNumeric, beforeDate, afterDate);
    } else {
      switch(type) {
      case TEXT:
        setMinimumLabel(minLength);
        setMaximumLabel(maxLength);
        minLength.setVisible(true).setEnabled(true);
        maxLength.setVisible(true).setEnabled(true);
        clearAndHide(minNumeric, maxNumeric, beforeDate, afterDate);
        break;

      case DECIMAL:
      case INTEGER:
        setMinimumLabel(minNumeric);
        setMaximumLabel(maxNumeric);
        minNumeric.setVisible(true);
        maxNumeric.setVisible(true);
        clearAndHide(minLength, maxLength, beforeDate, afterDate);
        break;

      case DATE:
        setMinimumLabel(beforeDate);
        setMaximumLabel(afterDate);
        beforeDate.setVisible(true);
        afterDate.setVisible(true);
        clearAndHide(minLength, maxLength, minNumeric, maxNumeric);
        break;
      }
    }
  }

  private void clearAndHide(FormComponent<?>... components) {
    if(components != null) {
      for(FormComponent<?> component : components) {
        component.setModelObject(null);
        component.setVisible(false);
      }
    }
  }

  private void setMinimumLabel(FormComponent<?> component) {
    SimpleFormComponentLabel newLabel = new SimpleFormComponentLabel(minimumLabel.getId(), component);
    minimumLabel.replaceWith(newLabel);
    minimumLabel = newLabel;
  }

  private void setMaximumLabel(FormComponent<?> component) {
    SimpleFormComponentLabel newLabel = new SimpleFormComponentLabel(maximumLabel.getId(), component);
    maximumLabel.replaceWith(newLabel);
    maximumLabel = newLabel;
  }

}
