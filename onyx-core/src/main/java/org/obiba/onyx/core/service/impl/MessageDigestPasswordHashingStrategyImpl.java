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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.obiba.core.util.HexUtil;
import org.obiba.onyx.core.service.IPasswordHashingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the method {@code hashPassword} to hash a password based on the supplied algorithm. The hashed password is
 * returned in hex encoded form.
 */
public class MessageDigestPasswordHashingStrategyImpl implements IPasswordHashingStrategy {

  private static final Logger log = LoggerFactory.getLogger(MessageDigestPasswordHashingStrategyImpl.class);

  private String algorithm;

  /**
   * Sets an algorithm to be used when hashing a password.
   * @param algorithm The name of an algorithm. MD5, SHA, etc.
   */
  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public String hashPassword(String plainText) {
    if(plainText == null) throw new IllegalArgumentException("Plaintext password cannot be null.");
    if(algorithm == null) throw new IllegalArgumentException("Algorithm cannot be null.");
    try {
      return HexUtil.bytesToHex(MessageDigest.getInstance(algorithm).digest(plainText.getBytes()));
    } catch(NoSuchAlgorithmException e) {
      throw new IllegalArgumentException("The specified algorithm [" + algorithm + "] is not available.");
    }
  }

  /**
   * Call to ensure that the currently set algorithm is available from an installed provider. If it is not a {@code
   * NoSuchAlgorithmException} will be thrown.
   * @throws NoSuchAlgorithmException thrown when the algorithm is unavailable.
   */
  public void validateAlgorithm() throws NoSuchAlgorithmException {
    if(algorithm == null) throw new NoSuchAlgorithmException("Algorithm cannot be null.");
    try {
      MessageDigest.getInstance(algorithm);
    } catch(NoSuchAlgorithmException e) {
      log.error("The specified algorithm [" + algorithm + "] is not available.", e);
      throw e;
    }
  }
}
