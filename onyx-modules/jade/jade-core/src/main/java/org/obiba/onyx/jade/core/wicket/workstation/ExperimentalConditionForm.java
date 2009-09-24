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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

public class ExperimentalConditionForm extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  @SpringBean
  private UserSessionService userSessionService;

  private FeedbackWindow feedbackWindow;

  private ExperimentalCondition experimentalCondition;

  public ExperimentalConditionForm(String id, IModel model) {
    super(id, model);
    add(new AttributeModifier("class", true, new Model("experimental-condition-form")));

    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    addComponents();
  }

  public void addComponents() {

    ExperimentalConditionLog experimentalConditionLog = (ExperimentalConditionLog) getDefaultModelObject();

    experimentalCondition = new ExperimentalCondition();
    experimentalCondition.setName(experimentalConditionLog != null ? experimentalConditionLog.getName() : "");
    experimentalCondition.setUser(userSessionService.getUser());
    experimentalCondition.setWorkstation("fake workstation"); // TODO Add real workstation code.

    final List<InstructionModel> instructionModels = getInstructionModels(experimentalConditionLog);
    Loop instructionsLoop = new Loop("instructionList", experimentalConditionLog != null ? experimentalConditionLog.getInstructions().size() : 0) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(LoopItem item) {
        item.setRenderBodyOnly(true);
        item.add(new InstructionFragment("instructionRows", "instructionFragment", ExperimentalConditionForm.this, instructionModels.get(item.getIteration())));
      }
    };
    addOrReplace(instructionsLoop);

    final List<AttributeModel> attributeModels = getAttributeModels(experimentalConditionLog);
    Loop attributeLoop = new Loop("table", experimentalConditionLog != null ? experimentalConditionLog.getAttributes().size() : 0) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(LoopItem item) {
        item.setRenderBodyOnly(true);
        item.add(new TextFieldFragment("inputRows", "textFieldFragment", ExperimentalConditionForm.this, attributeModels.get(item.getIteration())));
      }
    };
    if(getDefaultModelObject() == null) setVisible(false);
    addOrReplace(attributeLoop);
  }

  private class TextFieldFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public TextFieldFragment(String id, String markupId, MarkupContainer markupContainer, IModel model) {
      super(id, markupId, markupContainer, model);
      Attribute attribute = (Attribute) getDefaultModelObject();

      ExperimentalConditionValue experimentalConditionValue = new ExperimentalConditionValue();
      experimentalConditionValue.setAttributeName(attribute.getName());
      experimentalConditionValue.setAttributeType(attribute.getType());
      experimentalCondition.addExperimentalConditionValue(experimentalConditionValue);
      experimentalConditionValue.setExperimentalCondition(experimentalCondition);

      IModel experimentalConditionValueModel = new Model(experimentalConditionValue);

      DataField formComponent = new DataField("value", new PropertyModel(experimentalConditionValueModel, "data"), attribute.getType());
      formComponent.setRequired(true);
      formComponent.setLabel(new Model(attribute.getName()));
      if(attribute.getType().equals(DataType.TEXT)) {
        formComponent.add(new StringValidator.MaximumLengthValidator(250));
      }
      for(IValidator validator : attribute.getValidators()) {
        formComponent.add(validator);
      }

      add(new Label("label", new ResourceModel(attribute.getName(), attribute.getName())));
      // TODO: Add the brackets... hide if no units.
      add(new Label("unit", new Model<String>(attribute.getUnit())));
      add(formComponent);

    }
  }

  private class InstructionFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public InstructionFragment(String id, String markupId, MarkupContainer markupContainer, IModel model) {
      super(id, markupId, markupContainer, model);
      setRenderBodyOnly(false);
      String instruction = (String) getDefaultModelObject();
      Label instructionLabel = new Label("instruction", new ResourceModel(instruction, instruction));
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

}
