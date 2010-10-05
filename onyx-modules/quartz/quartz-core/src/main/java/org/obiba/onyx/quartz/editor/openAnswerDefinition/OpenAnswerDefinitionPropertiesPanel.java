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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.editor.category.VariableNamesPanel;
import org.obiba.onyx.quartz.editor.form.AbstractQuestionnaireElementPanel;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.data.DataValidator;

@SuppressWarnings("serial")
public class OpenAnswerDefinitionPropertiesPanel extends AbstractQuestionnaireElementPanel<OpenAnswerDefinition> {

  private VariableNamesPanel variableNamesPanel;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private List<Class<? extends IValidator>> iValidatorsAvailable = Arrays.asList(MaximumValidator.class, MinimumValidator.class, RangeValidator.class);

  public OpenAnswerDefinitionPropertiesPanel(String id, IModel<OpenAnswerDefinition> model, IModel<Questionnaire> questionnaireParentModel, ModalWindow modalWindow) {
    super(id, model, questionnaireParentModel, modalWindow);
    createComponent();
  }

  public void createComponent() {
    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
    name.add(new RequiredFormFieldBehavior());
    form.add(name);

    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", form.getModel(), localePropertiesModel));

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

    ListView<Class<? extends IValidator>> listViewValidator = new ListView<Class<? extends IValidator>>("listViewDataValidators", iValidatorsAvailable) {

      @Override
      protected void populateItem(ListItem<Class<? extends IValidator>> item) {
        try {
          Constructor<?> constructor = iValidatorsAvailable.get(item.getIndex()).getConstructors()[0];
          int nbParameters = constructor.getParameterTypes().length;
          IValidator validator = (IValidator) constructor.newInstance(new Object[nbParameters]);
          item.add(new ValidatorFragment("validatorItem", new Model(new ValidatorObject(false, new DataValidator(validator, null), new Integer[nbParameters]))));
        } catch(Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    form.add(listViewValidator);
  }

  public class ValidatorFragment extends Fragment {

    private List<TextField<Integer>> valuesTextFields;

    public ValidatorFragment(String id, IModel<ValidatorObject> validatorModel) {
      super(id, "fragmentValidator", OpenAnswerDefinitionPropertiesPanel.this, validatorModel);
      add(new CheckBox("wantedValidator", new Model<Boolean>(validatorModel.getObject().isWantThisValidator())));
      add(new Label("labelValidator", ClassUtils.getShortClassName(validatorModel.getObject().getDataValidator().getValidator().getClass())));
      add(new ListView<Integer>("listViewValues", new ListModel<Integer>(Arrays.asList(validatorModel.getObject().getValues()))) {

        @Override
        protected void populateItem(ListItem<Integer> item) {
          AttributeModifier attributeModifier = new AttributeModifier("size", true, new Model<Integer>(2));
          PropertyModel<Integer> propertyModel = new PropertyModel<Integer>(getModelObject(), "[" + item.getIndex() + "]");
          item.add(new TextField<Integer>("valueItem", propertyModel, Integer.class).add(attributeModifier));
        }
      });
    }
  }

  public class ValidatorObject implements Serializable {

    private boolean wantThisValidator;

    private DataValidator dataValidator;

    private Integer values[];

    public ValidatorObject(boolean wantThisValidator, DataValidator dataValidator, Integer values[]) {
      this.wantThisValidator = wantThisValidator;
      this.dataValidator = dataValidator;
      this.values = values;
    }

    public boolean isWantThisValidator() {
      return wantThisValidator;
    }

    public void setWantThisValidator(boolean wantThisValidator) {
      this.wantThisValidator = wantThisValidator;
    }

    public DataValidator getDataValidator() {
      return dataValidator;
    }

    public void setDataValidator(DataValidator dataValidator) {
      this.dataValidator = dataValidator;
    }

    public Integer[] getValues() {
      return values;
    }

    public void setValues(Integer[] values) {
      this.values = values;
    }

  }

  @Override
  public void onSave(AjaxRequestTarget target, OpenAnswerDefinition openAnswerDefinition) {
    for(Map.Entry<String, String> entries : variableNamesPanel.getNewMapData().entrySet()) {
      openAnswerDefinition.addVariableName(entries.getKey(), entries.getValue());
    }
  }
}
