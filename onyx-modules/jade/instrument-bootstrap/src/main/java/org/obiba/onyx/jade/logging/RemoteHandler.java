/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.obiba.onyx.jade.instrument.service.ClientLoggingService;

/**
 * An implementation of {@code java.util.logging.Handler} that sends {@code LogRecord} to Jade's
 * {@link ClientLoggingService}.
 */
public class RemoteHandler extends Handler {

  private ClientLoggingService clientLoggingService;

  @Override
  public void close() throws SecurityException {

  }

  @Override
  public void flush() {

  }

  @Override
  public void publish(LogRecord record) {
    clientLoggingService.logging(record);
  }

  public ClientLoggingService getClientLoggingService() {
    return clientLoggingService;
  }

  public void setClientLoggingService(ClientLoggingService clientLoggingService) {
    this.clientLoggingService = clientLoggingService;
  }

}
