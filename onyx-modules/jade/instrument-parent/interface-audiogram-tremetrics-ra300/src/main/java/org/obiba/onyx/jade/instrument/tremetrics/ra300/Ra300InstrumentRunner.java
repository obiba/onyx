/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.tremetrics.ra300;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.instrument.tremetrics.ra300.Ra300App.SaveListener;
import org.obiba.onyx.jade.instrument.tremetrics.ra300.Ra300Test.Frequency;
import org.obiba.onyx.jade.instrument.tremetrics.ra300.Ra300Test.HTL;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Ra300InstrumentRunner implements InstrumentRunner {

  protected Logger log = LoggerFactory.getLogger(Ra300InstrumentRunner.class);

  private final Ra300 ra300 = new Ra300();

  private InstrumentExecutionService instrumentExecutionService;

  private String comPort;

  private int baudRate;

  private Ra300App app;

  private Ra300Test testToSave;

  public String getComPort() {
    return comPort;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public void setComPort(String comPort) {
    this.comPort = comPort;
  }

  public int getBaudRate() {
    return baudRate;
  }

  public void setBaudRate(int baudRate) {
    this.baudRate = baudRate;
  }

  @Override
  public void initialize() {
    try {
      app = Ra300App.build(ra300, new SaveListener() {

        @Override
        public void onSave(Ra300Test test) {
          testToSave = test;
        }
      }).get();
      app.setCommSettings(getComPort(), getBaudRate());
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    } catch(ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run() {
    app.waitForExit();
  }

  @Override
  public void shutdown() {
    // Send the test to Onyx
    if(testToSave != null) {
      Map<String, Data> onyxData = asOnyxData();
      Set<String> vendorNames = instrumentExecutionService.getExpectedOutputParameterVendorNames();
      for(Iterator<String> iterator = onyxData.keySet().iterator(); iterator.hasNext();) {
        String vendorName = iterator.next();
        if(vendorNames.contains(vendorName) == false) {
          iterator.remove();
        }
      }
      instrumentExecutionService.addOutputParameterValues(onyxData);
    }
  }

  private Map<String, Data> asOnyxData() {
    Map<String, Data> onyxData = new LinkedHashMap<String, Data>();
    onyxData.put("TEST_ID", DataBuilder.buildText(testToSave.getTestId()));
    onyxData.put("TEST_DATETIME", DataBuilder.buildDate(testToSave.getTestDatetime()));
    onyxData.put("CALIBRATION_DATE", DataBuilder.buildDate(testToSave.getCalibrationDate()));
    addHearingLevels(onyxData, "R", testToSave.getHTLRight());
    addHearingLevels(onyxData, "L", testToSave.getHTLLeft());
    return onyxData;
  }

  private void addHearingLevels(Map<String, Data> onyxData, String rightOrLeft, HTL htl) {
    for(Frequency freq : Frequency.values()) {
      if(htl.hasValue(freq)) {
        onyxData.put(rightOrLeft + freq.toString(), DataBuilder.buildInteger(htl.getLevel(freq)));
      }
    }
  }
}
