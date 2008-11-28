/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.atcor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.atcor.dao.SphygmoCorDao;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SphygmoCorInstrumentRunner implements InstrumentRunner {

  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(SphygmoCorInstrumentRunner.class);

  //
  // Instance variables
  //

  private ExternalAppLauncherHelper externalAppHelper;

  private InstrumentExecutionService instrumentExecutionService;

  private SphygmoCorDao sphygmoCorDao;

  private String participantLastName;

  private String participantFirstName;

  private Date participantBirthDate;

  private String participantGender;

  private Long systolicPressure;

  private Long diastolicPressure;

  //
  // Methods
  //

  public void initialize() {
    // First, remove any data (patients and measurements) currently in the AtCor database.
    sphygmoCorDao.deleteAllOutput();
    sphygmoCorDao.deleteAllPatients();

    // Fetch the current participant's data.
    participantLastName = instrumentExecutionService.getParticipantLastName();
    participantFirstName = instrumentExecutionService.getParticipantFirstName();
    participantBirthDate = instrumentExecutionService.getParticipantBirthDate();
    participantGender = instrumentExecutionService.getParticipantGender();

    systolicPressure = instrumentExecutionService.getInputParameterValue("SystolicPressure").getValue();
    diastolicPressure = instrumentExecutionService.getInputParameterValue("DiastolicPressure").getValue();

    writeSphygmoCorInputFile();

  }

  private void writeSphygmoCorInputFile() {
    BufferedWriter localInputFile = null;
    try {
      localInputFile = new BufferedWriter(new FileWriter(externalAppHelper.getWorkDir() + File.separator + "patient.txt"));
      localInputFile.write(participantLastName + "\n");
      localInputFile.write(participantFirstName + "\n");

      SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
      localInputFile.write(formatter.format(participantBirthDate) + "\n");

      localInputFile.write(participantGender + "\n");
      localInputFile.write(instrumentExecutionService.getParticipantID() + "\n");
      localInputFile.write(systolicPressure + "\n");
      localInputFile.write(diastolicPressure + "\n");
    } catch(IOException e) {
      log.error("Could not write input file!");
      throw new RuntimeException(e);
    } finally {
      try {
        localInputFile.close();
      } catch(Exception e) {
      }
    }
  }

  public void run() {
    log.info("*** Running SphygmoCor Runner ***");

    // Launch the SphygmoCor software.
    externalAppHelper.launch();

    // Retrieve the output (measurements taken for the current participant).
    // NOTE: The getOutput method returns the output as a List of Maps. There
    // *should* only be one Map, corresponding to the single run.
    List output = sphygmoCorDao.getOutput(Integer.parseInt(instrumentExecutionService.getParticipantID()));

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

    outputToSend.put("Participant Barcode", DataBuilder.buildText((String) data.get("PATIENT_ID")));
    outputToSend.put("Participant First Name", DataBuilder.buildText((String) data.get("FIRST_NAME")));
    outputToSend.put("Participant Last Name", DataBuilder.buildText((String) data.get("FAM_NAME")));
    outputToSend.put("Participant Date of Birth", DataBuilder.buildDate((Date)data.get("DOB")));
    outputToSend.put("Participant Gender", DataBuilder.buildText((String) data.get("SEX")));
    outputToSend.put("Systolic Pressure", DataBuilder.buildInteger(((Float)data.get("SP")).longValue()));
    outputToSend.put("Diastolic Pressure", DataBuilder.buildInteger(((Float) data.get("DP")).longValue()));

    outputToSend.put("Peripheral_Pulse_Quality_Control_Pulse_Height", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_PH"))));
    outputToSend.put("Peripheral_Pulse_Quality_Control_Pulse_Height_Variation", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_PHV"))));
    outputToSend.put("Peripheral_Pulse_Quality_Control_Pulse_Length_Variation", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_PLV"))));
    outputToSend.put("Peripheral_Pulse_Quality_Control_Diastolic_Variation", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_DV"))));
    outputToSend.put("Peripheral_Systolic_Pressure", new Data(DataType.DECIMAL, new Double((Float) data.get("P_SP"))));
    outputToSend.put("Peripheral_Diastolic_Pressure", new Data(DataType.DECIMAL, new Double((Float) data.get("P_DP"))));
    outputToSend.put("Peripheral_Mean_Pressure", new Data(DataType.DECIMAL, new Double((Float) data.get("P_MEANP"))));
    outputToSend.put("Peripheral_T1", new Data(DataType.DECIMAL, new Double((Float) data.get("P_T1"))));
    outputToSend.put("Peripheral_T2", new Data(DataType.DECIMAL, new Double((Float) data.get("P_T2"))));
    outputToSend.put("Peripheral_Augmentation_Index", new Data(DataType.DECIMAL, new Double((Float) data.get("P_AI"))));
    outputToSend.put("End_Systolic_Pressure", new Data(DataType.DECIMAL, new Double((Float) data.get("P_ESP"))));
    outputToSend.put("Peripheral_P1", new Data(DataType.DECIMAL, new Double((Float) data.get("P_P1"))));
    outputToSend.put("Peripheral_P2", new Data(DataType.DECIMAL, new Double((Float) data.get("P_P2"))));
    outputToSend.put("Peripheral_Confidence_Level_of_T1", new Data(DataType.INTEGER, new Long((Integer) data.get("P_QUALITY_T1"))));
    outputToSend.put("Peripheral_Confidence_Level_of_T2", new Data(DataType.INTEGER, new Long((Integer) data.get("P_QUALITY_T2"))));
    outputToSend.put("Central_Augmented_Pressure", new Data(DataType.DECIMAL, new Double((Float) data.get("C_AP"))));
    outputToSend.put("Central_Mean_Pressure_of_Systole", new Data(DataType.DECIMAL, new Double((Float) data.get("C_MPS"))));
    outputToSend.put("Central_Mean_Pressure_of_Diastole", new Data(DataType.DECIMAL, new Double((Float) data.get("C_MPD"))));
    outputToSend.put("Central_Tension_Time_Index", new Data(DataType.DECIMAL, new Double((Float) data.get("C_TTI"))));
    outputToSend.put("Central_Diastolic_Time_Index", new Data(DataType.DECIMAL, new Double((Float) data.get("C_DTI"))));
    outputToSend.put("Central_Buckberg_SubEndocardial_Viability_Ratio", new Data(DataType.DECIMAL, new Double((Float) data.get("C_SVI"))));
    outputToSend.put("Augmentation_Load", new Data(DataType.DECIMAL, new Double((Float) data.get("C_AL"))));
    outputToSend.put("Augmentation_Time_Index", new Data(DataType.DECIMAL, new Double((Float) data.get("C_ATI"))));
    outputToSend.put("Heart_Rate", new Data(DataType.DECIMAL, new Double((Float) data.get("HR"))));
    outputToSend.put("Central_Pulse_Period", new Data(DataType.DECIMAL, new Double((Float) data.get("C_PERIOD"))));
    outputToSend.put("Central_Diastolic_Duration", new Data(DataType.DECIMAL, new Double((Float) data.get("C_DD"))));
    outputToSend.put("Central_ED_Period_Percent_Ejection_duration", new Data(DataType.DECIMAL, new Double((Float) data.get("C_ED_PERIOD"))));
    outputToSend.put("Period_ED_Period_Percent", new Data(DataType.DECIMAL, new Double((Float) data.get("C_DD_PERIOD"))));
    outputToSend.put("Central_Pulse_Height", new Data(DataType.DECIMAL, new Double((Float) data.get("C_PH"))));
    outputToSend.put("Central_Aug_PH_Percent", new Data(DataType.DECIMAL, new Double((Float) data.get("C_AGPH"))));
    outputToSend.put("Central_Pressure_at_T1_Dp", new Data(DataType.DECIMAL, new Double((Float) data.get("C_P1_HEIGHT"))));
    outputToSend.put("Time_of_the_Start_of_the_Reflected_Wave", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T1R"))));
    outputToSend.put("Central_Systolic_Pressure", new Data(DataType.DECIMAL, new Double((Float) data.get("C_SP"))));
    outputToSend.put("Central_Diastolic_Pressure", new Data(DataType.DECIMAL, new Double((Float) data.get("C_DP"))));
    outputToSend.put("Central_Mean_Pressure", new Data(DataType.DECIMAL, new Double((Float) data.get("C_MEANP"))));
    outputToSend.put("Central_T1", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T1"))));
    outputToSend.put("Central_T2", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T2"))));
    outputToSend.put("Central_Augmentation_Index", new Data(DataType.DECIMAL, new Double((Float) data.get("C_AI"))));
    outputToSend.put("Central_End_Systolic_Pressure", new Data(DataType.DECIMAL, new Double((Float) data.get("C_ESP"))));
    outputToSend.put("Central_Pressure_at_T1", new Data(DataType.DECIMAL, new Double((Float) data.get("C_P1"))));
    outputToSend.put("Central_Pressure_at_T2", new Data(DataType.DECIMAL, new Double((Float) data.get("C_P2"))));
    outputToSend.put("Central_T1_ED_Percent", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T1ED"))));
    outputToSend.put("Central_T2_ED_Percent", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T2ED"))));
    outputToSend.put("Central_Confidence_Level_of_T1", new Data(DataType.INTEGER, new Long((Integer) data.get("C_QUALITY_T1"))));
    outputToSend.put("Central_Confidence_Level_of_T2", new Data(DataType.INTEGER, new Long((Integer) data.get("C_QUALITY_T2"))));
    outputToSend.put("Operator_Index", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_OTHER4"))));

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
