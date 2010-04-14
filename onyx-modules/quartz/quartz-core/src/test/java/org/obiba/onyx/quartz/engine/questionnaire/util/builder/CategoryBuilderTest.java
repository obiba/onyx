/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.engine.questionnaire.util.builder;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;

public class CategoryBuilderTest {

  @Test(expected = IllegalArgumentException.class)
  public void makeSureThatThereCanOnlyBeOneNoAnswerCategoryPerQuestion() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("TestQuestionnaire", "1.0");
    builder.withSection("SECTION").withSection("SUB_SECTION").withPage("1").withQuestion("QUESTION");
    builder.inQuestion("QUESTION").optional().withSharedCategories("N", "Y");
    builder.inQuestion("QUESTION").withCategory("NO_ANSWER").noAnswer();
    builder.inQuestion("QUESTION").withCategory("NO_ANSWER2").noAnswer();
  }

  @Test
  public void makeSureThatNoAnswerCategoryIsAlwaysMissingCategory() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("TestQuestionnaire", "1.0");
    builder.withSection("SECTION").withSection("SUB_SECTION").withPage("1").withQuestion("QUESTION");
    Category noAnswerCategory = builder.inQuestion("QUESTION").withCategory("NO_ANSWER").noAnswer().getElement();
    Category otherCategory = builder.inQuestion("QUESTION").withCategory("OTHER").getElement();
    Assert.assertTrue(noAnswerCategory.isEscape());
    Assert.assertFalse(otherCategory.isEscape());
  }
}
