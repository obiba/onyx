/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant;

import java.io.IOException;
import java.io.InputStream;

import org.obiba.onyx.core.domain.participant.Participant;
import org.springframework.batch.item.ItemStreamReader;

/**
 * Contract for components that can read {@code Participant} information from a source.
 */
public interface IParticipantReader extends ItemStreamReader<Participant> {

  /**
   * Returns true if an update to the list of participants and appointments is available. Returns false otherwise.
   * @return
   */
  public boolean isUpdateAvailable();

  public boolean isFileBased();

  public String getFilePattern();

  public void addFileForProcessing(InputStream is) throws IOException;

}
