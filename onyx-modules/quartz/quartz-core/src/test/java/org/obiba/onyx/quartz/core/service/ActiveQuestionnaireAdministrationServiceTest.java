/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.service;

import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.validation.validator.PatternValidator;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.DataValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.SessionScope;

public class ActiveQuestionnaireAdministrationServiceTest extends BaseDefaultSpringContextTestCase {

  @Autowired
  private PersistenceManager persistenceManager;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private Questionnaire questionnaire;

  @Before
  public void setUp() {
    ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-spring-context.xml");
    applicationContext.getBeanFactory().registerScope("session", new SessionScope());

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    request.setSession(session);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    activeQuestionnaireAdministrationService = (ActiveQuestionnaireAdministrationService) applicationContext.getBean("activeQuestionnaireAdministrationService");

    questionnaire = createQuestionnaire();
  }

  /**
   * All the tests have been implemented in one method otherwise it is not possible to set the dataset because we use a
   * mock session and so cannot cast the service to allow data creation
   */
  @Test
  @Dataset
  public void testQuestionnaire() {
    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);
    Participant participant = persistenceManager.get(Participant.class, Long.valueOf("1"));
    QuestionnaireParticipant questionnaireParticipant = activeQuestionnaireAdministrationService.start(participant, Locale.FRENCH);
    Assert.assertEquals(questionnaireParticipant.getLocale(), Locale.FRENCH);
    Assert.assertEquals(questionnaireParticipant.getQuestionnaireName(), "HealthQuestionnaire");
    Assert.assertEquals(questionnaireParticipant.getQuestionnaireVersion(), "1.0");
    Assert.assertEquals(questionnaireParticipant.getParticipant(), participant);

    Question q1 = questionnaire.getPages().get(0).getQuestions().get(0);
    Question q2 = questionnaire.getPages().get(0).getQuestions().get(1);

    testAnswer(questionnaireParticipant, q1, q2);
    testFindAnswers(q1);
    testFindAnswer(q2);
    testSetActiveAnswers();
    testSetDeleteAnswer(q1);
    testSetDeleteAnswers(q1);
    testSetDeleteOpenAnswer(q2);

  }

  private void testAnswer(QuestionnaireParticipant questionnaireParticipant, Question q1, Question q2) {
    CategoryAnswer catAnswer_1 = activeQuestionnaireAdministrationService.answer(q1.getQuestionCategories().get(0), null);
    activeQuestionnaireAdministrationService.answer(q1.getQuestionCategories().get(1), null);
    activeQuestionnaireAdministrationService.answer(q1.getQuestionCategories().get(2), null);
    CategoryAnswer year = activeQuestionnaireAdministrationService.answer(q2.getQuestionCategories().get(0), new Data(DataType.TEXT, "1979"));

    Assert.assertEquals("1", catAnswer_1.getCategoryName());
    Assert.assertEquals(questionnaireParticipant, catAnswer_1.getQuestionAnswer().getQuestionnaireParticipant());
    Assert.assertEquals("Q1", catAnswer_1.getQuestionAnswer().getQuestionName());
    Assert.assertNotNull(year.getOpenAnswer());
    Assert.assertEquals("1979", year.getOpenAnswer().getData().getValueAsString());
    Assert.assertNotNull(catAnswer_1.getId());
  }

  private void testFindAnswers(Question q1) {
    List<CategoryAnswer> categoryAnswers = activeQuestionnaireAdministrationService.findAnswers(q1);
    Assert.assertEquals(3, categoryAnswers.size());
    Assert.assertEquals("1", categoryAnswers.get(0).getCategoryName());
    Assert.assertEquals("3", categoryAnswers.get(2).getCategoryName());
  }

  private void testFindAnswer(Question q2) {
    CategoryAnswer yearFound = activeQuestionnaireAdministrationService.findAnswer(q2.getQuestionCategories().get(0));
    Assert.assertEquals("1979", yearFound.getOpenAnswer().getData().getValueAsString());
  }

  private void testSetActiveAnswers() {
    Question q3 = questionnaire.getPages().get(1).getQuestions().get(0);
    Question q4 = q3.getQuestions().get(0);

    CategoryAnswer catAnswer_3 = activeQuestionnaireAdministrationService.answer(q3.getQuestionCategories().get(0), null);
    activeQuestionnaireAdministrationService.answer(q3.getQuestionCategories().get(1), null);
    CategoryAnswer catAnswer_4 = activeQuestionnaireAdministrationService.answer(q4.getQuestionCategories().get(1), null);
    activeQuestionnaireAdministrationService.answer(q4.getQuestionCategories().get(2), null);

    Assert.assertNull(catAnswer_3.getActive());
    Assert.assertNull(catAnswer_4.getActive());
    activeQuestionnaireAdministrationService.setActiveAnswers(q3, true);
    catAnswer_3 = activeQuestionnaireAdministrationService.findAnswer(q3.getQuestionCategories().get(0));
    catAnswer_4 = activeQuestionnaireAdministrationService.findAnswer(q4.getQuestionCategories().get(1));
    Assert.assertTrue(catAnswer_3.getActive());
    Assert.assertTrue(catAnswer_4.getActive());
  }

  public void testSetDeleteAnswer(Question q1) {
    activeQuestionnaireAdministrationService.deleteAnswer(q1.getQuestionCategories().get(0));
    Assert.assertNotNull(activeQuestionnaireAdministrationService.findAnswer(q1.getQuestionCategories().get(2)));
    Assert.assertNull(activeQuestionnaireAdministrationService.findAnswer(q1.getQuestionCategories().get(0)));
  }

  public void testSetDeleteAnswers(Question q1) {
    activeQuestionnaireAdministrationService.deleteAnswers(q1);
    Assert.assertNull(activeQuestionnaireAdministrationService.findAnswer(q1.getQuestionCategories().get(2)));
    Assert.assertNull(activeQuestionnaireAdministrationService.findAnswer(q1.getQuestionCategories().get(1)));
  }

  public void testSetDeleteOpenAnswer(Question q2) {
    Assert.assertNotNull(persistenceManager.get(OpenAnswer.class, Long.valueOf("1")));
    activeQuestionnaireAdministrationService.deleteAnswer(q2.getQuestionCategories().get(0));
    Assert.assertNull(activeQuestionnaireAdministrationService.findAnswer(q2.getQuestionCategories().get(0)));
    Assert.assertNull(persistenceManager.get(OpenAnswer.class, Long.valueOf("1")));
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");
    builder.inPage("P1").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("year", DataType.INTEGER).addOpenAnswerDefinitionValidator(new DataValidator(new PatternValidator("\\d{4}"), DataType.TEXT));
    builder.inSection("SB").withSection("MOCK").withPage("P2").withQuestion("Q3").withCategories("1", "2");
    builder.inQuestion("Q3").withQuestion("Q4").withCategories("subcat1", "subcat2", "subcat3");

    return builder.getQuestionnaire();
  }
}
