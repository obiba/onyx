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

import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.service.IPasswordHashingStrategy;

/**
 * Tests MessageDigestPasswordHashingStrategyImpl.
 */
public class MessageDigestPasswordHashingStrategyImplTest {

  private IPasswordHashingStrategy hashingStrategy;

  @Before
  public void setup() {
    hashingStrategy = new MessageDigestPasswordHashingStrategyImpl();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPassword() {
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).setAlgorithm("SHA");
    hashingStrategy.hashPassword(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAlgorithm() {
    hashingStrategy.hashPassword("secretPassword");
  }

  /**
   * Test is the supplied algorithm is not available.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNonsenseAlgorithm() {
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).setAlgorithm("Nonsense");
    hashingStrategy.hashPassword("secretPassword");
  }

  @Test
  public void testShaGeneral() {
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).setAlgorithm("SHA");
    String password = "secretPassword";
    String hash = hashingStrategy.hashPassword(password);
    Assert.assertEquals(hash, hashingStrategy.hashPassword(password));
  }

  @Test(expected = NoSuchAlgorithmException.class)
  public void testNullAlgorithmValidation() throws NoSuchAlgorithmException {
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).validateAlgorithm();
  }

  @Test(expected = NoSuchAlgorithmException.class)
  public void testNonsenseAlgorithmValidation() throws NoSuchAlgorithmException {
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).setAlgorithm("Nonsense");
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).validateAlgorithm();
  }

  @Test(expected = NoSuchAlgorithmException.class)
  public void testEmptyAlgorithmValidation() throws NoSuchAlgorithmException {
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).setAlgorithm("");
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).validateAlgorithm();
  }

  @Test
  public void testSHAAlgorithmValidation() throws NoSuchAlgorithmException {
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).setAlgorithm("SHA");
    ((MessageDigestPasswordHashingStrategyImpl) hashingStrategy).validateAlgorithm();
    Assert.assertTrue(true); // validateAlgorithm succeeded.
  }
}
