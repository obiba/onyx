/*
 * ***************************************************************************
 *  Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *  <p/>
 *  This program and the accompanying materials
 *  are made available under the terms of the GNU Public License v3.0.
 *  <p/>
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  ****************************************************************************
 */
package org.obiba.onyx.quartz.editor.openAnswer.autocomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.PanelCachingTab;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionSuggestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.utils.tab.AjaxSubmitTabbedPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
public class SuggestionVariableValuesPanel extends Panel {

  @SpringBean
  private MagmaInstanceProvider magmaInstanceProvider;

  private final DropDownChoice<String> datasource;
  private final DropDownChoice<String> table;
  private final AjaxSubmitTabbedPanel variableTabbedPanel;

  public SuggestionVariableValuesPanel(String id, IModel<OpenAnswerDefinition> model,
      IModel<Questionnaire> questionnaireModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);

    add(CSSPackageResource
        .getHeaderContribution(SuggestionVariableValuesPanel.class, "SuggestionVariableValuesPanel.css"));

    final OpenAnswerDefinitionSuggestion openAnswerSuggestion = new OpenAnswerDefinitionSuggestion(model.getObject());

    String datasourceValue = null;
    String tableValue = null;
    for(Locale locale : questionnaireModel.getObject().getLocales()) {
      String variablePath = openAnswerSuggestion.getVariableValues(locale);
      if(StringUtils.isNotBlank(variablePath)) {
        if(variablePath.contains(":")) {
          String datasourceTable = variablePath.split(":")[0];
          if(datasourceTable.contains(".")) {
            String[] split = datasourceTable.split(".");
            datasourceValue = split[0];
            tableValue = split[1];
          }
        }
        break;
      }
    }

    List<String> datasources = new ArrayList<String>();
    for(Datasource ds : magmaInstanceProvider.getDatasources()) {
      datasources.add(ds.getName());
    }
    Collections.sort(datasources);

    datasource = new DropDownChoice<String>("datasource", new Model<String>(datasourceValue), datasources);
    datasource.setLabel(new ResourceModel("Datasource"));
    datasource.setNullValid(false);
    datasource.add(new RequiredFormFieldBehavior());
    datasource.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.addComponent(table);
      }
    });
    add(datasource).add(new SimpleFormComponentLabel("datasourceLabel", datasource));

    IModel<List<String>> tableChoiceModel = new AbstractReadOnlyModel<List<String>>() {
      @Override
      public List<String> getObject() {
        if(datasource.getModelObject() == null) {
          return Collections.emptyList();
        }
        List<String> tables = new ArrayList<String>();
        for(ValueTable vt : magmaInstanceProvider.getDatasource(datasource.getModelObject()).getValueTables()) {
          tables.add(vt.getName());
        }
        Collections.sort(tables);
        return tables;
      }
    };

    table = new DropDownChoice<String>("table", new Model<String>(tableValue), tableChoiceModel);
    table.setLabel(new ResourceModel("Table"));
    table.setOutputMarkupId(true);
    table.setNullValid(false);
    table.add(new RequiredFormFieldBehavior());
    table.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.addComponent(variableTabbedPanel);
      }
    });
    add(table).add(new SimpleFormComponentLabel("tableLabel", table));

    CheckBox selectEntity = new CheckBox("selectEntity",
        new Model<Boolean>(openAnswerSuggestion.getVariableSelectEntity()));
    selectEntity.setLabel(new ResourceModel("SelectEntity"));
    add(selectEntity).add(new SimpleFormComponentLabel("selectEntityLabel", selectEntity));
    add(new HelpTooltipPanel("selectEntityHelp", new ResourceModel("SelectEntity.Tooltip")));

    Locale userLocale = Session.get().getLocale();
    List<ITab> tabs = new ArrayList<ITab>();
    for(final Locale locale : questionnaireModel.getObject().getLocales()) {
      AbstractTab tab = new AbstractTab(new Model<String>(locale.getDisplayLanguage(userLocale))) {
        @Override
        public Panel getPanel(String panelId) {
          String variableValue = null;
          String variablePath = openAnswerSuggestion.getVariableValues(locale);
          if(variablePath != null) {
            String[] split = variablePath.split(":");
            if(split.length == 2) variableValue = split[1];
          }
          return new VariablePanel(panelId, new Model<String>(variableValue));
        }
      };
      tabs.add(new PanelCachingTab(tab));
    }

    variableTabbedPanel = new AjaxSubmitTabbedPanel("variableTabs", feedbackPanel, feedbackWindow, tabs);
    variableTabbedPanel.setVisible(!tabs.isEmpty());

    WebMarkupContainer variableTabsContainer = new WebMarkupContainer("variableTabsContainer");
    variableTabsContainer.setOutputMarkupId(true);
    variableTabsContainer.add(variableTabbedPanel);

    add(variableTabsContainer);

  }

  public class VariablePanel extends Panel {

    public VariablePanel(String id, Model<String> model) {
      super(id, model);

      IModel<List<String>> variableChoiceModel = new AbstractReadOnlyModel<List<String>>() {
        @Override
        public List<String> getObject() {
          if(table.getModelObject() == null) {
            return Collections.emptyList();
          }
          List<String> variables = new ArrayList<String>();
          for(Variable variable : magmaInstanceProvider.getDatasource(datasource.getModelObject()).getValueTable(table
              .getModelObject()).getVariables()) {
            variables.add(variable.getName());
          }
          Collections.sort(variables);
          return variables;
        }
      };

      DropDownChoice<String> variable = new DropDownChoice<String>("variable", model, variableChoiceModel);
      variable.setLabel(new ResourceModel("Variable"));
      variable.setOutputMarkupId(true);
      variable.setNullValid(false);
      variable.add(new RequiredFormFieldBehavior());
      variable.add(new OnChangeAjaxBehavior() {
        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          OpenAnswerDefinition openAnswerDefinition = (OpenAnswerDefinition) SuggestionVariableValuesPanel.this
              .getDefaultModelObject();

        }
      });
      add(variable).add(new SimpleFormComponentLabel("variableLabel", variable));
    }
  }

}
