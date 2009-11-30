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

import org.obiba.magma.Collection;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.VariableValueSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDirectory;
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

  private Collection onyxCollection;

  private VariableValueSource variableValueSource;

  public void afterPropertiesSet() throws Exception {

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(Collection collection : datasource.getCollections()) {
        onyxCollection = collection;
      }
    }

    String magmaVariableName = path.replaceFirst("Onyx.", "");
    log.info("Retrieving the following Magma variable: {}", magmaVariableName);
    try {
      variableValueSource = MagmaEngine.get().lookupVariable("Participant", onyxCollection.getName(), magmaVariableName);
    } catch(NoSuchVariableException noSuchVariableEx) {
      log.error("No Magma variable found for the following name: {}", magmaVariableName);
    }

  }

  public Data getData(Participant participant) {

    if(participant == null) return null;

    org.obiba.onyx.engine.variable.Variable variable = variableDirectory.getVariable(path);
    Data variableDirectoryData = null;
    if(variable != null) {
      VariableData variableData = variableDirectory.getVariableData(participant, path);
      if(variableData != null && variableData.getDatas().size() > 0) {
        variableDirectoryData = variableData.getDatas().get(0);
      }
    }

    // ValueSet valueSet = onyxCollection.loadValueSet(new VariableEntityBean("Participant", participant.getBarcode()));
    // Value value = variableValueSource.getValue(valueSet);

    return variableDirectoryData;
  }

  public String getUnit() {
    org.obiba.onyx.engine.variable.Variable variable = variableDirectory.getVariable(path);
    String variableDirectoryUnit = (variable != null) ? variable.getUnit() : null;
    String magmaVariableUnit = (variable != null) ? variableValueSource.getVariable().getUnit() : null;
    if(!variableDirectoryUnit.equals(magmaVariableUnit)) {
      log.error("Unit for variable {} are different in Magma (VariableDirectory={}, Magma={})", new Object[] { variable.getName(), variableDirectoryUnit, magmaVariableUnit });
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
