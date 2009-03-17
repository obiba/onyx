package org.obiba.onyx.jade.instrument.cardiffuniversity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import org.springframework.context.support.ResourceBundleMessageSource;

public class NoddleTestInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(JnlpClient.class);

  protected ExternalAppLauncherHelper externalAppHelper;

  protected InstrumentExecutionService instrumentExecutionService;

  private String softwareInstallPath;

  private String resultPath;

  private Locale locale;

  private ResourceBundleMessageSource resourceBundleMessageSource;

  private static String CONFIG_FILENAME = "Config.txt";

  private static String INPUT_FILENAME = "List.txt";

  private static String CLINIC_NAME = "ONYX";

  private static String RESULT_FILENAME_PREFIX = "Noddletest_" + CLINIC_NAME + "_";

  private static Map<String, Integer> TEST_NAME_TO_TEST_CODE = populateNameCodeMap();

  private static Map<String, String> TEST_NAME_TO_TEST_DESC = populateNameDescMap();

  public void afterPropertiesSet() throws Exception {
    resourceBundleMessageSource = new ResourceBundleMessageSource();
    resourceBundleMessageSource.setBasename("ct-instrument");
  }

  public void initialize() {
    deleteDeviceData();
    initInputFile();
  }

  public void run() {
    externalAppHelper.launch();
    getDataFiles();

  }

  public void shutdown() {
    // Delete input file
    File inputFile = new File(getSoftwareInstallPath() + File.separator + INPUT_FILENAME);

    try {
      if(!inputFile.delete()) log.warn("Could not delete NoddleTest input file.");
    } catch(Exception ex) {
      log.warn("Could not delete NoddleTest input file: " + ex);
    }

    deleteDeviceData();
  }

  /**
   * Gets the data in the result file, compares the test codes obtained to the configuration file and show a warning
   * popup when no test key is found or when codes are missing in result file
   */
  public void getDataFiles() {
    List<File> resultFiles = new ArrayList<File>();

    for(File file : (new File(getResultPath())).listFiles()) {
      if(file.getName().contains(RESULT_FILENAME_PREFIX)) resultFiles.add(file);
    }

    if(resultFiles.size() == 0) {
      // TODO: Handle no result file
      log.info("No result file found");
    } else if(resultFiles.size() > 1) {
      // TODO: Handle multiple result files
      log.info("More than one result file found");
    } else {
      HashSet<String> resultTests = extractTestsFromResultFile(resultFiles.get(0), new LineCallback() {
        public String handleLine(String line) {
          return (line.substring(0, 2));
        }
      });

      if(resultTests.isEmpty()) {
        warningPopup("noTestKey");
      } else {
        Data binaryData = DataBuilder.buildBinary(resultFiles.get(0));
        sendDataToServer(binaryData);

        // Check that all configured tests are present
        HashSet<String> configuredTests = extractTestsFromResultFile(new File(getSoftwareInstallPath() + File.separator + CONFIG_FILENAME), new LineCallback() {
          public String handleLine(String line) {
            if(line.startsWith("!") == false) return (line.substring(0, 2));
            return null;
          }
        });

        HashSet<String> missingTests = new HashSet<String>();
        for(String configuredTest : configuredTests) {
          if(resultTests.contains(TEST_NAME_TO_TEST_CODE.get(configuredTest).toString()) == false) missingTests.add(TEST_NAME_TO_TEST_DESC.get(configuredTest));
        }

        if(missingTests.isEmpty() == false) warningPopup("missingTestKey", new String[] { failedTestsToString(missingTests) });
      }
    }
  }

  public void sendDataToServer(Data binaryData) {
    Map<String, Data> ouputToSend = new HashMap<String, Data>();

    // Save the Result File
    try {
      ouputToSend.put("RESULT_FILE", binaryData);
    } catch(Exception e) {
      log.warn("No device output file found");
    }
    instrumentExecutionService.addOutputParameterValues(ouputToSend);
  }

  /**
   * Function that opens the specified file and gets the testcode Uses a LineCallback implementation to act differently
   * on the files passed as parameter
   * @param resultFile
   * @param callback
   * @return
   */
  HashSet<String> extractTestsFromResultFile(File resultFile, LineCallback callback) {
    HashSet<String> testCodes = new HashSet<String>();
    InputStream resultFileStrm = null;
    InputStreamReader resultReader = null;
    BufferedReader fileReader = null;

    try {
      resultFileStrm = new FileInputStream(resultFile);
      resultReader = new InputStreamReader(resultFileStrm, "UnicodeLittleUnmarked");
      char bomChar = (char) resultReader.read();
      if(bomChar != 0xFEFF) { // The file is not encoded in UTF-16LE-BOM
        resultFileStrm = new FileInputStream(resultFile);
        resultReader = new InputStreamReader(resultFileStrm, "ISO8859_1");
      }
      fileReader = new BufferedReader(resultReader);
      String line;

      while((line = fileReader.readLine()) != null) {
        if(line.isEmpty() == false && line.startsWith("#") == false) {
          String testCode = callback.handleLine(line);
          if(testCode != null) testCodes.add(testCode);
        }
      }

      resultFileStrm.close();
      fileReader.close();
      resultReader.close();
    } catch(FileNotFoundException fnfEx) {
      log.warn("No device output found");
    } catch(IOException ioEx) {
      throw new RuntimeException("Error: retrieve cognitive test data IOException", ioEx);
    } catch(Exception ex) {
      throw new RuntimeException("Error: retrieve cognitive test data", ex);
    }

    return testCodes;
  }

  String warningPopup(String key, String[] args) {
    String message = resourceBundleMessageSource.getMessage(key, args, getLocale());
    String title = resourceBundleMessageSource.getMessage("warningTitle", null, getLocale());

    JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    return message;
  }

  String warningPopup(String key) {
    return warningPopup(key, null);
  }

  private String failedTestsToString(Set<String> failedTests) {
    String errorString = "";
    if(failedTests != null) {
      for(String errorCode : failedTests) {
        errorString += "\n" + errorCode;
      }
    }
    return errorString;
  }

  private void initInputFile() {
    BufferedWriter localInputFile = null;
    try {
      localInputFile = new BufferedWriter(new FileWriter(getSoftwareInstallPath() + File.separator + INPUT_FILENAME));
      localInputFile.write("#" + CLINIC_NAME + "\n");
      localInputFile.write(instrumentExecutionService.getInstrumentOperator() + "\n");
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

  private void deleteDeviceData() {
    // Delete resultPath files if any exist.
    File resultDir = new File(getResultPath());

    try {
      for(File file : resultDir.listFiles()) {
        if(!FileUtil.delete(file)) log.warn("Could not delete NoddleTest result file [" + file.getAbsolutePath() + "].");
      }
    } catch(IOException ex) {
      log.warn("Could not delete NoddleTest result file: " + ex);
    }
  }

  // Maps implemented to find correspondence between the codes in the result file, the name in the configuration file
  // and the description to show in the warning popup message
  private static Map<String, Integer> populateNameCodeMap() {
    Map<String, Integer> map = new HashMap<String, Integer>();
    map.put("RT", 11);
    map.put("PA", 21);
    map.put("RQ", 31);
    map.put("ST", 41);
    map.put("WM", 51);
    return map;
  }

  private static Map<String, String> populateNameDescMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("RT", "Reaction Time");
    map.put("PA", "Paired Associates Learning");
    map.put("RQ", "Reasoning Quiz");
    map.put("ST", "Attention Interface");
    map.put("WM", "Working Memory");
    return map;
  }

  public ExternalAppLauncherHelper getExternalAppHelper() {
    return externalAppHelper;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public void setSoftwareInstallPath(String softwareInstallPath) {
    this.softwareInstallPath = softwareInstallPath;
  }

  public void setResultPath(String resultPath) {
    this.resultPath = resultPath;
  }

  public String getResultPath() {
    return resultPath;
  }

  public String getSoftwareInstallPath() {
    return softwareInstallPath;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public void setResourceBundleMessageSource(ResourceBundleMessageSource resourceBundleMessageSource) {
    this.resourceBundleMessageSource = resourceBundleMessageSource;
  }

  interface LineCallback {
    public String handleLine(String line);
  }

  /**
   * Validates the softwareInstallPath and the resultPath. Shuts down instrument runner if not present.
   * @throws NoddleTestInsturmentRunnerException thrown when paths are not present.
   */
  public void validatePaths() throws NoddleTestInsturmentRunnerException {
    validateSoftwareInstallPathExists();
    validateResultPathExists();
  }

  private void validateSoftwareInstallPathExists() throws NoddleTestInsturmentRunnerException {
    File path = new File(this.softwareInstallPath);
    if(!path.exists()) {
      String errorMessage = warningPopup("noddleInstallationDirectoryMissing", new String[] { path.getAbsolutePath() });
      throw new NoddleTestInsturmentRunnerException(errorMessage);
    }
  }

  private void validateResultPathExists() throws NoddleTestInsturmentRunnerException {
    File path = new File(this.resultPath);
    if(!path.exists()) {
      String errorMessage = warningPopup("noddleResultsDirectoryMissing", new String[] { path.getAbsolutePath() });
      throw new NoddleTestInsturmentRunnerException(errorMessage);
    }
  }

}
