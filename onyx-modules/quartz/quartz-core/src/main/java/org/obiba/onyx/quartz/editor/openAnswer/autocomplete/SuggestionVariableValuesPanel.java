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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
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
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerSuggestionUtils;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
public class SuggestionVariableValuesPanel extends Panel {

  @SpringBean
  private MagmaEngine magmaEngine;

  public SuggestionVariableValuesPanel(String id, final IModel<OpenAnswerDefinition> model,
      final IModel<Question> questionModel,
      final IModel<Questionnaire> questionnaireModel, IModel<LocaleProperties> localePropertiesModel,
      final FeedbackPanel feedbackPanel, final FeedbackWindow feedbackWindow) {
    super(id, model);

    OpenAnswerDefinition openAnswerDefinition = model.getObject();

    List<String> datasources = new ArrayList<String>();
    for(Datasource ds : magmaEngine.getDatasources()) {
      datasources.add(ds.getName());
    }
    Collections.sort(datasources);

    final DropDownChoice<String> datasource = new DropDownChoice<String>("datasource", new Model<String>(),
        datasources);
    datasource.add(new RequiredFormFieldBehavior());
    datasource.setLabel(new ResourceModel("Datasource"));
    add(datasource).add(new SimpleFormComponentLabel("datasourceLabel", datasource));
    add(new HelpTooltipPanel("datasourceHelp", new ResourceModel("Datasource.Tooltip")));

    IModel<List<String>> tableChoiceModel = new AbstractReadOnlyModel<List<String>>() {
      @Override
      public List<String> getObject() {
        if(datasource.getModelObject() == null) {
          return Collections.emptyList();
        }
        List<String> tables = new ArrayList<String>();
        for(ValueTable vt : magmaEngine.getDatasource(datasource.getModelObject()).getValueTables()) {
          tables.add(vt.getName());
        }
        Collections.sort(tables);
        return tables;
      }
    };

    final DropDownChoice<String> table = new DropDownChoice<String>("table", new Model<String>(), tableChoiceModel);
    table.setOutputMarkupId(true);
    table.add(new RequiredFormFieldBehavior());
    table.setLabel(new ResourceModel("Table"));
    add(table).add(new SimpleFormComponentLabel("tableLabel", table));
    add(new HelpTooltipPanel("tableHelp", new ResourceModel("Table.Tooltip")));

    datasource.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.addComponent(table);
      }
    });

    CheckBox selectEntity = new CheckBox("selectEntity",
        new Model<Boolean>(OpenAnswerSuggestionUtils.getVariableSelectEntity(openAnswerDefinition)));
    selectEntity.setLabel(new ResourceModel("SelectEntity"));
    add(selectEntity).add(new SimpleFormComponentLabel("selectEntity", selectEntity));
    add(new HelpTooltipPanel("selectEntityHelp", new ResourceModel("SelectEntity.Tooltip")));

  }

}
