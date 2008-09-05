package org.obiba.onyx.jade.instrument.atcor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.atcor.dao.SphygmoCorDao;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SphygmoCorInstrumentRunner implements InstrumentRunner {

  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(SphygmoCorInstrumentRunner.class);

  private static final String SYSTEM_ID = "01400";

  private static final String STUDY_ID = "DATA";

  //
  // Instance variables
  //

  private ExternalAppLauncherHelper externalAppHelper;

  private InstrumentExecutionService instrumentExecutionService;

  private SphygmoCorDao sphygmoCorDao;

  private String participantID;
  
  private String participantLastName;
  
  private String participantFirstName;
  
  private Date participantBirthDate;
  
  private String participantGender;

  //
  // Methods
  //

  public void initialize() {
    // First, remove any data (patients and measurements) currently in the AtCor database.
    sphygmoCorDao.deleteAllOutput();
    sphygmoCorDao.deleteAllPatients();

    // Fetch the current participant's data.
    participantID = instrumentExecutionService.getParticipantID();
    participantLastName = instrumentExecutionService.getParticipantLastName();
    participantFirstName = instrumentExecutionService.getParticipantFirstName();
    participantBirthDate = instrumentExecutionService.getParticipantBirthDate();
    participantGender = instrumentExecutionService.getParticipantGender();

    // Use the participant data to create a new patient in the AtCor database.
    // NOTE: Populate the PATIENT_ID field (VARCHAR) with the participant's barcode,
    // and the PATIENT_NO field (INTEGER) with the number 1 (since there can never
    // be more than one patient at a time, we can always use the same number).
    sphygmoCorDao.addPatient(SYSTEM_ID, STUDY_ID, participantID, 1, participantLastName, participantFirstName, new java.sql.Date(participantBirthDate.getTime()), participantGender);
  }

  public void run() {
    log.info("*** Running SphygmoCor Runner ***");

    // Launch the SphygmoCor software.
    externalAppHelper.launch();

    // Retrieve the output (measurements taken for the current participant).
    // NOTE: The getOutput method returns the output as a List of Maps. There
    // *should* only be one Map, corresponding to the single run.
    List output = sphygmoCorDao.getOutput(SYSTEM_ID, STUDY_ID, 1);

    if(output != null) {
      // Send the data to the server.
      if(output.size() > 1) {
        log.warn("Multiple device outputs found; sending first one");
        sendDataToServer((Map) output.get(0));
      }

      sendDataToServer((Map) output.get(0));

    } else {
      String errMsg = "No device output found";
      log.error(errMsg);

      throw new RuntimeException(errMsg);
    }
  }

  public void shutdown() {
    log.info("*** Shutdown SphygmoCor Runner ***");

    // Delete locally stored participant data.
    sphygmoCorDao.deleteAllOutput();
    sphygmoCorDao.deleteAllPatients();
  }

  private void sendDataToServer(Map data) {

    Map<String, Data> outputToSend = new HashMap<String, Data>();

    outputToSend.put("Peripheral_Pulse_Quality_Control_Pulse_Height", new Data(DataType.DECIMAL, (Double) data.get("P_QC_PH")));
    outputToSend.put("Peripheral_Pulse_Quality_Control_Pulse_Height_Variation", new Data(DataType.DECIMAL, (Double) data.get("P_QC_PHV")));
    outputToSend.put("Peripheral_Pulse_Quality_Control_Pulse_Length_Variation", new Data(DataType.DECIMAL, (Double) data.get("P_QC_PLV")));
    outputToSend.put("Peripheral_Pulse_Quality_Control_Diastolic_Variation", new Data(DataType.DECIMAL, (Double) data.get("P_QC_DV")));
    outputToSend.put("Peripheral_Systolic_Pressure", new Data(DataType.DECIMAL, (Double) data.get("P_SP")));
    outputToSend.put("Peripheral_Diastolic_Pressure", new Data(DataType.DECIMAL, (Double) data.get("P_DP")));
    outputToSend.put("Peripheral_Mean_Pressure", new Data(DataType.DECIMAL, (Double) data.get("P_MEANP")));
    outputToSend.put("Peripheral_T1", new Data(DataType.DECIMAL, (Double) data.get("P_T1")));
    outputToSend.put("Peripheral_T2", new Data(DataType.DECIMAL, (Double) data.get("P_T2")));
    outputToSend.put("Peripheral_Augmentation_Index", new Data(DataType.DECIMAL, (Double) data.get("P_AI")));
    outputToSend.put("End_Systolic_Pressure", new Data(DataType.DECIMAL, (Double) data.get("P_ESP")));
    outputToSend.put("Peripheral_P1", new Data(DataType.DECIMAL, (Double) data.get("P_P1")));
    outputToSend.put("Peripheral_P2", new Data(DataType.DECIMAL, (Double) data.get("P_P2")));
    outputToSend.put("Peripheral_Confidence_Level_of_T1", new Data(DataType.INTEGER, (Integer) data.get("P_QUALITY_T1")));
    outputToSend.put("Peripheral_Confidence_Level_of_T2", new Data(DataType.INTEGER, (Integer) data.get("P_QUALITY_T2")));
    outputToSend.put("Central_Augmented_Pressure", new Data(DataType.DECIMAL, (Double) data.get("C_AP")));
    outputToSend.put("Central_Mean_Pressure_of_Systole", new Data(DataType.DECIMAL, (Double) data.get("C_MPS")));
    outputToSend.put("Central_Mean_Pressure_of_Diastole", new Data(DataType.DECIMAL, (Double) data.get("C_MPD")));
    outputToSend.put("Central_Tension_Time_Index", new Data(DataType.DECIMAL, (Double) data.get("C_TTI")));
    outputToSend.put("Central_Diastolic_Time_Index", new Data(DataType.DECIMAL, (Double) data.get("C_DTI")));
    outputToSend.put("Central_Buckberg_SubEndocardial_Viability_Ratio", new Data(DataType.DECIMAL, (Double) data.get("C_SVI")));
    outputToSend.put("Augmentation_Load", new Data(DataType.DECIMAL, (Double) data.get("C_AL")));
    outputToSend.put("Augmentation_Time_Index", new Data(DataType.DECIMAL, (Double) data.get("C_ATI")));
    outputToSend.put("Heart_Rate", new Data(DataType.DECIMAL, (Double) data.get("HR")));
    outputToSend.put("Central_Pulse_Period", new Data(DataType.DECIMAL, (Double) data.get("C_PERIOD")));
    outputToSend.put("Central_Diastolic_Duration", new Data(DataType.DECIMAL, (Double) data.get("C_DD")));
    outputToSend.put("Central_ED_Period_Percent_Ejection_duration", new Data(DataType.DECIMAL, (Double) data.get("C_ED_PERIOD")));
    outputToSend.put("Period_ED_Period_Percent", new Data(DataType.DECIMAL, (Double) data.get("C_DD_PERIOD")));
    outputToSend.put("Central_Pulse_Height", new Data(DataType.DECIMAL, (Double) data.get("C_PH")));
    outputToSend.put("Central_Aug_PH_Percent", new Data(DataType.DECIMAL, (Double) data.get("C_AGPH")));
    outputToSend.put("Central_Pressure_at_T1_Dp", new Data(DataType.DECIMAL, (Double) data.get("C_P1_HEIGHT")));
    outputToSend.put("Time_of_the_Start_of_the_Reflected_Wave", new Data(DataType.DECIMAL, (Double) data.get("C_T1R")));
    outputToSend.put("Central_Systolic_Pressure", new Data(DataType.DECIMAL, (Double) data.get("C_SP")));
    outputToSend.put("Central_Diastolic_Pressure", new Data(DataType.DECIMAL, (Double) data.get("C_DP")));
    outputToSend.put("Central_Mean_Pressure", new Data(DataType.DECIMAL, (Double) data.get("C_MEANP")));
    outputToSend.put("Central_T1", new Data(DataType.DECIMAL, (Double) data.get("C_T1")));
    outputToSend.put("Central_T2", new Data(DataType.DECIMAL, (Double) data.get("C_T2")));
    outputToSend.put("Central_Augmentation_Index", new Data(DataType.DECIMAL, (Double) data.get("C_AI")));
    outputToSend.put("Central_End_Systolic_Pressure", new Data(DataType.DECIMAL, (Double) data.get("C_ESP")));
    outputToSend.put("Central_Pressure_at_T1", new Data(DataType.DECIMAL, (Double) data.get("C_P1")));
    outputToSend.put("Central_Pressure_at_T2", new Data(DataType.DECIMAL, (Double) data.get("C_P2")));
    outputToSend.put("Central_T1_ED_Percent", new Data(DataType.DECIMAL, (Double) data.get("C_T1ED")));
    outputToSend.put("Central_T2_ED_Percent", new Data(DataType.DECIMAL, (Double) data.get("C_T2ED")));
    outputToSend.put("Central_Confidence_Level_of_T1", new Data(DataType.INTEGER, (Integer) data.get("C_QUALITY_T1")));
    outputToSend.put("Central_Confidence_Level_of_T2", new Data(DataType.INTEGER, (Integer) data.get("C_QUALITY_T2")));

    instrumentExecutionService.addOutputParameterValues(outputToSend);
  }

  public ExternalAppLauncherHelper getExternalAppHelper() {
    return externalAppHelper;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public InstrumentExecutionService getInstrumentExecutionService() {
    return instrumentExecutionService;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public SphygmoCorDao getSphygmoCorDao() {
    return sphygmoCorDao;
  }

  public void setSphygmoCorDao(SphygmoCorDao sphygmoCorDao) {
    this.sphygmoCorDao = sphygmoCorDao;
  }
}