/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.ComparingDataSourceValidator;
import org.obiba.onyx.wicket.data.IDataValidator;

/**
 * 
 */
public class OpenAnswerDefinitionValidatorFactory {

  public static List<IValidator> getValidators(IModel openAnswerDefinitionModel, Participant participant) {
    List<IValidator> validators = new ArrayList<IValidator>();

    OpenAnswerDefinition open = (OpenAnswerDefinition) openAnswerDefinitionModel.getObject();
    for(IDataValidator validator : open.getDataValidators()) {
      validators.add(validator);
    }

    for(int i = 0; i < open.getValidationDataSources().size(); i++) {
      validators.add(new ComparingDataSourceValidator(openAnswerDefinitionModel, participant, i));
    }

    return validators;
  }

}
