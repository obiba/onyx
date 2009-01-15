/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.variable.impl;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.engine.variable.IInstrumentTypeToVariableMappingStrategy;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class DefaultInstrumentTypeToVariableMappingStrategy implements IInstrumentTypeToVariableMappingStrategy {

  public static final String INSTRUMENT_RUN = "InstrumentRun";

  public static final String INSTRUMENT = "Instrument";

  public static final String NAME = "name";

  public static final String VENDOR = "vendor";

  public static final String MODEL = "model";

  public static final String SERIAL_NUMBER = "serialNumber";

  public static final String BARCODE = "barcode";

  public static final String USER = "user";

  public static final String USER_KEY = "user";

  public static final String TIMESTART = "timeStart";

  public static final String TIMEEND = "timeEnd";

  public static final String CONTRAINDICATION = "Contraindication";

  public static final String CONTRAINDICATION_CODE = "code";

  public static final String CONTRAINDICATION_TYPE = "type";

  public static final String OTHER_CONTRAINDICATION = "otherContraindication";

  public static final String INPUT = "Input";

  public static final String OUTPUT = "Output";

  public static final String INTERPRETIVE = "Interpretive";

  private InstrumentRunService instrumentRunService;

  private InstrumentService instrumentService;

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public Variable getVariable(InstrumentType type) {
    Variable typeVariable = new Variable(type.getName());

    // instrument run
    Variable runVariable = typeVariable.addVariable(new Variable(INSTRUMENT_RUN));

    runVariable.addVariable(new Variable(USER).setDataType(DataType.TEXT).setKey(USER_KEY));
    runVariable.addVariable(new Variable(TIMESTART).setDataType(DataType.DATE));
    runVariable.addVariable(new Variable(TIMEEND).setDataType(DataType.DATE));
    runVariable.addVariable(new Variable(OTHER_CONTRAINDICATION).setDataType(DataType.TEXT));

    Variable ciVariable = runVariable.addVariable(new Variable(CONTRAINDICATION));
    ciVariable.addVariable(new Variable(CONTRAINDICATION_CODE).setDataType(DataType.TEXT));
    ciVariable.addVariable(new Variable(CONTRAINDICATION_TYPE).setDataType(DataType.TEXT));

    Variable instrumentVariable = runVariable.addVariable(new Variable(INSTRUMENT));
    instrumentVariable.addVariable(new Variable(NAME).setDataType(DataType.TEXT));
    instrumentVariable.addVariable(new Variable(VENDOR).setDataType(DataType.TEXT));
    instrumentVariable.addVariable(new Variable(MODEL).setDataType(DataType.TEXT));
    instrumentVariable.addVariable(new Variable(SERIAL_NUMBER).setDataType(DataType.TEXT));
    instrumentVariable.addVariable(new Variable(BARCODE).setDataType(DataType.TEXT));

    // instrument parameters
    List<InstrumentParameter> parameters = type.getInstrumentParameters();
    if(parameters.size() > 0) {
      for(InstrumentParameter parameter : parameters) {
        Variable parameterType;
        if(parameter instanceof InstrumentInputParameter) {
          parameterType = typeVariable.getVariable(INPUT);
          if(parameterType == null) {
            parameterType = new Variable(INPUT);
            typeVariable.addVariable(parameterType);
          }
        } else if(parameter instanceof InstrumentOutputParameter) {
          parameterType = typeVariable.getVariable(OUTPUT);
          if(parameterType == null) {
            parameterType = new Variable(OUTPUT);
            typeVariable.addVariable(parameterType);
          }
        } else if(parameter instanceof InterpretativeParameter) {
          parameterType = typeVariable.getVariable(INTERPRETIVE);
          if(parameterType == null) {
            parameterType = new Variable(INTERPRETIVE);
            typeVariable.addVariable(parameterType);
          }
        } else {
          throw new IllegalStateException("Unknown instrument parameter type: " + parameter.getClass().getSimpleName());
        }
        parameterType.addVariable(new Variable(parameter.getCode()).setDataType(parameter.getDataType()));
      }
    }
    return typeVariable;
  }

  public Data getData(Participant participant, Variable variable) {
    // variable is expected to be a terminal one
    if(variable == null || variable.getDataType() == null) {
      return null;
    }

    Data rval = null;

    if(variable.getParent().getName().equals(INSTRUMENT)) {
      InstrumentRun run = getInstrumentRun(participant, getInstrumentTypeVariable(variable).getName());
      if(run != null && run.getInstrument() != null) {
        Instrument instrument = run.getInstrument();
        if(variable.getName().equals(NAME) && instrument.getName() != null) {
          rval = DataBuilder.buildText(instrument.getName());
        } else if(variable.getName().equals(VENDOR) && instrument.getVendor() != null) {
          rval = DataBuilder.buildText(instrument.getVendor());
        } else if(variable.getName().equals(MODEL) && instrument.getModel() != null) {
          rval = DataBuilder.buildText(instrument.getModel());
        } else if(variable.getName().equals(SERIAL_NUMBER) && instrument.getSerialNumber() != null) {
          rval = DataBuilder.buildText(instrument.getSerialNumber());
        } else if(variable.getName().equals(BARCODE) && instrument.getBarcode() != null) {
          rval = DataBuilder.buildText(instrument.getBarcode());
        }
      }
    } else if(variable.getParent().getName().equals(CONTRAINDICATION)) {
      InstrumentRun run = getInstrumentRun(participant, getInstrumentTypeVariable(variable).getName());
      if(run != null) {
        if(variable.getName().equals(CONTRAINDICATION_CODE) && run.getContraindication() != null) {
          rval = DataBuilder.buildText(run.getContraindication().getCode());
        } else if(variable.getName().equals(CONTRAINDICATION_TYPE) && run.getContraindication() != null) {
          rval = DataBuilder.buildText(run.getContraindication().getType().toString());
        }
      }
    } else if(variable.getParent().getName().equals(INSTRUMENT_RUN)) {
      InstrumentRun run = getInstrumentRun(participant, getInstrumentTypeVariable(variable).getName());
      if(run != null) {
        if(variable.getName().equals(USER) && run.getUser() != null) {
          rval = DataBuilder.buildText(run.getUser().getLogin());
        } else if(variable.getName().equals(TIMESTART) && run.getTimeStart() != null) {
          rval = DataBuilder.buildDate(run.getTimeStart());
        } else if(variable.getName().equals(TIMEEND) && run.getTimeEnd() != null) {
          rval = DataBuilder.buildDate(run.getTimeEnd());
        } else if(variable.getName().equals(OTHER_CONTRAINDICATION) && run.getOtherContraindication() != null) {
          rval = DataBuilder.buildText(run.getOtherContraindication());
        }
      }
    } else if(variable.getParent().getName().equals(INPUT) || variable.getParent().getName().equals(OUTPUT) || variable.getParent().getName().equals(INTERPRETIVE)) {
      String parameterCode = variable.getName();
      String instrumentTypeName = getInstrumentTypeVariable(variable).getName();

      InstrumentType type = instrumentService.getInstrumentType(instrumentTypeName);
      if(type == null) return null;

      InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(participant, type, parameterCode);

      if(runValue != null) {
        rval = runValue.getData();
      }
    }

    return rval;
  }

  private InstrumentRun getInstrumentRun(Participant participant, String instrumentTypeName) {
    InstrumentType type = instrumentService.getInstrumentType(instrumentTypeName);
    return instrumentRunService.getLastCompletedInstrumentRun(participant, type);
  }

  public Variable getInstrumentTypeVariable(Variable variable) {
    Variable instrumentTypeVariable = variable;

    while(instrumentTypeVariable.getParent() != null && instrumentTypeVariable.getParent().getParent() != null) {
      instrumentTypeVariable = instrumentTypeVariable.getParent();
    }

    return instrumentTypeVariable;
  }

}
