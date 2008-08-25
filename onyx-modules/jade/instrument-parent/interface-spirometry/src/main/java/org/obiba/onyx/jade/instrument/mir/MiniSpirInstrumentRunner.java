package org.obiba.onyx.jade.instrument.mir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.wicket.util.io.Streams;
import org.obiba.core.util.FileUtil;
import org.obiba.onyx.jade.client.JnlpClient;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniSpirInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(JnlpClient.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private String mirPath;

  private String initdbPath;

  private String externalDbName;

  private String externalInputName;

  private String externalOutputName;

  private String externalImageName;

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

  public String getMirPath() {
    return mirPath;
  }

  public void setMirPath(String mirPath) {
    this.mirPath = mirPath;
  }

  public String getInitdbPath() {
    return initdbPath;
  }

  public void setInitdbPath(String initdbPath) {
    this.initdbPath = initdbPath;
  }

  public String getExternalDbName() {
    return externalDbName;
  }

  public void setExternalDbName(String externalDbName) {
    this.externalDbName = externalDbName;
  }

  public String getExternalInputName() {
    return externalInputName;
  }

  public void setExternalInputName(String externalInputName) {
    this.externalInputName = externalInputName;
  }

  public String getExternalOutputName() {
    return externalOutputName;
  }

  public void setExternalOutputName(String externalOutputName) {
    this.externalOutputName = externalOutputName;
  }

  public String getExternalImageName() {
    return externalImageName;
  }

  public void setExternalImageName(String externalImageName) {
    this.externalImageName = externalImageName;
  }

  public void initParticipantData() throws Exception {
    // normally should retrieve latest data from database with current participant

    // Write participant data in spirometer input file
    File externalAppInputFile = new File(getMirPath() + getExternalInputName());
    try {
      Map<String, Data> inputData = instrumentExecutionService.getInputParametersValue("ID", "LastName", "FirstName", "Gender", "Height", "Weight", "EthnicGroup", "BirthDate");
      BufferedWriter inputFileWriter = new BufferedWriter(new FileWriter(externalAppInputFile));
      inputFileWriter.write("[Identification]\n");
      for(Map.Entry<String, Data> entry : inputData.entrySet()) {
        inputFileWriter.write(entry.getKey() + "=" + entry.getValue().getValue().toString() + "\n");
      }
      inputFileWriter.close();
    } catch(Exception ex) {
      throw new Exception("Error writing spirometer input file: ", ex);
    }
  }

  protected void deleteDeviceData() throws Exception {
    File backupDbFile = new File(getInitdbPath() + getExternalDbName());
    File currentDbFile = new File(getMirPath() + getExternalDbName());

    if(backupDbFile.exists()) {
      FileUtil.copyFile(backupDbFile, currentDbFile);
    } else {
      new File(getInitdbPath()).mkdir();
      FileUtil.copyFile(currentDbFile, backupDbFile);
    }
  }

  private LinkedHashMap<String, Double[]> retrieveDeviceData() throws Exception {

    InputStream resultFileStrm = null;
    InputStreamReader resultReader = null;
    BufferedReader fileReader = null;
    
    LinkedHashMap<String, Double[]> outputData = new LinkedHashMap<String, Double[]>();
    try {
      resultFileStrm = new FileInputStream(getMirPath() + getExternalOutputName());
      resultReader = new InputStreamReader(resultFileStrm);
      fileReader = new BufferedReader(resultReader);

      StringBuffer results = new StringBuffer();
      String line;
      Boolean lastParam = false;
      while((line = fileReader.readLine()) != null) {
        results.append(line + "\n");
        if(line.indexOf("PIF") == 0) lastParam = true;
      }
      
      if(lastParam == false) JOptionPane.showMessageDialog(null, "Data is incomplete", "Could not complete process", JOptionPane.ERROR_MESSAGE);

      Pattern pattern = Pattern.compile("(.*)\t(.*)\t(.*)\t(.*)\t(.*)");
      Matcher matcher = pattern.matcher(results.toString().replace(",", ".").replace("/", "_"));
      String description = null;
      Double[] data = null;

      while(matcher.find()) {
        description = matcher.group(1);
        data = new Double[2];
        data[0] = Double.valueOf(matcher.group(3));
        data[1] = Double.valueOf(matcher.group(4));
        outputData.put(description, data);
      }

    } catch(FileNotFoundException fnfEx) {
      log.error("*** Error: spirometry output data file not found: ", fnfEx);
      JOptionPane.showMessageDialog(null, "Error: spirometry output data file not found", "Could not complete process", JOptionPane.ERROR_MESSAGE);
    }
    
    finally {
      resultFileStrm.close();
      fileReader.close();
      resultReader.close();
    }
    
    return outputData;
  }

  public void sendDataToServer(LinkedHashMap<String, Double[]> results) throws Exception {
    Map<String, Data> ouputToSend = new HashMap<String, Data>();

    for(Map.Entry<String, Double[]> entry: results.entrySet()) {
      if(entry.getKey().indexOf("ELA") == 0) {
        ouputToSend.put(entry.getKey(), new Data(DataType.INTEGER, Math.round(entry.getValue()[0])));
        ouputToSend.put(entry.getKey() + "_pred", new Data(DataType.INTEGER, Math.round(entry.getValue()[1])));
      } else {
        ouputToSend.put(entry.getKey(), new Data(DataType.DECIMAL, entry.getValue()[0]));
        ouputToSend.put(entry.getKey() + "_pred", new Data(DataType.DECIMAL, entry.getValue()[1]));
      }
    }

    // Save the FVC image
    File FVCFile = new File(getMirPath() + getExternalImageName());
    String fileContent = Streams.readString(new FileInputStream(FVCFile), "UTF-8");
    byte[] FVCInput = fileContent.getBytes("UTF-8");
    ouputToSend.put("FVCImage", new Data(DataType.DATA, (Serializable) FVCInput));

    instrumentExecutionService.addOutputParameterValues(ouputToSend);
  }

  public void initialize() {
    log.info("*** Initializing MIR Runner ***");
    try {
      deleteDeviceData(); // Delete ancient data in instrument specific database
      initParticipantData(); // Create file with participant data
    } catch(Exception ex) {
      log.error("*** EXCEPTION INITIALIZE STEP: ", ex);
    }
  }

  public void run() {
    log.info("*** Running MIR Runner ***");
    externalAppHelper.launch();

    // Get data from external app
    try {
      LinkedHashMap<String, Double[]> results = retrieveDeviceData();
      sendDataToServer(results);
    } catch(Exception ex) {
      log.error("*** EXCEPTION SHUTDOWN STEP: ", ex);
    }
  }

  public void shutdown() {
    log.info("*** Shutdown MIR Runner ***");
    try {
      deleteDeviceData(); // Delete current data in instrument specific database for privacy
    } catch(Exception ex) {
      log.error("*** EXCEPTION INITIALIZE STEP: ", ex);
    }
  }
}