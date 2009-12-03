/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.magma;

import java.util.Set;

import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;

/**
 * Provider of Workstation ValueSets.
 */
public class WorkstationValueSetProvider implements VariableEntityProvider {
  //
  // Constants
  //

  public static final String WORKSTATION = "Workstation";

  //
  // Instance Variables
  //

  @Autowired(required = true)
  private InstrumentService instrumentService;

  //
  // ValueSetProvider Methods
  //

  public String getEntityType() {
    return WORKSTATION;
  }

  public Set<VariableEntity> getVariableEntities() {
    ImmutableSet.Builder<VariableEntity> builder = new ImmutableSet.Builder<VariableEntity>();

    for(String instrumentTypeName : instrumentService.getInstrumentTypes().keySet()) {
      for(Instrument instrument : instrumentService.getInstruments(instrumentTypeName)) {
        String workstationName = instrument.getWorkstation();
        if(workstationName != null) {
          builder.add(new VariableEntityBean(WORKSTATION, workstationName));
        }
      }
    }

    return builder.build();
  }

  public boolean isForEntityType(String entityType) {
    return entityType.equals(getEntityType());
  }

  //
  // Methods
  //

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }
}
