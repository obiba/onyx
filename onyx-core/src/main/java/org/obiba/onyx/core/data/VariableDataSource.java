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
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.magma.DataValueConverter;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Get data from a variable kept in variable directory
 */
public class VariableDataSource implements IDataSource, InitializingBean {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(VariableDataSource.class);

  private transient VariableDirectory variableDirectory;

  private String path;

  private ValueTable onyxParticipantTable;

  private VariableValueSource variableValueSource;

  private MagmaEngineReferenceResolver resolver = new MagmaEngineReferenceResolver();

  public void afterPropertiesSet() throws Exception {

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable table : datasource.getValueTables()) {
        onyxParticipantTable = table;
      }
    }

    String magmaVariableName = path.replaceFirst("Onyx.", "");
    log.info("Retrieving the following Magma variable (collectionName={}): {}", magmaVariableName, onyxParticipantTable);
    try {
      variableValueSource = resolver.resolve(onyxParticipantTable, magmaVariableName);
    } catch(NoSuchVariableException noSuchVariableEx) {
      log.error("[ONYX MAGMA MATCH FAILURE] No Magma variable found for the following name: {}", magmaVariableName);
    }

  }

  public Data getData(Participant participant) {

    if(participant == null) return null;

    org.obiba.onyx.engine.variable.Variable variable = variableDirectory.getVariable(path);
    Data data = null;
    if(variable != null) {
      VariableData variableData = variableDirectory.getVariableData(participant, path);
      if(variableData != null && variableData.getDatas().size() > 0) {
        data = variableData.getDatas().get(0);
      }
    }

    if(data == null) return data;

    if(data.equals(DataValueConverter.valueToData(getValue(participant)))) {
      return data;
    } else {
      log.error("[ONYX MAGMA MATCH FAILURE] Value for variable {} is different in Magma (Onyx Data={}, Magma Value={}). Returned the Onyx Data.", new Object[] { variable.getName(), data, DataValueConverter.valueToData(getValue(participant)) });
      return data;
    }
  }

  private Value getValue(Participant participant) {
    VariableEntity entity = new VariableEntityBean("Participant", participant.getBarcode());
    ValueSet valueSet = onyxParticipantTable.getValueSet(entity);
    return variableValueSource.getValue(valueSet);
  }

  public String getUnit() {
    org.obiba.onyx.engine.variable.Variable variable = variableDirectory.getVariable(path);
    String variableDirectoryUnit = (variable != null) ? variable.getUnit() : null;
    String magmaVariableUnit = (variable != null) ? variableValueSource.getVariable().getUnit() : null;
    if(!variableDirectoryUnit.equals(magmaVariableUnit)) {
      log.error("[ONYX MAGMA MATCH FAILURE] Unit for variable {} is different in Magma (VariableDirectory={}, Magma={})", new Object[] { variable.getName(), variableDirectoryUnit, magmaVariableUnit });
      return variableDirectoryUnit;
    }
    return magmaVariableUnit;
  }

  public VariableDataSource(String path) {
    this.path = path;
  }

  public void setVariableDirectory(VariableDirectory variableDirectory) {
    this.variableDirectory = variableDirectory;
  }

  @Override
  public String toString() {
    return "Variable[" + path + "]";
  }

}
