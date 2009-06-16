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

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.util.data.Data;

/**
 * Get data from a variable kept in variable directory
 */
public class VariableDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private transient VariableDirectory variableDirectory;

  private String path;

  public Data getData(Participant participant) {

    if(participant == null) return null;

    Variable variable = variableDirectory.getVariable(path);
    if(variable != null) {
      VariableData variableData = variableDirectory.getVariableData(participant, path);
      if(variableData != null && variableData.getDatas().size() > 0) return variableData.getDatas().get(0);
    }

    return null;
  }

  public String getUnit() {
    Variable variable = variableDirectory.getVariable(path);
    return (variable != null) ? variable.getUnit() : null;
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
