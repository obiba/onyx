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
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.magma.DataValueConverter;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get data from a variable.
 */
public class VariableDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(VariableDataSource.class);

  private String path;

  /**
   * Cached datasource name.
   */
  private transient String datasourceName;

  /**
   * Cached table name.
   */
  private transient String tableName;

  /**
   * Cached variable name.
   */
  private transient String variableName;

  /**
   * Flag indicating whether path elements have been extracted (only want to do this once).
   */
  private transient boolean pathElementsExtracted;

  public VariableDataSource(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  @Override
  public Data getData(Participant participant) {
    log.debug("Fetching variable data for path: '{}'", path);
    if(participant == null) return null;

    Value value = getValue(participant);

    return DataValueConverter.valueToData(value);
  }

  public Value getValue(Participant participant) {
    VariableEntity entity = new VariableEntityBean(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE, participant.getBarcode());
    ValueTable table = resolveTable();
    ValueSet valueSet = table.getValueSet(entity);

    return getVariableValueSource(table).getValue(valueSet);
  }

  @Override
  public String getUnit() {
    return getVariable().getUnit();
  }

  @Override
  public String toString() {
    return "$('" + path + "')";
  }

  private ValueTable resolveTable() {
    extractPathElements();
    if(tableName != null) {
      ValueTable table = resolveDatasource().getValueTable(tableName);
      if(table.isForEntityType(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE)) {
        return table;
      }
      throw new IllegalArgumentException("table " + tableName + " is for entity type " + table.getEntityType() + ". It cannot be used as a datasource.");
    }
    throw new IllegalStateException("Could not resolve ValueTable (path does not contain a table name)");
  }

  private Datasource resolveDatasource() {
    return datasourceName == null ? MagmaEngine.get().getDatasource(MagmaInstanceProvider.ONYX_DATASOURCE) : MagmaEngine.get().getDatasource(datasourceName);
  }

  private void extractPathElements() {
    if(!pathElementsExtracted) {
      MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(path);

      datasourceName = resolver.getDatasourceName();
      tableName = resolver.getTableName();
      variableName = resolver.getVariableName();

      pathElementsExtracted = true;
    }
  }

  private VariableValueSource getVariableValueSource(ValueTable table) {
    log.debug("Retrieving Magma variable {}:{}", table.getName(), variableName);
    try {
      return table.getVariableValueSource(variableName);
    } catch(NoSuchVariableException e) {
      log.error("[ONYX MAGMA MATCH FAILURE] No Magma variable found for the following name: {}", variableName);
      throw e;
    }
  }

  public Variable getVariable() {
    ValueTable table = resolveTable();
    return getVariableValueSource(table).getVariable();
  }

  public String getTableName() {
    extractPathElements();
    return tableName;
  }

  public String getVariableName() {
    extractPathElements();
    return variableName;
  }

}
