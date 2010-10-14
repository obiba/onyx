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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.editor.questionnaire.EditedQuestionnaire;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.IColumnProvider;

/**
 *
 */
@SuppressWarnings("serial")
public class ConditionPanel extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  private final ModalWindow dataSourceWindow;

  private final OnyxEntityList<ConditionDataSource> dataSources;

  private final Form<Conditions> form;

  private final WebMarkupContainer expressionContainer;

  private WebMarkupContainer expressionVisibility;

  public ConditionPanel(String id, IModel<Conditions> model, final IModel<Question> questionModel, final IModel<EditedQuestionnaire> questionnaireModel) {
    super(id, model);

    final Conditions conditions = model.getObject();

    dataSourceWindow = new ModalWindow("dataSourceWindow");
    dataSourceWindow.setCssClassName("onyx");
    dataSourceWindow.setInitialWidth(800);
    dataSourceWindow.setInitialHeight(300);
    dataSourceWindow.setResizable(true);
    add(dataSourceWindow);

    add(form = new Form<Conditions>("form", model));

    expressionContainer = new WebMarkupContainer("expressionContainer");
    expressionContainer.setOutputMarkupId(true);
    form.add(expressionContainer);

    expressionVisibility = new WebMarkupContainer("expressionVisibility");
    expressionVisibility.setVisible(conditions.getDataSources().size() > 1);
    expressionContainer.add(expressionVisibility);

    final TextArea<String> expression = new TextArea<String>("expression", new PropertyModel<String>(form.getModel(), "expression"));
    expression.setLabel(new ResourceModel("Expression"));
    expression.setEscapeModelStrings(false);
    expression.setRequired(true);
    expression.add(new AbstractValidator<String>() {

      private final Pattern PATTERN = Pattern.compile("(\\$[\\d]+)");

      @SuppressWarnings("unchecked")
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        String value = validatable.getValue();
        if(StringUtils.isBlank(value)) return;
        // check if all defined dataSources are used
        if(conditions.getDataSources().size() > 1) {
          List<String> ds = new ArrayList<String>();
          for(int i = 1; i <= conditions.getDataSources().size(); i++) {
            String variable = "$" + i;
            ds.add(variable);
            if(value.indexOf(variable) < 0) {
              Map<String, Object> vars = new HashMap<String, Object>();
              vars.put("variable", variable);
              error(validatable, "UnusedDataSourceVariable", vars);
            }
          }
          // check if for each expression variable, there is a datasource defined
          List<String> variables = new ArrayList<String>();
          find(value, variables);
          for(String notFound : (Collection<String>) CollectionUtils.disjunction(ds, variables)) {
            Map<String, Object> vars = new HashMap<String, Object>();
            vars.put("variable", notFound);
            error(validatable, "UndefinedDataSourceVariable", vars);
          }
        }

      }

      // I'm sure we can do this in a single regex but I didn't find it :-(
      private void find(String value, List<String> variables) {
        Matcher matcher = PATTERN.matcher(value);
        if(matcher.find()) {
          variables.add(matcher.group(0));
          String start = value.substring(0, matcher.start(0));
          String end = value.substring(matcher.end(0), value.length());
          find(start + end, variables);
        }
      }
    });

    expressionVisibility.add(expression);
    expressionVisibility.add(new SimpleFormComponentLabel("expressionLabel", expression));

    SortableDataProvider<ConditionDataSource> dataProvider = new SortableDataProvider<ConditionDataSource>() {

      @Override
      public Iterator<? extends ConditionDataSource> iterator(int first, int count) {
        return conditions.getDataSources().iterator();
      }

      @Override
      public int size() {
        return conditions.getDataSources().size();
      }

      @Override
      public IModel<ConditionDataSource> model(ConditionDataSource object) {
        return new Model<ConditionDataSource>(object);
      }

    };

    form.add(new AjaxLink<Void>("addDataSource") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        dataSourceWindow.setContent(new DataSourcePanel("content", questionModel, questionnaireModel, dataSourceWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, ConditionDataSource dataSource) {
            List<ConditionDataSource> list = form.getModelObject().getDataSources();
            list.add(dataSource);
            dataSource.setIndex(list.size());
            expressionVisibility.setVisible(list.size() > 1);
            target1.addComponent(dataSources);
            target1.addComponent(expressionContainer);
          }
        });
        dataSourceWindow.show(target);
      }
    });

    form.add(dataSources = new OnyxEntityList<ConditionDataSource>("dataSources", dataProvider, new DataSourcesColumnProvider(), new StringResourceModel("DataSources", ConditionPanel.this, null)));

  }

  private class DataSourcesColumnProvider implements IColumnProvider<ConditionDataSource>, Serializable {

    private final List<IColumn<ConditionDataSource>> columns = new ArrayList<IColumn<ConditionDataSource>>();

    public DataSourcesColumnProvider() {
      columns.add(new AbstractColumn<ConditionDataSource>(new StringResourceModel("Variable", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<ConditionDataSource>> cellItem, String componentId, IModel<ConditionDataSource> rowModel) {
          cellItem.add(new Label(componentId, "$" + rowModel.getObject().getIndex()));
        }
      });
      columns.add(new PropertyColumn<ConditionDataSource>(new StringResourceModel("Questionnaire", ConditionPanel.this, null), "questionnaire.name"));
      columns.add(new PropertyColumn<ConditionDataSource>(new StringResourceModel("Question", ConditionPanel.this, null), "question.name"));
      columns.add(new AbstractColumn<ConditionDataSource>(new StringResourceModel("Category", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<ConditionDataSource>> cellItem, String componentId, IModel<ConditionDataSource> rowModel) {
          Category category = rowModel.getObject().getCategory();
          cellItem.add(new Label(componentId, category == null ? new StringResourceModel("Any", ConditionPanel.this, null) : new Model<String>(category.getName())));
        }
      });
      columns.add(new AbstractColumn<ConditionDataSource>(new StringResourceModel("OpenAnswerDefinition", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<ConditionDataSource>> cellItem, String componentId, IModel<ConditionDataSource> rowModel) {
          OpenAnswerDefinition openAnswer = rowModel.getObject().getOpenAnswerDefinition();
          cellItem.add(new Label(componentId, openAnswer == null ? "" : openAnswer.getName()));
        }
      });

      columns.add(new HeaderlessColumn<ConditionDataSource>() {
        @Override
        public void populateItem(Item<ICellPopulator<ConditionDataSource>> cellItem, String componentId, IModel<ConditionDataSource> rowModel) {
          cellItem.add(new DataSourceAction(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<ConditionDataSource>> getAdditionalColumns() {
      return null;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<ConditionDataSource>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<ConditionDataSource>> getRequiredColumns() {
      return columns;
    }

  }

  public class DataSourceAction extends Fragment {

    public DataSourceAction(String id, final IModel<ConditionDataSource> model) {
      super(id, "dataSourceAction", ConditionPanel.this, model);
      add(new AjaxLink<ConditionDataSource>("deleteLink", model) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          List<ConditionDataSource> list = ((Conditions) ConditionPanel.this.getDefaultModelObject()).getDataSources();
          list.remove(model.getObject());
          expressionVisibility.setVisible(list.size() > 1);
          target.addComponent(dataSources);
          target.addComponent(expressionContainer);
        }
      });
    }
  }
}
