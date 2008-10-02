package org.obiba.onyx.jade.instrument.mir;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

public class MiniSpirInstrumentRunnerTest {

  private ExternalAppLauncherHelper externalAppHelper;

  private MiniSpirInstrumentRunner minispirInstrumentRunner;

  private InstrumentExecutionService instrumentExecutionServiceMock;
  
  private Set<String> expectedOutputParameterNamesSet = new HashSet<String>();

  File initDbFile;

  File miniSpirDbFile;

  @Before
  public void setUp() throws URISyntaxException {

    minispirInstrumentRunner = new MiniSpirInstrumentRunner();
    minispirInstrumentRunner.setExternalDbName("WinSpiroPRO.wdb");
    minispirInstrumentRunner.setExternalImageName("FVC.jpg");
    minispirInstrumentRunner.setExternalInputName("patient.srv");
    minispirInstrumentRunner.setExternalOutputName("Results.wsp");

    String resourcesParentDir = new File(getClass().getResource("/WinSpiroPRO.wdb").toURI().getPath()).getParent();
    minispirInstrumentRunner.setInitdbPath(new File(resourcesParentDir, File.separator).getPath());

    // Create a test directory to simulate WinSpiro software installation path.
    String winSpiroSimulatedPath = "target" + File.separator + "test-spirometry" + File.separator;
    (new File(winSpiroSimulatedPath)).mkdir();
    minispirInstrumentRunner.setMirPath(winSpiroSimulatedPath);

    // Cannot mock ExternalAppLauncherHelper (without EasyMock extension!),
    // so for now, use the class itself with the launch method overridden to
    // do nothing.
    externalAppHelper = new ExternalAppLauncherHelper() {
      public void launch() {
        // do nothing
      }
    };
    minispirInstrumentRunner.setExternalAppHelper(externalAppHelper);

    // Create a mock instrumentExecutionService for testing.
    instrumentExecutionServiceMock = createMock(InstrumentExecutionService.class);
    minispirInstrumentRunner.setInstrumentExecutionService(instrumentExecutionServiceMock);
    
    // Create the outputParameterNamesExpected set
    setExpectedOutputParameterNames();
    minispirInstrumentRunner.setExpectedOutputParameterNames(expectedOutputParameterNamesSet);
    
    initDbFile = new File(minispirInstrumentRunner.getInitdbPath(), minispirInstrumentRunner.getExternalDbName());
    miniSpirDbFile = new File(minispirInstrumentRunner.getMirPath(), minispirInstrumentRunner.getExternalDbName());

  }

  @Test
  public void testInitialize() throws FileNotFoundException, IOException {

    // Write some arbitrary data to simulate database access by external software.
    (new FileOutputStream(miniSpirDbFile)).write((byte) 234432141);
    
    // Insure that the previous participant data is deleted.
    minispirInstrumentRunner.deleteDeviceData();

    // Verify that the WinSpiro database content has been cleared successfully.
    Assert.assertEquals(initDbFile.length(), miniSpirDbFile.length());

    // Set arbitrary inputs for testing.
    Map<String, Data> inputData = new HashMap<String, Data>();
    inputData.put("ID", DataBuilder.buildText("123456789"));
    inputData.put("LastName", DataBuilder.buildText("Tremblay"));
    inputData.put("FirstName", DataBuilder.buildText("Chantal"));
    inputData.put("Gender", DataBuilder.buildText("Chantal"));
    inputData.put("Height", DataBuilder.buildInteger(178));
    inputData.put("Weight", DataBuilder.buildDecimal(76.4));
    inputData.put("EthnicGroup", DataBuilder.buildInteger(1));
    inputData.put("BirthDate", DataBuilder.buildDate(getBirthDate()));

    expect(instrumentExecutionServiceMock.getInputParametersValue("ID", "LastName", "FirstName", "Gender", "Height", "Weight", "EthnicGroup", "BirthDate")).andReturn(inputData);
    replay(instrumentExecutionServiceMock);

    // Generate the input file for WinSpiro.
    minispirInstrumentRunner.initParticipantData();
    verify(instrumentExecutionServiceMock);

    // Verify that input file has been created.
    File inputFile = new File(minispirInstrumentRunner.getMirPath() + minispirInstrumentRunner.getExternalInputName());
    Assert.assertTrue(inputFile.exists());

  }

  private java.util.Date getBirthDate() {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.YEAR, 1964);
    c.set(Calendar.MONTH, 2);
    c.set(Calendar.DAY_OF_MONTH, 12);

    return c.getTime();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRun() throws FileNotFoundException, IOException, URISyntaxException {
    externalAppHelper.launch();

    // Copy the result file + image file to test directory.
    FileUtil.copyFile(new File(getClass().getResource("/Results.wsp").toURI()), new File(minispirInstrumentRunner.getMirPath(), minispirInstrumentRunner.getExternalOutputName()));
    FileUtil.copyFile(new File(getClass().getResource("/FVC.jpg").toURI()), new File(minispirInstrumentRunner.getMirPath(), minispirInstrumentRunner.getExternalImageName()));

    // Read the results file.
    LinkedHashMap<String, Double[]> results = minispirInstrumentRunner.retrieveDeviceData();

    // Compare the values read with the ones from the result file.
    Assert.assertEquals(10.94, results.get("FVC")[0].doubleValue(), 0);
    Assert.assertEquals(4.36, results.get("FVC")[1].doubleValue(), 0);
    Assert.assertEquals(4.22, results.get("FEV1")[0].doubleValue(), 0);
    Assert.assertEquals(3.58, results.get("FEV1")[1].doubleValue(), 0);
    Assert.assertEquals(38.6, results.get("FEV1_FVC")[0].doubleValue(), 0);
    Assert.assertEquals(78.81, results.get("FEV1_FVC")[1].doubleValue(), 0);
    Assert.assertEquals(4.65, results.get("PEF")[0].doubleValue(), 0);
    Assert.assertEquals(7.36, results.get("PEF")[1].doubleValue(), 0);
    Assert.assertEquals(2.63, results.get("FEF2575")[0].doubleValue(), 0);
    Assert.assertEquals(3.94, results.get("FEF2575")[1].doubleValue(), 0);
    Assert.assertEquals(8.59, results.get("FEV3")[0].doubleValue(), 0);
    Assert.assertEquals(3.94, results.get("FEV3")[1].doubleValue(), 0);
    Assert.assertEquals(78.5, results.get("FEV3_FVC")[0].doubleValue(), 0);
    Assert.assertEquals(90.48, results.get("FEV3_FVC")[1].doubleValue(), 0);
    Assert.assertEquals(39, results.get("ELA")[0].doubleValue(), 0);
    Assert.assertEquals(39, results.get("ELA")[1].doubleValue(), 0);
    Assert.assertEquals(5.62, results.get("FET")[0].doubleValue(), 0);
    Assert.assertEquals(6, results.get("FET")[1].doubleValue(), 0);
    Assert.assertEquals(4.15, results.get("FEF25")[0].doubleValue(), 0);
    Assert.assertEquals(6.85, results.get("FEF25")[1].doubleValue(), 0);
    Assert.assertEquals(2.75, results.get("FEF50")[0].doubleValue(), 0);
    Assert.assertEquals(4.57, results.get("FEF50")[1].doubleValue(), 0);
    Assert.assertEquals(1.49, results.get("FEF75")[0].doubleValue(), 0);
    Assert.assertEquals(2.05, results.get("FEF75")[1].doubleValue(), 0);
    Assert.assertEquals(180, results.get("EVol")[0].doubleValue(), 0);
    Assert.assertEquals(0, results.get("EVol")[1].doubleValue(), 0);
    Assert.assertEquals(0, results.get("FIVC")[0].doubleValue(), 0);
    Assert.assertEquals(4.36, results.get("FIVC")[1].doubleValue(), 0);
    Assert.assertEquals(0, results.get("FIV1")[0].doubleValue(), 0);
    Assert.assertEquals(3.58, results.get("FIV1")[1].doubleValue(), 0);
    Assert.assertEquals(0, results.get("FIV1_FIVC")[0].doubleValue(), 0);
    Assert.assertEquals(78.81, results.get("FIV1_FIVC")[1].doubleValue(), 0);
    Assert.assertEquals(0, results.get("PIF")[0].doubleValue(), 0);
    Assert.assertEquals(7.36, results.get("PIF")[1].doubleValue(), 0);

    instrumentExecutionServiceMock.addOutputParameterValues((Map<String, Data>) anyObject());
    replay(instrumentExecutionServiceMock);

    // Make sure that the results are sent to the server.
    minispirInstrumentRunner.sendDataToServer(results);
    verify(instrumentExecutionServiceMock);

  }

  @Test
  public void testShutdown() throws FileNotFoundException, IOException {

    // Write some arbitrary data to simulate database access by external software.
    (new FileOutputStream(miniSpirDbFile)).write((byte) 234432141);
    
    minispirInstrumentRunner.shutdown();

    // Verify that the WinSpiro database content has been cleared successfully.
    Assert.assertEquals(initDbFile.length(), miniSpirDbFile.length());

  }
  
  private void setExpectedOutputParameterNames() {
    String elements[] = { "last_name",
        "first_name",
        "birth_date",
        "gender",
        "fvc",
        "fev1",
        "fev1_fvc",
        "fev6",
        "fev1_fev6",
        "pef",
        "fef2575",
        "fev3",
        "fev3_fvc",
        "ela",
        "fet",
        "fef25",
        "fef50",
        "fef75",
        "evol",
        "fivc",
        "fiv1",
        "fiv1_fivc",
        "pif",
        "fvc_pred",
        "fev1_pred",
        "fev1_fvc_pred",
        "fev6_pred",
        "fev1_fev6_pred",
        "pef_pred",
        "fef2575_pred",
        "fev3_pred",
        "fev3_fvc_pred",
        "ela_pred",
        "fet_pred",
        "fef25_pred",
        "fef50_pred",
        "fef75_pred",
        "evol_pred",
        "fivc_pred",
        "fiv1_pred",
        "fiv1_fivc_pred",
        "pif_pred",
        "fvc_image"};
    
    expectedOutputParameterNamesSet.addAll(new HashSet<String>(Arrays.asList(elements)));
  }
}