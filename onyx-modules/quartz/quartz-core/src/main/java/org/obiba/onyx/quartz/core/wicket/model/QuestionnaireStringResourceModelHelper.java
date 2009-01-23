/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.model;

import java.util.regex.Pattern;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class QuestionnaireStringResourceModelHelper {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionnaireStringResourceModelHelper.class);

  /**
   * Get {@link OpenAnswerDefinition} string resource model.
   * @param question
   * @param questionCategory
   * @param openAnswerDefinition
   * @return
   */
  public static IModel getStringResourceModel(Question question, QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition) {
    IModel model;

    QuestionnaireStringResourceModel openLabel = new QuestionnaireStringResourceModel(openAnswerDefinition, "label");
    QuestionnaireStringResourceModel unitLabel = new QuestionnaireStringResourceModel(openAnswerDefinition, "unitLabel");
    QuestionnaireStringResourceModel questionCategoryLabel = new QuestionnaireStringResourceModel(questionCategory, "label");
    QuestionnaireStringResourceModel questionLabel = new QuestionnaireStringResourceModel(question, "label");

    if(!questionCategory.getQuestion().getName().equals(question.getName())) {
      model = new Model(questionLabel.getString() + " / " + getStringResourceModel(questionCategory.getQuestion(), questionCategory, openAnswerDefinition).getObject());
    } else if(isValidString(openLabel.getString())) {
      model = openLabel;
    } else if(isValidString(unitLabel.getString())) {
      model = unitLabel;
    } else if(isValidString(questionCategoryLabel.getString())) {
      model = questionCategoryLabel;
    } else {
      // last chance : the question label !
      model = questionLabel;
    }

    return model;
  }

  /**
   * Check if string is not empty and may mean something.
   * @param str
   * @return
   */
  private static boolean isValidString(String str) {
    if(str != null && str.trim().length() > 0) {
      Pattern pattern = Pattern.compile("[\\w\\s]+");
      return pattern.matcher(str).matches();
    }
    return false;
  }
}
