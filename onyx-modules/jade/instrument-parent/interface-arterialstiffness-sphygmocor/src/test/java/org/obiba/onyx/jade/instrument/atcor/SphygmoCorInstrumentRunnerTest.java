/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.atcor;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.atcor.dao.SphygmoCorDao;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class SphygmoCorInstrumentRunnerTest {

  private ExternalAppLauncherHelper externalAppHelper;

  private SphygmoCorInstrumentRunner sphygmoCorInstrumentRunner;

  private SphygmoCorDao sphygmoCorDaoMock;

  private InstrumentExecutionService instrumentExecutionServiceMock;

  @Before
  public void setUp() {
    sphygmoCorInstrumentRunner = new SphygmoCorInstrumentRunner();

    // Cannot mock ExternalAppLauncherHelper (without EasyMock extension!),
    // so for now, use the class itself with the launch method overridden to
    // do nothing.
    externalAppHelper = new ExternalAppLauncherHelper() {
      public void launch() {
        // do nothing
      }
    };
    externalAppHelper.setWorkDir("target");

    sphygmoCorInstrumentRunner.setExternalAppHelper(externalAppHelper);

    sphygmoCorDaoMock = createMock(SphygmoCorDao.class);
    sphygmoCorInstrumentRunner.setSphygmoCorDao(sphygmoCorDaoMock);

    instrumentExecutionServiceMock = createMock(InstrumentExecutionService.class);
    sphygmoCorInstrumentRunner.setInstrumentExecutionService(instrumentExecutionServiceMock);
  }

  /**
   * Test that the <code>initialize</code> method does the following:
   * 
   * <ul>
   * <li>Deletes all instrument data (measurements and patients)</li>
   * <li>Fetches the current participant</li>
   * <li>Adds the current participant as a patient</li>
   * </ul>
   */
  @Test
  public void testInitialize() {
    // Expect that all instrument data are deleted.
    sphygmoCorDaoMock.deleteAllOutput();
    sphygmoCorDaoMock.deleteAllPatients();

    // Expect that the current participant is retrieved...
    String participantLastName = "Tremblay";
    String participantFirstName = "Chantal";
    java.util.Date participantBirthDate = getBirthDate();
    String participantGender = "FEMALE";
    int participantBarcode = 123;
    long systolicPressure = 123;
    long diastolicPressure = 65;

    expect(instrumentExecutionServiceMock.getInputParameterValue("INPUT_PARTICIPANT_LAST_NAME")).andReturn(DataBuilder.buildText(participantLastName));
    expect(instrumentExecutionServiceMock.getInputParameterValue("INPUT_PARTICIPANT_FIRST_NAME")).andReturn(DataBuilder.buildText(participantFirstName));
    expect(instrumentExecutionServiceMock.getInputParameterValue("INPUT_PARTICIPANT_DATE_BIRTH")).andReturn(DataBuilder.buildDate(participantBirthDate));
    expect(instrumentExecutionServiceMock.getInputParameterValue("INPUT_PARTICIPANT_GENDER")).andReturn(DataBuilder.buildText(participantGender));
    expect(instrumentExecutionServiceMock.getInputParameterValue("INPUT_PARTICIPANT_BARCODE")).andReturn(DataBuilder.buildInteger(participantBarcode));
    expect(instrumentExecutionServiceMock.getInputParameterValue("INPUT_SYSTOLIC_PRESSURE")).andReturn(DataBuilder.buildInteger(systolicPressure));
    expect(instrumentExecutionServiceMock.getInputParameterValue("INPUT_DIASTOLIC_PRESSURE")).andReturn(DataBuilder.buildInteger(diastolicPressure));
    expect(instrumentExecutionServiceMock.getInputParameterValue("SPHYGMOCOR_SYSTEM_ID")).andReturn(DataBuilder.buildText("01400"));

    // ...and added as a patient.
    // sphygmoCorDaoMock.addPatient(SYSTEM_ID, STUDY_ID, participantId, 1, participantLastName, participantFirstName,
    // new java.sql.Date(participantBirthDate.getTime()), participantGender);

    replay(sphygmoCorDaoMock);
    replay(instrumentExecutionServiceMock);

    sphygmoCorInstrumentRunner.initialize();

    verify(sphygmoCorDaoMock);
    verify(instrumentExecutionServiceMock);
  }

  /**
   * Test the behaviour of the <code>run</code> method, when the instrument's output is successfully retrieved (normal
   * case).
   */
  // @Test
  public void testRunOutputNotNull() {

    // Expect that the measurements taken for the current participant are retrieved,
    // with a non-null return value.
    expect(sphygmoCorDaoMock.getOutput(1)).andReturn(getOutput());

    // Expect that the measurements are sent to the server.
    instrumentExecutionServiceMock.addOutputParameterValues(formatOutputForServer(getOutput().get(0)));

    replay(instrumentExecutionServiceMock);
    replay(sphygmoCorDaoMock);

    sphygmoCorInstrumentRunner.run();

    verify(instrumentExecutionServiceMock);
    verify(sphygmoCorDaoMock);
  }

  /**
   * Test the behaviour of the <code>run</code> method, when the instrument's output is <code>null</code> (error
   * case).
   */
  @Test
  public void testRunOutputIsNull() {

    // Expect that the measurements taken for the current participant are retrieved,
    // with a null return value. This could happen if the SphygmoCor application is
    // closed prematurely.
    expect(sphygmoCorDaoMock.getOutput(123)).andReturn(null);

    replay(instrumentExecutionServiceMock);
    replay(sphygmoCorDaoMock);

    // The run method should NOT throw an exception in this case. It simply
    // logs an error message.
    sphygmoCorInstrumentRunner.run();

    verify(sphygmoCorDaoMock);
    verify(instrumentExecutionServiceMock);
  }

  /**
   * Test that the <code>shutdown</code> method deletes all instrument data (measurements and patients).
   */
  @Test
  public void testShutdown() {
    sphygmoCorDaoMock.deleteAllOutput();
    sphygmoCorDaoMock.deleteAllPatients();

    replay(sphygmoCorDaoMock);

    sphygmoCorInstrumentRunner.shutdown();

    verify(sphygmoCorDaoMock);
  }

  private java.util.Date getBirthDate() {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.YEAR, 1964);
    c.set(Calendar.MONTH, 2);
    c.set(Calendar.DAY_OF_MONTH, 12);

    return c.getTime();
  }

  private List<Map> getOutput() {
    List<Map> output = new ArrayList<Map>();

    Map outputMap = new HashMap();

    outputMap.put("P_QC_PH", new Float(1.0f));
    outputMap.put("P_QC_PHV", new Float(1.0f));
    outputMap.put("P_QC_PLV", new Float(1.0f));
    outputMap.put("P_QC_DV", new Float(1.0f));
    outputMap.put("P_SP", new Float(1.0f));
    outputMap.put("P_DP", new Float(1.0f));
    outputMap.put("P_MEANP", new Float(1.0f));
    outputMap.put("P_T1", new Float(1.0f));
    outputMap.put("P_T2", new Float(1.0f));
    outputMap.put("P_AI", new Float(1.0f));
    outputMap.put("P_ESP", new Float(1.0f));
    outputMap.put("P_P1", new Float(1.0f));
    outputMap.put("P_P2", new Float(1.0f));
    outputMap.put("P_QUALITY_T1", new Integer(1));
    outputMap.put("P_QUALITY_T2", new Integer(1));
    outputMap.put("C_AP", new Float(1.0f));
    outputMap.put("C_MPS", new Float(1.0f));
    outputMap.put("C_MPD", new Float(1.0f));
    outputMap.put("C_TTI", new Float(1.0f));
    outputMap.put("C_DTI", new Float(1.0f));
    outputMap.put("C_SVI", new Float(1.0f));
    outputMap.put("C_AL", new Float(1.0f));
    outputMap.put("C_ATI", new Float(1.0f));
    outputMap.put("HR", new Float(1.0f));
    outputMap.put("C_PERIOD", new Float(1.0f));
    outputMap.put("C_DD", new Float(1.0f));
    outputMap.put("C_ED_PERIOD", new Float(1.0f));
    outputMap.put("C_DD_PERIOD", new Float(1.0f));
    outputMap.put("C_PH", new Float(1.0f));
    outputMap.put("C_AGPH", new Float(1.0f));
    outputMap.put("C_P1_HEIGHT", new Float(1.0f));
    outputMap.put("C_T1R", new Float(1.0f));
    outputMap.put("C_SP", new Float(1.0f));
    outputMap.put("C_DP", new Float(1.0f));
    outputMap.put("C_MEANP", new Float(1.0f));
    outputMap.put("C_T1", new Float(1.0f));
    outputMap.put("C_T2", new Float(1.0f));
    outputMap.put("C_AI", new Float(1.0f));
    outputMap.put("C_ESP", new Float(1.0f));
    outputMap.put("C_P1", new Float(1.0f));
    outputMap.put("C_P2", new Float(1.0f));
    outputMap.put("C_T1ED", new Float(1.0f));
    outputMap.put("C_T2ED", new Float(1.0f));
    outputMap.put("C_QUALITY_T1", new Integer(1));
    outputMap.put("C_QUALITY_T2", new Integer(1));
    outputMap.put("P_QC_OTHER4", new Float(1.0f));

    output.add(outputMap);

    return output;
  }

  private Map<String, Data> formatOutputForServer(Map data) {
    Map<String, Data> outputToSend = new HashMap<String, Data>();

    Iterator dataIter = data.entrySet().iterator();

    while(dataIter.hasNext()) {
      Map.Entry dataEntry = (Map.Entry) dataIter.next();

      Data dataObj = null;

      Object value = dataEntry.getValue();

      if(value instanceof Float) {
        dataObj = new Data(DataType.DECIMAL, new Double((Float) value));
      } else if(value instanceof Integer) {
        dataObj = new Data(DataType.INTEGER, new Long((Integer) value));
      }

      outputToSend.put((String) dataEntry.getKey(), dataObj);
    }

    return outputToSend;
  }
}
