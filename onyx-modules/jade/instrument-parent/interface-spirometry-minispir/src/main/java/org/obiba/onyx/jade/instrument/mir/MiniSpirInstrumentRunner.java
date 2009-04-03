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
  LinkedHashMap<String, Double[]> retrieveDeviceData(String externalOutputName, String measurementSuffix) {

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
        outputData.put(description + measurementSuffix, data);
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

  public LinkedHashMap<String, Double[]> retrieveDeviceData(String externalOutputName) {
    return retrieveDeviceData(externalOutputName, "");
  }

  /**
   * Send the results to the server for persistence
   * @param results The data to send to the server.
   * @param includePredictedData Include or not the predicted values calculated by the Spirometry software.
   */
  public void sendDataToServer(LinkedHashMap<String, Double[]> results, boolean includePredictedData) {
    Map<String, Data> ouputToSend = new HashMap<String, Data>();

    for(Map.Entry<String, Double[]> entry : results.entrySet()) {
      if(!expectedOutputParameterNames.contains(entry.getKey())) {
        log.debug("Output parameter {} is not expected but has an entry in result file.", entry.getKey());
        continue;
      }
      if(entry.getKey().indexOf("ELA") == 0) {
        ouputToSend.put(entry.getKey(), DataBuilder.buildInteger(Math.round(entry.getValue()[0])));
        if(includePredictedData) {
          ouputToSend.put(entry.getKey() + "_pred", DataBuilder.buildInteger(Math.round(entry.getValue()[1])));
        }
      } else {
        ouputToSend.put(entry.getKey(), DataBuilder.buildDecimal(entry.getValue()[0]));
        if(includePredictedData) {
          ouputToSend.put(entry.getKey() + "_pred", DataBuilder.buildDecimal(entry.getValue()[1]));
        }
      }
    }
    instrumentExecutionService.addOutputParameterValues(ouputToSend);
  }

  public void sendDataToServer(LinkedHashMap<String, Double[]> retrieveDeviceData) {
    sendDataToServer(retrieveDeviceData, false);

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
    Long bestmeasurementsExpected = 1l;
    Data bestmeasurementsExpectedData = null;
    try {
      bestmeasurementsExpectedData = instrumentExecutionService.getInputParameterValue("BEST_MEASUREMENTS_EXPECTED");
    } catch(IllegalArgumentException ex) {
      // If this has not been defined, the default value is one.
    }

    if(bestmeasurementsExpectedData != null) {
      bestmeasurementsExpected = bestmeasurementsExpectedData.getValue();
      if(bestmeasurementsExpected >= 1 && bestmeasurementsExpected <= 3) {
        bestmeasurementsExpected = bestmeasurementsExpectedData.getValue();
      } else {
        throw new IllegalArgumentException("The number of best measurement expected has to be between 1 and 3.  Please check your instrument configuration file.");
      }
    }

    log.info("Number of best measurements expected: {}", bestmeasurementsExpected);

    log.info("Sending the first best measurement and predicted values...");
    sendDataToServer(retrieveDeviceData(getExternalOutputName()), true);
    if(bestmeasurementsExpected > 1) {
      log.info("Sending the second best measurement...");
      sendDataToServer(retrieveDeviceData(getExternalOutputName2ndBest(), "_2"));
      if(bestmeasurementsExpected > 2) {
        log.info("Sending the third best measurement...");
        sendDataToServer(retrieveDeviceData(getExternalOutputName3rdBest(), "_3"));
      }
    }

    // Save the FVC image
    try {
      File FVCFile = new File(getMirPath() + getExternalImageName());
      instrumentExecutionService.addOutputParameterValue("FVCImage", DataBuilder.buildBinary(FVCFile));
    } catch(Exception e) {
      log.warn("No device output image found");
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
