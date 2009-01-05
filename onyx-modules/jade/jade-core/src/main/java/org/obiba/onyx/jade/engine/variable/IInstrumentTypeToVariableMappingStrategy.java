/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.variable;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.engine.variable.Entity;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;

/**
 * 
 */
public interface IInstrumentTypeToVariableMappingStrategy {

  public Entity getEntity(EntityQueryService queryService, InstrumentType type);

}
