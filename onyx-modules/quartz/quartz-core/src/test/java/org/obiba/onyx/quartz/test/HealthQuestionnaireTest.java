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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.test.provider.AnswerProvider;
import org.obiba.onyx.quartz.test.provider.impl.ConfigurableAnswerProvider;

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

    AnswerProvider answerProvider = ConfigurableAnswerProvider.fromXmlResource(getAnswerProviderResourcePath() + "/answerProvider.xml");

    // answerQuestionsUpTo(answerProvider, getQuestion("Q3"));

    Question q29 = getQuestion("Q29");
    answerQuestionsUpTo(answerProvider, q29);
    assertCurrentPage(getPage("P22"));

    returnToEarlierQuestion(getQuestion("Q1"));
    assertCurrentPage(getPage("P1"));

    returnToLaterQuestion(q29);
    assertCurrentPage(getPage("P22"));

  }
}
