/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.FileUtil;
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
  private static final Logger log = LoggerFactory.getLogger(MiniSpirInstrumentRunner.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private Set<String> expectedOutputParameterNames;

  private String mirPath;

  private String initdbPath;

  private String externalDbName;

  private String externalInputName;

  private String externalOutputName;

  private String externalOutputName2ndBest;

  private String externalOutputName3rdBest;

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
    if(new File(this.mirPath).exists()) {
      return mirPath;
    } else {
      log.error("The path {} was not found, please make sure that you installed winspiroPro software to that path.", mirPath);
      throw new RuntimeException();
    }
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

  public void setExternalOutputName2ndBest(String externalOutputName2ndBest) {
    this.externalOutputName2ndBest = externalOutputName2ndBest;
  }

  public String getExternalOutputName2ndBest() {
    return externalOutputName2ndBest;
  }

  public void setExternalOutputName3rdBest(String externalOutputName3rdBest) {
    this.externalOutputName3rdBest = externalOutputName3rdBest;
  }

  public String getExternalOutputName3rdBest() {
    return externalOutputName3rdBest;
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
      String[] inputParameterCodes = new String[] { "INPUT_PARTICIPANT_BARCODE", "INPUT_PARTICIPANT_LAST_NAME", "INPUT_PARTICIPANT_FIRST_NAME", "INPUT_PARTICIPANT_GENDER", "INPUT_PARTICIPANT_HEIGHT", "INPUT_PARTICIPANT_WEIGHT", "INPUT_PARTICIPANT_ETHNIC_GROUP", "INPUT_PARTICIPANT_DATE_BIRTH" };
      Map<String, Data> inputData = instrumentExecutionService.getInputParametersValue(inputParameterCodes);
      Map<String, String> inputKeyTranslation = instrumentExecutionService.getInputParametersVendorNames(inputParameterCodes);

      BufferedWriter inputFileWriter = new BufferedWriter(new FileWriter(externalAppInputFile));
      inputFileWriter.write("[Identification]\n");
      for(Map.Entry<String, Data> entry : inputData.entrySet()) {
        if(entry.getKey().equals("INPUT_PARTICIPANT_DATE_BIRTH")) {
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
          inputFileWriter.write(inputKeyTranslation.get(entry.getKey()) + "=" + formatter.format(entry.getValue().getValue()) + "\n");
        } else
          inputFileWriter.write(inputKeyTranslation.get(entry.getKey()) + "=" + ((entry.getKey().equals("INPUT_PARTICIPANT_GENDER")) ? getGenderConverter(entry.getValue()) : entry.getValue().getValueAsString()) + "\n");
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
   * Retrieve the data from a result file
   * @param externalOutputName The name of the output file
   * @param measurementSuffix A suffix which will be added to the name of each variable found in the file
   * @return A list of each variable found in the file with its corresponding value
   */
  LinkedHashMap<String, Double[]> retrieveDeviceData(String externalOutputName) {

    InputStream resultFileStrm = null;
    InputStreamReader resultReader = null;
    BufferedReader fileReader = null;

    LinkedHashMap<String, Double[]> outputData = new LinkedHashMap<String, Double[]>();
    try {
      resultFileStrm = new FileInputStream(getMirPath() + externalOutputName);
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
      log.warn("No device output found");

    } catch(IOException ioEx) {
      throw new RuntimeException("Error: retrieve spirometry data IOException", ioEx);
    } catch(Exception ex) {
      throw new RuntimeException("Error: retrieve spirometry data", ex);
    }

    return outputData;
  }

  /**
   * Send the results to the server for persistence, always including the predicted values calculated by the Spirometry
   * software.
   * @param results The data to send to the server.
   */
  public void sendDataToServer(LinkedHashMap<String, Double[]> results) {
    Map<String, Data> outputToSend = new HashMap<String, Data>();

    for(Map.Entry<String, Double[]> entry : results.entrySet()) {
      if(!expectedOutputParameterNames.contains(entry.getKey())) {
        log.warn("Output parameter {} is not expected but has an entry in result file.", entry.getKey());
        continue;
      }
      if(entry.getKey().indexOf("ELA") == 0) {
        outputToSend.put(entry.getKey(), DataBuilder.buildInteger(Math.round(entry.getValue()[0])));
        outputToSend.put(entry.getKey() + "_pred", DataBuilder.buildInteger(Math.round(entry.getValue()[1])));
      } else {
        outputToSend.put(entry.getKey(), DataBuilder.buildDecimal(entry.getValue()[0]));
        outputToSend.put(entry.getKey() + "_pred", DataBuilder.buildDecimal(entry.getValue()[1]));
      }
    }

    if(outputToSend.size() > 0) {

      // Save the FVC image
      File FVCFile = new File(getMirPath() + getExternalImageName());
      outputToSend.put("FVCImage", DataBuilder.buildBinary(FVCFile));

      instrumentExecutionService.addOutputParameterValues(outputToSend);
    }
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

    // Get the number of best measurement expected.
    int expectedMeasureCount = instrumentExecutionService.getExpectedMeasureCount();
    int currentMeasureCount = instrumentExecutionService.getCurrentMeasureCount();

    int remainingMeasureCount = expectedMeasureCount - currentMeasureCount;

    if(remainingMeasureCount > 0) {

      if(remainingMeasureCount > 3) {
        log.warn(remainingMeasureCount + " measures are requested. The maximum number of best measurements in one run is 3. This will require a new run.");
      }

      log.info("Number of best measurements expected: {}", remainingMeasureCount);

      log.info("Sending the first best measure...");
      sendDataToServer(retrieveDeviceData(getExternalOutputName()));
      if(remainingMeasureCount > 1) {
        log.info("Sending the second best measure...");
        sendDataToServer(retrieveDeviceData(getExternalOutputName2ndBest()));
        if(remainingMeasureCount > 2) {
          log.info("Sending the third best measure...");
          sendDataToServer(retrieveDeviceData(getExternalOutputName3rdBest()));
        }
      }
    }

  }

  /**
   * Implements parent method shutdown from InstrumentRunner Delete results from current measurement
   */
  public void shutdown() {
    deleteDeviceData();
  }

  public Set<String> getExpectedOutputParameterNames() {
    return expectedOutputParameterNames;
  }

  public void setExpectedOutputParameterNames(Set<String> expectedOutputParameterNames) {
    this.expectedOutputParameterNames = expectedOutputParameterNames;
  }
}
