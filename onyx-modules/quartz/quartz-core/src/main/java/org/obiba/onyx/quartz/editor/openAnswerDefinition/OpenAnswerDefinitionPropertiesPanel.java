/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswerDefinition;

import java.util.Arrays;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.editor.category.VariableNamesPanel;
import org.obiba.onyx.quartz.editor.form.AbstractQuestionnaireElementPanelForm;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

@SuppressWarnings("serial")
public class OpenAnswerDefinitionPropertiesPanel extends AbstractQuestionnaireElementPanelForm<OpenAnswerDefinition> {

  private VariableNamesPanel variableNamesPanel;

  public OpenAnswerDefinitionPropertiesPanel(String id, IModel<OpenAnswerDefinition> model, Questionnaire questionnaireParent, ModalWindow modalWindow) {
    super(id, model, questionnaireParent, modalWindow);
    createComponent();
  }

  public void createComponent() {
    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new StringValidator.MaximumLengthValidator(20));
    form.add(name);

    form.add(new DropDownChoice<DataType>("dataTypeDropDownChoice", new PropertyModel<DataType>(form.getModel(), "dataType"), Arrays.asList(DataType.values()), new ChoiceRenderer<DataType>()));

    form.add(new CheckBox("required", new PropertyModel<Boolean>(form.getModel(), "required")));

    form.add(new TextField<String>("unit", new PropertyModel<String>(form.getModel(), "unit")));

    final TextField<String> sizeTextFieldForUIArguments = new TextField<String>("size", new Model<String>());
    sizeTextFieldForUIArguments.setOutputMarkupPlaceholderTag(true);
    sizeTextFieldForUIArguments.setVisible(false);
    ValueMap uiArgumentsValueMap = form.getModelObject().getUIArgumentsValueMap();
    AjaxCheckBox specifySize = new AjaxCheckBox("wantSpecifySize", new Model<Boolean>(uiArgumentsValueMap != null ? uiArgumentsValueMap.get(DefaultOpenAnswerDefinitionPanel.INPUT_NB_ROWS) != null : false)) {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        sizeTextFieldForUIArguments.setVisible(this.getModelObject());
        target.addComponent(sizeTextFieldForUIArguments);
      }
    };
    form.add(specifySize, sizeTextFieldForUIArguments);

    form.add(variableNamesPanel = new VariableNamesPanel("variableNamesPanel", form.getModelObject().getVariableNames()));

  }

  @Override
  public void onSave(AjaxRequestTarget target, OpenAnswerDefinition openAnswerDefinition) {
    super.onSave(target, openAnswerDefinition);
    for(Map.Entry<String, String> entries : variableNamesPanel.getNewMapData().entrySet()) {
      openAnswerDefinition.addVariableName(entries.getKey(), entries.getValue());
    }
  }
}
