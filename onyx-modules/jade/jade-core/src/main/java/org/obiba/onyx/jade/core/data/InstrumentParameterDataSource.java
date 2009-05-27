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
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;

public class InstrumentParameterDataSource implements IDataSource {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  private String instrumentType;

  private String parameterCode;

  private Integer measure;

  private InstrumentRunService instrumentRunService;

  private InstrumentService instrumentService;

  public InstrumentParameterDataSource(String instrumentType, String parameterCode) {
    super();
    this.instrumentType = instrumentType;
    this.parameterCode = parameterCode;
  }

  //
  // IDataSource Methods
  //

  public Data getData(Participant participant) {
    Data data = null;

    if(participant == null) return null;

    InstrumentParameter param = getInstrumentParameter();
    InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValueFromLastRun(participant, instrumentService.getInstrumentType(instrumentType), parameterCode, measure);

    if(runValue != null) {
      data = runValue.getData(param.getDataType());
    }

    return data;
  }

  public String getUnit() {
    String unit = null;

    InstrumentParameter param = instrumentService.getInstrumentType(instrumentType).getInstrumentParameter(parameterCode);
    if(param != null) {
      unit = param.getMeasurementUnit();
    }

    return unit;
  }

  public InstrumentParameter getInstrumentParameter() {
    return instrumentService.getInstrumentType(instrumentType).getInstrumentParameter(parameterCode);
  }

  public String getInstrumentType() {
    return instrumentType;
  }

  //
  // Methods
  //

  public void setIntrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }
}
