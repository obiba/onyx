/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.engine.variable.impl;

import java.util.ArrayList;
import java.util.List;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.engine.variable.ITubeToVariableMappingStrategy;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class DefaultTubeToVariableMappingStrategy implements ITubeToVariableMappingStrategy {

  private static final String PARTICIPANT_TUBE_REGISTRATION = "ParticipantTubeRegistration";

  private static final String TIMESTART = "startTime";

  private static final String TIMEEND = "endTime";

  private static final String CONTRAINDICATION = "Contraindication";

  private static final String CONTRAINDICATION_CODE = "code";

  private static final String CONTRAINDICATION_TYPE = "type";

  private static final String OTHER_CONTRAINDICATION = "otherContraindication";

  private static final String REGISTERED_PARTICIPANT_TUBE = "RegisteredParticipantTube";

  private static final String BARCODE = "barcode";

  private static final String REGISTRATION_TIME = "registrationTime";

  private static final String COMMENT = "comment";

  private static final String REMARK_CODE = "remarks";

  private EntityQueryService queryService;

  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public void setTubeRegistrationConfiguration(TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    this.tubeRegistrationConfiguration = tubeRegistrationConfiguration;
  }

  public Variable getParticipantTubeRegistrationVariable() {

    Variable runVariable = new Variable(PARTICIPANT_TUBE_REGISTRATION);

    runVariable.addVariable(new Variable(TIMESTART).setDataType(DataType.DATE));
    runVariable.addVariable(new Variable(TIMEEND).setDataType(DataType.DATE));
    runVariable.addVariable(new Variable(OTHER_CONTRAINDICATION).setDataType(DataType.TEXT));

    Variable ciVariable = runVariable.addVariable(new Variable(CONTRAINDICATION));
    ciVariable.addVariable(new Variable(CONTRAINDICATION_CODE).setDataType(DataType.TEXT));
    ciVariable.addVariable(new Variable(CONTRAINDICATION_TYPE).setDataType(DataType.TEXT));

    return runVariable;
  }

  public Variable getRegisteredParticipantTubeVariable() {

    Variable tubeVariable = new Variable(REGISTERED_PARTICIPANT_TUBE).setDataType(DataType.TEXT).setRepeatable(true);
    tubeVariable.addVariable(new Variable(BARCODE).setDataType(DataType.TEXT).setKey("ruby"));
    tubeVariable.addVariable(new Variable(REGISTRATION_TIME).setDataType(DataType.DATE));
    tubeVariable.addVariable(new Variable(COMMENT).setDataType(DataType.TEXT));
    tubeVariable.addVariable(new Variable(REMARK_CODE).setDataType(DataType.TEXT));

    return tubeVariable;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy, VariableData varData) {

    ParticipantTubeRegistration tubeRegistration = new ParticipantTubeRegistration();
    tubeRegistration.setInterview(participant.getInterview());
    tubeRegistration = queryService.matchOne(tubeRegistration);
    if(tubeRegistration == null) {
      return varData;
    }

    if(variable.getParent().getName().equals(PARTICIPANT_TUBE_REGISTRATION)) {
      if(variable.getName().equals(TIMESTART) && tubeRegistration.getStartTime() != null) {
        varData.addData(DataBuilder.buildDate(tubeRegistration.getStartTime()));
      } else if(variable.getName().equals(TIMEEND) && tubeRegistration.getEndTime() != null) {
        varData.addData(DataBuilder.buildDate(tubeRegistration.getEndTime()));
      } else if(variable.getName().equals(OTHER_CONTRAINDICATION) && tubeRegistration.getOtherContraindication() != null) {
        varData.addData(DataBuilder.buildText(tubeRegistration.getOtherContraindication()));
      }
    } else if(variable.getParent().getName().equals(CONTRAINDICATION)) {
      tubeRegistration.setTubeRegistrationConfig(tubeRegistrationConfiguration);
      Contraindication ci = tubeRegistration.getContraindication();
      if(ci != null) {
        if(variable.getName().equals(CONTRAINDICATION_CODE)) {
          varData.addData(DataBuilder.buildText(ci.getCode()));
        } else if(variable.getName().equals(CONTRAINDICATION_TYPE)) {
          varData.addData(DataBuilder.buildText(ci.getType().toString()));
        }
      }
    } else if(variable.getParent().getName().equals(REGISTERED_PARTICIPANT_TUBE) || variable.getName().equals(REGISTERED_PARTICIPANT_TUBE)) {
      RegisteredParticipantTube template = new RegisteredParticipantTube();
      template.setParticipantTubeRegistration(tubeRegistration);

      for(RegisteredParticipantTube registeredTube : queryService.match(template)) {
        List<Data> datas = new ArrayList<Data>();

        if(variable.getName().equals(REGISTERED_PARTICIPANT_TUBE)) {
          varData.addData(DataBuilder.buildText(registeredTube.getId().toString()));
        } else if(variable.getName().equals(BARCODE)) {
          datas.add(DataBuilder.build(registeredTube.getBarcode()));
        } else if(variable.getName().equals(COMMENT) && registeredTube.getComment() != null) {
          datas.add(DataBuilder.buildText(registeredTube.getComment()));
        } else if(variable.getName().equals(REGISTRATION_TIME) && registeredTube.getRegistrationTime() != null) {
          datas.add(DataBuilder.buildDate(registeredTube.getRegistrationTime()));
        } else if(variable.getName().equals(REMARK_CODE)) {
          for(String code : registeredTube.getRemarks()) {
            datas.add(DataBuilder.buildText(code));
          }
        }

        if(datas.size() > 0) {
          VariableData childVarData = new VariableData(variablePathNamingStrategy.getPath(variable, REGISTERED_PARTICIPANT_TUBE, registeredTube.getId().toString()));
          varData.addVariableData(childVarData);
          for(Data data : datas) {
            childVarData.addData(data);
          }
        }
      }
    }

    return varData;
  }

}
