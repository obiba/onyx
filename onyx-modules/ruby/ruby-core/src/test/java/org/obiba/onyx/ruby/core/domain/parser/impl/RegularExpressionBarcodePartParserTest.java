/*******************************************************************************
 * Copyright 2011(c) OBiBa. All rights reserved.
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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.MessageSourceResolvable;

/**
 *
 */
public class RegularExpressionBarcodePartParserTest {

  @Test
  public void test_patate() {
    RegularExpressionBarcodePartParser parser = new RegularExpressionBarcodePartParser();
    parser.setSize(9);
    parser.setExpression("[1-9][0-9]{5,}\\.[01][0-9]");
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    parser.eatAndValidatePart(new StringBuilder("123456.01"), errors);
    parser.eatAndValidatePart(new StringBuilder("1234567.01"), errors);
    Assert.assertEquals(0, errors.size());
  }
}
