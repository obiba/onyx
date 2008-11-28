/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question.validation;

import junit.framework.Assert;

import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.junit.Test;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DataValidatorTest {

  static final Logger log = LoggerFactory.getLogger(DataValidatorTest.class);

  @Test
  public void testNumberValidator() {
    DataValidator validator = new DataValidator(new NumberValidator.MinimumValidator(1), DataType.INTEGER);
    Validatable validatable = new Validatable(DataBuilder.buildInteger(2));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());
  }

  @Test
  public void testDoubleNumberValidator() {
    DataValidator validator = new DataValidator(new NumberValidator.MinimumValidator(1), DataType.DECIMAL);
    Validatable validatable = new Validatable(DataBuilder.buildDecimal(1.2f));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());
  }

  @Test
  public void testFailedNumberValidator() {
    DataValidator validator = new DataValidator(new NumberValidator.MinimumValidator(1), DataType.INTEGER);
    Validatable validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testFailedDoubleNumberValidator() {
    DataValidator validator = new DataValidator(new NumberValidator.MinimumValidator(1), DataType.DECIMAL);
    Validatable validatable = new Validatable(DataBuilder.buildDecimal(0.2f));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testStringValidator() {
    DataValidator validator = new DataValidator(new StringValidator.ExactLengthValidator(6), DataType.TEXT);
    Validatable validatable = new Validatable(DataBuilder.buildText("coucou"));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());
  }

  @Test
  public void testFailedStringValidator() {
    DataValidator validator = new DataValidator(new StringValidator.MaximumLengthValidator(3), DataType.TEXT);
    Validatable validatable = new Validatable(DataBuilder.buildText("coucou"));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

}
