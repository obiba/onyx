package org.obiba.onyx.jade.instrument.mir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.obiba.onyx.jade.client.JnlpClient;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specified instrument runner for the Spirometer
 * @author acarey
 */

public class MiniSpirInstrumentRunner implements InstrumentRunner {

  @SuppressWarnings("unused")
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

  /**
   * Retrieve participant data from the database and write them in the spirometer input file
   * @throws Exception
   */
  public void initParticipantData() {
    File externalAppInputFile = new File(getMirPath() + getExternalInputName());
    try {
      Map<String, Data> inputData = instrumentExecutionService.getInputParametersValue("ID", "LastName", "FirstName", "Gender", "Height", "Weight", "EthnicGroup", "BirthDate");
      BufferedWriter inputFileWriter = new BufferedWriter(new FileWriter(externalAppInputFile));
      inputFileWriter.write("[Identification]\n");
      for(Map.Entry<String, Data> entry : inputData.entrySet()) {
        if(entry.getKey().equals("BirthDate")) {
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
          inputFileWriter.write(entry.getKey() + "=" + formatter.format(entry.getValue().getValue()) + "\n");
        } else
          inputFileWriter.write(entry.getKey() + "=" + ((entry.getKey().equals("Gender")) ? getGenderConverter(entry.getValue()) : entry.getValue().getValueAsString()) + "\n");
      }
      inputFileWriter.close();
    } catch(Exception ex) {
      throw new RuntimeException("Error writing spirometer input file: ", ex);
    }
  }

  /**
   * Replace the result database with a backup version
   * @throws Exception
   */
  protected void deleteDeviceData() {
    File backupDbFile = new File(getInitdbPath() + getExternalDbName());
    File currentDbFile = new File(getMirPath() + getExternalDbName());

    try {
      if(backupDbFile.exists()) {
        FileUtil.copyFile(backupDbFile, currentDbFile);
      } else {
        new File(getInitdbPath()).mkdir();
        FileUtil.copyFile(currentDbFile, backupDbFile);
      }
    } catch(Exception ex) {
      throw new RuntimeException("Error in MiniSpirInstrumentRunner deleteDeviceData: ", ex);
    }
  }

  /**
   * Retrieve the data from the result file
   * @return a map with the result data
   * @throws Exception
   */
  LinkedHashMap<String, Double[]> retrieveDeviceData() {

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

      resultFileStrm.close();
      fileReader.close();
      resultReader.close();

    } catch(FileNotFoundException fnfEx) {
      JOptionPane.showMessageDialog(null, "Error: spirometry output data file not found", "Could not complete process", JOptionPane.ERROR_MESSAGE);
      throw new RuntimeException("Error: spirometry output data file not found", fnfEx);
    } catch(IOException ioEx) {
      throw new RuntimeException("Error: retrieve spirometry data IOException", ioEx);
    } catch(Exception ex) {
      throw new RuntimeException("Error: retrieve spirometry data", ex);
    }

    return outputData;
  }

  /**
   * Send the results to the server for persistence
   * @param results
   * @throws Exception
   */
  public void sendDataToServer(LinkedHashMap<String, Double[]> results) {
    Map<String, Data> ouputToSend = new HashMap<String, Data>();

    for(Map.Entry<String, Double[]> entry : results.entrySet()) {
      if(entry.getKey().indexOf("ELA") == 0) {
        ouputToSend.put(entry.getKey(), DataBuilder.buildInteger(Math.round(entry.getValue()[0])));
        ouputToSend.put(entry.getKey() + "_pred", DataBuilder.buildInteger(Math.round(entry.getValue()[1])));
      } else {
        ouputToSend.put(entry.getKey(), DataBuilder.buildDecimal(entry.getValue()[0]));
        ouputToSend.put(entry.getKey() + "_pred", DataBuilder.buildDecimal(entry.getValue()[1]));
      }
    }

    // Save the FVC image
    File FVCFile = new File(getMirPath() + getExternalImageName());
    ouputToSend.put("FVCImage", DataBuilder.buildBinary(FVCFile));

    instrumentExecutionService.addOutputParameterValues(ouputToSend);
  }

  private int getGenderConverter(Data data) {
    if(data.getValueAsString().equals("MALE")) return 0;
    else
      return 1;
  }

  /**
   * Implements parent method initialize from InstrumentRunner Delete results from previous measurement and initiate the
   * input file to be read by the external application
   */
  public void initialize() {
    deleteDeviceData();
    initParticipantData();
  }

  /**
   * Implements parent method run from InstrumentRunner Launch the external application, retrieve and send the data
   */
  public void run() {
    externalAppHelper.launch();
    LinkedHashMap<String, Double[]> results = retrieveDeviceData();
    sendDataToServer(results);
  }

  /**
   * Implements parent method shutdown from InstrumentRunner Delete results from current measurement
   */
  public void shutdown() {
    deleteDeviceData();
  }
}