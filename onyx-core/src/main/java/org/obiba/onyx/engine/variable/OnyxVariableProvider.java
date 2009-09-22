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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.stage.IStageInstanceAlgorithm;
import org.obiba.onyx.core.domain.stage.StageInstance;
import org.obiba.onyx.core.domain.stage.StageTransition;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.InterviewService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Onyx system variable provider: participant, user, action, interview related variables.
 */
public class OnyxVariableProvider implements IVariableProvider {
  //
  // Constants
  //

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(OnyxVariableProvider.class);

  public static final String ADMIN = "Admin";

  public static final String ONYX_VERSION = "onyxVersion";

  public static final String APPLICATION_CONFIGURATION = "ApplicationConfiguration";

  public static final String SITE_CODE = "siteCode";

  public static final String SITE_NAME = "siteName";

  public static final String STUDY_NAME = "studyName";

  public static final String PARTICIPANT = "Participant";

  public static final String BARCODE = "barcode";

  public static final String ENROLLMENT_ID = "enrollmentId";

  public static final String APPOINTMENT_DATE = "appointmentDate";

  public static final String GENDER = "gender";

  public static final String FIRST_NAME = "firstName";

  public static final String LAST_NAME = "lastName";

  public static final String FULL_NAME = "fullName";

  public static final String BIRTH_DATE = "birthDate";

  public static final String BIRTH_YEAR = "birthYear";

  public static final String AGE = "age";

  public static final String SITENO = "siteNo";

  public static final String RECRUITMENT_TYPE = "recruitementType";

  public static final String INTERVIEW = "Interview";

  public static final String START_DATE = "startDate";

  public static final String END_DATE = "endDate";

  public static final String INTERVIEW_STATUS = "status";

  public static final String ACTION = "Action";

  public static final String ACTION_KEY = "action";

  public static final String ACTION_USER = "user";

  public static final String ACTION_STAGE = "stage";

  public static final String ACTION_FROM_STATE = "fromState";

  public static final String ACTION_TO_STATE = "toState";

  public static final String ACTION_TYPE = "actionType";

  public static final String ACTION_DATE_TIME = "dateTime";

  public static final String ACTION_COMMENT = "comment";

  public static final String ACTION_EVENT_REASON = "eventReason";

  public static final String STAGE_INSTANCE = "StageInstance";

  public static final String STAGE = "stage";

  public static final String START_TIME = "startTime";

  public static final String LAST_TIME = "lastTime";

  public static final String LAST_STATE = "lastState";

  public static final String DURATION = "duration";

  public static final String INTERRUPTION_COUNT = "interruptionCount";

  public static final String USER = "user";

  public static final String LAST = "last";

  //
  // Instance Variables
  //

  private ParticipantMetadata participantMetadata;

  private ParticipantService participantService;

  private ApplicationConfigurationService applicationConfigurationService;

  private Version version;

  private VariableHelper variableHelper;

  private ModuleRegistry moduleRegistry;

  private InterviewService interviewService;

  private IStageInstanceAlgorithm stageInstanceAlgorithm;

  //
  // Methods
  //

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
    this.applicationConfigurationService = applicationConfigurationService;
  }

  public void setVariableHelper(VariableHelper variableHelper) {
    this.variableHelper = variableHelper;
  }

  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    this.moduleRegistry = moduleRegistry;
  }

  public void setInterviewService(InterviewService interviewService) {
    this.interviewService = interviewService;
  }

  public void setStageInstanceAlgorithm(IStageInstanceAlgorithm stageInstanceAlgorithm) {
    this.stageInstanceAlgorithm = stageInstanceAlgorithm;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    VariableData varData = new VariableData(variablePathNamingStrategy.getPath(variable));

    if(variable.getParent().getName().equals(PARTICIPANT)) {
      if(variable.getName().equals(BARCODE)) {
        varData.addData(DataBuilder.buildText(participant.getBarcode()));
      } else if(variable.getName().equals(ENROLLMENT_ID)) {
        varData.addData(DataBuilder.buildText(participant.getEnrollmentId()));
      } else if(variable.getName().equals(APPOINTMENT_DATE) && participant.getAppointment() != null) {
        varData.addData(DataBuilder.buildDate(participant.getAppointment().getDate()));
      } else if(variable.getName().equals(GENDER)) {
        varData.addData(DataBuilder.buildText(participant.getGender().toString()));
      } else if(variable.getName().equals(FIRST_NAME)) {
        varData.addData(DataBuilder.buildText(participant.getFirstName()));
      } else if(variable.getName().equals(LAST_NAME)) {
        varData.addData(DataBuilder.buildText(participant.getLastName()));
      } else if(variable.getName().equals(FULL_NAME)) {
        varData.addData(DataBuilder.buildText(participant.getFullName()));
      } else if(variable.getName().equals(BIRTH_DATE)) {
        if(participant.getBirthDate() != null) {
          varData.addData(DataBuilder.buildDate(participant.getBirthDate()));
        }
      } else if(variable.getName().equals(BIRTH_YEAR)) {
        if(participant.getBirthDate() != null) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(participant.getBirthDate());
          varData.addData(DataBuilder.buildInteger((long) cal.get(Calendar.YEAR)));
        }
      } else if(variable.getName().equals(AGE)) {
        varData.addData(DataBuilder.buildInteger(participant.getAge()));
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
        } else if(variable.getName().equals(INTERVIEW_STATUS) && interview.getStatus() != null) {
          varData.addData(DataBuilder.buildText(interview.getStatus().toString()));
        } else if(variable.getName().equals(DURATION) && interview.getDuration() != null) {
          varData.addData(DataBuilder.buildInteger(interview.getDuration()));
        }
      }
    } else if(variable.getName().equals(ONYX_VERSION)) {
      varData.addData(DataBuilder.buildText(version.toString()));
    } else if(variable.getParent().getName().equals(APPLICATION_CONFIGURATION)) {

      if(applicationConfigurationService.getApplicationConfiguration() != null) {
        ApplicationConfiguration appConfig = applicationConfigurationService.getApplicationConfiguration();

        if(variable.getName().equals(SITE_CODE) && appConfig.getSiteNo() != null) {
          varData.addData(DataBuilder.buildText(appConfig.getSiteNo()));
        } else if(variable.getName().equals(SITE_NAME) && appConfig.getSiteName() != null) {
          varData.addData(DataBuilder.buildText(appConfig.getSiteName()));
        } else if(variable.getName().equals(STUDY_NAME) && appConfig.getStudyName() != null) {
          varData.addData(DataBuilder.buildText(appConfig.getStudyName()));
        }
      }
    } else if(variable.getParent().getName().equals(ACTION) || variable.getName().equals(ACTION)) {
      List<Action> actions = participantService.getActions(participant);

      for(Action action : actions) {
        Data data = null;
        if(variable.getName().equals(ACTION)) {
          varData.addData(DataBuilder.buildText(action.getId().toString()));
        } else if(variable.getName().equals(ACTION_USER) && action.getUser() != null) {
          data = DataBuilder.buildText(action.getUser().getLogin());
        } else if(variable.getName().equals(ACTION_STAGE) && action.getStage() != null) {
          data = DataBuilder.buildText(action.getStage());
        } else if(variable.getName().equals(ACTION_FROM_STATE) && action.getFromState() != null) {
          data = DataBuilder.buildText(action.getFromState().toString());
        } else if(variable.getName().equals(ACTION_TO_STATE) && action.getToState() != null) {
          data = DataBuilder.buildText(action.getToState().toString());
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
          VariableData childVarData = new VariableData(variablePathNamingStrategy.getPath(variable, ACTION, action.getId().toString()));
          varData.addVariableData(childVarData);
          childVarData.addData(data);
        }
      }
    } else if(variable.getParent().getName().equals(STAGE_INSTANCE) || variable.getName().equals(STAGE_INSTANCE)) {
      // TODO: Comment this out until IStageInstanceAlgorithm has been implemented.
      // addStageInstanceVariableData(participant, variable, variablePathNamingStrategy, varData);
    }

    return varData;
  }

  public List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<Variable>();

    Variable admin = new Variable(ADMIN);
    variables.add(admin);

    admin.addVariable(new Variable(ONYX_VERSION).setDataType(DataType.TEXT));

    Variable entity = admin.addVariable(new Variable(APPLICATION_CONFIGURATION));
    entity.addVariable(new Variable(SITE_CODE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(SITE_NAME).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(STUDY_NAME).setDataType(DataType.TEXT));

    entity = admin.addVariable(new Variable(PARTICIPANT));
    entity.addVariable(new Variable(BARCODE).setDataType(DataType.TEXT).setKey("onyx"));
    entity.addVariable(new Variable(ENROLLMENT_ID).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(APPOINTMENT_DATE).setDataType(DataType.DATE));
    entity.addVariable(new Variable(GENDER).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(FIRST_NAME).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(LAST_NAME).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(FULL_NAME).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(BIRTH_DATE).setDataType(DataType.DATE));
    entity.addVariable(new Variable(BIRTH_YEAR).setDataType(DataType.INTEGER));
    entity.addVariable(new Variable(AGE).setDataType(DataType.INTEGER));
    entity.addVariable(new Variable(SITENO).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(RECRUITMENT_TYPE).setDataType(DataType.TEXT));
    for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
      Variable var = entity.addVariable(new Variable(attribute.getName()).setDataType(attribute.getType()));
      addLocalizedAttributes(var);
      if(attribute.getGroup() != null) {
        VariableHelper.addGroupAttribute(var, attribute.getGroup().getName());
      }
    }

    entity = admin.addVariable(new Variable(INTERVIEW));
    entity.addVariable(new Variable(START_DATE).setDataType(DataType.DATE));
    entity.addVariable(new Variable(END_DATE).setDataType(DataType.DATE));
    entity.addVariable(new Variable(INTERVIEW_STATUS).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(DURATION).setDataType(DataType.INTEGER).setUnit("s"));

    entity = admin.addVariable(new Variable(OnyxVariableProvider.ACTION).setDataType(DataType.TEXT).setRepeatable(true));
    entity.addVariable(new Variable(OnyxVariableProvider.ACTION_USER).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(OnyxVariableProvider.ACTION_STAGE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(OnyxVariableProvider.ACTION_FROM_STATE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(OnyxVariableProvider.ACTION_TO_STATE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(OnyxVariableProvider.ACTION_TYPE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(OnyxVariableProvider.ACTION_DATE_TIME).setDataType(DataType.DATE));
    entity.addVariable(new Variable(OnyxVariableProvider.ACTION_COMMENT).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(OnyxVariableProvider.ACTION_EVENT_REASON).setDataType(DataType.TEXT));

    entity = admin.addVariable(new Variable(OnyxVariableProvider.STAGE_INSTANCE).setDataType(DataType.TEXT).setRepeatable(true));
    entity.addVariable(new Variable(OnyxVariableProvider.STAGE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(OnyxVariableProvider.START_TIME).setDataType(DataType.DATE));
    entity.addVariable(new Variable(OnyxVariableProvider.LAST_TIME).setDataType(DataType.DATE));
    entity.addVariable(new Variable(OnyxVariableProvider.LAST_STATE).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(OnyxVariableProvider.DURATION).setDataType(DataType.INTEGER).setUnit("s"));
    entity.addVariable(new Variable(OnyxVariableProvider.INTERRUPTION_COUNT).setDataType(DataType.INTEGER));
    entity.addVariable(new Variable(OnyxVariableProvider.USER).setDataType(DataType.TEXT));
    entity.addVariable(new Variable(OnyxVariableProvider.LAST).setDataType(DataType.BOOLEAN));

    return variables;
  }

  private void addLocalizedAttributes(Variable variable) {
    if(variableHelper != null) {
      variableHelper.addLocalizedAttributes(variable);
    }
  }

  public List<Variable> getContributedVariables(Variable root, IVariablePathNamingStrategy variablePathNamingStrategy) {
    return null;
  }

  private void addStageInstanceVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy, VariableData varData) {
    for(Module module : moduleRegistry.getModules()) {
      for(Stage stage : module.getStages()) {
        List<StageTransition> stageTransitions = interviewService.getStageTransitions(participant.getInterview(), stage);

        List<StageInstance> stageInstances = stageInstanceAlgorithm.getStageInstances(stageTransitions);
        for(int i = 1; i <= stageInstances.size(); i++) {
          StageInstance stageInstance = stageInstances.get(i);
          String stageInstanceId = String.valueOf(i);
          Data data = null;

          if(variable.getName().equals(STAGE_INSTANCE)) {
            varData.addData(DataBuilder.buildText(stageInstanceId));
          } else if(variable.getName().equals(STAGE) && stageInstance.getStage() != null) {
            data = DataBuilder.buildText(stageInstance.getStage());
          } else if(variable.getName().equals(START_TIME) && stageInstance.getStartTime() != null) {
            data = DataBuilder.buildDate(stageInstance.getStartTime());
          } else if(variable.getName().equals(LAST_TIME) && stageInstance.getLastTime() != null) {
            data = DataBuilder.buildDate(stageInstance.getLastTime());
          } else if(variable.getName().equals(LAST_STATE) && stageInstance.getLastState() != null) {
            data = DataBuilder.buildText(stageInstance.getLastState().toString());
          } else if(variable.getName().equals(DURATION)) {
            data = DataBuilder.buildInteger(stageInstance.getDuration());
          } else if(variable.getName().equals(INTERRUPTION_COUNT)) {
            data = DataBuilder.buildInteger(stageInstance.getInterruptionCount());
          } else if(variable.getName().equals(USER) && stageInstance.getUser() != null) {
            data = DataBuilder.buildInteger(stageInstance.getUser().getLogin());
          } else if(variable.getName().equals(LAST)) {
            data = DataBuilder.buildBoolean(stageInstance.isLast());
          }

          if(data != null) {
            VariableData childVarData = new VariableData(variablePathNamingStrategy.getPath(variable, STAGE_INSTANCE, stageInstanceId));
            varData.addVariableData(childVarData);
            childVarData.addData(data);
          }
        }
      }
    }
  }
}
