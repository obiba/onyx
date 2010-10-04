/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.form.AbstractQuestionnaireElementPanelForm;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

@SuppressWarnings("serial")
public class CategoryPropertiesPanel extends AbstractQuestionnaireElementPanelForm<Category> {

  private VariableNamesPanel variableNamesPanel;

  public CategoryPropertiesPanel(String id, Model<Category> model, Questionnaire questionnaireParent, ModalWindow modalWindow) {
    super(id, model, questionnaireParent, modalWindow);
    createComponent();
  }

  public void createComponent() {
    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
    name.add(new RequiredFormFieldBehavior());
    form.add(name);

    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", form.getModelObject(), localePropertiesModel));

    form.add(new CheckBox("escape", new PropertyModel<Boolean>(form.getModel(), "escape")));
    form.add(new CheckBox("noAnswer", new PropertyModel<Boolean>(form.getModel(), "noAnswer")));
    form.add(variableNamesPanel = new VariableNamesPanel("variableNamesPanel", form.getModelObject().getVariableNames()));
  }

  @Override
  public void onSave(AjaxRequestTarget target, Category category) {
    for(Map.Entry<String, String> entries : variableNamesPanel.getNewMapData().entrySet()) {
      category.addVariableName(entries.getKey(), entries.getValue());
    }
  }
}
