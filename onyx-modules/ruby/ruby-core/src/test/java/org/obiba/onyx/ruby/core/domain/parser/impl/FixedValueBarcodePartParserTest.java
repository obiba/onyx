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
public class FixedValueBarcodePartParserTest {

  private FixedValueBarcodePartParser parser;

  private String fixedValue;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    parser = new FixedValueBarcodePartParser();
    parser.setSize(5);
    fixedValue = "54321";
    parser.setFixedValue(fixedValue);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.FixedValueBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, java.util.List)}
   * .
   */
  @Test
  public void testShouldPassEatAndValidatePart() {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart result = parser.eatAndValidatePart(new StringBuilder("5432108978"), errors);
    Assert.assertEquals(fixedValue, result.getPart());
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.FixedValueBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, java.util.List)}
   * .
   */
  @Test
  public void testShouldGetPartLengthError() {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart result = parser.eatAndValidatePart(new StringBuilder("5432"), errors);
    Assert.assertNull(result);
    Assert.assertEquals(1, errors.size());
    MessageSourceResolvable error = errors.get(0);
    Assert.assertEquals("barcodePartLengthError", error.getCodes()[0]);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.FixedValueBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, java.util.List)}
   * .
   */
  @Test
  public void testShouldGetPartValueError() {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart result = parser.eatAndValidatePart(new StringBuilder("1234567890"), errors);
    Assert.assertNull(result);
    Assert.assertEquals(1, errors.size());
    MessageSourceResolvable error = errors.get(0);
    Assert.assertEquals("barcodePartValueError", error.getCodes()[0]);
  }

}
