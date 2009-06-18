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
 * Provider of {@link Variable} and {@link VariableData} for a given {@link Participant}.
 */
public interface IVariableProvider {

  /**
   * Get the list of variable branches to be added to the root.
   * @return the variables to be connected to the variable root
   */
  public List<Variable> getVariables();

  /**
   * Get the list of variables that have been be placed in the existing variable tree.
   * @param root the of the variable tree
   * @param variablePathNamingStrategy utility to perform the placing of the variables into the tree
   * @return the flat list of variables that have been placed in the tree
   */
  public List<Variable> getContributedVariables(Variable root, IVariablePathNamingStrategy variablePathNamingStrategy);

  /**
   * Get the participant's variable data for the given variable.
   * @param participant
   * @param variable
   * @param variablePathNamingStrategy
   * @return null if none
   */
  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy);

}
