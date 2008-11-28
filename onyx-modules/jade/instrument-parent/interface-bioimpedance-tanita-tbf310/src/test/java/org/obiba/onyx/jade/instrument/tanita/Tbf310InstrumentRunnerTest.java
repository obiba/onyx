/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.tanita;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;

public class Tbf310InstrumentRunnerTest {

  private ExternalAppLauncherHelper externalAppHelper;
  private Tbf310InstrumentRunner tbf310InstrumentRunner;
  private InstrumentExecutionService instrumentExecutionServiceMock;
  
  @Before
  public void setUp() throws Exception{

    tbf310InstrumentRunner = new Tbf310InstrumentRunner();

    // Cannot mock ExternalAppLauncherHelper (without EasyMock extension!),
    // so for now, use the class itself with the launch method overridden to
    // do nothing.
    externalAppHelper = new ExternalAppLauncherHelper() {
      public void launch() {
        // do nothing
      }
    };
    tbf310InstrumentRunner.setExternalAppHelper(externalAppHelper);

    // Create a mock instrumentExecutionService for testing.
    instrumentExecutionServiceMock = createMock(InstrumentExecutionService.class);
    tbf310InstrumentRunner.setInstrumentExecutionService(instrumentExecutionServiceMock);
  }


  /**
   * Test the behaviour of the <code>run</code> method, when the instrument's output is
   * successfully retrieved (normal case).
   */
  @Test
  public void testRun(){
    externalAppHelper.launch();

    // Parse the results
    tbf310InstrumentRunner.setTanitaData(tbf310InstrumentRunner.parseTanitaData("0,2,170,110.6,431,28.4,31.4,79.2,58.0,27,32.3,9771"));

    // Compare the values read with the ones from the input stream
    Assert.assertEquals("STANDARD",tbf310InstrumentRunner.getBodyTypeTxt().getText());
    Assert.assertEquals("FEMALE",tbf310InstrumentRunner.getGenderTxt().getText());
    Assert.assertEquals(String.valueOf(170),tbf310InstrumentRunner.getHeightTxt().getText());
    Assert.assertEquals(String.valueOf(110.6),tbf310InstrumentRunner.getWeightTxt().getText());
    Assert.assertEquals(String.valueOf(431),tbf310InstrumentRunner.getImpedanceTxt().getText());
    Assert.assertEquals(String.valueOf(28.4),tbf310InstrumentRunner.getFatPctTxt().getText());
    Assert.assertEquals(String.valueOf(31.4),tbf310InstrumentRunner.getFatMassTxt().getText());
    Assert.assertEquals(String.valueOf(79.2),tbf310InstrumentRunner.getFfmTxt().getText());
    Assert.assertEquals(String.valueOf(58.0),tbf310InstrumentRunner.getTbwTxt().getText());
    Assert.assertEquals(String.valueOf(27),tbf310InstrumentRunner.getAgeTxt().getText());
    Assert.assertEquals(String.valueOf(32.3),tbf310InstrumentRunner.getBmiTxt().getText());
    Assert.assertEquals(String.valueOf(9771),tbf310InstrumentRunner.getBmrTxt().getText());
   
    instrumentExecutionServiceMock.addOutputParameterValues((Map<String, Data>) anyObject());
    }

  @Test 
  public void testShutdown(){
    tbf310InstrumentRunner.shutdown();
  }
}
