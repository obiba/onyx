/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.CategoryBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;

/**
 *
 */
public interface QuestionnaireVariableNameResolver {

  /**
   * Returns the name of a {@link Question} variable.
   * 
   * <pre>
   * The name may be anything if it has been explicitly set. e.g:
   *    binge_male
   * Otherwise the name will take one of the following forms:
   *    QuestionnaireName.QuestionName
   *    QuestionnaireName.ParentQuestionName.QuestionName
   * e.g:
   *    CIPreliminaryQuestionnaire.ABLE_TO_STAND
   *    CIPreliminaryQuestionnaire.TABLE_CONTRAINDICATION.ULTRASOUND_GEL_ALLERGY
   * </pre>
   * @param question The name of this {@code Question} will be returned.
   * @return The name of the {@code Question}.
   * @see QuestionBuilder#setVariableName(String)
   */
  public String variableName(Question question);

  /**
   * Returns the name of {@link QuestionCategory} variable.
   * 
   * <pre>
   * The name may be anything if it has been explicitly set. e.g:
   *    binge_male_y
   * Otherwise the name will take one of the following forms:
   *    QuestionnaireName.QuestionName.CategoryName
   *    QuestionnaireName.ParentQuestionName.QuestionName.CategoryName
   * e.g:
   *    CIPreliminaryQuestionnaire.ABLE_TO_STAND.Y
   *    CIPreliminaryQuestionnaire.CURRENTLY_PREGNANT.PNA
   *    CIPreliminaryQuestionnaire.TABLE_CONTRAINDICATION.ISOPROPYL_ALCOHOL_ALLERGY.DNK
   * </pre>
   * @param question The {@link Question} the {@code QuestionCategory} is associated with.
   * @param questionCategory The name of this {@code QuestionCategory} will be returned.
   * @return The name of the {@code Category}.
   * @see CategoryBuilder#setVariableName(String)
   */
  public String variableName(Question question, QuestionCategory questionCategory);

  /**
   * Returns the name of an {@link OpenAnswerDefinition} variable.
   * 
   * <pre>
   * The name may be anything if it has been explicitly set. e.g:
   *    tv_time_hours
   * Otherwise the name will take one of the following forms:
   *    QuestionnaireName.QuestionName.CategoryName.OpenAnswerDefinitionName
   *    QuestionnaireName.ParentQuestionName.QuestionName.CategoryName.OpenAnswerDefinitionName
   * e.g:
   *    HealthQuestionnaireTouchScreen.LEISURE_MOD_ACT_TIME_DAY.ACTIVITY_TIME_DAY.ACTIVITY_MIN_DAY
   *    HealthQuestionnaireTouchScreen.TV_TIME_WEEKLY.OPEN_N_HOURS.OPEN_N_HOURS
   * </pre>
   * 
   * @param question The {@link Question} the {@code QuestionCategory} is associated with.
   * @param questionCategory The name of this {@code QuestionCategory} will form the category part of the name.
   * @param oad The name of this {@code OpenAnswerDefinition} will be returned.
   * @return
   */
  public String variableName(Question question, QuestionCategory questionCategory, OpenAnswerDefinition oad);

}
