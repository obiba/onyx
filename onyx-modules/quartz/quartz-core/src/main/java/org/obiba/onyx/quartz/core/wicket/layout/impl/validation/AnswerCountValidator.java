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

import org.apache.wicket.IClusterable;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

/**
 * Validates the question choices minimum/maximum count of answers. It uses the settings of the question by default, and
 * if none is found and question parent exists, question parent settings are used.
 */
public class AnswerCountValidator implements INullAcceptingValidator, IClusterable {

  private static final long serialVersionUID = 1L;

  private static final String KEY_PREFIX = AnswerCountValidator.class.getSimpleName();

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private IModel questionModel;

  public AnswerCountValidator(IModel questionModel) {
    InjectorHolder.getInjector().inject(this);
    this.questionModel = questionModel;
  }

  @SuppressWarnings("unchecked")
  public void validate(IValidatable validatable) {
    Question question = (Question) questionModel.getObject();
    int count = activeQuestionnaireAdministrationService.findAnswers(question).size();

    if(count == 0 && question.isRequired()) {
      ValidationError error = new ValidationError();
      error.addMessageKey(KEY_PREFIX + ".Required");
      validatable.error(error);
    } else {
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
        validatable.error(newValidationError(minCount, maxCount, count));
      } else if(maxCount != null && maxCount < count) {
        validatable.error(newValidationError(minCount, maxCount, count));
      }
    }
  }

  private ValidationError newValidationError(Integer minCount, Integer maxCount, int count) {
    ValidationError error = new ValidationError();

    String key = "";
    if(minCount != null) {
      error.setVariable("min", minCount);
      key = KEY_PREFIX + ".Min";
    }

    if(maxCount != null) {
      error.setVariable("max", maxCount);
      if(key.length() == 0) {
        key = "AnswerCountValidator.Max";
      } else {
        key = "AnswerCountValidator.Count";
      }
    }

    error.addMessageKey(key);

    return error;
  }
}
