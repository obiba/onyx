/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;

/**
 * 
 */
public interface IVariableProvider {

  /**
   * Get the list of entity/variables.
   * @return
   */
  public List<Variable> getVariables();

  /**
   * Get the participant's variable data for the given variable.
   * @param participant
   * @param variable
   * @param variablePathNamingStrategy
   * @return null if none
   */
  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy);

}
