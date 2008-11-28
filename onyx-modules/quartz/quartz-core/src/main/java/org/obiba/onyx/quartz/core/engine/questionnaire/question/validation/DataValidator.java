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

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.util.data.IDataUnitProvider;

/**
 * Validates a {@link Data} value, given a standard {@link IValidator} and specifying what is the {@link DataType} this
 * validator is capable to validate.
 * @see StringValidator
 * @see NumberValidator
 */
public class DataValidator implements IDataValidator {

  private static final long serialVersionUID = 1L;

  private DataType dataType;

  private IValidator validator;

  private IDataUnitProvider dataUnitProvider;

  public DataValidator(IValidator validator, DataType dataType) {
    this.validator = validator;
    this.dataType = dataType;
  }

  public void setDataUnitProvider(IDataUnitProvider dataUnitProvider) {
    this.dataUnitProvider = dataUnitProvider;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void validate(IValidatable validatable) {
    Validatable tempValidatable = getValidatorCast(validatable);
    validator.validate(tempValidatable);
    for(Object error : tempValidatable.getErrors()) {
      validatable.error((IValidationError) error);
    }
  }

  public IValidator getValidator() {
    return validator;
  }

  private Validatable getValidatorCast(IValidatable validatable) {
    String value = ((Data) validatable.getValue()).getValueAsString();

    switch(dataType) {

    case TEXT:
      return new Validatable(value);

    case INTEGER:
      return new Validatable(Long.valueOf(value));

    case DECIMAL:
      return new Validatable(Double.valueOf(value));
    }

    return null;
  }

  public String getUnit() {
    return dataUnitProvider != null ? dataUnitProvider.getUnit() : null;
  }
}
