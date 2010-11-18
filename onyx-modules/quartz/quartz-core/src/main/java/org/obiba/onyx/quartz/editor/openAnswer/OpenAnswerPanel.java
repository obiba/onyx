/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswer;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.behavior.VariableNameBehavior;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties.KeyValue;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.openAnswer.validation.ValidationDataSourceWindow;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.data.IDataValidator;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.markup.html.table.IColumnProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 *
 */
@SuppressWarnings("serial")
public class OpenAnswerPanel extends Panel {

  // TODO: localize date format
  public final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final DropDownChoice<DataType> dataTypeDropDown;

  private final WebMarkupContainer minMaxContainer;

  private final TextField<String> minLength;

  private final TextField<String> maxLength;

  private final TextField<String> minNumeric;

  private final TextField<String> maxNumeric;

  private final TextField<String> beforeDate;

  private final TextField<String> afterDate;

  private SimpleFormComponentLabel minimumLabel;

  private SimpleFormComponentLabel maximumLabel;

  private OnyxEntityList<ComparingDataSource> validators;

  private final ModalWindow validatorWindow;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<Question> questionModel;

  private SortableList<Data> defaultValuesList;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final IModel<LocaleProperties> localePropertiesModel;

  private LabelsPanel labelsPanel;

  @SuppressWarnings("rawtypes")
  private TextField defaultValue;

  private final VariableNameBehavior variableNameBehavior;

  private TextField<String> variable;

  public OpenAnswerPanel(String id, final IModel<OpenAnswerDefinition> model, final IModel<Category> categoryModel, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel, IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);
    this.questionModel = questionModel;
    this.questionnaireModel = questionnaireModel;
    this.localePropertiesModel = localePropertiesModel;
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;

    final Question question = questionModel.getObject();
    final Category category = categoryModel.getObject();
    final OpenAnswerDefinition openAnswer = model.getObject();

    validatorWindow = new ModalWindow("validatorWindow");
    validatorWindow.setCssClassName("onyx");
    validatorWindow.setInitialWidth(850);
    validatorWindow.setInitialHeight(450);
    validatorWindow.setResizable(true);
    validatorWindow.setTitle(new ResourceModel("Validator"));
    add(validatorWindow);

    final TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(!StringUtils.equalsIgnoreCase(model.getObject().getName(), validatable.getValue())) {
          QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
          if(questionnaireFinder.findOpenAnswerDefinition(validatable.getValue()) != null) {
            error(validatable, "OpenAnswerAlreadyExists");
          }
        }
      }
    });
    add(name).add(new SimpleFormComponentLabel("nameLabel", name));
    add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    variable = new TextField<String>("variable", new MapModel<String>(new PropertyModel<Map<String, String>>(model, "variableNames"), question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    add(variable).add(new SimpleFormComponentLabel("variableLabel", variable));
    add(new HelpTooltipPanel("variableHelp", new ResourceModel("Variable.Tooltip")));

    if(category == null) {
      variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), question, null) {
        @Override
        protected String generateVariableName(Question parentQuestion, @SuppressWarnings("hiding") Question question, @SuppressWarnings("hiding") Category category, @SuppressWarnings("hiding") String name) {
          String variableName = super.generateVariableName(parentQuestion, question, category, name);
          return StringUtils.isBlank(variableName) ? "" : variableName + "." + name;
        }
      };
    } else {
      variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), question, category);
    }

    add(variableNameBehavior);

    List<DataType> typeChoices = new ArrayList<DataType>(Arrays.asList(DataType.values()));
    typeChoices.remove(DataType.BOOLEAN);
    typeChoices.remove(DataType.DATA);
    dataTypeDropDown = new DropDownChoice<DataType>("dataType", new PropertyModel<DataType>(model, "dataType"), typeChoices, new IChoiceRenderer<DataType>() {
      @Override
      public Object getDisplayValue(DataType type) {
        return new StringResourceModel("DataType." + type, OpenAnswerPanel.this, null).getString();
      }

      @Override
      public String getIdValue(DataType type, int index) {
        return type.name();
      }
    });

    dataTypeDropDown.setLabel(new ResourceModel("DataType"));
    dataTypeDropDown.add(new RequiredFormFieldBehavior());
    dataTypeDropDown.setNullValid(false);

    add(dataTypeDropDown).add(new SimpleFormComponentLabel("dataTypeLabel", dataTypeDropDown));
    // add(new HelpTooltipPanel("dataTypeHelp", new ResourceModel("DataType.Tooltip")));

    TextField<String> unit = new TextField<String>("unit", new PropertyModel<String>(model, "unit"));
    unit.setLabel(new ResourceModel("Unit"));
    add(unit).add(new SimpleFormComponentLabel("unitLabel", unit));
    add(new HelpTooltipPanel("unitHelp", new ResourceModel("Unit.Tooltip")));

    localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), openAnswer);
    add(labelsPanel = new LabelsPanel("labels", localePropertiesModel, model, feedbackPanel, feedbackWindow));

    CheckBox requiredCheckBox = new CheckBox("required", new PropertyModel<Boolean>(model, "required"));
    requiredCheckBox.setLabel(new ResourceModel("AnswerRequired"));
    add(requiredCheckBox);
    add(new SimpleFormComponentLabel("requiredLabel", requiredCheckBox));

    // min/max validators
    String maxValue = null, minValue = null;
    for(IDataValidator<?> dataValidator : openAnswer.getDataValidators()) {
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

    setMinMaxLabels(dataTypeDropDown.getModelObject());

    add(validators = new OnyxEntityList<ComparingDataSource>("validators", new ValidationDataSourcesProvider(), new ValidationDataSourcesColumnProvider(), new ResourceModel("Validators")));

    final AjaxLink<Void> addValidator = new AjaxLink<Void>("addValidator") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        validatorWindow.setContent(new ValidationDataSourceWindow("content", new Model<ComparingDataSource>(), model, questionModel, questionnaireModel, validatorWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, ComparingDataSource comparingDataSource) {
            openAnswer.addValidationDataSource(comparingDataSource);
            target1.addComponent(validators);
          }
        });
        validatorWindow.show(target);
      }

      @Override
      public boolean isEnabled() {
        return StringUtils.isNotBlank(name.getModelObject());
      }
    };
    addValidator.setOutputMarkupId(true);
    add(addValidator.add(new Image("img", Images.ADD)));

    name.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.addComponent(addValidator);
      }
    });

    dataTypeDropDown.add(new OnChangeAjaxBehavior() {

      @SuppressWarnings("incomplete-switch")
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        setFieldType();
        String value = dataTypeDropDown.getValue(); // use value because model is not set if validation error
        DataType valueOf = DataType.valueOf(value);
        if(value != null) {
          OpenAnswerDefinition openAnswerDefinition = (OpenAnswerDefinition) getDefaultModelObject();
          for(Data data : openAnswerDefinition.getDefaultValues()) {
            switch(valueOf) {
            case DATE:
              try {
                DATE_FORMAT.parse(data.getValueAsString());
              } catch(ParseException nfe) {
                error(new StringResourceModel("InvalidCastType", OpenAnswerPanel.this, null).getObject());
                showFeedbackErrorAndReset(target);
                return;
              }
              break;
            case DECIMAL:
              try {
                Double.parseDouble(data.getValueAsString());
              } catch(NumberFormatException nfe) {
                error(new StringResourceModel("InvalidCastType", OpenAnswerPanel.this, null).getObject());
                showFeedbackErrorAndReset(target);
                return;
              }
              break;
            case INTEGER:
              if(data.getType() == DataType.DECIMAL) {
                Double d = data.getValue();
                if(d != d.longValue()) {
                  error(new StringResourceModel("InvalidCastType", OpenAnswerPanel.this, null).getObject());
                  showFeedbackErrorAndReset(target);
                  return;
                }
              } else {
                try {
                  Long.parseLong(data.getValueAsString());
                } catch(NumberFormatException nfe) {
                  error(new StringResourceModel("InvalidCastType", OpenAnswerPanel.this, null).getObject());
                  showFeedbackErrorAndReset(target);
                  return;
                }
              }
              break;
            case TEXT:
              break;
            }
          }
          for(Data data : openAnswerDefinition.getDefaultValues()) {
            switch(valueOf) {
            case DATE:
              try {
                data.setTypeAndValue(valueOf, DATE_FORMAT.parse(data.getValueAsString()));
              } catch(ParseException e) {
                throw new RuntimeException(e);
              }
              break;
            case DECIMAL:
              data.setTypeAndValue(valueOf, Double.parseDouble(data.getValueAsString()));
              break;
            case INTEGER:
              if(data.getType() == DataType.DECIMAL) {
                data.setTypeAndValue(valueOf, ((Double) data.getValue()).longValue());
              } else {
                data.setTypeAndValue(valueOf, Integer.parseInt(data.getValueAsString()));
              }
              break;
            case TEXT:
              data.setTypeAndValue(valueOf, data.getType() == DataType.DATE ? DATE_FORMAT.format(data.getValue()) : data.getValueAsString());
              break;
            }
          }
        }
        setMinMaxLabels(value == null ? null : valueOf);
        target.addComponent(minMaxContainer);
        target.addComponent(defaultValuesList);
      }

      private void showFeedbackErrorAndReset(AjaxRequestTarget target) {
        dataTypeDropDown.setModelObject(openAnswer.getDefaultValues().get(0).getType());
        target.addComponent(dataTypeDropDown);
        OpenAnswerPanel.this.feedbackWindow.setContent(OpenAnswerPanel.this.feedbackPanel);
        OpenAnswerPanel.this.feedbackWindow.show(target);
      }
    });

    final IModel<String> addDefaultValuesModel = new Model<String>();

    List<ITab> tabs = new ArrayList<ITab>();
    tabs.add(new AbstractTab(new ResourceModel("Add.simple")) {
      @Override
      public Panel getPanel(String panelId) {
        return new SimpleAddPanel(panelId, addDefaultValuesModel);
      }
    });
    tabs.add(new AbstractTab(new ResourceModel("Add.bulk")) {
      @Override
      public Panel getPanel(String panelId) {
        return new BulkAddPanel(panelId, addDefaultValuesModel);
      }
    });
    add(new AjaxTabbedPanel("addTabs", tabs));

    defaultValuesList = new SortableList<Data>("defaultValues", openAnswer.getDefaultValues(), true) {

      @Override
      public Component getItemTitle(@SuppressWarnings("hiding") String id, Data data) {
        return new Label(id, data.getType() == DataType.DATE ? DATE_FORMAT.format(data.getValue()) : data.getValueAsString());
      }

      @Override
      public void editItem(Data t, AjaxRequestTarget target) {

      }

      @SuppressWarnings("unchecked")
      @Override
      public void deleteItem(final Data data, AjaxRequestTarget target) {
        ((OpenAnswerDefinition) OpenAnswerPanel.this.getDefaultModelObject()).removeDefaultData(data);
        for(Locale locale : OpenAnswerPanel.this.localePropertiesModel.getObject().getLocales()) {
          List<KeyValue> list = OpenAnswerPanel.this.localePropertiesModel.getObject().getElementLabels(openAnswer).get(locale);
          Collection<KeyValue> toDelete = Collections2.filter(list, new Predicate<KeyValue>() {

            @Override
            public boolean apply(KeyValue input) {
              return input.getKey().equals(data.getValue().toString());
            }

          });
          list.remove(toDelete.iterator().next());
        }
        OpenAnswerPanel.this.addOrReplace(labelsPanel = new LabelsPanel("labels", OpenAnswerPanel.this.localePropertiesModel, (IModel<OpenAnswerDefinition>) OpenAnswerPanel.this.getDefaultModel(), OpenAnswerPanel.this.feedbackPanel, OpenAnswerPanel.this.feedbackWindow));
        target.addComponent(labelsPanel);
        refreshList(target);
      }

      @Override
      public Button[] getButtons() {
        return null;
      }

    };
    add(defaultValuesList);
    add(new HelpTooltipPanel("defaultValuesHelp", new ResourceModel("DefaultValues.Tooltip")));
  }

  private class SimpleAddPanel extends Panel {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SimpleAddPanel(String id, IModel<String> model) {
      super(id, model);
      Form<String> form = new Form<String>("form", model);
      add(form);
      defaultValue = new TextField("defaultValue", model);
      defaultValue.setOutputMarkupId(true);
      defaultValue.setLabel(new ResourceModel("NewDefaultValue"));
      setFieldType();
      form.add(defaultValue);
      form.add(new SimpleFormComponentLabel("defaultValueLabel", defaultValue));
      AjaxSubmitLink simpleAddLink = new AjaxSubmitLink("link") {
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          if(defaultValue.getModelObject() == null) return;
          OpenAnswerDefinition openAnswerDefinition = (OpenAnswerDefinition) OpenAnswerPanel.this.getDefaultModelObject();
          String dataTypeValue = dataTypeDropDown.getValue();
          if(StringUtils.isBlank(dataTypeValue)) {
            error(new StringResourceModel("ChooseDataType", OpenAnswerPanel.this, null).getObject());
            return;
          }
          DataType dataTypeEnum = DataType.valueOf(dataTypeValue);
          openAnswerDefinition.setDataType(dataTypeEnum);
          openAnswerDefinition.addDefaultValue(String.valueOf(defaultValue.getModelObject()));
          defaultValue.setModelObject(null);
          localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), (OpenAnswerDefinition) OpenAnswerPanel.this.getDefaultModelObject());
          OpenAnswerPanel.this.addOrReplace(labelsPanel = new LabelsPanel("labels", localePropertiesModel, (IModel<OpenAnswerDefinition>) OpenAnswerPanel.this.getDefaultModel(), feedbackPanel, feedbackWindow));
          target.addComponent(labelsPanel);
          target.addComponent(defaultValue);
          target.addComponent(defaultValuesList);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form1) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      simpleAddLink.add(new Image("img", Images.ADD));
      form.add(simpleAddLink);
    }
  }

  @SuppressWarnings("unchecked")
  private void setFieldType() {
    String value = dataTypeDropDown.getValue();
    if(!isValidStringEnum(value)) return;
    defaultValue.setType(getType(DataType.valueOf(value)));
  }

  private boolean isValidStringEnum(String value) {
    for(DataType data : DataType.values()) {
      if(data.name().equals(value)) return true;
    }
    return false;
  }

  private class BulkAddPanel extends Panel {

    public BulkAddPanel(String id, IModel<String> model) {
      super(id, model);
      Form<String> form = new Form<String>("form", model);
      add(form);
      final TextArea<String> defaultValues = new TextArea<String>("defaultValues", model);
      defaultValues.setOutputMarkupId(true);
      defaultValues.setLabel(new ResourceModel("NewDefaultValues"));
      form.add(defaultValues);
      form.add(new SimpleFormComponentLabel("defaultValuesLabel", defaultValues));
      AjaxSubmitLink bulkAddLink = new AjaxSubmitLink("bulkAddLink") {

        @SuppressWarnings({ "unchecked", "incomplete-switch" })
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          String[] names = StringUtils.split(defaultValues.getModelObject(), ',');
          if(names == null) return;
          String dataTypeValue = dataTypeDropDown.getValue();
          if(StringUtils.isBlank(dataTypeValue)) {
            error(new StringResourceModel("ChooseDataType", OpenAnswerPanel.this, null).getObject());
            return;
          }
          OpenAnswerDefinition openAnswerDefinition = (OpenAnswerDefinition) OpenAnswerPanel.this.getDefaultModelObject();
          openAnswerDefinition.setDataType(DataType.valueOf(dataTypeValue));
          for(String name : new HashSet<String>(Arrays.asList(names))) {
            switch(DataType.valueOf(dataTypeValue)) {
            case DATE:
              try {
                DATE_FORMAT.parse(name);
              } catch(ParseException nfe) {
                error(new StringResourceModel("InvalidCastType", OpenAnswerPanel.this, null).getObject());
                return;
              }
              break;
            case DECIMAL:
              try {
                Double.parseDouble(name);
              } catch(NumberFormatException nfe) {
                error(new StringResourceModel("InvalidCastType", OpenAnswerPanel.this, null).getObject());
                return;
              }
              break;
            case INTEGER:
              try {
                Long.parseLong(name);
              } catch(NumberFormatException nfe) {
                error(new StringResourceModel("InvalidCastType", OpenAnswerPanel.this, null).getObject());
                return;
              }
              break;
            case TEXT:
              break;
            }

          }
          for(String name : new HashSet<String>(Arrays.asList(names))) {
            openAnswerDefinition.addDefaultValue(name);
          }
          defaultValues.setModelObject(null);
          localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), (OpenAnswerDefinition) OpenAnswerPanel.this.getDefaultModelObject());
          labelsPanel = new LabelsPanel("labels", localePropertiesModel, (IModel<OpenAnswerDefinition>) OpenAnswerPanel.this.getDefaultModel(), feedbackPanel, feedbackWindow);
          OpenAnswerPanel.this.addOrReplace(labelsPanel);
          target.addComponent(labelsPanel);
          target.addComponent(defaultValues);
          target.addComponent(defaultValuesList);
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

  private Class<?> getType(DataType dataType) {
    switch(dataType) {
    case DATE:
      return Date.class;
    case DECIMAL:
      return Double.class;
    case INTEGER:
      return Integer.class;
    case TEXT:
      return String.class;
    default:
      throw new RuntimeException("Unknown type");
    }
  }

  /**
   * 
   * @param target
   */
  public void onSave(AjaxRequestTarget target) {
    if(!variableNameBehavior.isVariableNameDefined()) {
      variable.setModelObject(null);
    }
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

  private class ValidationDataSourcesProvider extends SortableDataProvider<ComparingDataSource> {

    private final List<ComparingDataSource> dataSources;

    public ValidationDataSourcesProvider() {
      dataSources = ((OpenAnswerDefinition) getDefaultModelObject()).getValidationDataSources();
    }

    @Override
    public Iterator<ComparingDataSource> iterator(int first, int count) {
      return dataSources.iterator();
    }

    @Override
    public int size() {
      return dataSources.size();
    }

    @Override
    public IModel<ComparingDataSource> model(ComparingDataSource ds) {
      return new Model<ComparingDataSource>(ds);
    }

  }

  private class ValidationDataSourcesColumnProvider implements IColumnProvider<ComparingDataSource>, Serializable {

    private final List<IColumn<ComparingDataSource>> columns = new ArrayList<IColumn<ComparingDataSource>>();

    public ValidationDataSourcesColumnProvider() {
      columns.add(new AbstractColumn<ComparingDataSource>(new ResourceModel("Operator")) {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDataSource>> cellItem, String componentId, IModel<ComparingDataSource> rowModel) {
          cellItem.add(new Label(componentId, new ResourceModel("Operator." + rowModel.getObject().getComparisonOperator())));
        }
      });
      columns.add(new AbstractColumn<ComparingDataSource>(new ResourceModel("Variable")) {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDataSource>> cellItem, String componentId, IModel<ComparingDataSource> rowModel) {
          cellItem.add(new Label(componentId, ((VariableDataSource) rowModel.getObject().getDataSourceRight()).getPath()));
        }
      });

      columns.add(new HeaderlessColumn<ComparingDataSource>() {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDataSource>> cellItem, String componentId, IModel<ComparingDataSource> rowModel) {
          cellItem.add(new ValidatorsActionFragment(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<ComparingDataSource>> getAdditionalColumns() {
      return null;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<ComparingDataSource>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<ComparingDataSource>> getRequiredColumns() {
      return columns;
    }

  }

  private class ValidatorsActionFragment extends Fragment {

    public ValidatorsActionFragment(String id, final IModel<ComparingDataSource> rowModel) {
      super(id, "validatorsAction", OpenAnswerPanel.this, rowModel);
      final ComparingDataSource comparingDataSource = rowModel.getObject();

      add(new AjaxLink<ComparingDataSource>("editLink", rowModel) {
        @Override
        @SuppressWarnings("unchecked")
        public void onClick(AjaxRequestTarget target) {
          validatorWindow.setContent(new ValidationDataSourceWindow("content", rowModel, (IModel<OpenAnswerDefinition>) OpenAnswerPanel.this.getDefaultModel(), questionModel, questionnaireModel, validatorWindow) {
            @Override
            public void onSave(AjaxRequestTarget target1, @SuppressWarnings("hiding") ComparingDataSource comparingDataSource) {
              target1.addComponent(validators);
            }
          });
          validatorWindow.show(target);
        }
      }.add(new Image("editImg", Images.EDIT).add(new AttributeModifier("title", true, new ResourceModel("Edit")))));

      add(new AjaxLink<ComparingDataSource>("deleteLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          OpenAnswerDefinition openAnswer = (OpenAnswerDefinition) OpenAnswerPanel.this.getDefaultModelObject();
          openAnswer.getValidationDataSources().remove(comparingDataSource);
          target.addComponent(validators);
        }
      }.add(new Image("deleteImg", Images.DELETE).add(new AttributeModifier("title", true, new ResourceModel("Delete")))));

    }
  }

}
