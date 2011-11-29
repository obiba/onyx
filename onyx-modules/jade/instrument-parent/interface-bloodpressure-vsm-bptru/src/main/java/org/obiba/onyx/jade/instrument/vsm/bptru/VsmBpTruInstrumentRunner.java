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

  @Autowired
  public VsmBpTruInstrumentRunner(InstrumentExecutionService instrumentExcecutionService) {
    this.instrumentExcecutionService = instrumentExcecutionService;
  }

  @Override
  public void initialize() {
  }

  @Override
  public void run() {
    window = new BpTru();

    window.addResultListener(new BpTruResultListener() {
      @Override
      public void onBpResult(int readingNumber, Date startTime, Date endTime, BloodPressure result) {
        // We don't send the first measurement nor any measurement that has an error
        if(result.hasError() == false) {
          if(readingNumber == 1) {
            instrumentExcecutionService.addOutputParameterValues(asData("FIRST_", startTime, endTime, result));
          } else if(readingNumber > 1) {
            instrumentExcecutionService.addOutputParameterValues(asData(startTime, endTime, result));
          }
        }
      }

      public void onAvgResult(org.obiba.vsm.bptru.bpm.Data.AvgPressure result) {
        instrumentExcecutionService.addOutputParameterValues(asData(result));
      };
    });

    // We need to block here
    window.waitForExit();

  }

  @Override
  public void shutdown() {
  }

  private Map<String, Data> asData(Date startTime, Date endTime, BloodPressure result) {
    return asData("", startTime, endTime, result);
  }

  private Map<String, Data> asData(String prefix, Date startTime, Date endTime, BloodPressure result) {
    Map<String, Data> values = new LinkedHashMap<String, Data>();
    values.put(prefix + "RES_START_TIME", DataBuilder.buildDate(startTime));
    values.put(prefix + "RES_END_TIME", DataBuilder.buildDate(endTime));
    values.put(prefix + "RES_SYSTOLIC", DataBuilder.buildInteger(result.sbp()));
    values.put(prefix + "RES_DIASTOLIC", DataBuilder.buildInteger(result.dbp()));
    values.put(prefix + "RES_PULSE", DataBuilder.buildInteger(result.pulse()));
    return values;
  }

  private Map<String, Data> asData(AvgPressure result) {
    Map<String, Data> values = new LinkedHashMap<String, Data>();
    values.put("RES_AVG_SYSTOLIC", DataBuilder.buildInteger(result.sbp()));
    values.put("RES_AVG_DIASTOLIC", DataBuilder.buildInteger(result.dbp()));
    values.put("RES_AVG_PULSE", DataBuilder.buildInteger(result.pulse()));
    return values;
  }
}
