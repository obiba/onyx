/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.engine.variable;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;

/**
 * Defines the way tubes will be expressed in terms of variables.
 */
public interface ITubeToVariableMappingStrategy {

  /**
   * Get the {@link ParticipantTubeRegistration} variable.
   * @return
   */
  public Variable getParticipantTubeRegistrationVariable();

  /**
   * Get the {@link RegisteredParticipantTube} variable.
   * @return
   */
  public Variable getRegisteredParticipantTubeVariable();

  /**
   * Get the registered data for the variable.
   * @param participant
   * @param variable
   * @param variablePathNamingStrategy
   * @param varData
   * @return
   */
  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy, VariableData varData);

}
