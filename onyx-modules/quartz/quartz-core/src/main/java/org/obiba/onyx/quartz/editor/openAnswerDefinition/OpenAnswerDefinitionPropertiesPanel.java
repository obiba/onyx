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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

/**
 * @deprecated use OpenAnswerPanel or OpenAnswerWindow
 */
@Deprecated
@SuppressWarnings("serial")
public class OpenAnswerDefinitionPropertiesPanel extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());
  //
  // @SpringBean
  // private LocalePropertiesUtils localePropertiesUtils;
  //
  // @SpringBean
  // private QuestionnairePersistenceUtils questionnairePersistenceUtils;
  //
  // private final FeedbackPanel feedbackPanel;
  //
  // private final FeedbackWindow feedbackWindow;
  //
  // private final Form<OpenAnswerDefinition> form;
  //
  // private ListModel<LocaleProperties2> localePropertiesModel;
  //
  // private IModel<Questionnaire> questionnaireModel;
  //
  // private AjaxCheckBox maximumValidatorCheckbox;
  //
  // private AjaxCheckBox minimumValidatorCheckbox;
  //
  // private AjaxCheckBox rangeValidatorCheckbox;
  //
  // private TextField<String> maximumValidatorValueTextField;
  //
  // private TextField<String> minimumValidatorValueTextField;
  //
  // private TextField<String> rangeMinValidatorValueTextField;
  //
  // private TextField<String> rangeMaxValidatorValueTextField;
  //
  // private DropDownChoice<DataType> dataTypeDropDownChoice;

  @SuppressWarnings("unchecked")
  public OpenAnswerDefinitionPropertiesPanel(String id, IModel<OpenAnswerDefinition> model, IModel<Questionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id);
    // this.questionnaireModel = questionnaireModel;

    // IModel<EditedOpenAnswerDefinition> editedModel = new Model<EditedOpenAnswerDefinition>(new
    // EditedOpenAnswerDefinition(model.getObject()));
    // setDefaultModel(editedModel);
    //
    // localePropertiesModel = new ListModel<LocaleProperties2>(localePropertiesUtils.loadLocaleProperties(model,
    // questionnaireModel));
    //
    // feedbackPanel = new FeedbackPanel("content");
    // feedbackWindow = new FeedbackWindow("feedback");
    // feedbackWindow.setOutputMarkupId(true);
    //
    // add(feedbackWindow);
    //
    // add(form = new Form<EditedOpenAnswerDefinition>("form", editedModel));
    //
    // TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(),
    // "element.name"));
    // name.setLabel(new ResourceModel("Name"));
    // name.add(new RequiredFormFieldBehavior());
    // form.add(name);
    // form.add(new SimpleFormComponentLabel("nameLabel", name));
    //
    // form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", new
    // PropertyModel<OpenAnswerDefinition>(form.getModel(), "element"), localePropertiesModel));
    //
    // DataType dataType = form.getModelObject().getElement().getDataType();
    // final boolean isTextType = (dataType == null ? false : dataType.equals(DataType.TEXT));
    //
    // final WebMarkupContainer validatorContainer = new WebMarkupContainer("basicValidatorContainer");
    // validatorContainer.setOutputMarkupPlaceholderTag(true);
    // form.add(validatorContainer);
    //
    // dataTypeDropDownChoice = new DropDownChoice<DataType>("dataTypeDropDownChoice", new
    // PropertyModel<DataType>(form.getModel(), "element.dataType"), Arrays.asList(DataType.values()), new
    // ChoiceRenderer<DataType>());
    // dataTypeDropDownChoice.setLabel(new ResourceModel("DataType"));
    // dataTypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
    //
    // @Override
    // protected void onUpdate(AjaxRequestTarget target) {
    // DataType dropDownChoiceModelObject2 = dataTypeDropDownChoice.getModelObject();
    // validatorContainer.setVisible(dropDownChoiceModelObject2 == null ? false :
    // (!dropDownChoiceModelObject2.equals(DataType.BOOLEAN) && !dropDownChoiceModelObject2.equals(DataType.DATA)));
    // target.addComponent(validatorContainer);
    // }
    // });
    // form.add(new SimpleFormComponentLabel("dataTypeLabel", dataTypeDropDownChoice));
    // form.add(dataTypeDropDownChoice);
    //
    // DataType dropDownChoiceModelObject = dataTypeDropDownChoice.getModelObject();
    // validatorContainer.setVisible(dropDownChoiceModelObject == null ? false :
    // (!dropDownChoiceModelObject.equals(DataType.BOOLEAN) && !dropDownChoiceModelObject.equals(DataType.DATA)));
    //
    // CheckBox requiredCheckBox = new CheckBox("required", new PropertyModel<Boolean>(form.getModel(),
    // "element.required"));
    // requiredCheckBox.setLabel(new ResourceModel("Required2"));
    // form.add(requiredCheckBox);
    // form.add(new SimpleFormComponentLabel("requiredLabel", requiredCheckBox));
    //
    // TextField<String> unitTextField = new TextField<String>("unit", new PropertyModel<String>(form.getModel(),
    // "element.unit"));
    // unitTextField.setLabel(new ResourceModel("Unit"));
    // form.add(unitTextField);
    // form.add(new SimpleFormComponentLabel("unitLabel", unitTextField));
    //
    // ValueMap uiArgumentsValueMap = form.getModelObject().getElement().getUIArgumentsValueMap();
    // Integer size = null;
    // if(uiArgumentsValueMap != null && uiArgumentsValueMap.get(DefaultOpenAnswerDefinitionPanel.INPUT_NB_ROWS) !=
    // null) {
    // size = (Integer) uiArgumentsValueMap.get(DefaultOpenAnswerDefinitionPanel.INPUT_NB_ROWS);
    // }
    // final TextField<Integer> sizeTextFieldForUIArguments = new TextField<Integer>("size", new Model<Integer>(size),
    // Integer.class);
    // sizeTextFieldForUIArguments.setLabel(new ResourceModel("Size"));
    // sizeTextFieldForUIArguments.setOutputMarkupPlaceholderTag(true);
    // sizeTextFieldForUIArguments.setVisible(false);
    // AjaxCheckBox specifySize = new AjaxCheckBox("wantSpecifySize", new Model<Boolean>(uiArgumentsValueMap != null ?
    // uiArgumentsValueMap.get(DefaultOpenAnswerDefinitionPanel.INPUT_NB_ROWS) != null : false)) {
    //
    // @Override
    // protected void onUpdate(AjaxRequestTarget target) {
    // sizeTextFieldForUIArguments.setVisible(this.getModelObject());
    // target.addComponent(sizeTextFieldForUIArguments);
    // }
    // };
    // form.add(new SimpleFormComponentLabel("sizeLabel", sizeTextFieldForUIArguments), specifySize,
    // sizeTextFieldForUIArguments);
    //
    // // MAXIMUM VALIDATOR
    // Collection<IDataValidator<?>> maximumDataValidators =
    // Collections2.filter(form.getModelObject().getElement().getDataValidators(), new Predicate<IDataValidator<?>>() {
    //
    // @Override
    // public boolean apply(IDataValidator<?> input) {
    // if(isTextType) {
    // return (input.getValidator() instanceof StringValidator.MaximumLengthValidator);
    // }
    // return (input.getValidator() instanceof MaximumValidator);
    // }
    // });
    // String maximumValidatorValue = null;
    // if(!maximumDataValidators.isEmpty()) {
    // if(isTextType) {
    // maximumValidatorValue = String.valueOf(((StringValidator.MaximumLengthValidator)
    // (maximumDataValidators.iterator().next()).getValidator()).getMaximum());
    // } else {
    // maximumValidatorValue = String.valueOf(((MaximumValidator<String>)
    // (maximumDataValidators.iterator().next()).getValidator()).getMaximum());
    // }
    // }
    // maximumValidatorValueTextField = new TextField<String>("maximumValidatorValueTextField", new
    // Model<String>(maximumValidatorValue), String.class);
    // maximumValidatorValueTextField.setOutputMarkupPlaceholderTag(true);
    // maximumValidatorValueTextField.setVisible(!maximumDataValidators.isEmpty());
    // maximumValidatorValueTextField.setRequired(!maximumDataValidators.isEmpty());
    // maximumValidatorCheckbox = new AjaxCheckBox("maximumValidatorCheckbox", new
    // Model<Boolean>(!maximumDataValidators.isEmpty())) {
    //
    // @Override
    // protected void onUpdate(AjaxRequestTarget target) {
    // maximumValidatorValueTextField.setVisible(this.getModelObject());
    // maximumValidatorValueTextField.setRequired(this.getModelObject());
    // target.addComponent(maximumValidatorValueTextField);
    // }
    // };
    // maximumValidatorCheckbox.setLabel(new ResourceModel("MaximumValidator"));
    // validatorContainer.add(new SimpleFormComponentLabel("maximumValidatorLabel", maximumValidatorCheckbox),
    // maximumValidatorCheckbox, maximumValidatorValueTextField);
    //
    // // MINIMUM VALIDATOR
    // Collection<IDataValidator<?>> minimumDataValidators =
    // Collections2.filter(form.getModelObject().getElement().getDataValidators(), new Predicate<IDataValidator<?>>() {
    //
    // @Override
    // public boolean apply(IDataValidator<?> input) {
    // if(isTextType) {
    // return input.getValidator() instanceof StringValidator.MinimumLengthValidator;
    // }
    // return input.getValidator() instanceof MinimumValidator;
    // }
    // });
    // String minimumValidatorValue = null;
    // if(!minimumDataValidators.isEmpty()) {
    // if(isTextType) {
    // minimumValidatorValue = String.valueOf(((StringValidator.MinimumLengthValidator)
    // (minimumDataValidators.iterator().next()).getValidator()).getMinimum());
    // } else {
    // minimumValidatorValue = String.valueOf(((MinimumValidator<String>)
    // (minimumDataValidators.iterator().next()).getValidator()).getMinimum());
    // }
    // }
    // minimumValidatorValueTextField = new TextField<String>("minimumValidatorValueTextField", new
    // Model<String>(minimumValidatorValue), String.class);
    // minimumValidatorValueTextField.setOutputMarkupPlaceholderTag(true);
    // minimumValidatorValueTextField.setVisible(!minimumDataValidators.isEmpty());
    // minimumValidatorValueTextField.setRequired(!minimumDataValidators.isEmpty());
    // minimumValidatorCheckbox = new AjaxCheckBox("minimumValidatorCheckbox", new
    // Model<Boolean>(!minimumDataValidators.isEmpty())) {
    //
    // @Override
    // protected void onUpdate(AjaxRequestTarget target) {
    // minimumValidatorValueTextField.setVisible(this.getModelObject());
    // minimumValidatorValueTextField.setRedirect(this.getModelObject());
    // target.addComponent(minimumValidatorValueTextField);
    // }
    // };
    // minimumValidatorCheckbox.setLabel(new ResourceModel("MinimumValidator"));
    // validatorContainer.add(new SimpleFormComponentLabel("minimumValidatorLabel", minimumValidatorCheckbox),
    // minimumValidatorCheckbox, minimumValidatorValueTextField);
    //
    // // RANGE VALIDATOR
    // Collection<IDataValidator<?>> rangeDataValidators =
    // Collections2.filter(form.getModelObject().getElement().getDataValidators(), new Predicate<IDataValidator<?>>() {
    //
    // @Override
    // public boolean apply(IDataValidator<?> input) {
    // if(form.getModelObject().getElement().getDataType().equals(DataType.TEXT)) {
    // return input.getValidator() instanceof StringValidator.LengthBetweenValidator;
    // }
    // return input.getValidator() instanceof RangeValidator;
    // }
    // });
    // String rangeMinValidatorValue = null;
    // String rangeMaxValidatorValue = null;
    // if(!rangeDataValidators.isEmpty()) {
    // if(isTextType) {
    // StringValidator.LengthBetweenValidator rangeValidator = (StringValidator.LengthBetweenValidator)
    // (rangeDataValidators.iterator().next()).getValidator();
    // rangeMinValidatorValue = String.valueOf(rangeValidator.getMinimum());
    // rangeMaxValidatorValue = String.valueOf(rangeValidator.getMaximum());
    // } else {
    // RangeValidator<String> rangeValidator = (RangeValidator<String>)
    // (rangeDataValidators.iterator().next()).getValidator();
    // rangeMinValidatorValue = String.valueOf(rangeValidator.getMinimum());
    // rangeMaxValidatorValue = String.valueOf(rangeValidator.getMaximum());
    // }
    // }
    // rangeMinValidatorValueTextField = new TextField<String>("rangeMinValidatorValueTextField", new
    // Model<String>(rangeMinValidatorValue), String.class);
    // rangeMaxValidatorValueTextField = new TextField<String>("rangeMaxValidatorValueTextField", new
    // Model<String>(rangeMaxValidatorValue), String.class);
    //
    // rangeMinValidatorValueTextField.setOutputMarkupPlaceholderTag(true);
    // rangeMaxValidatorValueTextField.setOutputMarkupPlaceholderTag(true);
    //
    // rangeMinValidatorValueTextField.setVisible(!rangeDataValidators.isEmpty());
    // rangeMaxValidatorValueTextField.setVisible(!rangeDataValidators.isEmpty());
    //
    // rangeMinValidatorValueTextField.setRequired(!rangeDataValidators.isEmpty());
    // rangeMaxValidatorValueTextField.setRequired(!rangeDataValidators.isEmpty());
    //
    // rangeValidatorCheckbox = new AjaxCheckBox("rangeValidatorCheckbox", new
    // Model<Boolean>(!rangeDataValidators.isEmpty())) {
    //
    // @Override
    // protected void onUpdate(AjaxRequestTarget target) {
    // rangeMinValidatorValueTextField.setVisible(this.getModelObject());
    // rangeMaxValidatorValueTextField.setVisible(this.getModelObject());
    // rangeMinValidatorValueTextField.setRequired(this.getModelObject());
    // rangeMaxValidatorValueTextField.setRequired(this.getModelObject());
    // target.addComponent(rangeMinValidatorValueTextField);
    // target.addComponent(rangeMaxValidatorValueTextField);
    // }
    // };
    // rangeValidatorCheckbox.setLabel(new ResourceModel("RangeValidator"));
    // validatorContainer.add(new SimpleFormComponentLabel("rangeValidatorLabel", rangeValidatorCheckbox),
    // rangeValidatorCheckbox, rangeMinValidatorValueTextField, rangeMaxValidatorValueTextField);
    //
    // form.add(new AjaxButton("save", form) {
    // @Override
    // public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
    // onSave(target, form.getModelObject());
    // modalWindow.close(target);
    // }
    //
    // @Override
    // protected void onError(AjaxRequestTarget target, Form<?> form2) {
    // feedbackWindow.setContent(feedbackPanel);
    // feedbackWindow.show(target);
    // }
    // });
    //
    // form.add(new AjaxButton("cancel", form) {
    // @Override
    // public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
    // modalWindow.close(target);
    // }
    // }.setDefaultFormProcessing(false));
    // }
    //
    // /**
    // *
    // * @param target
    // * @param editedOpenAnswerDefinition
    // */
    // public void onSave(AjaxRequestTarget target, EditedOpenAnswerDefinition editedOpenAnswerDefinition) {
    // editedOpenAnswerDefinition.setLocalePropertiesWithNamingStrategy(localePropertiesModel.getObject());
    // OpenAnswerDefinition openAnswerDefinition = editedOpenAnswerDefinition.getElement();
    //
    // openAnswerDefinition.clearDataValidators();
    // String max = maximumValidatorValueTextField.getModelObject();
    // String min = minimumValidatorValueTextField.getModelObject();
    // String rangeMin = rangeMinValidatorValueTextField.getModelObject();
    // String rangeMax = rangeMaxValidatorValueTextField.getModelObject();
    // switch(dataTypeDropDownChoice.getModelObject()) {
    // case INTEGER: {
    // if(maximumValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(new MaximumValidator<Long>(Long.parseLong(max)),
    // DataType.INTEGER);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // if(minimumValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(new MinimumValidator<Long>(Long.parseLong(min)),
    // DataType.INTEGER);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // if(rangeValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(new RangeValidator<Long>(Long.parseLong(rangeMin),
    // Long.parseLong(rangeMax)), DataType.INTEGER);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // break;
    // }
    // case DECIMAL: {
    // if(maximumValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(new MaximumValidator<Double>(Double.parseDouble(max)),
    // DataType.DECIMAL);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // if(minimumValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(new MinimumValidator<Double>(Double.parseDouble(min)),
    // DataType.DECIMAL);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // if(rangeValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(new RangeValidator<Double>(Double.parseDouble(rangeMin),
    // Double.parseDouble(rangeMax)), DataType.DECIMAL);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // break;
    // }
    // case DATE: {
    // if(maximumValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(DateValidator.maximum(parseDate(max)), DataType.DECIMAL);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // if(minimumValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(DateValidator.minimum(parseDate(min)), DataType.DECIMAL);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // if(rangeValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(DateValidator.range(parseDate(rangeMin), parseDate(rangeMax)),
    // DataType.DECIMAL);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // break;
    // }
    // case TEXT: {
    // if(maximumValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(new
    // StringValidator.MaximumLengthValidator(Integer.parseInt(max)), DataType.TEXT);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // if(minimumValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(new
    // StringValidator.MinimumLengthValidator(Integer.parseInt(min)), DataType.TEXT);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // if(rangeValidatorCheckbox.getModelObject()) {
    // DataValidator dataValidator = new DataValidator(new
    // StringValidator.LengthBetweenValidator(Integer.parseInt(rangeMin), Integer.parseInt(rangeMax)), DataType.TEXT);
    // openAnswerDefinition.addDataValidator(dataValidator);
    // }
    // break;
    // }
    // }
    // }
    //
    // protected Date parseDate(String dateString) {
    // SimpleDateFormat[] formats = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM-dd"), new
    // SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z") };
    // for(SimpleDateFormat sdf : formats) {
    // try {
    // return sdf.parse(dateString);
    // } catch(ParseException e) {
    // // Ignore, try the next format if any
    // }
    // }
    // throw new ConversionException("Cannot parse date '" + dateString + "'");
    // }
    //
    // public void persist(AjaxRequestTarget target) {
    // try {
    // QuestionnaireBuilder builder = questionnairePersistenceUtils.createBuilder(questionnaireModel.getObject());
    // questionnairePersistenceUtils.persist(form.getModelObject(), builder);
    // } catch(Exception e) {
    // log.error("Cannot persist questionnaire", e);
    // error(e.getMessage());
    // feedbackWindow.setContent(feedbackPanel);
    // feedbackWindow.show(target);
    // }
  }
}
