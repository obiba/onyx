/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.validation;

import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates a {@link Data} value by comparing it to the one provided by the {@link IDataSource} in the context of the
 * currently administered questionnaire.
 */
public class ComparingDataSourceValidator implements IValidator<Data> {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(ComparingDataSourceValidator.class);

  private Participant participant;

  private IModel<OpenAnswerDefinition> openAnswerDefinitionModel;

  private int validationDataSourceIndex;

  public ComparingDataSourceValidator(IModel<OpenAnswerDefinition> openAnswerDefinitionModel, Participant participant, int validationDataSourceIndex) {
    super();
    this.participant = participant;
    this.openAnswerDefinitionModel = openAnswerDefinitionModel;
    this.validationDataSourceIndex = validationDataSourceIndex;
  }

  public void validate(IValidatable<Data> validatable) {
    Data dataToCompare = validatable.getValue();

    OpenAnswerDefinition open = openAnswerDefinitionModel.getObject();
    ComparingDataSource comparingDataSource = open.getValidationDataSource(validationDataSourceIndex);
    comparingDataSource.setDataSourceLeft(new FixedDataSource(dataToCompare));

    Boolean result = comparingDataSource.getData(participant).getValue();
    log.info("comparing: {}={}", comparingDataSource, result);
    if(!result) {
      ValidationError error = null;

      Data data = comparingDataSource.getDataSourceRight().getData(participant);

      switch(comparingDataSource.getComparisonOperator()) {
      case eq:
        error = newValidationError("ExpectedToBeEqual", data, dataToCompare);
        break;
      case ne:
        error = newValidationError("ExpectedToBeDifferent", data, dataToCompare);
        break;
      case lt:
        error = newValidationError("ExpectedToBeLower", data, dataToCompare);
        break;
      case le:
        error = newValidationError("ExpectedToBeLowerEqual", data, dataToCompare);
        break;
      case gt:
        error = newValidationError("ExpectedToBeGreater", data, dataToCompare);
        break;
      case ge:
        error = newValidationError("ExpectedToBeGreaterEqual", data, dataToCompare);
        break;
      default:
        break;
      }

      if(error != null) {
        validatable.error(error);
      }
    }
  }

  private ValidationError newValidationError(String message, Data data, Data dataToCompare) {
    ValidationError error = new ValidationError();
    error.addMessageKey("DataSourceValidator." + message);
    if(data != null) {
      error.setVariable("expected", data.getValue());
    } else {
      error.setVariable("expected", "?");
    }
    if(dataToCompare != null) {
      error.setVariable("found", dataToCompare.getValue());
    } else {
      error.setVariable("found", "?");
    }
    return error;
  }
}
