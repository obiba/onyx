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

import org.junit.Ignore;
import org.junit.Test;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.test.provider.AnswerProvider;
import org.obiba.onyx.quartz.test.provider.impl.ConfigurableAnswerProvider;
import org.obiba.onyx.util.data.DataType;

/**
 * Tests for Health Questionnaire.
 */
public class LargeQuestionnaireTest extends AbstractQuestionnaireTest {

  protected String getQuestionnaireName() {
    return "LargeQuestionnaire";
  }

  protected String getQuestionnaireVersion() {
    return "1.0";
  }

  @Ignore
  @Test
  @Dataset
  public void testSpeed() {

    startQuestionnaire();
    assertCurrentPage(getPage("P1"));

    AnswerProvider answerProvider = ConfigurableAnswerProvider.fromXmlResource(getAnswerProviderResourcePath() + "/answerProvider.xml");

    Question q = getQuestion("Q150");
    answerQuestionsUpTo(answerProvider, q);

    assertCurrentPage(getPage("P150"));
  }

  private static final String QUESTIONNAIRE = "LargeQuestionnaire";

  private static final String OPEN = "OPEN";

  private static final String OTHER = "OTHER";

  private static final String PNA = "PNA";

  private static final String DNK = "DNK";

  public static QuestionnaireBuilder buildQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire(QUESTIONNAIRE, "1.0");

    builder.withSection("Section");

    for(int i = 1; i <= 500; i++) {
      builder.inSection("Section").withPage("P" + i).withQuestion("Q" + i).withCategories("1", "2", "3", PNA, DNK).withCategory(OTHER).withOpenAnswerDefinition(OPEN + i, DataType.TEXT);
    }

    return builder;
  }
}
