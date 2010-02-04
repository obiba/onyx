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
import java.text.SimpleDateFormat;
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
import org.obiba.onyx.util.FileUtil;
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
    inputData.put("INPUT_PARTICIPANT_BARCODE", DataBuilder.buildText("123456789"));
    inputData.put("INPUT_PARTICIPANT_LAST_NAME", DataBuilder.buildText("Tremblay"));
    inputData.put("INPUT_PARTICIPANT_FIRST_NAME", DataBuilder.buildText("Chantal"));
    inputData.put("INPUT_PARTICIPANT_GENDER", DataBuilder.buildText("FEMALE"));
    inputData.put("INPUT_PARTICIPANT_HEIGHT", DataBuilder.buildInteger(178));
    inputData.put("INPUT_PARTICIPANT_WEIGHT", DataBuilder.buildDecimal(76.4));
    inputData.put("INPUT_PARTICIPANT_ETHNIC_GROUP", DataBuilder.buildInteger(1));
    inputData.put("INPUT_PARTICIPANT_BIORTH_DATE", DataBuilder.buildDate(getBirthDate()));

    Map<String, String> inputKeyTranslation = new HashMap<String, String>();
    inputKeyTranslation.put("INPUT_PARTICIPANT_BARCODE", "ID");
    inputKeyTranslation.put("INPUT_PARTICIPANT_LAST_NAME", "LastName");
    inputKeyTranslation.put("INPUT_PARTICIPANT_FIRST_NAME", "FirstName");
    inputKeyTranslation.put("INPUT_PARTICIPANT_GENDER", "Gender");
    inputKeyTranslation.put("INPUT_PARTICIPANT_HEIGHT", "Height");
    inputKeyTranslation.put("INPUT_PARTICIPANT_WEIGHT", "Weight");
    inputKeyTranslation.put("INPUT_PARTICIPANT_ETHNIC_GROUP", "EthnicGroup");
    inputKeyTranslation.put("INPUT_PARTICIPANT_BIORTH_DATE", "BirthDate");

    expect(instrumentExecutionServiceMock.getInputParametersValue("INPUT_PARTICIPANT_BARCODE", "INPUT_PARTICIPANT_LAST_NAME", "INPUT_PARTICIPANT_FIRST_NAME", "INPUT_PARTICIPANT_GENDER", "INPUT_PARTICIPANT_HEIGHT", "INPUT_PARTICIPANT_WEIGHT", "INPUT_PARTICIPANT_ETHNIC_GROUP", "INPUT_PARTICIPANT_DATE_BIRTH")).andReturn(inputData);
    expect(instrumentExecutionServiceMock.getInputParametersVendorNames("INPUT_PARTICIPANT_BARCODE", "INPUT_PARTICIPANT_LAST_NAME", "INPUT_PARTICIPANT_FIRST_NAME", "INPUT_PARTICIPANT_GENDER", "INPUT_PARTICIPANT_HEIGHT", "INPUT_PARTICIPANT_WEIGHT", "INPUT_PARTICIPANT_ETHNIC_GROUP", "INPUT_PARTICIPANT_DATE_BIRTH")).andReturn(inputKeyTranslation);
    SimpleDateFormat birthDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    expect(instrumentExecutionServiceMock.getDateAsString("INPUT_PARTICIPANT_DATE_BIRTH", birthDateFormatter)).andReturn("1965-01-01");
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
    LinkedHashMap<String, Double[]> results = minispirInstrumentRunner.retrieveDeviceData(minispirInstrumentRunner.getExternalOutputName());

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
    String elements[] = { "FVC", "FEV1", "FEV1_FVC", "FEV6", "FEV1_FEV6", "PEF", "FEF2575", "FEV3", "FEV3_FVC", "ELA", "FET", "FEF25", "FEF50", "FEF75", "EVol", "FIVC", "FIV1", "FIV1_FIVC", "PIF" };
    expectedOutputParameterNamesSet.addAll(new HashSet<String>(Arrays.asList(elements)));
  }
}
