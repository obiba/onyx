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
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.engine.variable.IInstrumentTypeToVariableMappingStrategy;
import org.obiba.onyx.util.data.Data;

/**
 * 
 */
public class DefaultInstrumentTypeToVariableMappingStrategy implements IInstrumentTypeToVariableMappingStrategy {

  private static final String INPUT = "Input";

  private static final String OUTPUT = "Output";

  private static final String INTERPRETIVE = "Interpretive";

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
    List<InstrumentParameter> parameters = type.getInstrumentParameters();
    if(parameters.size() > 0) {
      for(InstrumentParameter parameter : parameters) {
        Variable parameterType;
        if(parameter instanceof InstrumentInputParameter) {
          parameterType = new Variable(INPUT);
        } else if(parameter instanceof InstrumentOutputParameter) {
          parameterType = new Variable(OUTPUT);
        } else if(parameter instanceof InterpretativeParameter) {
          parameterType = new Variable(INTERPRETIVE);
        } else {
          throw new IllegalStateException("Unknown instrument parameter type: " + parameter.getClass().getSimpleName());
        }
        typeVariable.addVariable(parameterType);
        parameterType.addVariable(new Variable(parameter.getCode()).setDataType(parameter.getDataType()));
      }
    }
    return typeVariable;
  }

  public Data getData(Variable variable, Participant participant) {
    // variable is expected to be a terminal one
    if(variable == null || variable.getParent() == null || variable.getParent().getParent() == null) {
      throw new IllegalArgumentException("Invalid variable hierarchy: " + variable);
    }

    String parameterCode = variable.getName();
    String instrumentTypeName = variable.getParent().getParent().getName();

    InstrumentType type = instrumentService.getInstrumentType(instrumentTypeName);
    if(type == null) return null;

    InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(participant, type, parameterCode);

    return (runValue != null) ? runValue.getData() : null;
  }

}
