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

import org.apache.wicket.validation.validator.NumberValidator;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.validation.DataValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.util.data.DataBuilder;
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
    Question q3 = questionnaire.getPages().get(2).getQuestions().get(0);

    testAnswer(questionnaireParticipant, q1, q2, q3);
    testFindAnswers(q1);
    testFindAnswer(q2);
    testFindOpenAnswer(q3);
    testFindExternalOpenAnswer(q3);
    testSetActiveAnswers();
    testSetDeleteAnswer(q1);
    testSetDeleteAnswers(q1);
    testSetDeleteOpenAnswer(q2);
    testRetrieveQuestionComment(q1, q2, q3);

  }

  private void testAnswer(QuestionnaireParticipant questionnaireParticipant, Question q1, Question q2, Question q3) {
    CategoryAnswer catAnswer_1 = activeQuestionnaireAdministrationService.answer(q1.getQuestionCategories().get(0), null, null);
    activeQuestionnaireAdministrationService.answer(q1.getQuestionCategories().get(1), null, null);
    activeQuestionnaireAdministrationService.answer(q1.getQuestionCategories().get(2), null, null);
    activeQuestionnaireAdministrationService.addComment(q1, "comment question 1");

    QuestionCategory questionCategory = q2.getQuestionCategories().get(0);
    CategoryAnswer year = activeQuestionnaireAdministrationService.answer(questionCategory, questionCategory.getCategory().getOpenAnswerDefinition(), DataBuilder.buildText("1979"));
    activeQuestionnaireAdministrationService.addComment(q2, "comment question 2");

    Assert.assertEquals("1", catAnswer_1.getCategoryName());
    Assert.assertEquals(questionnaireParticipant, catAnswer_1.getQuestionAnswer().getQuestionnaireParticipant());
    Assert.assertEquals("Q1", catAnswer_1.getQuestionAnswer().getQuestionName());
    Assert.assertNotNull(year.getOpenAnswers().get(0));
    Assert.assertEquals("1979", year.getOpenAnswers().get(0).getData().getValueAsString());
    Assert.assertNotNull(catAnswer_1.getId());

    questionCategory = q3.getQuestionCategories().get(0);
    CategoryAnswer secondYear = activeQuestionnaireAdministrationService.answer(questionCategory, questionCategory.getCategory().getOpenAnswerDefinition().getOpenAnswerDefinitions().get(0), DataBuilder.buildInteger("1966"));
    activeQuestionnaireAdministrationService.answer(questionCategory, questionCategory.getCategory().getOpenAnswerDefinition().getOpenAnswerDefinitions().get(1), DataBuilder.buildInteger("6"));
    activeQuestionnaireAdministrationService.answer(questionCategory, questionCategory.getCategory().getOpenAnswerDefinition().getOpenAnswerDefinitions().get(2), DataBuilder.buildInteger("5"));
    activeQuestionnaireAdministrationService.addComment(q3, "comment question 3");

    Assert.assertEquals("1966", secondYear.getOpenAnswers().get(0).getData().getValueAsString());
  }

  private void testFindAnswers(Question q1) {
    List<CategoryAnswer> categoryAnswers = activeQuestionnaireAdministrationService.findAnswers(q1);
    Assert.assertEquals(3, categoryAnswers.size());
    Assert.assertEquals("1", categoryAnswers.get(0).getCategoryName());
    Assert.assertEquals("3", categoryAnswers.get(2).getCategoryName());
  }

  private void testFindAnswer(Question q2) {
    CategoryAnswer yearFound = activeQuestionnaireAdministrationService.findAnswer(q2.getQuestionCategories().get(0));
    OpenAnswer template = new OpenAnswer();
    template.setCategoryAnswer(yearFound);
    List<OpenAnswer> openAnswers = persistenceManager.match(template);
    Assert.assertEquals("1979", openAnswers.get(0).getData().getValueAsString());
  }

  private void testFindOpenAnswer(Question q3) {
    OpenAnswer openAnswer = activeQuestionnaireAdministrationService.findOpenAnswer(q3.getQuestionCategories().get(0), q3.getQuestionCategories().get(0).getCategory().findOpenAnswerDefinition("MONTH"));
    Assert.assertEquals("6", openAnswer.getData().getValueAsString());
  }

  private void testFindExternalOpenAnswer(Question q3) {
    OpenAnswer openAnswer = activeQuestionnaireAdministrationService.findOpenAnswer(activeQuestionnaireAdministrationService.getQuestionnaire().getName(), q3.getName(), q3.getQuestionCategories().get(0).getCategory().getName(), q3.getQuestionCategories().get(0).getCategory().findOpenAnswerDefinition("MONTH").getName());
    Assert.assertEquals("6", openAnswer.getData().getValueAsString());
  }

  private void testSetActiveAnswers() {
    Question q3 = questionnaire.getPages().get(1).getQuestions().get(0);
    Question q4 = q3.getQuestions().get(0);

    List<CategoryAnswer> actives = activeQuestionnaireAdministrationService.findActiveAnswers(q3);
    Assert.assertEquals(0, actives.size());

    CategoryAnswer catAnswer_3 = activeQuestionnaireAdministrationService.answer(q3.getQuestionCategories().get(0), null, null);
    activeQuestionnaireAdministrationService.answer(q3.getQuestionCategories().get(1), null, null);

    actives = activeQuestionnaireAdministrationService.findActiveAnswers(q3);
    Assert.assertEquals(2, actives.size());

    CategoryAnswer catAnswer_4 = activeQuestionnaireAdministrationService.answer(q4.getQuestionCategories().get(1), null, null);
    activeQuestionnaireAdministrationService.answer(q4.getQuestionCategories().get(2), null, null);

    Assert.assertTrue(catAnswer_3.getActive());
    Assert.assertTrue(catAnswer_4.getActive());
    activeQuestionnaireAdministrationService.setActiveAnswers(q3, false);

    actives = activeQuestionnaireAdministrationService.findActiveAnswers(q3);
    Assert.assertEquals(0, actives.size());

    catAnswer_3 = activeQuestionnaireAdministrationService.findAnswer(q3.getQuestionCategories().get(0));
    catAnswer_4 = activeQuestionnaireAdministrationService.findAnswer(q4.getQuestionCategories().get(1));
    Assert.assertFalse(catAnswer_3.getActive());
    Assert.assertFalse(catAnswer_4.getActive());
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
    OpenAnswer template = new OpenAnswer();
    template.setCategoryAnswer(activeQuestionnaireAdministrationService.findAnswer(q2.getQuestionCategories().get(0)));
    Assert.assertNotNull(persistenceManager.matchOne(template));
    activeQuestionnaireAdministrationService.deleteAnswer(q2.getQuestionCategories().get(0));
    Assert.assertNull(activeQuestionnaireAdministrationService.findAnswer(q2.getQuestionCategories().get(0)));
    Assert.assertNull(persistenceManager.matchOne(template));
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");
    builder.inPage("P1").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("year", DataType.INTEGER).addValidator(new DataValidator(new PatternValidator("\\d{4}"), DataType.TEXT));
    builder.inSection("SB").withSection("MOCK").withPage("P2").withQuestion("Q3").withCategories("1", "2");
    builder.inQuestion("Q3").withQuestion("Q4").withCategories("subcat1", "subcat2", "subcat3");

    builder.inSection("SB").withPage("P3").withQuestion("Q5").withCategory("DATE").withOpenAnswerDefinition("DATE", DataType.DATE).withOpenAnswerDefinition("YEAR", DataType.INTEGER).addValidator(new DataValidator(new PatternValidator("\\d{4}"), DataType.TEXT));
    builder.inOpenAnswerDefinition("DATE").withOpenAnswerDefinition("MONTH", DataType.INTEGER).addValidator(new DataValidator(new NumberValidator.RangeValidator(1, 12), DataType.INTEGER));
    builder.inOpenAnswerDefinition("DATE").withOpenAnswerDefinition("DAY", DataType.INTEGER).addValidator(new DataValidator(new NumberValidator.RangeValidator(1, 31), DataType.INTEGER));

    return builder.getQuestionnaire();
  }

  private void testRetrieveQuestionComment(Question q1, Question q2, Question q3) {
    activeQuestionnaireAdministrationService.deleteAnswers(q1);
    String comment = activeQuestionnaireAdministrationService.getComment(q1);
    Assert.assertEquals("comment question 1", comment);

    activeQuestionnaireAdministrationService.deleteAnswers(q2);
    comment = activeQuestionnaireAdministrationService.getComment(q2);
    Assert.assertEquals("comment question 2", comment);

    activeQuestionnaireAdministrationService.deleteAnswers(q3);
    comment = activeQuestionnaireAdministrationService.getComment(q3);
    Assert.assertEquals("comment question 3", comment);
  }

}
