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
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;

/**
 * Defines the way tubes will be expressed in terms of variables.
 */
public interface ITubeToVariableMappingStrategy {

  /**
   * Get the {@link ParticipantTubeRegistration} variable.
   * 
   * @return
   */
  public Variable getParticipantTubeRegistrationVariable();

  /**
   * Get the {@link RegisteredParticipantTube} variable, for the specified Ruby stage.
   * 
   * Note that it is necessary to specify the Ruby stage because the stage determines the
   * {@link TubeRegistrationConfiguration}, which determines the {@link BarcodeStructure}, which in turn determines
   * the barcode part child variables.
   * 
   * @param stageName the associated Ruby stage
   * @return
   */
  public Variable getRegisteredParticipantTubeVariable(String stageName);

  /**
   * Get the registered data for the variable.
   * @param participant
   * @param variable
   * @param variablePathNamingStrategy
   * @param varData
   * @param stageName the associated Ruby stage
   * @return
   */
  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy, VariableData varData, String stageName);

  /**
   * Returns the name of variable root, if applicable.
   * 
   * @return variable root name (<code>null</code> if none)
   */
  public String getVariableRoot();
}
