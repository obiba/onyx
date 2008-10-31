/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.test;

import org.junit.Test;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.test.provider.AnswerProvider;
import org.obiba.onyx.quartz.test.provider.impl.ConfigurableAnswerProvider;
import org.obiba.onyx.util.data.DataType;

/**
 * Tests for Health Questionnaire.
 */
public class HealthQuestionnaireTest extends AbstractQuestionnaireTest {

  protected String getQuestionnaireName() {
    return "HealthQuestionnaire";
  }

  @Test
  @Dataset
  public void testQ1() {
    startQuestionnaire();
    assertCurrentPage(getPage("P1"));

    Question q29 = getQuestion("Q29");
    answerQuestionsUpTo(getAnswerProvider(), q29, true);
    assertCurrentPage(getPage("P22"));

    returnToEarlierQuestion(getQuestion("Q1"));
    assertCurrentPage(getPage("P1"));

    returnToLaterQuestion(q29);
    assertCurrentPage(getPage("P22"));
  }

  private AnswerProvider getAnswerProvider() {
    ConfigurableAnswerProvider answerProvider = new ConfigurableAnswerProvider();

    // Set the default answer: NO_ANSWER (i.e., Prefer not to answer).
    CategoryAnswer defaultAnswer = new CategoryAnswer();
    defaultAnswer.setCategoryName("NO_ANSWER");
    answerProvider.setDefaultAnswer(defaultAnswer);

    // Set Q2 (BIRTHDATE) answer to 1980.
    Question q2 = getQuestion("Q2");
    CategoryAnswer q2Answer = new CategoryAnswer();
    q2Answer.setCategoryName("1");
    q2Answer.setDataType(DataType.INTEGER);
    q2Answer.setIntegerValue(1980l);
    answerProvider.setAnswer(q2, q2Answer);

    // Set Q3 (BIRTHMONTH) answer to 11.
    Question q3 = getQuestion("Q3");
    CategoryAnswer q3Answer = new CategoryAnswer();
    q3Answer.setCategoryName("1");
    q3Answer.setDataType(DataType.INTEGER);
    q3Answer.setIntegerValue(11l);
    answerProvider.setAnswer(q3, q3Answer);

    return answerProvider;
  }
}
