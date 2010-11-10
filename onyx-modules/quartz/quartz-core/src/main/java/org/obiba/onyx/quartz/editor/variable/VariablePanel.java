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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
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
import org.apache.wicket.validation.validator.AbstractValidator;
import org.obiba.magma.MagmaEngine;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnairePersistenceUtils;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class VariablePanel extends Panel {

  private transient Logger logger = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<EditedVariable> form;

  private final IModel<Questionnaire> questionnaireModel;

  public VariablePanel(String id, final IModel<Variable> variableModel, final IModel<Questionnaire> questionnaireModel) {
    super(id);
    this.questionnaireModel = questionnaireModel;

    final EditedVariable editedVariable = new EditedVariable();
    final Variable variable = variableModel.getObject();
    if(variable != null) {
      editedVariable.setName(variable.getName());
      editedVariable.setValueType(variable.getValueType());
      editedVariable.setScript(variable.getAttributeStringValue("script"));
    }
    final IModel<EditedVariable> model = new Model<EditedVariable>(editedVariable);
    setDefaultModel(model);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<EditedVariable>("form", model));

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
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
    form.add(name).add(new SimpleFormComponentLabel("nameLabel", name));

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
    form.add(valueType.setLabel(new ResourceModel("Type"))).add(new SimpleFormComponentLabel("typeLabel", valueType));

    TextArea<String> script = new TextArea<String>("script", new PropertyModel<String>(form.getModel(), "script"));
    script.add(new RequiredFormFieldBehavior());
    form.add(script.setLabel(new ResourceModel("Script"))).add(new SimpleFormComponentLabel("scriptLabel", script));

    Set<ValueTable> valueTables = MagmaEngine.get().getDatasource("onyx-datasource").getValueTables();
    List<String> tables = new ArrayList<String>(valueTables.size());
    for(ValueTable valueTable : valueTables) {
      tables.add(valueTable.getName());
    }
    Collections.sort(tables);
    final DropDownChoice<String> tablesDropDown = new DropDownChoice<String>("tables", new Model<String>(tables.get(0)), tables);
    tablesDropDown.setNullValid(false);
    form.add(tablesDropDown.setLabel(new ResourceModel("Tables"))).add(new SimpleFormComponentLabel("tablesLabel", tablesDropDown));

    final List<String> tableVariables = new ArrayList<String>();
    tableVariables.add("                ");
    final DropDownChoice<String> tableVariablesDropDown = new DropDownChoice<String>("variables", new Model<String>(tableVariables.get(0)), tableVariables);
    tableVariablesDropDown.setNullValid(false).setOutputMarkupId(true);
    form.add(tableVariablesDropDown.setLabel(new ResourceModel("Variables"))).add(new SimpleFormComponentLabel("variablesLabel", tableVariablesDropDown));

    tablesDropDown.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        tableVariables.clear();
        String selectedTable = tablesDropDown.getModelObject();
        if(selectedTable == null) {
          tableVariables.add("                ");
        } else {
          ValueTable table = MagmaEngine.get().getDatasource("onyx-datasource").getValueTable(selectedTable);
          for(Variable v : table.getVariables()) {
            tableVariables.add(v.getName());
          }
          Collections.sort(tableVariables);
        }
        tableVariablesDropDown.setModelObject(tableVariables.get(0));
        target.addComponent(tableVariablesDropDown);
      }
    });

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        Builder builder = Variable.Builder.newVariable(editedVariable.getName(), editedVariable.getValueType(), "Participant");
        builder.addAttribute("script", editedVariable.getScript());
        onSave(target, builder.build());
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
        onCancel(target);
      }
    }.setDefaultFormProcessing(false));
  }

  /**
   * 
   * @param target
   * @param section
   */
  public abstract void onSave(AjaxRequestTarget target, Variable variable);

  public abstract void onCancel(AjaxRequestTarget target);

  public void persist(AjaxRequestTarget target) {
    try {
      questionnairePersistenceUtils.persist(questionnaireModel.getObject());
    } catch(Exception e) {
      logger.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }
}
