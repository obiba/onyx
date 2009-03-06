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

import org.obiba.onyx.core.service.IPasswordHashingStrategy;

/**
 * Returns the given plain-text password without any transformation.
 */
public class NoPasswordHashingStrategyImpl implements IPasswordHashingStrategy {

  public String hashPassword(String plainText) {
    if(plainText == null) throw new IllegalArgumentException();
    return plainText;
  }

}
