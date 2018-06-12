/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument;

/**
 * Implementations of the instrument runner interface will be in charge of handling the instrument data: backup
 * instrument state, push participant data, extract and send data, restore instrument state or anything appropriate
 * for running the measurement and respecting the participant's privacy.
 */
public interface InstrumentRunner {

  /**
   * Initialization before instrument run: prepare the instrument (open communication channel, push participant data,
   * backup database and configuration files).
   */
  void initialize();

  /**
   * Start the instrument: do the measure, extract, prepare and send data to the server.
   */
  void run();

  /**
   * Shutdown the instrument (close communication channel, restore database and configuration files etc). If the
   * initialize method is executed (without error), the shutdown method is guaranteed
   * to be executed also. Any exception thrown during shutdown will be ignored.
   */
  void shutdown();

}
