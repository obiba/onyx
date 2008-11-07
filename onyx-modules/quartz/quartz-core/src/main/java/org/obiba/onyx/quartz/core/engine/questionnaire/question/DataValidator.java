/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class DataValidator implements IValidator {

  private static final long serialVersionUID = 1L;

  private DataType dataType;

  private IValidator validator;

  public DataValidator() {

  }

  public DataValidator(IValidator validator, DataType dataType) {
    this.validator = validator;
    this.dataType = dataType;
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
    }

    return null;
  }
}
