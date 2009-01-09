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

  private static final String ACTION = "Action";

  private static final String USER = "user";

  private static final String STAGE = "stage";

  private static final String ACTION_TYPE = "actionType";

  private static final String DATE_TIME = "dateTime";

  private static final String COMMENT = "comment";

  private static final String EVENT_REASON = "eventReason";

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
    } else if(variable.getParent().getName().equals(ACTION)) {
      for(Action action : participantService.getActions(participant)) {
        if(variable.getName().equals(USER)) {
          varData.addData(DataBuilder.buildText(action.getUser().getLogin()));
        } else if(variable.getName().equals(STAGE)) {
          varData.addData(DataBuilder.buildText(action.getStage()));
        } else if(variable.getName().equals(ACTION_TYPE)) {
          varData.addData(DataBuilder.buildText(action.getActionType().toString()));
        } else if(variable.getName().equals(DATE_TIME)) {
          varData.addData(DataBuilder.buildDate(action.getDateTime()));
        } else if(variable.getName().equals(COMMENT) && action.getComment() != null) {
          varData.addData(DataBuilder.buildText(action.getComment()));
        } else if(variable.getName().equals(EVENT_REASON) && action.getEventReason() != null) {
          varData.addData(DataBuilder.buildText(action.getEventReason()));
        }
      }
    }

    return varData;
  }

  public List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<Variable>();

    Variable admin = new Variable(ADMIN);
    variables.add(admin);

    Variable entity = new Variable(PARTICIPANT);
    admin.addVariable(entity);
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

    entity = new Variable(ACTION);
    admin.addVariable(entity);
    entity.addVariable(new Variable(USER).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(STAGE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(ACTION_TYPE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(DATE_TIME).setDataType(DataType.DATE));
    entity.addVariable(new Variable(COMMENT).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(EVENT_REASON).setDataType(DataType.TEXT));

    return variables;
  }

}
