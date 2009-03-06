/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

/**
 * Provides a single method to hash a plain text password.
 */
public interface IPasswordHashingStrategy {

  /**
   * Returns a {@code String} containing a hash of the supplied {@code plainText} password.
   * @param plainText A password in plain text.
   * @return A hash of the password.
   */
  String hashPassword(String plainText);

}
