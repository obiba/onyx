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
import org.apache.commons.lang.WordUtils;
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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.editor.question.condition.datasource.ComparingDS;
import org.obiba.onyx.quartz.editor.question.condition.datasource.ComparingDSPanel;
import org.obiba.onyx.quartz.editor.question.condition.datasource.DS;
import org.obiba.onyx.quartz.editor.question.condition.datasource.QuestionnaireDS;
import org.obiba.onyx.quartz.editor.question.condition.datasource.QuestionnaireDSPanel;
import org.obiba.onyx.quartz.editor.questionnaire.EditedQuestionnaire;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.IColumnProvider;

/**
 *
 */
@SuppressWarnings("serial")
public class ConditionPanel extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private ConditionsFactory conditionsFactory;

  private final ModalWindow dataSourceWindow;

  private final OnyxEntityList<QuestionnaireDS> questionnaireDataSources;

  private final OnyxEntityList<ComparingDS> comparingDataSources;

  private final Form<Conditions> form;

  private final WebMarkupContainer expressionContainer;

  private WebMarkupContainer expressionVisibility;

  @SuppressWarnings("unchecked")
  public ConditionPanel(String id, final IModel<Question> questionModel, final IModel<EditedQuestionnaire> questionnaireModel) {
    super(id);
    final Conditions conditions = conditionsFactory.create(questionModel.getObject());
    setDefaultModel(new Model<Conditions>(conditions));

    dataSourceWindow = new ModalWindow("dataSourceWindow");
    dataSourceWindow.setCssClassName("onyx");
    dataSourceWindow.setInitialWidth(700);
    dataSourceWindow.setInitialHeight(200);
    dataSourceWindow.setResizable(true);
    add(dataSourceWindow);

    add(form = new Form<Conditions>("form", (IModel<Conditions>) getDefaultModel()));

    expressionContainer = new WebMarkupContainer("expressionContainer");
    expressionContainer.setOutputMarkupId(true);
    form.add(expressionContainer);

    expressionVisibility = new WebMarkupContainer("expressionVisibility");
    expressionVisibility.setVisible(conditions.getNbDataSources() > 1);
    expressionContainer.add(expressionVisibility);

    final TextArea<String> expression = new TextArea<String>("expression", new PropertyModel<String>(form.getModel(), "expression"));
    expression.setLabel(new ResourceModel("Expression"));
    expression.setEscapeModelStrings(false);
    expression.setRequired(true);
    expression.add(new AbstractValidator<String>() {

      private final Pattern PATTERN = Pattern.compile("(\\$[\\d]+)");

      @Override
      protected void onValidate(IValidatable<String> validatable) {
        String value = validatable.getValue();
        if(StringUtils.isBlank(value)) return;
        // check if all defined dataSources are used
        if(conditions.getNbDataSources() > 1) {
          List<String> ds = new ArrayList<String>();
          for(int i = 1; i <= conditions.getNbDataSources(); i++) {
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

    SortableDataProvider<QuestionnaireDS> questionnaireDSProvider = new SortableDataProvider<QuestionnaireDS>() {

      @Override
      public Iterator<? extends QuestionnaireDS> iterator(int first, int count) {
        return conditions.getQuestionnaireDataSources().iterator();
      }

      @Override
      public int size() {
        return conditions.getQuestionnaireDataSources().size();
      }

      @Override
      public IModel<QuestionnaireDS> model(QuestionnaireDS object) {
        return new Model<QuestionnaireDS>(object);
      }
    };

    SortableDataProvider<ComparingDS> comparingDSProvider = new SortableDataProvider<ComparingDS>() {

      @Override
      public Iterator<? extends ComparingDS> iterator(int first, int count) {
        return conditions.getComparingDataSources().iterator();
      }

      @Override
      public int size() {
        return conditions.getComparingDataSources().size();
      }

      @Override
      public IModel<ComparingDS> model(ComparingDS object) {
        return new Model<ComparingDS>(object);
      }
    };

    form.add(new AjaxLink<Void>("addQuestionnaireDataSource") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        dataSourceWindow.setContent(new QuestionnaireDSPanel("content", questionModel, questionnaireModel, dataSourceWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, QuestionnaireDS dataSource) {
            List<QuestionnaireDS> list = conditions.getQuestionnaireDataSources();
            list.add(dataSource);
            dataSource.setVariable(conditions.getNbDataSources());
            expressionVisibility.setVisible(conditions.getNbDataSources() > 1);
            target1.addComponent(questionnaireDataSources);
            target1.addComponent(expressionContainer);
          }
        });
        dataSourceWindow.show(target);
      }
    });

    form.add(new AjaxLink<Void>("addComparingDataSource") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        dataSourceWindow.setContent(new ComparingDSPanel("content", new Model<ComparingDS>(new ComparingDS()), dataSourceWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, ComparingDS dataSource) {
            List<ComparingDS> list = conditions.getComparingDataSources();
            list.add(dataSource);
            dataSource.setVariable(conditions.getNbDataSources());
            expressionVisibility.setVisible(conditions.getNbDataSources() > 1);
            target1.addComponent(comparingDataSources);
            target1.addComponent(expressionContainer);
          }
        });
        dataSourceWindow.show(target);
      }
    });

    form.add(questionnaireDataSources = new OnyxEntityList<QuestionnaireDS>("questionnaireDataSources", questionnaireDSProvider, new QuestionnaireDSColumnProvider(), new StringResourceModel("DataSources.questionnaire", ConditionPanel.this, null)));

    form.add(comparingDataSources = new OnyxEntityList<ComparingDS>("comparingDataSources", comparingDSProvider, new ComparingDSColumnProvider(), new StringResourceModel("DataSources.comparing", ConditionPanel.this, null)));

  }

  private class QuestionnaireDSColumnProvider implements IColumnProvider<QuestionnaireDS>, Serializable {

    private final List<IColumn<QuestionnaireDS>> columns = new ArrayList<IColumn<QuestionnaireDS>>();

    public QuestionnaireDSColumnProvider() {
      columns.add(new AbstractColumn<QuestionnaireDS>(new StringResourceModel("Variable", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<QuestionnaireDS>> cellItem, String componentId, IModel<QuestionnaireDS> rowModel) {
          cellItem.add(new Label(componentId, "$" + rowModel.getObject().getVariable()));
        }
      });
      columns.add(new PropertyColumn<QuestionnaireDS>(new StringResourceModel("Questionnaire", ConditionPanel.this, null), "questionnaire.name"));
      columns.add(new PropertyColumn<QuestionnaireDS>(new StringResourceModel("Question", ConditionPanel.this, null), "question.name"));
      columns.add(new AbstractColumn<QuestionnaireDS>(new StringResourceModel("Category", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<QuestionnaireDS>> cellItem, String componentId, IModel<QuestionnaireDS> rowModel) {
          Category category = rowModel.getObject().getCategory();
          cellItem.add(new Label(componentId, category == null ? new StringResourceModel("Any", ConditionPanel.this, null) : new Model<String>(category.getName())));
        }
      });
      columns.add(new AbstractColumn<QuestionnaireDS>(new StringResourceModel("OpenAnswerDefinition", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<QuestionnaireDS>> cellItem, String componentId, IModel<QuestionnaireDS> rowModel) {
          OpenAnswerDefinition openAnswer = rowModel.getObject().getOpenAnswerDefinition();
          cellItem.add(new Label(componentId, openAnswer == null ? "" : openAnswer.getName()));
        }
      });

      columns.add(new HeaderlessColumn<QuestionnaireDS>() {
        @Override
        public void populateItem(Item<ICellPopulator<QuestionnaireDS>> cellItem, String componentId, IModel<QuestionnaireDS> rowModel) {
          cellItem.add(new DataSourceAction<QuestionnaireDS>(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<QuestionnaireDS>> getAdditionalColumns() {
      return null;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<QuestionnaireDS>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<QuestionnaireDS>> getRequiredColumns() {
      return columns;
    }

  }

  private class ComparingDSColumnProvider implements IColumnProvider<ComparingDS>, Serializable {

    private final List<IColumn<ComparingDS>> columns = new ArrayList<IColumn<ComparingDS>>();

    public ComparingDSColumnProvider() {
      columns.add(new AbstractColumn<ComparingDS>(new StringResourceModel("Variable", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDS>> cellItem, String componentId, IModel<ComparingDS> rowModel) {
          cellItem.add(new Label(componentId, "$" + rowModel.getObject().getVariable()));
        }
      });
      columns.add(new AbstractColumn<ComparingDS>(new StringResourceModel("Operator", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDS>> cellItem, String componentId, IModel<ComparingDS> rowModel) {
          cellItem.add(new Label(componentId, new ResourceModel("Operator." + rowModel.getObject().getOperator().name())));
        }
      });
      columns.add(new AbstractColumn<ComparingDS>(new StringResourceModel("Type", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDS>> cellItem, String componentId, IModel<ComparingDS> rowModel) {
          cellItem.add(new Label(componentId, WordUtils.capitalizeFully(rowModel.getObject().getType())));
        }
      });
      columns.add(new AbstractColumn<ComparingDS>(new StringResourceModel("Value", ConditionPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDS>> cellItem, String componentId, IModel<ComparingDS> rowModel) {
          ComparingDS comparingDS = rowModel.getObject();
          IModel<String> m = ComparingDS.GENDER_TYPE.equals(comparingDS.getType()) ? new ResourceModel("Gender." + comparingDS.getGender().name()) : new Model<String>(comparingDS.getValue());
          cellItem.add(new Label(componentId, m));
        }
      });
      columns.add(new HeaderlessColumn<ComparingDS>() {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDS>> cellItem, String componentId, IModel<ComparingDS> rowModel) {
          cellItem.add(new DataSourceAction<ComparingDS>(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<ComparingDS>> getAdditionalColumns() {
      return null;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<ComparingDS>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<ComparingDS>> getRequiredColumns() {
      return columns;
    }

  }

  public class DataSourceAction<T extends DS> extends Fragment {

    public DataSourceAction(String id, final IModel<T> model) {
      super(id, "dataSourceAction", ConditionPanel.this, model);
      add(new AjaxLink<T>("deleteLink", model) {
        @Override
        @SuppressWarnings({ "unchecked", "null" })
        public void onClick(AjaxRequestTarget target) {
          T ds = model.getObject();
          List<T> list = null;
          Conditions conditions = form.getModelObject();
          if(ds instanceof QuestionnaireDS) {
            list = (List<T>) conditions.getQuestionnaireDataSources();
            target.addComponent(questionnaireDataSources);
          } else if(ds instanceof ComparingDS) {
            list = (List<T>) conditions.getComparingDataSources();
            target.addComponent(comparingDataSources);
          }
          list.remove(ds);
          expressionVisibility.setVisible(conditions.getNbDataSources() > 1);
          target.addComponent(expressionContainer);
        }
      });
    }
  }
}
