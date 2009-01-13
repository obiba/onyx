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

import org.obiba.onyx.core.domain.participant.Participant;

/**
 * Interface for sharing {@link Action} variable by different {@link IVariableProvider}.
 * @see Action
 * @see IVariableProvider
 */
public interface IActionVariableProvider {

  /**
   * Check if given variable represent a {@link Action} variable.
   * @param variable
   * @return
   */
  public boolean isActionVariable(Variable variable);

  /**
   * Get the variable data for the for the given {@link Action} variable.
   * @param participant
   * @param variable
   * @param variablePathNamingStrategy
   * @param varData
   * @param stage
   * @return
   */
  public VariableData getActionVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy, VariableData varData, String stage);

  /**
   * Create a the {@link Action} variable, without parent {@link Variable}.
   * @return
   */
  public Variable createActionVariable();
}
