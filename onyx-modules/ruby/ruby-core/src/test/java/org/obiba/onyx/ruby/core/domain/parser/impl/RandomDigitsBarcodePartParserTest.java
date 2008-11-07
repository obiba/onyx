/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain.parser.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.springframework.context.MessageSourceResolvable;

/**
 *
 */
public class RandomDigitsBarcodePartParserTest {

  private RandomDigitsBarcodePartParser parser;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    parser = new RandomDigitsBarcodePartParser();
    parser.setSize(5);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.RandomDigitsBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, java.util.List)}
   * .
   */
  @Test
  public void testShouldPassWithNumericFormat() {
    parser.setFormat("^\\d+$");
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart result = parser.eatAndValidatePart(new StringBuilder("1234567890"), null, errors);
    Assert.assertEquals(0, errors.size());
    Assert.assertEquals("12345", result.getPart());
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.RandomDigitsBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, java.util.List)}
   * .
   */
  @Test
  public void testShouldFailWithNumericFormat() {
    parser.setFormat("^\\d+$");
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart result = parser.eatAndValidatePart(new StringBuilder("e1234567890"), null, errors);
    Assert.assertEquals(1, errors.size());
    Assert.assertNull(result);
    Assert.assertEquals("BarcodePartFormatError", errors.get(0).getCodes()[0]);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.RandomDigitsBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, java.util.List)}
   * .
   */
  @Test
  public void testShouldPassWithAlphaNumericFormat() {
    parser.setFormat("^[A-Za-z0-9]+$");
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart result = parser.eatAndValidatePart(new StringBuilder("Ae1234567890"), null, errors);
    Assert.assertEquals(0, errors.size());
    Assert.assertEquals("Ae123", result.getPart());
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.RandomDigitsBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, java.util.List)}
   * .
   */
  @Test
  public void testShouldFailWithAlphaNumericFormat() {
    parser.setFormat("^[A-Za-z0-9]+$");
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart result = parser.eatAndValidatePart(new StringBuilder("e1 234567890"), null, errors);
    Assert.assertEquals(1, errors.size());
    Assert.assertNull(result);
    Assert.assertEquals("BarcodePartFormatError", errors.get(0).getCodes()[0]);
  }

}
