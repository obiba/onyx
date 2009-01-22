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
import java.io.FileFilter;
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

    // Also, remove all export files.
    deleteExportFiles();

    // Fetch the current participant's data.
    participantLastName = instrumentExecutionService.getParticipantLastName();
    participantFirstName = instrumentExecutionService.getParticipantFirstName();
    participantBirthDate = instrumentExecutionService.getParticipantBirthDate();
    participantGender = instrumentExecutionService.getParticipantGender();

    systolicPressure = instrumentExecutionService.getInputParameterValue("INPUT_SYSTOLIC_PRESSURE").getValue();
    diastolicPressure = instrumentExecutionService.getInputParameterValue("INPUT_DIASTOLIC_PRESSURE").getValue();

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
      String errMsg = "No device output found. This usually happens if the SphygmoCor application is closed before completing the measurement.";
      log.error(errMsg);
    }
  }

  public void shutdown() {
    log.info("*** Shutdown SphygmoCor Runner ***");

    // Delete locally stored participant data.
    sphygmoCorDao.deleteAllOutput();
    sphygmoCorDao.deleteAllPatients();
    deleteExportFiles();
  }

  private void sendDataToServer(Map data) {

    Map<String, Data> outputToSend = new HashMap<String, Data>();

    outputToSend.put("PATIENT_ID", DataBuilder.buildText((String) data.get("PATIENT_ID")));
    outputToSend.put("FIRST_NAME", DataBuilder.buildText((String) data.get("FIRST_NAME")));
    outputToSend.put("FAM_NAME", DataBuilder.buildText((String) data.get("FAM_NAME")));
    outputToSend.put("DOB", DataBuilder.buildDate((Date) data.get("DOB")));
    outputToSend.put("SEX", DataBuilder.buildText((String) data.get("SEX")));
    outputToSend.put("SP", DataBuilder.buildInteger(((Float) data.get("SP")).longValue()));
    outputToSend.put("DP", DataBuilder.buildInteger(((Float) data.get("DP")).longValue()));

    outputToSend.put("P_QC_PH", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_PH"))));
    outputToSend.put("P_QC_PHV", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_PHV"))));
    outputToSend.put("P_QC_PLV", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_PLV"))));
    outputToSend.put("P_QC_DV", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_DV"))));
    outputToSend.put("P_SP", new Data(DataType.DECIMAL, new Double((Float) data.get("P_SP"))));
    outputToSend.put("P_DP", new Data(DataType.DECIMAL, new Double((Float) data.get("P_DP"))));
    outputToSend.put("P_MEANP", new Data(DataType.DECIMAL, new Double((Float) data.get("P_MEANP"))));
    outputToSend.put("P_T1", new Data(DataType.DECIMAL, new Double((Float) data.get("P_T1"))));
    outputToSend.put("P_T2", new Data(DataType.DECIMAL, new Double((Float) data.get("P_T2"))));
    outputToSend.put("P_AI", new Data(DataType.DECIMAL, new Double((Float) data.get("P_AI"))));
    outputToSend.put("P_ESP", new Data(DataType.DECIMAL, new Double((Float) data.get("P_ESP"))));
    outputToSend.put("P_P1", new Data(DataType.DECIMAL, new Double((Float) data.get("P_P1"))));
    outputToSend.put("P_P2", new Data(DataType.DECIMAL, new Double((Float) data.get("P_P2"))));
    outputToSend.put("P_QUALITY_T1", new Data(DataType.INTEGER, new Long((Integer) data.get("P_QUALITY_T1"))));
    outputToSend.put("P_QUALITY_T2", new Data(DataType.INTEGER, new Long((Integer) data.get("P_QUALITY_T2"))));
    outputToSend.put("C_AP", new Data(DataType.DECIMAL, new Double((Float) data.get("C_AP"))));
    outputToSend.put("C_MPS", new Data(DataType.DECIMAL, new Double((Float) data.get("C_MPS"))));
    outputToSend.put("C_MPD", new Data(DataType.DECIMAL, new Double((Float) data.get("C_MPD"))));
    outputToSend.put("C_TTI", new Data(DataType.DECIMAL, new Double((Float) data.get("C_TTI"))));
    outputToSend.put("C_DTI", new Data(DataType.DECIMAL, new Double((Float) data.get("C_DTI"))));
    outputToSend.put("C_SVI", new Data(DataType.DECIMAL, new Double((Float) data.get("C_SVI"))));
    outputToSend.put("C_AL", new Data(DataType.DECIMAL, new Double((Float) data.get("C_AL"))));
    outputToSend.put("C_ATI", new Data(DataType.DECIMAL, new Double((Float) data.get("C_ATI"))));
    outputToSend.put("HR", new Data(DataType.DECIMAL, new Double((Float) data.get("HR"))));
    outputToSend.put("C_PERIOD", new Data(DataType.DECIMAL, new Double((Float) data.get("C_PERIOD"))));
    outputToSend.put("C_DD", new Data(DataType.DECIMAL, new Double((Float) data.get("C_DD"))));
    outputToSend.put("C_ED_PERIOD", new Data(DataType.DECIMAL, new Double((Float) data.get("C_ED_PERIOD"))));
    outputToSend.put("C_DD_PERIOD", new Data(DataType.DECIMAL, new Double((Float) data.get("C_DD_PERIOD"))));
    outputToSend.put("C_PH", new Data(DataType.DECIMAL, new Double((Float) data.get("C_PH"))));
    outputToSend.put("C_AGPH", new Data(DataType.DECIMAL, new Double((Float) data.get("C_AGPH"))));
    outputToSend.put("C_P1_HEIGHT", new Data(DataType.DECIMAL, new Double((Float) data.get("C_P1_HEIGHT"))));
    outputToSend.put("C_T1R", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T1R"))));
    outputToSend.put("C_SP", new Data(DataType.DECIMAL, new Double((Float) data.get("C_SP"))));
    outputToSend.put("C_DP", new Data(DataType.DECIMAL, new Double((Float) data.get("C_DP"))));
    outputToSend.put("C_MEANP", new Data(DataType.DECIMAL, new Double((Float) data.get("C_MEANP"))));
    outputToSend.put("C_T1", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T1"))));
    outputToSend.put("C_T2", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T2"))));
    outputToSend.put("C_AI", new Data(DataType.DECIMAL, new Double((Float) data.get("C_AI"))));
    outputToSend.put("C_ESP", new Data(DataType.DECIMAL, new Double((Float) data.get("C_ESP"))));
    outputToSend.put("C_P1", new Data(DataType.DECIMAL, new Double((Float) data.get("C_P1"))));
    outputToSend.put("C_P2", new Data(DataType.DECIMAL, new Double((Float) data.get("C_P2"))));
    outputToSend.put("C_T1ED", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T1ED"))));
    outputToSend.put("C_T2ED", new Data(DataType.DECIMAL, new Double((Float) data.get("C_T2ED"))));
    outputToSend.put("C_QUALITY_T1", new Data(DataType.INTEGER, new Long((Integer) data.get("C_QUALITY_T1"))));
    outputToSend.put("C_QUALITY_T2", new Data(DataType.INTEGER, new Long((Integer) data.get("C_QUALITY_T2"))));
    outputToSend.put("P_QC_OTHER4", new Data(DataType.DECIMAL, new Double((Float) data.get("P_QC_OTHER4"))));

    // Add export files to output.
    addExportFileToOutput(outputToSend, "PIC_CLINIC");
    addExportFileToOutput(outputToSend, "PIC_DETAIL");
    addExportFileToOutput(outputToSend, "PIC_CLASSIFICATION");

    instrumentExecutionService.addOutputParameterValues(outputToSend);
  }

  /**
   * Adds the specified export file to the output to send.
   * 
   * @param outputToSend output to send to the server
   * @param exportFilenamePrefix export file name prefix
   */
  private void addExportFileToOutput(Map<String, Data> outputToSend, String exportFilenamePrefix) {
    File exportFile = getExportFile(exportFilenamePrefix);

    if(exportFile != null) {
      outputToSend.put(exportFilenamePrefix, DataBuilder.buildBinary(exportFile));
    } else {

    }
  }

  /**
   * Returns the export file with the specified file name prefix.
   * 
   * @param exportFilenamePrefix export file name prefix
   * @return export file (or <code>null</code> if no such file exists)
   */
  private File getExportFile(final String exportFilenamePrefix) {
    FileFilter filter = new FileFilter() {
      public boolean accept(File file) {
        String fileName = file.getName();
        return file.isFile() && fileName.startsWith(exportFilenamePrefix) && fileName.endsWith(".jpg");
      }
    };

    File exportDir = getExportDir();
    File[] matches = exportDir.listFiles(filter);

    return (matches.length != 0) ? matches[0] : null;
  }

  /**
   * Deletes all export files.
   */
  private void deleteExportFiles() {
    File exportDir = getExportDir();

    if(exportDir.exists()) {
      File[] exportFiles = exportDir.listFiles();
      for(File f : exportFiles) {
        if(f.isFile()) {
          f.delete();
        }
      }
    }
  }

  private File getExportDir() {
    return new File(externalAppHelper.getWorkDir() + File.separator + "export");
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
