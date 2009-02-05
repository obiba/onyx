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
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.util.FileUtil;
import org.obiba.onyx.util.data.Data;

public class NoddleTestInstrumentRunnerTest {

  private ExternalAppLauncherHelper externalAppHelper;

  private NoddleTestInstrumentRunner noddleTestInstrumentRunner;

  private InstrumentExecutionService instrumentExecutionServiceMock;

  private static String RESULT_FILENAME = "Noddletest_ONYX_result.csv";

  private String errorKey = null;

  private HashSet<String> errorDescSet = null;

  @Before
  public void setUp() throws URISyntaxException {

    // Skip tests when were not on Windows.
    Assume.assumeTrue(System.getProperty("os.name").toLowerCase().contains("windows"));

    noddleTestInstrumentRunner = new NoddleTestInstrumentRunner() {
      @Override
      public void warningPopup(String key, HashSet<String> errSet) {
        setErrorKey(key);
        if(errSet != null) setErrorDescSet(errSet);
        // super.warningPopup(key, errSet);
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

    noddleTestInstrumentRunner.setCtResourceBundle(ResourceBundle.getBundle("ct-instrument", Locale.getDefault()));

    // Cannot mock ExternalAppLauncherHelper (without EasyMock extension!),
    // so for now, use the class itself with the launch method overridden to
    // do nothing.
    externalAppHelper = new ExternalAppLauncherHelper() {
      @Override
      public void launch() {
        // do nothing
      }
    };

    noddleTestInstrumentRunner.setExternalAppHelper(externalAppHelper);

    // Create a mock instrumentExecutionService for testing.
    instrumentExecutionServiceMock = createMock(InstrumentExecutionService.class);
    noddleTestInstrumentRunner.setInstrumentExecutionService(instrumentExecutionServiceMock);
  }

  @Test
  public void testInitializeWithoutFiles() throws FileNotFoundException, IOException, URISyntaxException {
    expect(instrumentExecutionServiceMock.getInstrumentOperator()).andReturn("administratorUser");
    replay(instrumentExecutionServiceMock);
    noddleTestInstrumentRunner.initialize();
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
    Assert.assertTrue(getErrorDescSet().contains("Reasoning Quiz"));
    // Assert.assertTrue(getErrorDescSet().contains("Working Memory"));
    Assert.assertTrue(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME).exists());
    setErrorKey(null);
    setErrorDescSet(null);
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

  @Test
  public void testShutdown() throws FileNotFoundException, IOException, URISyntaxException {
    simulateResultsAndInput(RESULT_FILENAME);
    noddleTestInstrumentRunner.shutdown();

    Assert.assertFalse(new File(noddleTestInstrumentRunner.getResultPath(), RESULT_FILENAME).exists());
    Assert.assertFalse(new File(noddleTestInstrumentRunner.getSoftwareInstallPath(), "List.txt").exists());
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

    verifyFileContent(new File(noddleTestInstrumentRunner.getSoftwareInstallPath(), "List.txt"), new LineCallback() {
      public void handleLine(LineNumberReader reader) throws IOException {
        for(int i = 0; i < reader.getLineNumber(); i++) {
          if(i == 0) Assert.assertTrue(reader.readLine().contains("#ONYX"));
          if(i == 1) Assert.assertTrue(reader.readLine().contains("administratorUser"));
          if(i > 1) Assert.assertTrue(reader.readLine().isEmpty());
        }
      }
    });
  }

  private void verifyFileContent(File file, LineCallback callback) {
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

  private interface LineCallback {
    public void handleLine(LineNumberReader reader) throws IOException;
  }

  public String getErrorKey() {
    return errorKey;
  }

  public void setErrorKey(String errorKey) {
    this.errorKey = errorKey;
  }

  public HashSet<String> getErrorDescSet() {
    return (errorDescSet != null) ? errorDescSet : new HashSet<String>();
  }

  public void setErrorDescSet(HashSet<String> errorDescSet) {
    this.errorDescSet = errorDescSet;
  }

}
