/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.crypt;

import java.security.PublicKey;

/**
 * A simple interface for mapping a {@code PublicKey} to a name.
 * <p>
 * A {@code KeyStore} is a type of {@code IPublicKeyFactory}, since it can contain a public key mapped to an alias.
 */
public interface IPublicKeyFactory {
  /**
   * Returns the {@code PublicKey} for the specified {@code name}.
   * @param name
   * @return
   */
  public PublicKey getPublicKey(String name);
}
