/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.model;

/**
 *
 */
public class InvalidMultipleMediaTypesException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public InvalidMultipleMediaTypesException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public InvalidMultipleMediaTypesException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public InvalidMultipleMediaTypesException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public InvalidMultipleMediaTypesException(Throwable cause) {
    super(cause);
  }

}
