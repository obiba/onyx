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
import org.obiba.onyx.engine.variable.Entity;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.engine.variable.IInstrumentTypeToVariableMappingStrategy;

/**
 * 
 */
public class DefaultInstrumentTypeToVariableMappingStrategy implements IInstrumentTypeToVariableMappingStrategy {

  public Entity getEntity(EntityQueryService queryService, InstrumentType type) {
    Entity typeEntity = new Entity(type.getName());

    InstrumentParameter template = new InstrumentInputParameter();
    template.setInstrumentType(type);
    Entity entity = new Entity("In");
    typeEntity.addEntity(entity);
    for(InstrumentParameter parameter : queryService.match(template)) {
      entity.addEntity(new Variable(parameter.getName()).setDataType(parameter.getDataType()));
    }

    template = new InstrumentOutputParameter();
    template.setInstrumentType(type);
    entity = new Entity("Out");
    typeEntity.addEntity(entity);
    for(InstrumentParameter parameter : queryService.match(template)) {
      entity.addEntity(new Variable(parameter.getName()).setDataType(parameter.getDataType()));
    }

    return typeEntity;
  }

}
