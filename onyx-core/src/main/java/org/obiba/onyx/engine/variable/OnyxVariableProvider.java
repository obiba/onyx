/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class OnyxVariableProvider implements IVariableProvider {

  private static final String ADMIN = "Admin";

  private static final String PARTICIPANT = "Participant";

  private static final String BARCODE = "barcode";

  private static final String GENDER = "gender";

  private static final String FIRST_NAME = "firstName";

  private static final String LAST_NAME = "lastName";

  private static final String BIRTH_DATE = "birthDate";

  private static final String SITENO = "siteNo";

  private static final String RECRUITMENT_TYPE = "recruitementType";

  public static final String ACTION = "Action";

  public static final String ACTION_ID = "id";

  public static final String ACTION_USER = "user";

  public static final String ACTION_STAGE = "stage";

  public static final String ACTION_TYPE = "actionType";

  public static final String ACTION_DATE_TIME = "dateTime";

  public static final String ACTION_COMMENT = "comment";

  public static final String ACTION_EVENT_REASON = "eventReason";

  private ParticipantMetadata participantMetadata;

  private ParticipantService participantService;

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    VariableData varData = new VariableData(variablePathNamingStrategy.getPath(variable));

    if(variable.getParent().getName().equals(PARTICIPANT)) {
      if(variable.getName().equals(BARCODE)) {
        varData.addData(DataBuilder.buildText(participant.getBarcode()));
      } else if(variable.getName().equals(GENDER)) {
        varData.addData(DataBuilder.buildText(participant.getGender().toString()));
      } else if(variable.getName().equals(FIRST_NAME)) {
        varData.addData(DataBuilder.buildText(participant.getFirstName()));
      } else if(variable.getName().equals(LAST_NAME)) {
        varData.addData(DataBuilder.buildText(participant.getLastName()));
      } else if(variable.getName().equals(BIRTH_DATE)) {
        varData.addData(DataBuilder.buildDate(participant.getBirthDate()));
      } else if(variable.getName().equals(SITENO)) {
        varData.addData(DataBuilder.buildText(participant.getSiteNo()));
      } else if(variable.getName().equals(RECRUITMENT_TYPE)) {
        varData.addData(DataBuilder.buildText(participant.getRecruitmentType().toString()));
      } else {
        // look for it in configured attributes
        Data data = participantService.getConfiguredAttributeValue(participant, variable.getName());
        if(data != null) {
          varData.addData(data);
        }
      }
    } else if(isActionVariable(variable)) {
      varData = getActionVariableData(participant, variable, variablePathNamingStrategy, varData, null);
    }

    return varData;
  }

  public List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<Variable>();

    Variable admin = new Variable(ADMIN);
    variables.add(admin);

    Variable entity = admin.addVariable(new Variable(PARTICIPANT));
    entity.addVariable(new Variable(BARCODE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(GENDER).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(FIRST_NAME).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(LAST_NAME).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(BIRTH_DATE).setDataType(DataType.DATE));
    entity.addVariable(new Variable(SITENO).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(RECRUITMENT_TYPE).setDataType(DataType.TEXT));
    for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
      entity.addVariable(new Variable(attribute.getName()).setDataType(attribute.getType()));
    }

    admin.addVariable(createActionVariable());

    return variables;
  }

  public boolean isActionVariable(Variable variable) {
    return (variable.getParent().getName().equals(ACTION));
  }

  /**
   * Get the action variable data, restricted to a stage or not.
   * @param participant
   * @param variable
   * @param variablePathNamingStrategy
   * @param varData
   * @param stage if null, get all actions
   * @return
   */
  public VariableData getActionVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy, VariableData varData, String stage) {
    List<Action> actions;
    if(stage == null) {
      actions = participantService.getActions(participant);
    } else {
      actions = participantService.getActions(participant, stage);
    }

    for(Action action : actions) {
      Data data = null;
      if(variable.getName().equals(ACTION_ID)) {
        varData.addData(DataBuilder.build(action.getId()));
      } else if(variable.getName().equals(ACTION_USER) && action.getUser() != null) {
        data = DataBuilder.buildText(action.getUser().getLogin());
      } else if(variable.getName().equals(ACTION_STAGE) && action.getStage() != null) {
        data = DataBuilder.buildText(action.getStage());
      } else if(variable.getName().equals(ACTION_TYPE) && action.getActionType() != null) {
        data = DataBuilder.buildText(action.getActionType().toString());
      } else if(variable.getName().equals(ACTION_DATE_TIME) && action.getDateTime() != null) {
        data = DataBuilder.buildDate(action.getDateTime());
      } else if(variable.getName().equals(ACTION_COMMENT) && action.getComment() != null) {
        data = DataBuilder.buildText(action.getComment());
      } else if(variable.getName().equals(ACTION_EVENT_REASON) && action.getEventReason() != null) {
        data = DataBuilder.buildText(action.getEventReason());
      }

      if(data != null) {
        VariableData childVarData = new VariableData(variablePathNamingStrategy.getPath(variable, ACTION_ID, action.getId().toString()));
        varData.addVariableData(childVarData);
        childVarData.addData(data);
      }
    }

    return varData;
  }

  public Variable createActionVariable() {
    Variable actionVariable = new Variable(OnyxVariableProvider.ACTION);
    actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_ID).setDataType(DataType.INTEGER));
    actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_USER).setDataType(DataType.TEXT)).addReference(ACTION_ID);
    actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_STAGE).setDataType(DataType.TEXT)).addReference(ACTION_ID);
    actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_TYPE).setDataType(DataType.TEXT)).addReference(ACTION_ID);
    actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_DATE_TIME).setDataType(DataType.DATE)).addReference(ACTION_ID);
    actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_COMMENT).setDataType(DataType.TEXT)).addReference(ACTION_ID);
    actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_EVENT_REASON).setDataType(DataType.TEXT)).addReference(ACTION_ID);

    return actionVariable;
  }

}
