/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class DataValidatorTest {

  IValidator mockValidator;

  @Before
  public void createMocks() {
    mockValidator = EasyMock.createMock(IValidator.class);
  }

  @Test
  public void testDataValidatorNormalBehavior() {
    Validatable value = new Validatable();
    value.setValue(DataBuilder.buildInteger(10));

    // We should match the argument to make sure we obtain the proper value
    mockValidator.validate((IValidatable) EasyMock.anyObject());

    DataValidator validator = new DataValidator(mockValidator, DataType.INTEGER);
    EasyMock.replay(mockValidator);
    validator.validate(value);
    EasyMock.verify(mockValidator);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDataValidatorWithInvalidType() {
    Date testDate = new Date();
    Validatable value = new Validatable();
    value.setValue(DataBuilder.buildDate(testDate));

    // Expect a DECIMAL, but obtain an DATE
    DataValidator validator = new DataValidator(mockValidator, DataType.DECIMAL);
    // Expect no calls on mockValidator
    EasyMock.replay(mockValidator);
    validator.validate(value);
    EasyMock.verify(mockValidator);

    List<IValidationError> errors = value.getErrors();
    Assert.assertNotNull(errors);
    Assert.assertEquals(1, errors.size());
    IValidationError ierror = errors.get(0);
    Assert.assertTrue(ierror instanceof ValidationError);
    ValidationError error = (ValidationError) ierror;
    Map<String, Object> variables = error.getVariables();
    Assert.assertTrue(variables.containsKey("expectedType"));
    Assert.assertEquals(DataType.DECIMAL, variables.get("expectedType"));

    Assert.assertTrue(variables.containsKey("actualType"));
    Assert.assertEquals(DataType.DATE, variables.get("actualType"));

    Assert.assertTrue(variables.containsKey("value"));
    Assert.assertEquals(testDate, variables.get("value"));

    // We can only get at the list of keys by asking the error for its message
    IErrorMessageSource mockSource = EasyMock.createMock(IErrorMessageSource.class);
    EasyMock.expect(mockSource.getMessage(EasyMock.eq("DataValidator.incompatibleTypes"))).andReturn("theMessage");
    EasyMock.expectLastCall().once();
    // We are not testing the IValidationError implementation, but we need to expect this call anyway. Make it as
    // flexible as possible. The important expectation is the first which expects the proper message key.
    EasyMock.expect(mockSource.substitute((String) EasyMock.anyObject(), (Map) EasyMock.anyObject())).andReturn("theSubistutedMessage");
    EasyMock.expectLastCall().anyTimes();

    EasyMock.replay(mockSource);
    error.getErrorMessage(mockSource);
    EasyMock.verify(mockSource);
  }

  @Test
  public void testStringValidatorWithConversionFromNumber() {
    // Tests that a StringValidator can be applied to a input data with type INTEGER.
    DataValidator validator = new DataValidator(new StringValidator.ExactLengthValidator(4), DataType.TEXT);
    Validatable validatable = new Validatable(DataBuilder.buildInteger(2009));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());
  }

  @Test
  public void testNumberValidator() {
    DataValidator validator = new DataValidator(new MinimumValidator(1), DataType.INTEGER);
    Validatable validatable = new Validatable(DataBuilder.buildInteger(2));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());
  }

  @Test
  public void testDoubleNumberValidator() {
    DataValidator validator = new DataValidator(new MinimumValidator(1f), DataType.DECIMAL);
    Validatable validatable = new Validatable(DataBuilder.buildDecimal(1.2f));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());
  }

  @Test
  public void testFailedNumberValidator() {
    DataValidator validator = new DataValidator(new MinimumValidator(1), DataType.INTEGER);
    Validatable validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testFailedDoubleNumberValidator() {
    DataValidator validator = new DataValidator(new MinimumValidator(1f), DataType.DECIMAL);
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
