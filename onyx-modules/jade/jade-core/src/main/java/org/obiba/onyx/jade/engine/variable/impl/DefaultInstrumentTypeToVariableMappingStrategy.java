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

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.engine.variable.IInstrumentTypeToVariableMappingStrategy;
import org.obiba.onyx.util.data.Data;

/**
 * 
 */
public class DefaultInstrumentTypeToVariableMappingStrategy implements IInstrumentTypeToVariableMappingStrategy {

  private static final String IN = "In";

  private static final String OUT = "Out";

  public Variable getVariable(EntityQueryService queryService, InstrumentType type) {
    Variable typeEntity = new Variable(type.getName());

    InstrumentParameter template = new InstrumentInputParameter();
    template.setInstrumentType(type);
    Variable entity = new Variable(IN);
    typeEntity.addVariable(entity);
    for(InstrumentParameter parameter : queryService.match(template)) {
      entity.addVariable(new Variable(parameter.getName()).setDataType(parameter.getDataType()));
    }

    template = new InstrumentOutputParameter();
    template.setInstrumentType(type);
    entity = new Variable(OUT);
    typeEntity.addVariable(entity);
    for(InstrumentParameter parameter : queryService.match(template)) {
      entity.addVariable(new Variable(parameter.getName()).setDataType(parameter.getDataType()));
    }

    return typeEntity;
  }

  public Data getData(EntityQueryService queryService, InstrumentRunService instrumentRunService, Participant participant, Variable variable) {
    // variable is expected to be a terminal one
    Variable typeVariable = variable.getParent().getParent();
    InstrumentType type = new InstrumentType();
    type.setName(typeVariable.getName());
    type = queryService.matchOne(type);
    if(type == null) return null;

    InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(participant, type, variable.getName());

    return (runValue != null) ? runValue.getData() : null;
  }

}
