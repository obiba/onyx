/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.summitdoppler;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;

public class VantageABIInstrumentRunnerTest {

  private ExternalAppLauncherHelper externalAppHelper;

  private VantageABIInstrumentRunner runner;

  private InstrumentExecutionService instrumentExecutionServiceMock;

  @Before
  public void setUp() throws Exception {

    runner = new VantageABIInstrumentRunner();

    // Cannot mock ExternalAppLauncherHelper (without EasyMock extension!),
    // so for now, use the class itself with the launch method overridden to
    // do nothing.
    externalAppHelper = new ExternalAppLauncherHelper() {
      public void launch() {
        // do nothing
      }
    };
    runner.setExternalAppHelper(externalAppHelper);

    // Create a mock instrumentExecutionService for testing.
    instrumentExecutionServiceMock = createMock(InstrumentExecutionService.class);
    runner.setInstrumentExecutionService(instrumentExecutionServiceMock);
  }

  /**
   * Test the behaviour of the <code>run</code> method, when the instrument's output is successfully retrieved (normal
   * case).
   */
  @Test
  public void testRun() {
    externalAppHelper.launch();

    // Parse the results
    // runner.setTanitaData(runner.parseTanitaData("0,2,170,110.6,431,28.4,31.4,79.2,58.0,27,32.3,9771"));

    // Compare the values read with the ones from the input stream
    // Assert.assertEquals("STANDARD", tbf310InstrumentRunner.getBodyTypeTxt().getText());
    // Assert.assertEquals("FEMALE", tbf310InstrumentRunner.getGenderTxt().getText());
    // Assert.assertEquals(String.valueOf(170), tbf310InstrumentRunner.getHeightTxt().getText());

    instrumentExecutionServiceMock.addOutputParameterValues((Map<String, Data>) anyObject());
  }

  @Test
  public void testShutdown() {
    runner.shutdown();
  }
}
