/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.data;

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;

/**
 * Used to get output parameter data and unit
 */
public class OutputParameterDataSource implements IDataSource {

  private String instrumentType;

  private String parameterCode;

  private InstrumentRunService instrumentRunService;

  private InstrumentService instrumentService;

  public Data getData(Participant participant) {
    if(participant == null) return null;

    InstrumentOutputParameter outputParam = instrumentService.getInstrumentOutputParameter(instrumentService.getInstrumentType(instrumentType), parameterCode);
    InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(participant, instrumentService.getInstrumentType(instrumentType), parameterCode);

    if(runValue != null) return runValue.getData(outputParam.getDataType());
    return null;
  }

  public String getUnit() {
    InstrumentOutputParameter outputParam = instrumentService.getInstrumentOutputParameter(instrumentService.getInstrumentType(instrumentType), parameterCode);
    return (outputParam != null) ? outputParam.getMeasurementUnit() : null;
  }

  public OutputParameterDataSource(String instrumentType, String parameterCode) {
    super();
    if(instrumentType == null) throw new IllegalArgumentException("Instrument type cannot be null.");
    this.instrumentType = instrumentType;
    if(parameterCode == null) throw new IllegalArgumentException("Parameter code cannot be null.");
    this.parameterCode = parameterCode;
  }

  public void setIntrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

}
