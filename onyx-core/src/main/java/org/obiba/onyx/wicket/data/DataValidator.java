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

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.ValidationError;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * Validates a {@link Data} value, given a standard {@link IValidator} and specifying what is the {@link DataType} this
 * validator is capable to validate.
 * <p>
 * This validator validates that the validated data's type is compatible with the underlying validator's expected type.
 * In case of a mismatch, an error is reported using the key DataValidator.incompatibleTypes with the following
 * attributes:
 * <ul>
 * <li><em>expectedType</em>: the expected {@code DataType}</li>
 * <li><em>actualType</em>: the actual {@code DataType} (the one obtained from the validated value)</li>
 * <li><em>value</em>: the value being validated</li>
 * </ul>
 * @see IValidator
 */
public class DataValidator implements IDataValidator {

  private static final long serialVersionUID = 1L;

  private DataType dataType;

  private IValidator validator;

  public DataValidator(IValidator validator, DataType dataType) {
    this.validator = validator;
    this.dataType = dataType;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void validate(IValidatable validatable) {
    Data value = (Data) validatable.getValue();

    // targetValue should be null if either value or value.getValue is null.
    Object targetValue = value != null ? value.getValue() : null;

    // Handle the case where there's a value and it needs conversion before being validated
    if(targetValue != null && dataType != value.getType()) {
      try {
        targetValue = convertValue(value);
      } catch(IllegalArgumentException e) {
        // Can't convert a String to a number
        ValidationError error = new ValidationError();
        error.addMessageKey("DataValidator.incompatibleTypes");
        error.setVariable("expectedType", dataType);
        error.setVariable("actualType", value.getType());
        error.setVariable("value", value.getValue());
        validatable.error(error);
        return;
      }
    }

    // value may be null here. If so, create the Validatable with null value.
    Validatable adaptedValidatable = new Validatable(targetValue);

    // Have the underlying validator validate the value
    validator.validate(adaptedValidatable);

    // Extract any validation errors and report them on the original Validatable.
    for(Object error : adaptedValidatable.getErrors()) {
      validatable.error((IValidationError) error);
    }

  }

  public IValidator getValidator() {
    return validator;
  }

  /**
   * Converts the input value to a value of the expected {@code DataType}. Throws an {@code IllegalArgumentException}
   * when it is not possible to convert the value.
   * 
   * @param value the value to convert
   * @return the converted value
   * @throws IllegalArgumentException when a conversion error occurs or the value cannot be converted to the expected
   * type safely.
   */
  protected Object convertValue(Data value) throws IllegalArgumentException {
    Object convertedValue = value.getValue();
    switch(dataType) {
    case TEXT:
      convertedValue = value.getValueAsString();
      break;
    case INTEGER:
      if(value.getType().isNumberType() == false) {
        String valueStr = value.getValueAsString();
        // This throws an IllegalArgumentException
        convertedValue = Long.valueOf(valueStr);
      }
      break;
    case DECIMAL:
      if(value.getType().isNumberType() == false) {
        String valueStr = value.getValueAsString();
        // This throws an IllegalArgumentException
        return Double.valueOf(valueStr);
      }
      break;
    default:
      // Cannot convert to expected type
      throw new IllegalArgumentException("Cannot convert type " + value.getType() + " to type " + this.dataType);
    }
    return convertedValue;
  }

}
