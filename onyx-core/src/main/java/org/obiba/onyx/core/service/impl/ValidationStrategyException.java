/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

/**
 * Thrown when exceptions occur configuring Validation strategies.
 */
public class ValidationStrategyException extends Exception {

  private static final long serialVersionUID = -2778238221335605064L;

  /**
   * Description of exception.
   * @param msg exception description.
   */
  public ValidationStrategyException(String msg) {
    super(msg);
  }

}
