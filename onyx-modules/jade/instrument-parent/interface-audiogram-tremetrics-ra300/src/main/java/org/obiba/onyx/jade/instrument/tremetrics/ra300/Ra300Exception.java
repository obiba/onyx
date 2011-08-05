/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.tremetrics.ra300;

public class Ra300Exception extends RuntimeException {

  private static final long serialVersionUID = 1552768930207317598L;

  public enum Cause {
    RECEIVE_TIMEOUT, CONNECTION_ERROR, INVALID_PORT, COMMUNICATION_ERROR
  }

  private final Cause cause;

  public Ra300Exception(Cause cause, Exception e) {
    super(cause.toString(), e);
    this.cause = cause;
  }

  public Ra300Exception(Cause cause) {
    this(cause, null);
  }

  public Cause getExceptionCause() {
    return cause;
  }

}
