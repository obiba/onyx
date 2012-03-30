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

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.onyx.core.domain.participant.Participant;

/**
 * Decouples the mechanism of obtaining Magma-related entities from Onyx code.
 */
public interface MagmaInstanceProvider {

  /**
   * The entity type for participants
   */
  public static final String ONYX_DATASOURCE = "onyx-datasource";

  /**
   * The entity type for participants
   */
  public static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  /**
   * The name of the onyx-core value table
   */
  public static final String PARTICIPANTS_TABLE_NAME = "Participants";

  Set<Datasource> getDatasources();

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
   * Resolves a {@code ValueTable} using a {@code variablePath}. If the variable path does not contain a datasource
   * name, the path is resolved against the Onyx {@code Datasource} (the one returned by {@code #getOnyxDatasource}.
   * 
   * <p/>
   * For example, the path {@literal Consent:pdfForm} will return the {@code ValueTable} named {@literal Consent} in the
   * Onyx {@code Datasource}. Which would be equivalent to: <code>getOnyxDatasource().getValueTable("Consent")</code><br/>
   * The path {@literal baseline.Participants:GENDER} will return the {@code ValueTable} named {@literal Participants}
   * in the {@code Datasource} named {@literal baseline}.
   * 
   * @param variablePath the path to the variable
   * @return a {@code ValueTable} instance
   * @throws NoSuchValueTableException when the table does not exist in the datasource
   * @throws NoSuchDatasourceException when the datasource does not exist
   */
  public ValueTable resolveTableFromVariablePath(String variablePath);

  /**
   * Resolves a {@code ValueTable} using a {@literal path}. This method is equivalent to
   * {@code resolveTableFromVariablePath}, except that it does not require a variable name in the path.
   * 
   * @param valueTablePath relative or absolute path to the value table
   * @return
   */
  public ValueTable resolveTable(String valueTablePath);

  public VariableValueSource resolveVariablePath(String variablePath);

  /**
   * Creates a new {@code VariableEntity} for a participant.
   * 
   * @param identifier the participant's identifier
   * @return an instance of {@code VariableEntity}
   */
  public VariableEntity newParticipantEntity(String identifier);

  /**
   * Creates a new {@code VariableEntity} for a participant.
   * 
   * @param participant the participant's identifier
   * @return an instance of {@code VariableEntity}
   */
  public VariableEntity newParticipantEntity(Participant participant);

  Datasource getDatasource(String name);
}
