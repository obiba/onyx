/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.vsm.bptru;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.vsm.bptru.bpm.Data.AvgPressure;
import org.obiba.vsm.bptru.bpm.Data.BloodPressure;
import org.obiba.vsm.bptru.bpm.gui.BpTru;
import org.obiba.vsm.bptru.bpm.gui.BpTruResultListener;
import org.springframework.beans.factory.annotation.Autowired;

public class VsmBpTruInstrumentRunner implements InstrumentRunner {

  private final InstrumentExecutionService instrumentExcecutionService;

  private BpTru window;

  private Set<String> expectedNames;

  @Autowired
  public VsmBpTruInstrumentRunner(InstrumentExecutionService instrumentExcecutionService) {
    this.instrumentExcecutionService = instrumentExcecutionService;
  }

  @Override
  public void initialize() {
    expectedNames = instrumentExcecutionService.getExpectedOutputParameterVendorNames();
  }

  @Override
  public void run() {
    window = new BpTru();

    window.addResultListener(new BpTruResultListener() {
      @Override
      public void onBpResult(int readingNumber, Date startTime, Date endTime, BloodPressure result) {
        // We always send the first measurement even if it has errors (we send nulls).
        if(readingNumber == 1 && wantsFirst()) {
          instrumentExcecutionService.addOutputParameterValues(asData("First", startTime, endTime, result));
        } else {
          // We don't send failed measurements
          if(result.hasError() == false) {
            instrumentExcecutionService.addOutputParameterValues(asData(startTime, endTime, result));
          }
        }
      }

      public void onAvgResult(org.obiba.vsm.bptru.bpm.Data.AvgPressure result) {
        if(wantsAverage()) {
          instrumentExcecutionService.addOutputParameterValues(asData(result));
        }
      };
    });

    // We need to block here
    window.waitForExit();

  }

  @Override
  public void shutdown() {
  }

  private boolean wantsFirst() {
    return wantsAny("FirstStartTime", "FirstEndTime", "FirstSytolic", "FirstDiastolic", "FirstPulse");
  }

  private boolean wantsAverage() {
    return wantsAny("AvgSytolic", "AvgDiastolic", "AvgPulse", "AvgCount");
  }

  private boolean wantsAny(String... names) {
    for(String name : names) {
      if(expectedNames.contains(name)) return true;
    }
    return false;
  }

  private Map<String, Data> asData(Date startTime, Date endTime, BloodPressure result) {
    return asData("", startTime, endTime, result);
  }

  private Map<String, Data> asData(String prefix, Date startTime, Date endTime, BloodPressure result) {
    Map<String, Data> values = new LinkedHashMap<String, Data>();
    values.put(prefix + "StartTime", DataBuilder.buildDate(startTime));
    values.put(prefix + "EndTime", DataBuilder.buildDate(endTime));
    values.put(prefix + "Systolic", DataBuilder.buildInteger(result.hasError() ? null : result.sbp()));
    values.put(prefix + "Diastolic", DataBuilder.buildInteger(result.hasError() ? null : result.dbp()));
    values.put(prefix + "Pulse", DataBuilder.buildInteger(result.hasError() ? null : result.pulse()));
    return values;
  }

  private Map<String, Data> asData(AvgPressure result) {
    Map<String, Data> values = new LinkedHashMap<String, Data>();
    addIfWanted(values, "AvgSytolic", DataBuilder.buildInteger(result.sbp()));
    addIfWanted(values, "AvgDiastolic", DataBuilder.buildInteger(result.dbp()));
    addIfWanted(values, "AvgPulse", DataBuilder.buildInteger(result.pulse()));
    addIfWanted(values, "AvgCount", DataBuilder.buildInteger(result.count()));
    return values;
  }

  private void addIfWanted(Map<String, Data> values, String name, Data data) {
    if(wantsAny(name)) values.put(name, data);
  }
}
