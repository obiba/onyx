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

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.core.service.IPasswordHashingStrategy;

/**
 * Tests NoPasswordHashingStrategyImpl.
 */
public class NoPasswordHashingStrategyImplTest {

  @Test
  public void testEmptyString() {
    IPasswordHashingStrategy hashingStrategy = new NoPasswordHashingStrategyImpl();
    String empty = "";
    String hash = hashingStrategy.hashPassword(empty);
    Assert.assertEquals(hash, empty);
  }

  @Test
  public void testGeneralString() {
    IPasswordHashingStrategy hashingStrategy = new NoPasswordHashingStrategyImpl();
    String password = "SecretPassword";
    String hash = hashingStrategy.hashPassword(password);
    Assert.assertEquals(hash, password);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullString() {
    IPasswordHashingStrategy hashingStrategy = new NoPasswordHashingStrategyImpl();
    hashingStrategy.hashPassword(null);
  }

}
