/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.workstation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

public class ExperimentalConditionForm extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private FeedbackWindow feedbackWindow;

  private ExperimentalCondition experimentalCondition;

  private ExperimentalConditionLog selectedExperimentalConditionLog;

  private Component formParent;

  private Component oldFormParent;

  private List<ExperimentalConditionLog> experimentalConditionLogs;

  public ExperimentalConditionForm(String id, IModel<ExperimentalConditionLog> model, List<ExperimentalConditionLog> experimentalConditionLogs) {
    super(id, model);
    add(new AttributeModifier("class", true, new Model<String>("experimental-condition-form")));

    this.experimentalConditionLogs = experimentalConditionLogs;

    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    addDropDown();
    addComponents();
  }

  public void setExperimentalConditionLogs(List<ExperimentalConditionLog> experimentalConditionLogs) {
    this.experimentalConditionLogs = experimentalConditionLogs;
  }

  public void addDropDown() {
    if(experimentalConditionLogs == null) experimentalConditionLogs = new ArrayList<ExperimentalConditionLog>();
    if(experimentalConditionLogs.size() >= 1) selectedExperimentalConditionLog = experimentalConditionLogs.get(0);

    WebMarkupContainer selectCalibrationId = new WebMarkupContainer("selectCalibrationId");

    final DropDownChoice<ExperimentalConditionLog> experimentalConditionLogChoice = new DropDownChoice<ExperimentalConditionLog>("experimentalConditionLogChoice", new PropertyModel<ExperimentalConditionLog>(this, "selectedExperimentalConditionLog"), experimentalConditionLogs, new ChoiceRenderer<ExperimentalConditionLog>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object getDisplayValue(ExperimentalConditionLog object) {
        return new SpringStringResourceModel(object.getName(), object.getName()).getString();
      }

      @Override
      public String getIdValue(ExperimentalConditionLog object, int index) {
        return object.getName();
      }
    });
    experimentalConditionLogChoice.add(new OnChangeAjaxBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        addComponents();
        formParent.replaceWith(oldFormParent);
        formParent = oldFormParent;
        target.addComponent(formParent);
      }

    });
    selectCalibrationId.addOrReplace(experimentalConditionLogChoice);
    addOrReplace(selectCalibrationId);
    if(experimentalConditionLogs.size() <= 1) selectCalibrationId.setVisible(false);
  }

  public void addComponents() {

    WebMarkupContainer experimentalConditionFormParent = new WebMarkupContainer("experimentalConditionFormParent");
    experimentalConditionFormParent.setOutputMarkupId(true);

    experimentalCondition = new ExperimentalCondition();
    experimentalCondition.setName(selectedExperimentalConditionLog != null ? selectedExperimentalConditionLog.getName() : "");
    experimentalCondition.setUser(userSessionService.getUser());
    experimentalCondition.setWorkstation(userSessionService.getWorkstation());

    final List<InstructionModel> instructionModels = getInstructionModels(selectedExperimentalConditionLog);
    Label instructionTitle = new Label("instructionTitle", new ResourceModel("InstructionTitle"));
    experimentalConditionFormParent.addOrReplace(instructionTitle);
    if(instructionModels.size() == 0) instructionTitle.setVisible(false);
    Loop instructionsLoop = new Loop("instructionList", selectedExperimentalConditionLog != null ? selectedExperimentalConditionLog.getInstructions().size() : 0) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(LoopItem item) {
        item.setRenderBodyOnly(true);
        item.add(new InstructionFragment("instructionRows", "instructionFragment", ExperimentalConditionForm.this, instructionModels.get(item.getIteration())));
      }
    };
    experimentalConditionFormParent.addOrReplace(instructionsLoop);

    final List<AttributeModel> attributeModels = getAttributeModels(selectedExperimentalConditionLog);
    Loop attributeLoop = new Loop("table", selectedExperimentalConditionLog != null ? selectedExperimentalConditionLog.getAttributes().size() : 0) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(LoopItem item) {
        item.setRenderBodyOnly(true);
        Attribute attribute = (Attribute) attributeModels.get(item.getIteration()).getObject();
        if(attribute.getAllowedValues() != null && attribute.getAllowedValues().size() > 0) {
          item.add(new DropDownFragment("inputRows", "dropDownFragment", ExperimentalConditionForm.this, attributeModels.get(item.getIteration())));
        } else {
          item.add(new TextFieldFragment("inputRows", "textFieldFragment", ExperimentalConditionForm.this, attributeModels.get(item.getIteration())));
        }
      }
    };
    if(getDefaultModelObject() == null) {
      setVisible(false);
    }
    experimentalConditionFormParent.addOrReplace(attributeLoop);
    if(formParent == null) {
      formParent = experimentalConditionFormParent;
      addOrReplace(formParent);
    } else {
      oldFormParent = experimentalConditionFormParent;
      formParent.replaceWith(experimentalConditionFormParent);
      formParent = experimentalConditionFormParent;
    }
  }

  private class TextFieldFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public TextFieldFragment(String id, String markupId, MarkupContainer markupContainer, IModel<?> model) {
      super(id, markupId, markupContainer, model);
      Attribute attribute = (Attribute) getDefaultModelObject();

      ExperimentalConditionValue experimentalConditionValue = new ExperimentalConditionValue();
      experimentalConditionValue.setAttributeName(attribute.getName());
      experimentalConditionValue.setAttributeType(attribute.getType());
      experimentalCondition.addExperimentalConditionValue(experimentalConditionValue);
      experimentalConditionValue.setExperimentalCondition(experimentalCondition);

      IModel<ExperimentalConditionValue> experimentalConditionValueModel = new Model<ExperimentalConditionValue>(experimentalConditionValue);

      SpringStringResourceModel fieldNameModel = new SpringStringResourceModel(attribute.getName(), attribute.getName());

      DataField formComponent = new DataField("value", new PropertyModel<ExperimentalConditionValue>(experimentalConditionValueModel, "data"), attribute.getType());
      formComponent.setRequired(true);
      formComponent.setLabel(fieldNameModel);
      if(attribute.getType().equals(DataType.TEXT)) {
        formComponent.add(new DataValidator(new StringValidator.MaximumLengthValidator(250), DataType.TEXT));
      }
      for(IValidator validator : attribute.getValidators()) {
        formComponent.add(validator);
      }

      add(new Label("label", fieldNameModel));
      WebMarkupContainer parenthesis = new WebMarkupContainer("parenthesis");
      add(parenthesis);
      parenthesis.add(new Label("unit", new Model<String>(attribute.getUnit())));
      if(attribute.getUnit() == null || attribute.getUnit().equals("")) {
        parenthesis.setVisible(false);
      }
      add(formComponent);

    }
  }

  private class DropDownFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public DropDownFragment(String id, String markupId, MarkupContainer markupProvider, IModel<?> model) {
      super(id, markupId, markupProvider, model);
      Attribute attribute = (Attribute) getDefaultModelObject();

      Set<String> allowedValuesSet = attribute.getAllowedValues();
      List<Data> allowedDataList = new ArrayList<Data>(allowedValuesSet.size());
      for(String allowed : allowedValuesSet) {
        allowedDataList.add(new Data(attribute.getType(), allowed));
      }

      ExperimentalConditionValue experimentalConditionValue = new ExperimentalConditionValue();
      experimentalConditionValue.setAttributeName(attribute.getName());
      experimentalConditionValue.setAttributeType(attribute.getType());
      experimentalCondition.addExperimentalConditionValue(experimentalConditionValue);
      experimentalConditionValue.setExperimentalCondition(experimentalCondition);

      IModel<ExperimentalConditionValue> experimentalConditionValueModel = new Model<ExperimentalConditionValue>(experimentalConditionValue);

      SpringStringResourceModel fieldNameModel = new SpringStringResourceModel(attribute.getName(), attribute.getName());

      DataField formComponent = new DataField("value", new PropertyModel<ExperimentalConditionValue>(experimentalConditionValueModel, "data"), attribute.getType(), allowedDataList, new ChoiceRenderer<Data>() {
        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(Data object) {
          return new SpringStringResourceModel(object.getValueAsString(), object.getValueAsString()).getString();
        }

        @Override
        public String getIdValue(Data object, int index) {
          return super.getIdValue(object, index);
        }
      }, "");
      formComponent.setRequired(true);
      formComponent.setLabel(fieldNameModel);

      add(formComponent);

      add(new Label("label", fieldNameModel));
      WebMarkupContainer parenthesis = new WebMarkupContainer("parenthesis");
      add(parenthesis);
      parenthesis.add(new Label("unit", new Model<String>(attribute.getUnit())));
      if(attribute.getUnit() == null || attribute.getUnit().equals("")) {
        parenthesis.setVisible(false);
      }
    }

  }

  private class InstructionFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public InstructionFragment(String id, String markupId, MarkupContainer markupContainer, IModel<InstructionModel> model) {
      super(id, markupId, markupContainer, model);
      setRenderBodyOnly(false);
      String instruction = (String) getDefaultModelObject();
      Label instructionLabel = new Label("instruction", new SpringStringResourceModel(instruction, instruction));
      add(instructionLabel);
    }
  }

  private List<InstructionModel> getInstructionModels(ExperimentalConditionLog experimentalConditionLog) {
    if(experimentalConditionLog == null) return Collections.emptyList();
    List<InstructionModel> instructionModels = new ArrayList<InstructionModel>(experimentalConditionLog.getInstructions().size());
    for(String instruction : experimentalConditionLog.getInstructions()) {
      instructionModels.add(new InstructionModel(instruction));
    }
    return instructionModels;
  }

  private class InstructionModel extends LoadableDetachableModel {

    private static final long serialVersionUID = 1L;

    private String instruction;

    public InstructionModel(String instruction) {
      this.instruction = instruction;
    }

    @Override
    protected Object load() {
      return instruction;
    }

  }

  private List<AttributeModel> getAttributeModels(ExperimentalConditionLog experimentalConditionLog) {
    if(experimentalConditionLog == null) return Collections.emptyList();
    List<AttributeModel> attributeModels = new ArrayList<AttributeModel>(experimentalConditionLog.getAttributes().size());
    for(Attribute attribute : experimentalConditionLog.getAttributes()) {
      attributeModels.add(new AttributeModel(attribute));
    }
    return attributeModels;
  }

  private class AttributeModel extends LoadableDetachableModel {
    private static final long serialVersionUID = 1L;

    private Attribute attribute;

    public AttributeModel(Attribute attribute) {
      this.attribute = attribute;
    }

    @Override
    protected Object load() {
      return attribute;
    }
  }

  @Override
  public boolean isVisible() {
    if(getDefaultModelObject() == null) {
      return false;
    } else {
      return true;
    }
  }

  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

  public void save() {
    experimentalCondition.setTime(new Date());
    experimentalConditionService.save(experimentalCondition);
  }

  public void addInstrument(String barcode) {
    ExperimentalConditionValue experimentalConditionValue = new ExperimentalConditionValue();
    experimentalConditionValue.setAttributeName(ExperimentalConditionService.INSTRUMENT_BARCODE);
    experimentalConditionValue.setAttributeType(DataType.TEXT);
    experimentalCondition.addExperimentalConditionValue(experimentalConditionValue);
    experimentalConditionValue.setExperimentalCondition(experimentalCondition);
    experimentalConditionValue.setData(new Data(DataType.TEXT, barcode));
  }

}
