/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class QuestionnaireFinderTest {

  private static Questionnaire questionnaire;

  @BeforeClass
  public static void buildLargeQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.getInstance(new Questionnaire("LargeQuestionnaire", "1.0"));

    builder.withSection("Section");

    for(int i = 1; i <= 1000; i++) {
      builder.inSection("Section").withPage("P" + i).withQuestion("Q" + i).withCategories("1", "2", "3", "PNA", "DNK").withCategory("OTHER").withOpenAnswerDefinition("OPEN" + i, DataType.TEXT);
    }

    questionnaire = builder.getQuestionnaire();
  }

  @Test
  public void testSpeed() {
    findQuestion(1);
    findQuestion(1000);

    findQuestionCategory(1, "OTHER");
    findQuestionCategory(1000, "OTHER");

    findOpenAnswerDefinition(1);
    findOpenAnswerDefinition(1000);

    findAllQuestion(1);
    findAllQuestion(1000);

  }

  private void findQuestion(int index) {
    long start = System.currentTimeMillis();
    QuestionnaireFinder finder = QuestionnaireFinder.getInstance(questionnaire);
    Assert.assertNotNull(finder.findQuestion("Q" + index));
    System.out.println("Q" + index + "=" + (System.currentTimeMillis() - start) + "ms");
  }

  private void findQuestionCategory(int index, String category) {
    String questionCategory = "Q" + index + "." + category;
    long start = System.currentTimeMillis();
    QuestionnaireFinder finder = QuestionnaireFinder.getInstance(questionnaire);
    Assert.assertNotNull(finder.findQuestionCategory(QuestionCategory.getQuestionName(questionCategory), QuestionCategory.getCategoryName(questionCategory)));
    System.out.println(questionCategory + "=" + (System.currentTimeMillis() - start) + "ms");
  }

  private void findOpenAnswerDefinition(int index) {
    long start = System.currentTimeMillis();
    QuestionnaireFinder finder = QuestionnaireFinder.getInstance(questionnaire);
    Assert.assertNotNull(finder.findOpenAnswerDefinition("OPEN" + index));
    System.out.println("OPEN" + index + "=" + (System.currentTimeMillis() - start) + "ms");
  }

  private void findAllQuestion(int index) {
    long start = System.currentTimeMillis();
    QuestionnaireFinder finder = QuestionnaireFinder.getInstance(questionnaire);
    Assert.assertNotNull(finder.findQuestion("Q" + index));
    Assert.assertNotNull(finder.findQuestionCategory(QuestionCategory.getQuestionName("Q" + index + ".1"), QuestionCategory.getCategoryName("Q" + index + ".1")));
    Assert.assertNotNull(finder.findQuestionCategory(QuestionCategory.getQuestionName("Q" + index + ".2"), QuestionCategory.getCategoryName("Q" + index + ".2")));
    Assert.assertNotNull(finder.findQuestionCategory(QuestionCategory.getQuestionName("Q" + index + ".3"), QuestionCategory.getCategoryName("Q" + index + ".3")));
    Assert.assertNotNull(finder.findQuestionCategory(QuestionCategory.getQuestionName("Q" + index + ".OTHER"), QuestionCategory.getCategoryName("Q" + index + ".OTHER")));
    Assert.assertNotNull(finder.findQuestionCategory(QuestionCategory.getQuestionName("Q" + index + ".PNA"), QuestionCategory.getCategoryName("Q" + index + ".PNA")));
    Assert.assertNotNull(finder.findQuestionCategory(QuestionCategory.getQuestionName("Q" + index + ".DNK"), QuestionCategory.getCategoryName("Q" + index + ".DNK")));
    Assert.assertNotNull(finder.findOpenAnswerDefinition("OPEN" + index));
    System.out.println("Q" + index + ".all=" + (System.currentTimeMillis() - start) + "ms");
  }

}
