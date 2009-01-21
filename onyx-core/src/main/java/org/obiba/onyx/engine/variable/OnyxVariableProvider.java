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
import java.util.Date;
import java.util.List;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Onyx system variable provider: participant, user, action, interview related variables.
 */
public class OnyxVariableProvider implements IVariableProvider, IActionVariableProvider {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(OnyxVariableProvider.class);

  public static final String ADMIN = "Admin";

  public static final String PARTICIPANT = "Participant";

  public static final String PARTICIPANT_KEY = "participant";

  public static final String BARCODE = "barcode";

  public static final String GENDER = "gender";

  public static final String FIRST_NAME = "firstName";

  public static final String LAST_NAME = "lastName";

  public static final String BIRTH_DATE = "birthDate";

  public static final String SITENO = "siteNo";

  public static final String RECRUITMENT_TYPE = "recruitementType";

  public static final String INTERVIEW = "Interview";

  public static final String START_DATE = "startDate";

  public static final String END_DATE = "endDate";

  public static final String INTERVIEW_USER = "user";

  public static final String INTERVIEW_STATUS = "status";

  public static final String ACTION = "Action";

  public static final String ACTION_KEY = "action";

  public static final String ACTIONS = "actions";

  public static final String ACTION_USER = "user";

  public static final String ACTION_STAGE = "stage";

  public static final String ACTION_TYPE = "actionType";

  public static final String ACTION_DATE_TIME = "dateTime";

  public static final String ACTION_COMMENT = "comment";

  public static final String ACTION_EVENT_REASON = "eventReason";

  public static final String USER = "User";

  public static final String USER_KEY = "user";

  public static final String USER_LOGIN = "login";

  public static final String USER_EMAIL = "email";

  public static final String USER_STATUS = "status";

  public static final String USER_LANGUAGE = "language";

  public static final String USER_ROLES = "roles";

  private ParticipantMetadata participantMetadata;

  private ParticipantService participantService;

  private EntityQueryService queryService;

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
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
    } else if(variable.getParent().getName().equals(INTERVIEW)) {
      if(participant.getInterview() != null) {
        Interview interview = participant.getInterview();
        if(variable.getName().equals(START_DATE) && interview.getStartDate() != null) {
          varData.addData(DataBuilder.buildDate(interview.getStartDate()));
        } else if(variable.getName().equals(END_DATE) && interview.getEndDate() != null) {
          varData.addData(DataBuilder.buildDate(interview.getEndDate()));
        } else if(variable.getName().equals(INTERVIEW_USER) && interview.getUser() != null) {
          varData.addData(DataBuilder.buildText(interview.getUser().getLogin()));
        } else if(variable.getName().equals(INTERVIEW_STATUS) && interview.getStatus() != null) {
          varData.addData(DataBuilder.buildText(interview.getStatus().toString()));
        }
      }
    } else if(isActionVariable(variable)) {
      varData = getActionVariableData(participant, variable, variablePathNamingStrategy, varData, null);
    } else if(variable.getParent().getName().equals(USER)) {

      User template = new User();
      template.setDeleted(false);

      for(User user : queryService.match(template)) {
        List<Data> datas = new ArrayList<Data>();

        if(variable.getName().equals(USER_LOGIN)) {
          varData.addData(DataBuilder.buildText(user.getLogin()));
        } else if(variable.getName().equals(FIRST_NAME)) {
          datas.add(DataBuilder.buildText(user.getFirstName()));
        } else if(variable.getName().equals(LAST_NAME)) {
          datas.add(DataBuilder.buildText(user.getLastName()));
        } else if(variable.getName().equals(USER_EMAIL) && user.getEmail() != null) {
          datas.add(DataBuilder.buildText(user.getEmail()));
        } else if(variable.getName().equals(USER_LANGUAGE) && user.getLanguage() != null) {
          datas.add(DataBuilder.buildText(user.getLanguage().toString()));
        } else if(variable.getName().equals(USER_STATUS) && user.getStatus() != null) {
          datas.add(DataBuilder.buildText(user.getStatus().toString()));
        } else if(variable.getName().equals(USER_ROLES) && user.getRoles().size() > 0) {
          for(Role role : user.getRoles()) {
            datas.add(DataBuilder.buildText(role.getName()));
          }
        }

        if(datas.size() > 0) {
          VariableData childVarData = new VariableData(variablePathNamingStrategy.getPath(variable, USER_KEY, user.getLogin()));
          varData.addVariableData(childVarData);
          for(Data data : datas) {
            childVarData.addData(data);
          }
        }
      }

    }

    return varData;
  }

  public List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<Variable>();

    Variable admin = new Variable(ADMIN);
    variables.add(admin);

    Variable entity = admin.addVariable(new Variable(PARTICIPANT));
    entity.addVariable(new Variable(BARCODE).setDataType(DataType.TEXT).setKey(PARTICIPANT_KEY));
    entity.addVariable(new Variable(GENDER).setDataType(DataType.TEXT)).addReference(PARTICIPANT_KEY);
    entity.addVariable(new Variable(FIRST_NAME).setDataType(DataType.TEXT)).addReference(PARTICIPANT_KEY);
    entity.addVariable(new Variable(LAST_NAME).setDataType(DataType.TEXT)).addReference(PARTICIPANT_KEY);
    entity.addVariable(new Variable(BIRTH_DATE).setDataType(DataType.DATE)).addReference(PARTICIPANT_KEY);
    entity.addVariable(new Variable(SITENO).setDataType(DataType.TEXT)).addReference(PARTICIPANT_KEY);
    entity.addVariable(new Variable(RECRUITMENT_TYPE).setDataType(DataType.TEXT)).addReference(PARTICIPANT_KEY);
    for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
      entity.addVariable(new Variable(attribute.getName()).setDataType(attribute.getType())).addReference(PARTICIPANT_KEY);
    }

    entity = admin.addVariable(new Variable(INTERVIEW));
    entity.addVariable(new Variable(START_DATE).setDataType(DataType.DATE));
    entity.addVariable(new Variable(END_DATE).setDataType(DataType.DATE));
    entity.addVariable(new Variable(INTERVIEW_USER).setDataType(DataType.TEXT)).setKey(USER_KEY);
    entity.addVariable(new Variable(INTERVIEW_STATUS).setDataType(DataType.TEXT));

    admin.addVariable(createActionVariable(false));

    entity = admin.addVariable(new Variable(USER));
    entity.addVariable(new Variable(USER_LOGIN).setDataType(DataType.TEXT).setKey(USER_KEY));
    entity.addVariable(new Variable(FIRST_NAME).setDataType(DataType.TEXT)).addReference(USER_KEY);
    entity.addVariable(new Variable(LAST_NAME).setDataType(DataType.TEXT)).addReference(USER_KEY);
    entity.addVariable(new Variable(USER_EMAIL).setDataType(DataType.TEXT)).addReference(USER_KEY);
    entity.addVariable(new Variable(USER_LANGUAGE).setDataType(DataType.TEXT)).addReference(USER_KEY);
    entity.addVariable(new Variable(USER_STATUS).setDataType(DataType.TEXT)).addReference(USER_KEY);
    entity.addVariable(new Variable(USER_ROLES).setDataType(DataType.TEXT)).addReference(USER_KEY);

    return variables;
  }

  public boolean isActionVariable(Variable variable) {
    return variable.getName().equals(ACTIONS) || variable.getParent().getName().equals(ACTION);
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
    if(!isActionVariable(variable)) {
      throw new IllegalArgumentException("Not a Action variable: " + variablePathNamingStrategy.getPath(variable));
    }

    List<Action> actions;
    if(stage == null) {
      actions = participantService.getActions(participant);
    } else {
      actions = participantService.getActions(participant, stage);
    }

    for(Action action : actions) {
      Data data = null;
      if(variable.getName().equals(ACTIONS)) {
        varData.addData(DataBuilder.build(action.getId()));
      } else if(variable.getName().equals(ACTION_USER) && action.getUser() != null) {
        data = DataBuilder.buildText(action.getUser().getLogin());
      } else if(variable.getName().equals(ACTION_STAGE) && action.getStage() != null) {
        data = DataBuilder.buildText(action.getStage());
      } else if(variable.getName().equals(ACTION_TYPE) && action.getActionType() != null) {
        data = DataBuilder.buildText(action.getActionType().toString());
      } else if(variable.getName().equals(ACTION_DATE_TIME) && action.getDateTime() != null) {
        // make a copy of the date as it will be dumped several times and we do not want xstream to reference it
        data = DataBuilder.buildDate((Date) action.getDateTime().clone());
      } else if(variable.getName().equals(ACTION_COMMENT) && action.getComment() != null) {
        data = DataBuilder.buildText(action.getComment());
      } else if(variable.getName().equals(ACTION_EVENT_REASON) && action.getEventReason() != null) {
        data = DataBuilder.buildText(action.getEventReason());
      }

      if(data != null) {
        VariableData childVarData = new VariableData(variablePathNamingStrategy.getPath(variable, ACTION_KEY, action.getId().toString()));
        varData.addVariableData(childVarData);
        childVarData.addData(data);
      }
    }

    return varData;
  }

  public Variable createActionVariable(boolean primaryKeyOnly) {
    Variable actionVariable;

    if(primaryKeyOnly) {
      actionVariable = new Variable(OnyxVariableProvider.ACTIONS).setDataType(DataType.INTEGER).setKey(ACTION_KEY);
    } else {
      actionVariable = new Variable(OnyxVariableProvider.ACTION);
      actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTIONS).setDataType(DataType.INTEGER).setKey(ACTION_KEY));
      actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_USER).setDataType(DataType.TEXT)).setKey(USER_KEY).addReference(ACTION_KEY);
      actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_STAGE).setDataType(DataType.TEXT)).addReference(ACTION_KEY);
      actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_TYPE).setDataType(DataType.TEXT)).addReference(ACTION_KEY);
      actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_DATE_TIME).setDataType(DataType.DATE)).addReference(ACTION_KEY);
      actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_COMMENT).setDataType(DataType.TEXT)).addReference(ACTION_KEY);
      actionVariable.addVariable(new Variable(OnyxVariableProvider.ACTION_EVENT_REASON).setDataType(DataType.TEXT)).addReference(ACTION_KEY);
    }

    return actionVariable;
  }

}
