/*******************************************************************************
 * Copyright 2011(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

/**
 * Decouples the mechanism of obtaining Magma-related entities from Onyx code.
 */
public interface MagmaInstanceProvider {

  /**
   * The entity type for participants
   */
  public static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  /**
   * The name of the onyx-core value table
   */
  public static final String PARTICIPANTS_TABLE_NAME = "Participants";

  /**
   * Returns the Onyx Magma Datasource.
   * 
   * @return an instance of {@code Datasource}
   */
  public Datasource getOnyxDatasource();

  /**
   * Return the {@code ValueTable} that onyx-core contributes to the {@code Datasource}. Use this method to obtain an
   * instance of {@code ValueTable} that is guaranteed to exist, regardless of the configuration.
   * @return the {@ValueTable}
   */
  public ValueTable getParticipantsTable();

  /**
   * Returns a {@code ValueTable} from the Onyx {@code Datasource}.
   * 
   * @param name the name of the table, ususally an Onyx stage name.
   * @return the {@code ValueTable} instance
   */
  public ValueTable getValueTable(String name);

  /**
   * Creates a new {@code VariableEntity} for a participant.
   * 
   * @param identifier the participant's identifier
   * @return an instance of {@code VariableEntity}
   */
  public VariableEntity newParticipantEntity(String identifier);

}
