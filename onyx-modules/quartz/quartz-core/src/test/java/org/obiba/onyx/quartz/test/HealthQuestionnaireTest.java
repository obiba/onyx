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

/**
 * Tests for Health Questionnaire.
 */
public class HealthQuestionnaireTest extends AbstractQuestionnaireTest {

  protected String getQuestionnaireName() {
    return "HealthQuestionnaire";
  }

  protected String getQuestionnaireVersion() {
    return "1.0";
  }

  @Test
  @Dataset
  public void testQ1() {
    startQuestionnaire();
    assertCurrentPage(getPage("P1"));

    // AnswerProvider answerProvider = ConfigurableAnswerProvider.fromXmlResource(getAnswerProviderResourcePath() +
    // "/answerProvider.xml");
    //
    // Question q31 = getQuestion("Q31");
    // answerQuestionsUpTo(answerProvider, q31);
    // assertCurrentPage(getPage("P23"));
    //
    // returnToEarlierQuestion(getQuestion("Q1"));
    // assertCurrentPage(getPage("P1"));
    //
    // returnToLaterQuestion(q31);
    // assertCurrentPage(getPage("P23"));
  }

  @Test
  @Dataset
  public void testCondition() {
    startQuestionnaire();
    assertCurrentPage(getPage("P1"));

    // AnswerProvider answerProvider = ConfigurableAnswerProvider.fromXmlResource(getAnswerProviderResourcePath() +
    // "/answerProviderForConditionTest.xml");
    //
    // // if Q4 > 45 => question Q5 available
    // // if Q5 answered => Q6 available but not Q7
    // answerQuestionsUpTo(answerProvider, getQuestion("Q5"));
    // assertNextPage(getPage("P6"));
    // assertNextPage(getPage("P8"));
    //
    // // if Q4 < 45 => question Q7 available only
    // returnToEarlierQuestion(getQuestion("Q1"));
    // answerQuestionsUpTo(answerProvider, getQuestion("Q3"));
    // answerQuestion(getQuestion("Q4"), answerProvider.getAnswer(new Question("Q4Changed")));
    // assertCurrentPage(getPage("P4"));
    // assertNextPage(getPage("P7"));

  }
}
