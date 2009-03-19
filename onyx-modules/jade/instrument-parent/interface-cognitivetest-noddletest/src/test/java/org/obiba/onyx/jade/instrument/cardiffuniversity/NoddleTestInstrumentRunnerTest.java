package org.obiba.onyx.jade.instrument.cardiffuniversity;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.cardiffuniversity.NoddleTestInstrumentRunner.LineCallback;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.springframework.context.support.ResourceBundleMessageSource;

public class NoddleTestInstrumentRunnerTest {

  private ExternalAppLauncherHelper externalAppHelper;

  private NoddleTestInstrumentRunner noddleTestInstrumentRunner;

  private InstrumentExecutionService instrumentExecutionServiceMock;

  private static String RESULT_FILENAME = "Noddletest_ONYX_result.csv";

  private String errorKey = null;

  private Set<String> errorDescriptions = null;

  @Before
  public void setUp() throws URISyntaxException {

    noddleTestInstrumentRunner = new NoddleTestInstrumentRunner() {
      @Override
      String warningPopup(String key, String[] errSet) {
        setErrorKey(key);
        if(errSet != null) setErrorDescriptions(new HashSet<String>(Arrays.asList(errSet)));
        // super.warningPopup(key, errSet);
        return key;
      }

      @Override
      String warningPopup(String key) {
        return warningPopup(key, null);
      }
    };

    // Create a test directory to simulate Noddle Test software installation path.
    String noddleSoftSimulatedPath = new File("target", "test-noddle").getPath();
    (new File(noddleSoftSimulatedPath)).mkdir();
    noddleTestInstrumentRunner.setSoftwareInstallPath(noddleSoftSimulatedPath);

    // Noddle Test result path.
    File resultPath = new File(noddleSoftSimulatedPath, "RESULT");
    resultPath.mkdir();
    noddleTestInstrumentRunner.setResultPath(resultPath.getPath());

    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("ct-instrument");
    noddleTestInstrumentRunner.setResourceBundleMessageSource(messageSource);

    // Cannot mock ExternalAppLauncherHelper (without EasyMock extension!),
    // so for now, use the class itself with the launch method overridden to
    // do nothing.
    externalAppHelper = new ExternalAppLauncherHelper() {
      @Override
      public void launch() {
        // do nothing
      }

      @Override
      public boolean isSotfwareAlreadyStarted(String lockname) {
        return false;
      }
    };

    noddleTestInstrumentRunner.setExternalAppHelper(externalAppHelper);

    // Create a mock instrumentExecutionService for testing.
    instrumentExecutionServiceMock = createMock(InstrumentExecutionService.class);
    noddleTestInstrumentRunner.setInstrumentExecutionService(instrumentExecutionServiceMock);

    noddleTestInstrumentRunner.setLocale(Locale.CANADA);
    noddleTestInstrumentRunner.initializeEndDataCodeMap();
  }

  @Test
  public void testInitializeWithoutFiles() throws FileNotFoundException, IOException, URISyntaxException {
    expect(instrumentExecutionServiceMock.getInstrumentOperator()).andReturn("administratorUser");
    replay(instrumentExecutionServiceMock);
    noddleTestInstrumentRunner.initialize();
    noddleTestInstrumentRunner.releaseConfigFileAndResultFileLock();
    verify(instrumentExecutionServiceMock);

    // Verify that the Noddle Test result file has been deleted successfully.
    Assert.assertFalse(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME).exists());
    verifyInitialization();
  }

  @Test
  public void testInitializeWithFiles() throws FileNotFoundException, IOException, URISyntaxException {
    simulateResultsAndInput(RESULT_FILENAME);
    expect(instrumentExecutionServiceMock.getInstrumentOperator()).andReturn("administratorUser");
    replay(instrumentExecutionServiceMock);
    noddleTestInstrumentRunner.initialize();
    verify(instrumentExecutionServiceMock);

    // Verify that the Noddle Test result file has been deleted successfully.
    Assert.assertFalse(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME).exists());
    verifyInitialization();
  }

  @Test
  public void testRunNoResultFile() throws Exception {
    // nothing happens: we should find data files kept as after initialize step
    externalAppHelper.launch();
    noddleTestInstrumentRunner.getDataFiles();

    // Verify that the Noddle Test result file has been deleted successfully.
    Assert.assertFalse(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME).exists());
  }

  @Test
  public void testRunEmptyResultFile() throws Exception {
    // Create empty Result data file.
    FileOutputStream output = new FileOutputStream(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME));
    output.close();

    externalAppHelper.launch();
    noddleTestInstrumentRunner.getDataFiles();

    Assert.assertTrue(getErrorKey().equals("noTestKey"));
    Assert.assertTrue(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME).exists());
    setErrorKey(null);
  }

  @Test
  public void testRunMissingResultFile() throws Exception {
    simulateResultsAndInput("Noddletest_ONYX_resultMiss.csv");

    externalAppHelper.launch();
    noddleTestInstrumentRunner.getDataFiles();

    Assert.assertTrue(getErrorKey().equals("missingTestKey"));
    Assert.assertTrue(isContainedInErrorDescription("Reasoning Quiz"));

    // Assert.assertTrue(getErrorDescSet().contains("Working Memory"));
    Assert.assertTrue(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME).exists());
    setErrorKey(null);
    setErrorDescriptions(null);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRunNormalResultFile() throws Exception {
    simulateResultsAndInput(RESULT_FILENAME);
    instrumentExecutionServiceMock.addOutputParameterValues((Map<String, Data>) EasyMock.anyObject());

    replay(instrumentExecutionServiceMock);
    externalAppHelper.launch();
    noddleTestInstrumentRunner.getDataFiles();
    verify(instrumentExecutionServiceMock);

    Assert.assertTrue(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME).exists());
    Assert.assertTrue(getErrorKey() == null);
    Assert.assertTrue(getErrorDescSet().isEmpty());
  }

  @Test
  public void testRunTooMuchResultFile() throws Exception {
    // Create a second dummy Result data file.
    FileOutputStream output = new FileOutputStream(new File(noddleTestInstrumentRunner.getResultPath(), "Noddletest_ONYX_result_1.csv"));
    output.write((byte) 234432141);
    output.close();

    externalAppHelper.launch();
    noddleTestInstrumentRunner.getDataFiles();

    // Nothing happens: the two files are kept and a message in log appears
    Assert.assertTrue(new File(noddleTestInstrumentRunner.getResultPath()).listFiles().length == 2);
  }

  @Ignore("This test fails on Windows.")
  @Test
  public void testShutdown() throws FileNotFoundException, IOException, URISyntaxException, InterruptedException {
    simulateResultsAndInput(RESULT_FILENAME);
    noddleTestInstrumentRunner.shutdown();

    Assert.assertFalse(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME).exists());
    Assert.assertFalse(new File(noddleTestInstrumentRunner.getSoftwareInstallPath(), "List.txt").exists());
  }

  @Test(expected = NoddleTestInsturmentRunnerException.class)
  public void testSoftwareInstallPathDoesNotExist() throws NoddleTestInsturmentRunnerException {
    String nonExistentSoftwareInstallPath = new File("target", "non-existent-software-install-directory").getPath();
    noddleTestInstrumentRunner.setSoftwareInstallPath(nonExistentSoftwareInstallPath);
    noddleTestInstrumentRunner.initializeNoddleTestInstrumentRunner();
  }

  @Test
  public void testSoftwareInstallPathDoesExist() throws NoddleTestInsturmentRunnerException {
    String existingSoftwareInstallPath = new File("target", "test-noddle").getPath();
    noddleTestInstrumentRunner.setSoftwareInstallPath(existingSoftwareInstallPath);
    noddleTestInstrumentRunner.initializeNoddleTestInstrumentRunner();
    Assert.assertTrue("Validated true without throwing an exception.", true);
  }

  @Test(expected = NoddleTestInsturmentRunnerException.class)
  public void testResultPathDoesNotExist() throws NoddleTestInsturmentRunnerException {
    String nonExistentResultPath = new File("target/test-noddle", "non-existent-result-directory").getPath();
    noddleTestInstrumentRunner.setResultPath(nonExistentResultPath);
    noddleTestInstrumentRunner.initializeNoddleTestInstrumentRunner();
  }

  @Test
  public void testResultPathDoesExist() throws NoddleTestInsturmentRunnerException {
    String existingResultPath = new File("target/test-noddle", "RESULT").getPath();
    noddleTestInstrumentRunner.setResultPath(existingResultPath);
    noddleTestInstrumentRunner.initializeNoddleTestInstrumentRunner();
    Assert.assertTrue("Validated true without throwing an exception.", true);
  }

  @Test
  public void testParsingUtf16LeBomConfigFile() throws URISyntaxException {
    File utf16ConfigFile = new File(getClass().getResource("/Config-UTF-16LE-BOM.txt").toURI());

    HashSet<String> configuredTests = noddleTestInstrumentRunner.extractTestsFromResultFile(utf16ConfigFile, new LineCallback() {
      public String handleLine(String line) {
        if(line.startsWith("!") == false) return (line.substring(0, 2));
        return null;
      }
    });
    Assert.assertEquals("Expected 5 tests in the config file.", 5, configuredTests.size());
  }

  @Test
  public void testParsingIso8859ConfigFile() throws URISyntaxException {
    File iso8859ConfigFile = new File(getClass().getResource("/Config-iso-8859-1.txt").toURI());

    HashSet<String> configuredTests = noddleTestInstrumentRunner.extractTestsFromResultFile(iso8859ConfigFile, new LineCallback() {
      public String handleLine(String line) {
        if(line.startsWith("!") == false) return (line.substring(0, 2));
        return null;
      }
    });
    Assert.assertEquals("Expected 3 tests in the config file.", 3, configuredTests.size());
  }

  @Test
  public void testConvertingStringCodesToCompletedTests() {
    noddleTestInstrumentRunner.initializeEndDataCodeMap();
    Set<String> input = new HashSet<String>();
    input.add("22"); // PA end data code.
    input.add("31"); // RQ data code.
    Set<NoddleTests> completedTests = noddleTestInstrumentRunner.getTestsCompleted(input);
    Assert.assertTrue(completedTests.contains(NoddleTests.PA));
    Assert.assertEquals(1, completedTests.size());
  }

  @Test(expected = NumberFormatException.class)
  public void testConvertingStringCodesToCompletedTestsWithNonIntegerInput() {
    Set<String> input = new HashSet<String>();
    input.add("22"); // PA end data code.
    input.add("RQCodeIsNotAnInteger");
    noddleTestInstrumentRunner.getTestsCompleted(input);
  }

  private void simulateResultsAndInput(String fileToCopy) throws FileNotFoundException, IOException, URISyntaxException {
    // Copy Result data file.
    FileUtil.copyFile(new File(getClass().getResource("/" + fileToCopy).toURI()), new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME));

    // Copy Config file.
    FileUtil.copyFile(new File(getClass().getResource("/Config.txt").toURI()), new File(noddleTestInstrumentRunner.getSoftwareInstallPath(), "Config.txt"));

    // Create dummy Input file.
    FileOutputStream input = new FileOutputStream(new File(noddleTestInstrumentRunner.getSoftwareInstallPath(), "List.txt"));
    input.write((byte) 234432141);
    input.close();
  }

  private void verifyInitialization() {
    // Verify that the Input file has the right data.
    Assert.assertTrue(new File(noddleTestInstrumentRunner.getSoftwareInstallPath(), "List.txt").exists());

    verifyFileContent(new File(noddleTestInstrumentRunner.getSoftwareInstallPath(), "List.txt"), new TestLineCallback() {
      public void handleLine(LineNumberReader reader) throws IOException {
        for(int i = 0; i < reader.getLineNumber(); i++) {
          if(i == 0) Assert.assertTrue(reader.readLine().contains("#ONYX"));
          if(i == 1) Assert.assertTrue(reader.readLine().contains("administratorUser"));
          if(i > 1) Assert.assertTrue(reader.readLine().isEmpty());
        }
      }
    });
  }

  private void verifyFileContent(File file, TestLineCallback callback) {
    InputStream fileStrm = null;
    InputStreamReader strmReader = null;
    LineNumberReader reader = null;

    try {
      fileStrm = new FileInputStream(file);
      strmReader = new InputStreamReader(fileStrm);
      reader = new LineNumberReader(strmReader);

      callback.handleLine(reader);

      fileStrm.close();
      strmReader.close();
      reader.close();
    } catch(Exception ex) {
      throw new RuntimeException("Error: retrieve cognitive test data", ex);
    }
  }

  private interface TestLineCallback {
    public void handleLine(LineNumberReader reader) throws IOException;
  }

  public String getErrorKey() {
    return errorKey;
  }

  public void setErrorKey(String errorKey) {
    this.errorKey = errorKey;
  }

  public Set<String> getErrorDescSet() {
    return errorDescriptions != null ? errorDescriptions : Collections.<String> emptySet();
  }

  public void setErrorDescriptions(HashSet<String> errorDescriptions) {
    this.errorDescriptions = errorDescriptions;
  }

  private boolean isContainedInErrorDescription(String lookingFor) {
    for(String error : getErrorDescSet()) {
      if(error.indexOf(lookingFor) > 0) return true;
    }
    return false;
  }

}
