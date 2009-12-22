/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.magma.DataValueConverter;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get data from a variable.
 */
public class VariableDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(VariableDataSource.class);

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  private String path;

  public VariableDataSource(String path) {
    this.path = path;
  }

  public Data getData(Participant participant) {
    log.debug("Fetching variable data for path: '{}'", path);
    if(participant == null) return null;

    Value value = getValue(participant);

    return DataValueConverter.valueToData(value);
  }

  private Value getValue(Participant participant) {
    VariableEntity entity = new VariableEntityBean("Participant", participant.getBarcode());
    ValueTable table = getParticipantValueTable();
    ValueSet valueSet = table.getValueSet(entity);

    return getVariableValueSource(table).getValue(valueSet);
  }

  public String getUnit() {
    ValueTable table = getParticipantValueTable();
    String magmaVariableUnit = getVariableValueSource(table).getVariable().getUnit();

    return magmaVariableUnit;
  }

  @Override
  public String toString() {
    return "Variable[" + path + "]";
  }

  private ValueTable getParticipantValueTable() {
    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable table : datasource.getValueTables()) {
        if(table.isForEntityType(PARTICIPANT_ENTITY_TYPE)) {
          return table;
        }
      }
    }
    throw new IllegalStateException("No ValueTable for entityType " + PARTICIPANT_ENTITY_TYPE);
  }

  private VariableValueSource getVariableValueSource(ValueTable table) {
    String magmaVariableName = path.replaceFirst("Onyx.", "");

    log.debug("Retrieving Magma variable {}:{}", table.getName(), magmaVariableName);
    try {
      return table.getVariableValueSource(magmaVariableName);
    } catch(NoSuchVariableException e) {
      log.error("[ONYX MAGMA MATCH FAILURE] No Magma variable found for the following name: {}", magmaVariableName);
      throw e;
    }
  }

}
