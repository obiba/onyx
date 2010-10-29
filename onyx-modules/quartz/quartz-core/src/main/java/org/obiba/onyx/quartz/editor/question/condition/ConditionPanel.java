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

import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.IColumnProvider;

/**
 *
 */
@SuppressWarnings("serial")
public class ConditionPanel extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  private final ModalWindow variableWindow;

  private final OnyxEntityList<VariableDS> variables;

  private final Form<Conditions> form;

  private final WebMarkupContainer expressionContainer;

  private final WebMarkupContainer expressionVisibility;

  private final TextArea<String> expression;

  @SuppressWarnings("unchecked")
  public ConditionPanel(String id, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel) {
    super(id);
    final Conditions conditions = ConditionsFactory.create(questionModel.getObject(), questionnaireModel.getObject());
    setDefaultModel(new Model<Conditions>(conditions));

    variableWindow = new ModalWindow("variableWindow");
    variableWindow.setCssClassName("onyx");
    variableWindow.setInitialWidth(700);
    variableWindow.setInitialHeight(250);
    variableWindow.setResizable(true);
    add(variableWindow);

    add(form = new Form<Conditions>("form", (IModel<Conditions>) getDefaultModel()));

    expressionContainer = new WebMarkupContainer("expressionContainer");
    expressionContainer.setOutputMarkupId(true);
    form.add(expressionContainer);

    expressionVisibility = new WebMarkupContainer("expressionVisibility");
    expressionVisibility.setVisible(conditions.getVariables().size() > 1);
    expressionContainer.add(expressionVisibility);

    expression = new TextArea<String>("expression", new PropertyModel<String>(form.getModel(), "expression"));
    expression.setLabel(new ResourceModel("Expression"));
    expression.setRequired(true);
    expression.add(new AbstractValidator<String>() {

      private final Pattern PATTERN = Pattern.compile("(\\$[\\d]+)");

      @Override
      protected void onValidate(IValidatable<String> validatable) {
        String value = validatable.getValue();
        if(StringUtils.isBlank(value)) return;
        // check if all defined variables are used
        if(conditions.getVariables().size() > 1) {
          List<String> ds = new ArrayList<String>();
          for(int i = 1; i <= conditions.getVariables().size(); i++) {
            String variable = "$" + i;
            ds.add(variable);
            if(value.indexOf(variable) < 0) {
              Map<String, Object> vars = new HashMap<String, Object>();
              vars.put("variable", variable);
              error(validatable, "UnusedVariable", vars);
            }
          }
          // check if for each expression variable, there is a variable defined
          List<String> foundVariables = new ArrayList<String>();
          find(value, foundVariables);

          for(String notFound : (Collection<String>) CollectionUtils.subtract(foundVariables, ds)) {
            Map<String, Object> vars = new HashMap<String, Object>();
            vars.put("variable", notFound);
            error(validatable, "UndefinedVariable", vars);
          }
        }

      }

      // I'm sure we can do this in a single regex but I didn't find it :-(
      private void find(String value, List<String> foundVariables) {
        Matcher matcher = PATTERN.matcher(value);
        if(matcher.find()) {
          foundVariables.add(matcher.group(0));
          String start = value.substring(0, matcher.start(0));
          String end = value.substring(matcher.end(0), value.length());
          find(start + end, foundVariables);
        }
      }
    });

    expressionVisibility.add(expression);
    expressionVisibility.add(new SimpleFormComponentLabel("expressionLabel", expression));

    SortableDataProvider<VariableDS> variableProvider = new SortableDataProvider<VariableDS>() {

      @Override
      public Iterator<VariableDS> iterator(int first, int count) {
        return conditions.getVariables().iterator();
      }

      @Override
      public int size() {
        return conditions.getVariables().size();
      }

      @Override
      public IModel<VariableDS> model(VariableDS object) {
        return new Model<VariableDS>(object);
      }
    };

    form.add(new AjaxLink<Void>("addVariable") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        variableWindow.setContent(new VariableDSPanel("content", new Model<VariableDS>(new VariableDS()), variableWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, VariableDS variable) {
            conditions.getVariables().add(variable);
            variable.setIndex(conditions.getVariables().size());
            expressionVisibility.setVisible(conditions.getVariables().size() > 1);
            target1.addComponent(expressionContainer);
            target1.addComponent(variables);
          }
        });
        variableWindow.show(target);
      }
    });

    form.add(variables = new OnyxEntityList<VariableDS>("variables", variableProvider, new VariableDSColumnProvider(), new StringResourceModel("Variables", ConditionPanel.this, null)));

  }

  private class VariableDSColumnProvider implements IColumnProvider<VariableDS>, Serializable {

    private final List<IColumn<VariableDS>> columns = new ArrayList<IColumn<VariableDS>>();

    public VariableDSColumnProvider() {
      columns.add(new AbstractColumn<VariableDS>(new StringResourceModel("ID", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<VariableDS>> cellItem, String componentId, IModel<VariableDS> rowModel) {
          cellItem.add(new Label(componentId, "$" + rowModel.getObject().getIndex()));
        }
      });
      columns.add(new AbstractColumn<VariableDS>(new StringResourceModel("Variable", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<VariableDS>> cellItem, String componentId, IModel<VariableDS> rowModel) {
          VariableDS ds = rowModel.getObject();
          cellItem.add(new Label(componentId, isBlank(ds.getScript()) ? ds.getTable() + ":" + ds.getName() : ds.getScript()));
        }
      });
      columns.add(new HeaderlessColumn<VariableDS>() {
        @Override
        public void populateItem(Item<ICellPopulator<VariableDS>> cellItem, String componentId, IModel<VariableDS> rowModel) {
          cellItem.add(new DataSourceAction(componentId, rowModel));
        }
      });
    }

    @Override
    public List<IColumn<VariableDS>> getAdditionalColumns() {
      return null;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<VariableDS>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<VariableDS>> getRequiredColumns() {
      return columns;
    }

  }

  public class DataSourceAction extends Fragment {

    public DataSourceAction(String id, final IModel<VariableDS> model) {
      super(id, "variableAction", ConditionPanel.this, model);

      Image editImg = new Image("editImg", Images.EDIT);
      editImg.add(new AttributeModifier("title", true, new ResourceModel("Edit")));
      add(new AjaxLink<Void>("editLink") {
        @Override
        public void onClick(AjaxRequestTarget target) {
          variableWindow.setContent(new VariableDSPanel("content", model, variableWindow) {
            @Override
            public void onSave(AjaxRequestTarget target1, VariableDS variable) {
              target1.addComponent(variables);
            }
          });
          variableWindow.show(target);
        }
      }.add(editImg));

      Image deleteImg = new Image("deleteImg", Images.DELETE);
      deleteImg.add(new AttributeModifier("title", true, new ResourceModel("Delete")));
      add(new AjaxLink<Void>("deleteLink") {
        @Override
        public void onClick(AjaxRequestTarget target) {
          List<VariableDS> variableList = form.getModelObject().getVariables();
          variableList.remove(model.getObject());
          if(variableList.size() <= 1) {
            expression.setModelObject(null);
            expressionVisibility.setVisible(false);
            target.addComponent(expressionContainer);
          }
          target.addComponent(variables);
        }
      }.add(deleteImg));

    }
  }
}
