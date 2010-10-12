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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.editor.category.VariableNamesPanel;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.quartz.editor.questionnaire.EditedQuestionnaire;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

@SuppressWarnings("serial")
public class OpenAnswerDefinitionPropertiesPanel extends Panel {

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  // @SpringBean
  // private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<OpenAnswerDefinition> form;

  private final VariableNamesPanel variableNamesPanel;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private List<Class<? extends IValidator>> iValidatorsAvailable = Arrays.asList(MaximumValidator.class, MinimumValidator.class, RangeValidator.class);

  public OpenAnswerDefinitionPropertiesPanel(String id, IModel<OpenAnswerDefinition> model, IModel<EditedQuestionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id, model);

    List<LocaleProperties> listLocaleProperties = new ArrayList<LocaleProperties>();
    Questionnaire questionnaire = questionnaireModel.getObject().getElement();
    for(Locale locale : questionnaire.getLocales()) {
      LocaleProperties localeProperties = new LocaleProperties(locale, model);
      List<String> values = new ArrayList<String>();
      for(String property : localeProperties.getKeys()) {
        if(StringUtils.isNotBlank(model.getObject().getName())) {
          QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaire.getName());
          if(bundle != null) {
            values.add(QuestionnaireStringResourceModelHelper.getNonRecursiveResolutionMessage(bundle, model.getObject(), property, new Object[0], locale));
          }
        }
      }
      localeProperties.setValues(values.toArray(new String[localeProperties.getKeys().length]));
      listLocaleProperties.add(localeProperties);
    }
    ListModel<LocaleProperties> localePropertiesModel = new ListModel<LocaleProperties>(listLocaleProperties);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<OpenAnswerDefinition>("form", model));

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    form.add(name);
    form.add(new SimpleFormComponentLabel("nameLabel", name));

    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", form.getModel(), localePropertiesModel));

    DropDownChoice<DataType> dataTypeDropDownChoice = new DropDownChoice<DataType>("dataTypeDropDownChoice", new PropertyModel<DataType>(form.getModel(), "dataType"), Arrays.asList(DataType.values()), new ChoiceRenderer<DataType>());
    dataTypeDropDownChoice.setLabel(new ResourceModel("DataType"));
    form.add(dataTypeDropDownChoice);
    form.add(new SimpleFormComponentLabel("dataTypeLabel", dataTypeDropDownChoice));

    CheckBox requiredCheckBox = new CheckBox("required", new PropertyModel<Boolean>(form.getModel(), "required"));
    requiredCheckBox.setLabel(new ResourceModel("Required2"));
    form.add(requiredCheckBox);
    form.add(new SimpleFormComponentLabel("requiredLabel", requiredCheckBox));

    TextField<String> unitTextField = new TextField<String>("unit", new PropertyModel<String>(form.getModel(), "unit"));
    unitTextField.setLabel(new ResourceModel("Unit"));
    form.add(unitTextField);
    form.add(new SimpleFormComponentLabel("unitLabel", unitTextField));

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
    form.add(new SimpleFormComponentLabel("sizeLabel", specifySize), specifySize, sizeTextFieldForUIArguments);

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

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, form.getModelObject());
        modalWindow.close(target);
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
        modalWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
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

  public void onSave(AjaxRequestTarget target, OpenAnswerDefinition openAnswerDefinition) {
    for(Map.Entry<String, String> entries : variableNamesPanel.getNewMapData().entrySet()) {
      openAnswerDefinition.addVariableName(entries.getKey(), entries.getValue());
    }
  }
}
