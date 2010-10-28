/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class VariableDSPanel extends Panel {

  private static final int AUTO_COMPLETE_SIZE = 15;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final DropDownChoice<DataType> dataType;

  private final WebMarkupContainer valueContainer;

  private final TextField<String> stringValue;

  private final TextField<String> numericValue;

  private final TextField<String> dateValue;

  private SimpleFormComponentLabel valueLabel;

  private List<String> magmaNameChoices = new ArrayList<String>();

  private ListMultimap<String, String> valuesByTable = ArrayListMultimap.create();

  public VariableDSPanel(String id, final IModel<VariableDS> model, final ModalWindow variableWindow) {
    super(id, model);

    VariableDS variableDS = model.getObject();

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    final Form<VariableDS> form = new Form<VariableDS>("form", model);
    add(form);

    final List<String> tables = new ArrayList<String>();
    valuesByTable = ArrayListMultimap.create();

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable valueTable : datasource.getValueTables()) {
        String tableName = valueTable.getName();
        tables.add(tableName);
        for(Variable variable : valueTable.getVariables()) {
          valuesByTable.put(tableName.toLowerCase(), variable.getName());
        }
      }
    }
    Collections.sort(tables);

    setMagmaNameChoices(variableDS.getTable());

    final AutoCompleteTextField<String> magmaTable = new AutoCompleteTextField<String>("magmaTable", new PropertyModel<String>(model, "table")) {
      @Override
      protected Iterator<String> getChoices(String input) {
        if(StringUtils.isBlank(input)) {
          List<String> emptyList = Collections.emptyList();
          return emptyList.iterator();
        }

        List<String> choices = new ArrayList<String>(AUTO_COMPLETE_SIZE);
        for(String tableName : tables) {
          if(tableName.toLowerCase().startsWith(input.toLowerCase())) {
            choices.add(tableName);
            if(choices.size() == AUTO_COMPLETE_SIZE) break;
          }
        }
        return choices.iterator();
      }
    };
    magmaTable.setOutputMarkupId(true);
    magmaTable.setLabel(new ResourceModel("Table"));
    magmaTable.add(new AjaxFormComponentUpdatingBehavior("onblur") {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        setMagmaNameChoices(magmaTable.getModelObject());
      }
    });
    form.add(magmaTable);
    form.add(new SimpleFormComponentLabel("magmaTableLabel", magmaTable));

    final AutoCompleteTextField<String> magmaName = new AutoCompleteTextField<String>("magmaName", new PropertyModel<String>(model, "name")) {
      @Override
      protected Iterator<String> getChoices(String input) {
        if(StringUtils.isBlank(input)) {
          List<String> emptyList = Collections.emptyList();
          return emptyList.iterator();
        }

        List<String> choices = new ArrayList<String>(AUTO_COMPLETE_SIZE);
        for(String name : magmaNameChoices) {
          if(name.toLowerCase().startsWith(input.toLowerCase())) {
            choices.add(name);
            if(choices.size() == AUTO_COMPLETE_SIZE) break;
          }
        }
        return choices.iterator();
      }
    };
    magmaName.setOutputMarkupId(true);
    magmaName.setLabel(new ResourceModel("Name"));
    form.add(magmaName);
    form.add(new SimpleFormComponentLabel("magmaNameLabel", magmaName));

    // TODO add validation
    // TODO add boolean support
    final DropDownChoice<ComparisonOperator> operatorChoice = new DropDownChoice<ComparisonOperator>("operator", new PropertyModel<ComparisonOperator>(form.getModel(), "operator"), Arrays.asList(ComparisonOperator.values()), new IChoiceRenderer<ComparisonOperator>() {
      @Override
      public Object getDisplayValue(ComparisonOperator element) {
        return new StringResourceModel("Operator." + element.name(), VariableDSPanel.this, null).getString();
      }

      @Override
      public String getIdValue(ComparisonOperator element, int index) {
        return element.name();
      }
    });
    operatorChoice.setLabel(new ResourceModel("Operator"));
    operatorChoice.setNullValid(false);
    form.add(operatorChoice);
    form.add(new SimpleFormComponentLabel("operatorLabel", operatorChoice));

    List<DataType> types = new ArrayList<DataType>(Arrays.asList(DataType.values()));
    types.remove(DataType.DATA);

    dataType = new DropDownChoice<DataType>("type", new PropertyModel<DataType>(form.getModel(), "type"), types, new IChoiceRenderer<DataType>() {
      @Override
      public Object getDisplayValue(DataType element) {
        return new StringResourceModel("DataType." + element.name(), VariableDSPanel.this, null).getString();
      }

      @Override
      public String getIdValue(DataType element, int index) {
        return element.name();
      }
    });
    dataType.setLabel(new ResourceModel("Type"));
    dataType.setNullValid(false);
    form.add(dataType);
    form.add(new SimpleFormComponentLabel("typeLabel", dataType));

    valueContainer = new WebMarkupContainer("valueContainer");
    valueContainer.setOutputMarkupId(true);
    form.add(valueContainer);

    stringValue = new TextField<String>("stringValue", new PropertyModel<String>(model, "value"), String.class);
    stringValue.setLabel(new ResourceModel("Value"));
    valueContainer.add(stringValue);

    // PatternValidator numericPatternValidator = new PatternValidator("[0-9]*");
    numericValue = new TextField<String>("numericValue", new PropertyModel<String>(model, "value"), String.class);
    numericValue.setLabel(new ResourceModel("Value"));
    // numericValue.add(numericPatternValidator);
    valueContainer.add(numericValue);

    // TODO validate date
    // PatternValidator datePatternValidator = new PatternValidator("[0-9]4-[0-9]2-[0-9]2");
    dateValue = new TextField<String>("dateValue", new PropertyModel<String>(model, "value"), String.class);
    dateValue.setLabel(new ResourceModel("Value"));
    // dateValue(datePatternValidator);
    valueContainer.add(dateValue);

    valueContainer.add(valueLabel = new SimpleFormComponentLabel("valueLabel", stringValue));

    setValueLabels(dataType.getModelObject());

    // submit the whole form instead of just the dataType component
    dataType.add(new AjaxFormSubmitBehavior("onchange") {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        String value = dataType.getValue(); // use value because model is not set if validation error
        setValueLabels(value == null ? null : DataType.valueOf(value));
        target.addComponent(valueContainer);
      }

      @Override
      protected void onError(AjaxRequestTarget target) {
        Session.get().getFeedbackMessages().clear(); // we don't want to validate fields now
        onSubmit(target);
      }
    });

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, form.getModelObject());
        variableWindow.close(target);
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
        variableWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }

  public abstract void onSave(AjaxRequestTarget target, VariableDS variableDS);

  private void setMagmaNameChoices(String tableName) {
    magmaNameChoices.clear();
    if(StringUtils.isNotBlank(tableName)) {
      List<String> availableNames = valuesByTable.get(tableName.toLowerCase());
      if(availableNames != null) {
        magmaNameChoices.addAll(availableNames);
        Collections.sort(magmaNameChoices);
      }
    }
  }

  @SuppressWarnings("incomplete-switch")
  private void setValueLabels(DataType type) {
    if(type == null) {
      setValueLabel(stringValue);
      stringValue.setVisible(true).setEnabled(false);
      clearAndHide(numericValue, dateValue);
    } else {
      switch(type) {
      case TEXT:
        setValueLabel(stringValue);
        stringValue.setVisible(true).setEnabled(true);
        clearAndHide(numericValue, dateValue);
        break;

      case DECIMAL:
      case INTEGER:
        setValueLabel(numericValue);
        numericValue.setVisible(true);
        clearAndHide(stringValue, dateValue);
        break;

      case DATE:
        setValueLabel(dateValue);
        dateValue.setVisible(true);
        clearAndHide(stringValue, numericValue);
        break;
      }
    }
  }

  private void setValueLabel(FormComponent<?> component) {
    SimpleFormComponentLabel newLabel = new SimpleFormComponentLabel(valueLabel.getId(), component);
    valueLabel.replaceWith(newLabel);
    valueLabel = newLabel;
  }

  private void clearAndHide(FormComponent<?>... components) {
    if(components != null) {
      for(FormComponent<?> component : components) {
        component.setModelObject(null);
        component.setVisible(false);
      }
    }
  }
}
