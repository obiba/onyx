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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.springframework.context.MessageSourceResolvable;

/**
 * Unit tests for <code>AcceptableValuesBarcodePartParser</code>
 */
public class AcceptableValuesBarcodePartParserTest {

  private AcceptableValuesBarcodePartParser parser;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    parser = new AcceptableValuesBarcodePartParser();
    Set<String> acceptableValues = new HashSet<String>();
    acceptableValues.add("123");
    acceptableValues.add("234");
    acceptableValues.add("543");
    parser.setAcceptableValues(acceptableValues);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.AcceptableValuesBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, org.obiba.onyx.core.service.ActiveInterviewService, java.util.List)}
   * .
   */
  @Test
  public void testShouldPassEatAndValidatePart() {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    StringBuilder barcode = new StringBuilder("5432108978");

    BarcodePart result = parser.eatAndValidatePart(barcode, errors);

    Assert.assertEquals("543", result.getPartLabel().getCodes()[0]);
    Assert.assertEquals("2108978", barcode.toString());
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.AcceptableValuesBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, org.obiba.onyx.core.service.ActiveInterviewService, java.util.List)}
   * .
   */
  @Test
  public void testShouldFailEatAndValidatePartWithInvalidSize() {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    StringBuilder barcode = new StringBuilder("54");

    BarcodePart result = parser.eatAndValidatePart(barcode, errors);

    Assert.assertNull(result);
    Assert.assertEquals(1, errors.size());

    MessageSourceResolvable error = errors.get(0);
    Assert.assertEquals("BarcodePartSizeError", error.getCodes()[0]);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.AcceptableValuesBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, org.obiba.onyx.core.service.ActiveInterviewService, java.util.List)}
   * .
   */
  @Test
  public void testShouldFailEatAndValidatePartWithInvalidValue() {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    StringBuilder barcode = new StringBuilder("p5432108978");

    BarcodePart result = parser.eatAndValidatePart(barcode, errors);

    Assert.assertNull(result);
    Assert.assertEquals(1, errors.size());

    MessageSourceResolvable error = errors.get(0);
    Assert.assertEquals("BarcodePartValueError", error.getCodes()[0]);
  }

  /**
   * Test method for {@link org.obiba.onyx.ruby.core.domain.parser.impl.AcceptableValuesBarcodePartParser#getSize()}.
   */
  @Test
  public void testGetSize() {
    int size = parser.getSize();
    Assert.assertEquals(3, size);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.AcceptableValuesBarcodePartParser#setAcceptableValues()}.
   */
  @Test
  public void testShouldFailSetAcceptableValuesWithException() {
    Set<String> acceptableValues = new HashSet<String>();
    acceptableValues.add("123");
    acceptableValues.add("1234");

    try {
      parser.setAcceptableValues(acceptableValues);
      fail("Should get IllegalArgumentException.");
    } catch(IllegalArgumentException e) {
      Assert.assertEquals("The acceptable values have different size.", e.getMessage());
    }
  }
}
