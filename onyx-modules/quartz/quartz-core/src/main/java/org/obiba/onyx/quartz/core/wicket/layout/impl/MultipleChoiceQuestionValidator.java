/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.io.Serializable;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

/**
 * 
 */
public class MultipleChoiceQuestionValidator implements IValidator {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private IModel questionModel;

  public MultipleChoiceQuestionValidator(IModel questionModel) {
    InjectorHolder.getInjector().inject(this);
    this.questionModel = questionModel;
  }

  @SuppressWarnings("unchecked")
  public void validate(IValidatable validatable) {
    Question question = (Question) questionModel.getObject();
    int count = activeQuestionnaireAdministrationService.findAnswers(question).size();

    if(question.isMultiple()) {
      if(question.getMinCount() != null && question.getMinCount() > count) {
        validatable.error(new MultipleQuestionValidationError(count));
      } else if(question.getMaxCount() != null && question.getMaxCount() < count) {
        validatable.error(new MultipleQuestionValidationError(count));
      }
    }
  }

  @SuppressWarnings("serial")
  private class MultipleQuestionValidationError implements IValidationError, Serializable {

    private int count;

    public MultipleQuestionValidationError(int count) {
      this.count = count;
    }

    /**
     * TODO use a message source that uses the language into which the questionnaire is administered.
     * @see BaseQuestionPanel for message keys.
     */
    public String getErrorMessage(IErrorMessageSource messageSource) {
      Question question = (Question) questionModel.getObject();
      String key = "";
      ValueMap map = new ValueMap("count=" + count);
      if(question.getMinCount() != null) {
        map.put("min", question.getMinCount());
        key = "MultipleChoiceQuestion.Min";
      }
      if(question.getMaxCount() != null) {
        map.put("max", question.getMaxCount());
        if(key.length() == 0) {
          key = "MultipleChoiceQuestion.Max";
        } else {
          key = "MultipleChoiceQuestion.Count";
        }
      }
      return messageSource.substitute(messageSource.getMessage(key), map);
    }

  }

}
