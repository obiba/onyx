/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.questionnaire.utils.VariableValidationUtils;
import org.obiba.onyx.quartz.editor.utils.JavascriptUtils;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class VariablePanel extends Panel {

  // private transient Logger logger = LoggerFactory.getLogger(getClass());

  @SpringBean
  private MagmaInstanceProvider magmaInstanceProvider;

  @SpringBean
  private VariableValidationUtils variableValidationUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<EditedVariable> form;

  public VariablePanel(String id, final IModel<Variable> variableModel, final IModel<Questionnaire> questionnaireModel) {
    this(id, variableModel, questionnaireModel, null);
  }

  public VariablePanel(String id, final IModel<Variable> variableModel, final IModel<Questionnaire> questionnaireModel, ValueType forcedValueType) {
    super(id);

    add(CSSPackageResource.getHeaderContribution(VariablePanel.class, "VariablePanel.css"));

    final EditedVariable editedVariable = new EditedVariable();
    final Variable variable = variableModel.getObject();
    if(variable == null) {
      editedVariable.setValueType(forcedValueType);
      editedVariable.setRepeatable(false);
    } else {
      editedVariable.setName(variable.getName());
      editedVariable.setValueType(variable.getValueType());
      editedVariable.setRepeatable(variable.isRepeatable());
      editedVariable.setScript(variable.getAttributeStringValue("script"));
    }
    final IModel<EditedVariable> model = new Model<EditedVariable>(editedVariable);
    setDefaultModel(model);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<EditedVariable>("form", model));

    final TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new PatternValidator(Pattern.compile("[a-zA-Z0-9_\\-\\.]+")));
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(!StringUtils.equals(model.getObject().getName(), validatable.getValue())) {
          try {
            if(questionnaireModel.getObject().getVariable(validatable.getValue()) != null) {
              error(validatable, "VariableAlreadyExists");
            }
          } catch(Exception e) {
            // do nothing, variable was not found
          }
        }
      }
    });
    form.add(name).add(new SimpleFormComponentLabel("nameLabel", name)).add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    List<ValueType> types = new ArrayList<ValueType>();
    types.add(IntegerType.get());
    types.add(DecimalType.get());
    types.add(BooleanType.get());
    types.add(DateType.get());
    types.add(DateTimeType.get());
    types.add(TextType.get());
    types.add(LocaleType.get());
    types.add(BinaryType.get());

    DropDownChoice<ValueType> valueType = new DropDownChoice<ValueType>("type", new PropertyModel<ValueType>(form.getModel(), "valueType"), types, new ValueTypeRenderer());
    valueType.add(new RequiredFormFieldBehavior());

    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
    questionnaireFinder.buildQuestionnaireCache();
    boolean usedInQuestion = variableValidationUtils.findUsedInQuestion(variable, questionnaireModel.getObject().getQuestionnaireCache()) == null;
    valueType.setEnabled((forcedValueType == null) ? usedInQuestion : false);

    form.add(valueType.setLabel(new ResourceModel("Type"))).add(new SimpleFormComponentLabel("typeLabel", valueType));

    CheckBox repeatable = new CheckBox("repeatable", new PropertyModel<Boolean>(form.getModel(), "repeatable"));

    form.add(repeatable.setLabel(new ResourceModel("Repeatable"))).add(new SimpleFormComponentLabel("repeatableLabel", repeatable));

    TextArea<String> script = new TextArea<String>("script", new PropertyModel<String>(form.getModel(), "script"));
    script.add(new RequiredFormFieldBehavior());
    script.add(new IValidator<String>() {

      @Override
      public void validate(final IValidatable<String> validatable) {
        JavascriptUtils.compile(validatable.getValue(), name.getConvertedInput(), VariablePanel.this, form);
      }
    });

    form.add(script.setLabel(new ResourceModel("Script"))).add(new SimpleFormComponentLabel("scriptLabel", script)).add(new HelpTooltipPanel("scriptHelp", new ResourceModel("Script.Tooltip")));

    Set<ValueTable> valueTables = magmaInstanceProvider.getOnyxDatasource().getValueTables();
    List<String> tables = new ArrayList<String>(valueTables.size());
    for(ValueTable valueTable : valueTables) {
      if(valueTable.isForEntityType(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE)) {
        tables.add(valueTable.getName());
      }
    }
    Collections.sort(tables);

    final DropDownChoice<String> tablesDropDown = new DropDownChoice<String>("tables", new Model<String>(tables.get(0)), tables);
    tablesDropDown.setNullValid(false);
    form.add(tablesDropDown.setLabel(new ResourceModel("Tables"))).add(new SimpleFormComponentLabel("tablesLabel", tablesDropDown));

    final List<String> tableVariables = new ArrayList<String>();
    findTableVariables(tablesDropDown, tableVariables);
    final DropDownChoice<String> tableVariablesDropDown = new DropDownChoice<String>("variables", new Model<String>(tableVariables.get(0)), tableVariables);
    tableVariablesDropDown.setNullValid(false).setOutputMarkupId(true);
    form.add(tableVariablesDropDown.setLabel(new ResourceModel("Variables"))).add(new SimpleFormComponentLabel("variablesLabel", tableVariablesDropDown));

    final TextField<String> selectedVariable = new TextField<String>("selectedVariable", new Model<String>(tableVariables.isEmpty() ? "" : tablesDropDown.getModelObject() + ":" + tableVariablesDropDown.getModelObject()));
    form.add(selectedVariable.setOutputMarkupId(true));

    tablesDropDown.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        findTableVariables(tablesDropDown, tableVariables);
        tableVariablesDropDown.setModelObject(tableVariables.isEmpty() ? null : tableVariables.get(0));
        selectedVariable.setDefaultModelObject(tableVariables.isEmpty() ? "" : tablesDropDown.getModelObject() + ":" + tableVariablesDropDown.getModelObject());
        target.addComponent(tableVariablesDropDown);
        target.addComponent(selectedVariable);
      }
    });
    tableVariablesDropDown.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        selectedVariable.setDefaultModelObject(tablesDropDown.getModelObject() + ":" + tableVariablesDropDown.getModelObject());
        target.addComponent(selectedVariable);
      }
    });

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form1) {
        Builder builder = Variable.Builder.newVariable(editedVariable.getName(), editedVariable.getValueType(), "Participant");
        builder.addAttribute("script", editedVariable.getScript());

        if(editedVariable.isRepeatable()) builder.repeatable();

        VariablePanel.this.onSave(target, builder.build());
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        VariablePanel.this.onCancel(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

  }

  protected abstract void onSave(AjaxRequestTarget target, Variable variable);

  protected abstract void onCancel(AjaxRequestTarget target);

  private void findTableVariables(final DropDownChoice<String> tablesDropDown, final List<String> tableVariables) {
    tableVariables.clear();
    String selectedTable = tablesDropDown.getModelObject();
    if(selectedTable != null) {
      ValueTable table = magmaInstanceProvider.getValueTable(selectedTable);
      for(Variable v : table.getVariables()) {
        tableVariables.add(v.getName());
      }
      Collections.sort(tableVariables);
    }
  }
}
