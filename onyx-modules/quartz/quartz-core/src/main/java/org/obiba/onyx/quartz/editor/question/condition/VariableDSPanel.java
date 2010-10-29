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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
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

  private final List<String> magmaNameChoices = new ArrayList<String>();

  private final ListMultimap<String, String> valuesByTable = ArrayListMultimap.create();

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

    TextArea<String> script = new TextArea<String>("script", new PropertyModel<String>(model, "script"));
    script.setLabel(new ResourceModel("Script"));
    form.add(script);
    form.add(new SimpleFormComponentLabel("scriptLabel", script));

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

}
