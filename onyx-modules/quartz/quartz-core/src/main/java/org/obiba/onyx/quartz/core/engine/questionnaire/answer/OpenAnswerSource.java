/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;

/**
 * Get the {@link Data} from a previous answer in the context of the currently administered questionnaire.
 */
public class OpenAnswerSource extends DataSource {

  private static final long serialVersionUID = 1L;

  private Question question;

  private Category category;

  private OpenAnswerDefinition openAnswerDefinition;

  public OpenAnswerSource(Question question, Category category, OpenAnswerDefinition openAnswerDefinition) {
    super();
    this.question = question;
    this.category = category;
    this.openAnswerDefinition = openAnswerDefinition;
  }

  @Override
  public Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    OpenAnswer openAnswer = activeQuestionnaireAdministrationService.findOpenAnswer(question, category, openAnswerDefinition);
    if(openAnswer != null && openAnswer.getCategoryAnswer().getActive()) {
      return openAnswer.getData();
    }
    return null;
  }

  public String getUnit() {
    return openAnswerDefinition.getUnit();
  }

}
