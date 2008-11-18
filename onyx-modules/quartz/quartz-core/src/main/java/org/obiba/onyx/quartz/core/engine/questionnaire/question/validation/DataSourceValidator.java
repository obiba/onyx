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

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.DataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ComparisionOperator;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;

/**
 * Validates a {@link Data} value by comparing it to the one provided by the {@link DataSource} in the context of the
 * currently administered questionnaire.
 */
public class DataSourceValidator implements IDataValidator {

  private static final long serialVersionUID = 1L;

  private DataSource dataSource;

  private ComparisionOperator comparisionOperator;

  public DataSourceValidator(DataSource dataSource, ComparisionOperator comparisionOperator) {
    this.dataSource = dataSource;
    this.comparisionOperator = comparisionOperator;
  }

  public void validate(IValidatable validatable) {
    InnerDataSourceValidator validator = new InnerDataSourceValidator();
    validator.validate(validatable);
  }

  @SuppressWarnings("serial")
  private class InnerDataSourceValidator implements IValidator {

    @SpringBean
    private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

    public InnerDataSourceValidator() {
      InjectorHolder.getInjector().inject(this);
    }

    public void validate(IValidatable validatable) {
      Data dataToCompare = (Data) validatable.getValue();
      Data data = dataSource.getData(activeQuestionnaireAdministrationService);

      ValidationError error = null;
      if(dataToCompare == null && data != null) {
        error = newValidationError("ExpectedToBeNotNull");
        error.setVariable("expected", data);
        error.setVariable("found", dataToCompare);
      } else {
        int result = dataToCompare.compareTo(data);

        switch(comparisionOperator) {
        case eq:
          if(result != 0) {
            error = newValidationError("ExpectedToBeEqual");
            error.setVariable("expected", data);
            error.setVariable("found", dataToCompare);
          }
          break;
        case ne:
          if(result == 0) {
            error = newValidationError("ExpectedToBeDifferent");
            error.setVariable("expected", data);
            error.setVariable("found", dataToCompare);
          }
          break;
        case lt:
          if(result <= 0) {
            error = newValidationError("ExpectedToBeLower");
            error.setVariable("expected", data);
            error.setVariable("found", dataToCompare);
          }
          break;
        case le:
          if(result < 0) {
            error = newValidationError("ExpectedToBeLowerEqual");
            error.setVariable("expected", data);
            error.setVariable("found", dataToCompare);
          }
          break;
        case gt:
          if(result >= 0) {
            error = newValidationError("ExpectedToBeGreater");
            error.setVariable("expected", data);
            error.setVariable("found", dataToCompare);
          }
          break;
        case ge:
          if(result > 0) {
            error = newValidationError("ExpectedToBeGreaterEqual");
            error.setVariable("expected", data);
            error.setVariable("found", dataToCompare);
          }
          break;
        default:
          break;
        }
      }

      if(error != null) {
        validatable.error(error);
      }
    }

    private ValidationError newValidationError(String message) {
      ValidationError error = new ValidationError();
      error.setMessage("DataSourceValidator." + message);
      return error;
    }
  }
}
