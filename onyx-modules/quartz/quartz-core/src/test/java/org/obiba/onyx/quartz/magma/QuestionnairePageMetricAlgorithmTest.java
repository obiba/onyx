/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.magma;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireMetric;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;

public class QuestionnairePageMetricAlgorithmTest {
  @Test
  public void test_algorithmAcceptsNonExistingPage() {
    QuestionnaireParticipant qp = new QuestionnaireParticipant();
    QuestionnaireMetric qm = new QuestionnaireMetric();
    qm.setPage("DOES NOT EXIST");
    qm.setQuestionnaireParticipant(qp);
    qm.incrementDuration(50);

    QuestionnairePageMetricAlgorithm algorithm = new QuestionnairePageMetricAlgorithm(createQuestionnaire(), qm);
    Assert.assertEquals("DOES NOT EXIST", algorithm.getPage());
    Assert.assertEquals("", algorithm.getSection());
    Assert.assertEquals(50, algorithm.getDuration());
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(Locale.FRENCH);
    q.addLocale(Locale.ENGLISH);

    return q;
  }
}
