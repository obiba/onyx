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
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;

/**
 * Get the {@link Data} from a previous answer in the context of the previously administered questionnaire.
 */
public class ExternalOpenAnswerSource extends DataSource {

  private static final long serialVersionUID = 1L;

  private String questionnaireName;

  private String questionName;

  private String categoryName;

  private String openAnswerDefinitionName;

  public ExternalOpenAnswerSource(String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName) {
    super();
    this.questionnaireName = questionnaireName;
    this.questionName = questionName;
    this.categoryName = categoryName;
    this.openAnswerDefinitionName = openAnswerDefinitionName;
  }

  @Override
  public Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    OpenAnswer openAnswer = activeQuestionnaireAdministrationService.findOpenAnswer(questionnaireName, questionName, categoryName, openAnswerDefinitionName);
    if(openAnswer != null && openAnswer.getCategoryAnswer().getActive()) {
      return openAnswer.getData();
    }
    return null;
  }

  public String getUnit() {
    // TODO Auto-generated method stub
    return null;
  }

}
