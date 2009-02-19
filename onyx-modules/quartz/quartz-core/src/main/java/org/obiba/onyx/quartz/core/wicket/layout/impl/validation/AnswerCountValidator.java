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

import java.util.List;

import org.apache.wicket.IClusterable;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;

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
    if(question.getQuestions().size() > 0 && question.getCategories().size() > 0) {
      for(Question child : question.getQuestions()) {
        if(child.isToBeAnswered(activeQuestionnaireAdministrationService)) {
          validate(validatable, child);
        }
      }
    } else {
      validate(validatable, question);
    }
  }

  public void validate(IValidatable validatable, Question question) {
    List<CategoryAnswer> categoryAnswers = activeQuestionnaireAdministrationService.findAnswers(question);
    int count = categoryAnswers.size();

    if(count == 0 && question.isRequired()) {
      ValidationError error = newValidationError(question);
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
        validatable.error(newValidationError(question, minCount, maxCount, count));
      } else if(maxCount != null && maxCount < count) {
        validatable.error(newValidationError(question, minCount, maxCount, count));
      } else {

        // check open answer requiredness
        for(CategoryAnswer categoryAnswer : categoryAnswers) {

          // find category, it may be in parent question
          QuestionCategory questionCategory = question.findQuestionCategory(categoryAnswer.getCategoryName());
          if(questionCategory == null && question.getParentQuestion() != null) {
            questionCategory = question.getParentQuestion().findQuestionCategory(categoryAnswer.getCategoryName());
          }

          if(questionCategory != null && questionCategory.getCategory().getOpenAnswerDefinition() != null) {
            OpenAnswerDefinition openAnswerDefinition = questionCategory.getCategory().getOpenAnswerDefinition();
            if(openAnswerDefinition.isRequired()) {
              if(categoryAnswer.getOpenAnswers().size() == 0) {
                // at least one open answer is required
                ValidationError error = newValidationError(question);
                error.addMessageKey(KEY_PREFIX + ".Required");
                validatable.error(error);
              } else if(openAnswerDefinition.getOpenAnswerDefinitions().size() > 0) {
                // check child open answer requiredness
                for(OpenAnswerDefinition childOpenAnswerDefinition : openAnswerDefinition.getOpenAnswerDefinitions()) {
                  if(childOpenAnswerDefinition.isRequired()) {
                    boolean found = false;
                    for(OpenAnswer openAnswer : categoryAnswer.getOpenAnswers()) {
                      if(openAnswer.getOpenAnswerDefinitionName().equals(childOpenAnswerDefinition.getName())) {
                        found = true;
                        break;
                      }
                    }
                    if(!found) {
                      ValidationError error = newValidationError(question);
                      error.addMessageKey(KEY_PREFIX + ".OpenRequired");
                      error.setVariable("open", (String) QuestionnaireStringResourceModelHelper.getStringResourceModel(question, questionCategory, childOpenAnswerDefinition).getObject());
                      validatable.error(error);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private ValidationError newValidationError(Question question) {
    ValidationError error = new ValidationError();
    error.setVariable("question", new QuestionnaireStringResourceModel(question, "label").getString());
    return error;
  }

  private ValidationError newValidationError(Question question, Integer minCount, Integer maxCount, int count) {
    ValidationError error = newValidationError(question);

    error.setVariable("count", count);

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
