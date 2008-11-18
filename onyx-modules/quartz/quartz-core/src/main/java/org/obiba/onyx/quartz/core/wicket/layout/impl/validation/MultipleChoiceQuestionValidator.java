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
import org.obiba.onyx.quartz.core.wicket.layout.impl.BaseQuestionPanel;

/**
 * Validates the question choices minimum/maximum count of answers. It uses the settings of the question by default, and
 * if none is found and question parent exists, question parent settings are used.
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

    // get the min/max settings : for questions having a parent question, if no settings is found parent settings is
    // used.
    Integer minCount = question.getMinCount();
    if(minCount == null && question.getParentQuestion() != null) {
      minCount = question.getParentQuestion().getMinCount();
    }
    Integer maxCount = question.getMaxCount();
    if(maxCount == null && question.getParentQuestion() != null) {
      maxCount = question.getParentQuestion().getMaxCount();
    }

    // find if there is an error
    if(minCount != null && minCount > count) {
      validatable.error(new MultipleQuestionValidationError(minCount, maxCount, count));
    } else if(maxCount != null && maxCount < count) {
      validatable.error(new MultipleQuestionValidationError(minCount, maxCount, count));
    }
  }

  @SuppressWarnings("serial")
  private class MultipleQuestionValidationError implements IValidationError, Serializable {

    private Integer minCount;

    private Integer maxCount;

    private int count;

    public MultipleQuestionValidationError(Integer minCount, Integer maxCount, int count) {
      this.minCount = minCount;
      this.maxCount = maxCount;
      this.count = count;
    }

    /**
     * TODO use a message source that uses the language into which the questionnaire is administered.
     * @see BaseQuestionPanel for message keys.
     */
    public String getErrorMessage(IErrorMessageSource messageSource) {
      String key = "";
      ValueMap map = new ValueMap("count=" + count);
      if(minCount != null) {
        map.put("min", minCount);
        key = "MultipleChoiceQuestion.Min";
      }
      if(maxCount != null) {
        map.put("max", maxCount);
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
