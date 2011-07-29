/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.ndd;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.ndd.FVCDataExtractor.FVCData;
import org.obiba.onyx.jade.instrument.ndd.FVCDataExtractor.FVCTrialData;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specified instrument runner for the ndd Spirometer.
 */
public class EasyWareProInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(EasyWareProInstrumentRunner.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private String dbPath;

  private String exchangePath;

  private String inFileName;

  private String outFileName;

  private String reportBaseName;

  private String reportFormat;

  private Set<String> outVendorNames;

  public EasyWareProInstrumentRunner() {
    super();
  }

  /**
   * PerformTest command sent with participant data.
   * @throws Exception
   */
  private void initParticipantData() {
    File inFile = getInFile();
    try {
      PrintWriter writer = new PrintWriter(inFile);

      writer.print("<?xml version=\"1.0\" encoding=\"utf-16\"?>");
      writer.print("<ndd xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" Version=\"ndd.EasyWarePro.V1\">");
      writer.print("  <Command Type=\"PerformTest\">");
      writer.print("    <Parameter Name=\"OrderID\">1</Parameter>");
      writer.print("    <Parameter Name=\"TestType\">FVC</Parameter>");
      writer.print("  </Command>");
      writer.print("  <Patients>");
      writer.print("    <Patient ID=\"" + new Random().nextInt(1000000) + "\">");
      writer.print("      <LastName/>");
      writer.print("      <FirstName/>");
      writer.print("      <IsBioCal>false</IsBioCal>");
      writer.print("      <PatientDataAtPresent>");

      if(instrumentExecutionService.hasInputParameter("Gender")) {
        String gender = instrumentExecutionService.getInputParameterValue("Gender").getValue();
        if(gender.startsWith("F")) {
          gender = "Female";
        } else {
          gender = "Male";
        }
        writer.print("        <Gender>" + gender + "</Gender>");
      } else {
        writer.print("        <Gender/>");
      }

      if(instrumentExecutionService.hasInputParameter("DateOfBirth")) {
        SimpleDateFormat birthDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String dob = instrumentExecutionService.getDateAsString("DateOfBirth", birthDateFormatter);
        writer.print("        <DateOfBirth>" + dob + "</DateOfBirth>");
      } else {
        writer.print("        <DateOfBirth/>");
      }
      writer.print("        <ComputedDateOfBirth>false</ComputedDateOfBirth>");

      writeParameter(writer, "Height");
      writeParameter(writer, "Weight");
      writeParameter(writer, "Ethnicity");
      writeParameter(writer, "Smoker");
      writeParameter(writer, "Asthma");
      writeParameter(writer, "COPD");

      writer.print("      </PatientDataAtPresent>");
      writer.print("    </Patient>");
      writer.print("  </Patients>");
      writer.print("</ndd>");

      writer.flush();
      writer.close();

    } catch(Exception e) {
      log.error("Unable to write participant data: " + inFile.getAbsolutePath(), e);
      instrumentExecutionService.instrumentRunnerError(e);
    }
  }

  private void writeParameter(PrintWriter writer, String name) {
    if(instrumentExecutionService.hasInputParameter(name)) {
      Data data = instrumentExecutionService.getInputParameterValue(name);
      String value = data.getValue() != null ? data.getValueAsString() : "";
      log.info(name + "=" + value);
      writer.print("        <" + name + ">" + value + "</" + name + ">");
    } else {
      writer.print("        <" + name + "/>");
    }
  }

  private void initConfiguration() {
    File inFile = getInFile();
    try {
      PrintWriter writer = new PrintWriter(inFile);

      writer.print("<?xml version=\"1.0\" encoding=\"utf-16\"?>");
      writer.print("<ndd xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" Version=\"ndd.EasyWarePro.V1\">");
      writer.print("  <Command Type=\"Configuration\">");
      writer.print("    <Parameter Name=\"CloseAfterTest\">True</Parameter>");
      writer.print("    <Parameter Name=\"IncludeTrialValues\">True</Parameter>");
      writer.print("    <Parameter Name=\"IncludeCurveData\">True</Parameter>");
      if(withReport()) {
        writer.print("    <Parameter Name=\"AttachReport\">True</Parameter>");
        writer.print("    <Parameter Name=\"AttachmentFormat\">" + reportFormat + "</Parameter>");
        writer.print("    <Parameter Name=\"AttachmentFileName\">" + reportBaseName + "</Parameter>");
      } else {
        writer.print("    <Parameter Name=\"AttachReport\">False</Parameter>");
      }
      writer.print("  </Command>");
      writer.print("</ndd>");

      writer.flush();
      writer.close();

    } catch(Exception e) {
      log.error("Unable to write participant data: " + inFile.getAbsolutePath(), e);
      instrumentExecutionService.instrumentRunnerError(e);
    }
  }

  /**
   * Initialise or restore instrument data (database and scan files).
   * @throws Exception
   */
  protected void resetDeviceData() {
    File backupDbFile = new File(getDbPath() + ".orig");
    File currentDbFile = new File(getDbPath());

    try {
      if(backupDbFile.exists()) {
        FileUtil.copyFile(backupDbFile, currentDbFile);
        backupDbFile.delete();
        deleteFile(getInFile());
        // deleteFile(getOutFile());
        if(withReport()) {
          deleteFile(getReportFile());
        }
      } else {
        // init
        FileUtil.copyFile(currentDbFile, backupDbFile);
      }
    } catch(Exception ex) {
      throw new RuntimeException("Error while reseting device data: ", ex);
    }
  }

  private void deleteFile(File f) {
    if(f.exists()) {
      f.delete();
    }
  }

  private List<Map<String, Data>> retrieveDeviceData() {

    List<Map<String, Data>> dataList = new ArrayList<Map<String, Data>>();

    File outFile = getOutFile();
    try {
      EMRXMLParser<FVCData> parser = new EMRXMLParser<FVCData>();
      parser.parse(new FileInputStream(outFile), new FVCDataExtractor());

      ParticipantData pData = parser.getParticipantData();
      FVCData tData = parser.getTestData();

      for(FVCTrialData trialData : tData.getTrials()) {
        Map<String, Data> data = new HashMap<String, Data>();
        // participant data
        addOutput(data, "Height", DataBuilder.buildDecimal(pData.getHeight()));
        addOutput(data, "Weight", DataBuilder.buildInteger(pData.getWeight()));
        addOutput(data, "Ethnicity", DataBuilder.buildText(pData.getEthnicity().toUpperCase()));
        addOutput(data, "Asthma", DataBuilder.buildText(pData.getAsthma().toUpperCase()));
        addOutput(data, "Smoker", DataBuilder.buildText(pData.getSmoker().toUpperCase()));
        addOutput(data, "COPD", DataBuilder.buildText(pData.getCopd().toUpperCase()));

        // trial date
        addOutput(data, "TRIAL_DATE", DataBuilder.buildDate(trialData.getDate()));

        // results
        for(Entry<String, Number> entry : trialData.getResults().entrySet()) {
          addOutput(data, entry.getKey(), DataBuilder.build(entry.getValue()));
        }

        // curves
        addOutput(data, "FLOW_INTERVAL", DataBuilder.buildDecimal(trialData.getFlowInterval()));
        addOutput(data, "FLOW_VALUES", DataBuilder.buildText(trialData.getFlowValues()));
        addOutput(data, "VOLUME_INTERVAL", DataBuilder.buildDecimal(trialData.getVolumeInterval()));
        addOutput(data, "VOLUME_VALUES", DataBuilder.buildText(trialData.getVolumeValues()));

        dataList.add(data);
      }

    } catch(Exception e) {
      log.error("Unable to parse data from: " + outFile.getAbsolutePath(), e);
      instrumentExecutionService.instrumentRunnerError(e);
    }

    return dataList;
  }

  private void addOutput(Map<String, Data> data, String name, Data value) {
    if(outVendorNames.contains(name)) {
      data.put(name, value);
    }
  }

  private void sendDataToServer(Map<String, Data> data) {
    instrumentExecutionService.addOutputParameterValues(data);
  }

  /**
   * Implements parent method initialize from InstrumentRunner Delete results from previous measurement and initiate the
   * input file to be read by the external application
   */
  public void initialize() {
    log.info("Backup local database");
    resetDeviceData();

    log.info("Setting participant data");
    initParticipantData();

    outVendorNames = instrumentExecutionService.getExpectedOutputParameterVendorNames();

    // log.info("Configure external application");
    // initConfiguration();
  }

  /**
   * Implements parent method run from InstrumentRunner Launch the external application, retrieve and send the data
   */
  public void run() {
    // new Thread(new InitParticipantData()).start();

    log.info("Launching Easy on-PC software");
    externalAppHelper.launch();

    // wait for the output xml file to be written
    try {
      Thread.sleep(2000);
    } catch(InterruptedException e) {
    }

    log.info("Retrieving measurements");
    List<Map<String, Data>> dataList = retrieveDeviceData();

    log.info("Sending data to server");
    for(Map<String, Data> dataMap : dataList) {
      sendDataToServer(dataMap);
    }
  }

  /**
   * Implements parent method shutdown from InstrumentRunner Delete results from current measurement
   */
  public void shutdown() {
    log.info("Restoring local database and cleaning data files");
    resetDeviceData();
  }

  /**
   * Wait until application has answered the output file to our input configuration file before pushing the input
   * participant data file.
   */
  private class InitParticipantData implements Runnable {

    @Override
    public void run() {
      try {
        while(!externalAppHelper.isSotfwareAlreadyStarted() || !getOutFile().exists()) {
          Thread.sleep(200);
        }
        log.info("Setting participant data");
        initParticipantData();
      } catch(InterruptedException e) {
      }
    }
  }

  public InstrumentExecutionService getInstrumentExecutionService() {
    return instrumentExecutionService;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public ExternalAppLauncherHelper getExternalAppHelper() {
    return externalAppHelper;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public String getDbPath() {
    return dbPath;
  }

  public void setDbPath(String dbPath) {
    this.dbPath = dbPath;
  }

  public void setExchangePath(String exchangePath) {
    this.exchangePath = exchangePath;
  }

  public void setInFileName(String inFileName) {
    this.inFileName = inFileName;
  }

  public void setOutFileName(String outFileName) {
    this.outFileName = outFileName;
  }

  public File getInFile() {
    return new File(exchangePath, inFileName);
  }

  public File getOutFile() {
    return new File(exchangePath, outFileName);
  }

  public File getReportFile() {
    return new File(exchangePath, reportBaseName + "." + reportFormat.toLowerCase());
  }

  public boolean withReport() {
    return reportBaseName != null;
  }

  public void setReportBaseName(String reportBaseName) {
    this.reportBaseName = reportBaseName;
  }

  public void setReportFormat(String reportFormat) {
    this.reportFormat = reportFormat;
  }

}
