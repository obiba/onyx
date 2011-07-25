/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.vsm.bptru.bpm;

public interface BpmInstrument {

  public boolean isConnected();

  /**
   * Connects to the Bpm instrument and starts the comm loop.
   */
  public void connect();

  /**
   * Disconnects from the Bpm without stoping the comm loop. This will retry the connection.
   */
  public void disconnect();

  /**
   * Disconnects from the Bpm and stops the comm loop
   */
  public void close();

  public String getManufacturerString();

  public String getProductString();

  public String getSerialNumber();

  /**
   * Read the next {@code BpmMessage} sent by the instrument. Note that this method blocks for at most 100ms.
   * @return the {@code BpmMessage} received from the instrument or null if none received within 100ms.
   */
  public BpmMessage read();

  /**
   * Used to issue commands to the instrument.
   * @return an instance of {@code BpmCommands} available for this instrument.
   */
  public BpmCommands commands();

}
