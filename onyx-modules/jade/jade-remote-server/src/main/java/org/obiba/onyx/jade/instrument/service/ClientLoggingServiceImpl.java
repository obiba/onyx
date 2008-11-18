/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.service;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement the logging service for client
 * 
 */
public class ClientLoggingServiceImpl implements ClientLoggingService {

  public void logging(LogRecord record) {
    assertNotNull(record);
    Logger log;
    if(record.getLoggerName() != null) {
      log = LoggerFactory.getLogger(record.getLoggerName());
    } else {
      log = LoggerFactory.getLogger(ClientLoggingServiceImpl.class);
    }

    Level level = record.getLevel();
    if(level.equals(Level.FINE) || level.equals(Level.FINER) || level.equals(Level.FINEST)) {
      log.debug(record.getMessage());
    } else if(level.equals(Level.CONFIG)) {
      log.trace(record.getMessage());
    } else if(level.equals(Level.INFO) || level.equals(Level.ALL)) {
      log.info(record.getMessage());
    } else if(level.equals(Level.WARNING)) {
      log.warn(record.getMessage());
    } else if(level.equals(Level.SEVERE)) {
      log.error(record.getMessage());
    }
  }

  /**
   * @param record
   */
  private void assertNotNull(LogRecord record) {
    if(record == null) {
      throw new IllegalArgumentException("The parameter logRecord must not be null.");
    }
  }
}
