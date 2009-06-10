/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.variable;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.springframework.context.MessageSource;

/**
 * Defines the way instrument types will be expressed in terms of variables. It is also responsible for retrieving
 * instrument run data.
 */
public interface IInstrumentTypeToVariableMappingStrategy {

  /**
   * Given a variable, find among its parents which variable represents the instrument type.
   * @param variable
   * @return
   */
  public Variable getInstrumentTypeVariable(Variable variable);

  /**
   * For an instrument type, builds its variable, instrument parameters variables as children.
   * @param type
   * @return
   */
  public Variable getVariable(InstrumentType type);

  /**
   * Get the registered data for the variable.
   * @param participant
   * @param variable
   * @param variablePathNamingStrategy
   * @param varData
   * @return
   */
  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy, VariableData varData);

  /**
   * Set the Spring message source for localization of instrument attributes.
   * @param messageSource
   */
  public void setMessageSource(MessageSource messageSource);
}
