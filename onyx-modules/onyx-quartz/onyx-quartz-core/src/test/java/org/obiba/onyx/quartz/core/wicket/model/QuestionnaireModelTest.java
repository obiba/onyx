/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.model;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.DataType;
import org.obiba.wicket.test.MockSpringApplication;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class QuestionnaireModelTest {

  private ApplicationContextMock applicationContextMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private Questionnaire questionnaire;

  private QuestionnaireBundle questionnaireBundleMock;

  @Before
  public void setUp() {
    applicationContextMock = new ApplicationContextMock();

    questionnaireBundleManagerMock = createMock(QuestionnaireBundleManager.class);
    applicationContextMock.putBean("questionnaireBundleManager", questionnaireBundleManagerMock);

    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    applicationContextMock.putBean("activeQuestionnaireAdministrationService", activeQuestionnaireAdministrationServiceMock);

    questionnaire = createQuestionnaire();

    questionnaireBundleMock = createMock(QuestionnaireBundle.class);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    new WicketTester(application);
  }

  @Test
  public void testQuestionnaire() {
    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire);

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    QuestionnaireModel model = new QuestionnaireModel(questionnaire);
    Object result = model.getObject();

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals(questionnaire, result);
  }

  @Test
  public void testPage() {
    // Expect that methods are called on activeQuestionnaireAdministrationServiceMock to
    // retrieve the current locale and questionnaire.
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire);

    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire);

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IQuestionnaireElement localizable = questionnaire.getPages().get(1);
    QuestionnaireModel model = new QuestionnaireModel(localizable);
    Object result = model.getObject();

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals(localizable, result);
  }

  @Test
  public void testSection() {
    // Expect that methods are called on activeQuestionnaireAdministrationServiceMock to
    // retrieve the current locale and questionnaire.
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire);

    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire);

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IQuestionnaireElement localizable = questionnaire.getSections().get(1);
    QuestionnaireModel model = new QuestionnaireModel(localizable);
    Object result = model.getObject();

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals(localizable, result);
  }

  @Test
  public void testQuestion() {
    // Expect that methods are called on activeQuestionnaireAdministrationServiceMock to
    // retrieve the current locale and questionnaire.
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire);

    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire);

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IQuestionnaireElement localizable = questionnaire.getPages().get(1).getQuestions().get(0);
    QuestionnaireModel model = new QuestionnaireModel(localizable);
    Object result = model.getObject();

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals(localizable, result);
  }

  @Test
  public void testQuestionFromQuestionnaire() {
    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire);

    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IQuestionnaireElement localizable = questionnaire.getPages().get(1).getQuestions().get(0);
    QuestionnaireModel model = new QuestionnaireModel(questionnaire, localizable);
    Object result = model.getObject();

    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals(localizable, result);
  }

  @Test
  public void testQuestionCategory() {
    // Expect that methods are called on activeQuestionnaireAdministrationServiceMock to
    // retrieve the current locale and questionnaire.
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire);

    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire);

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IQuestionnaireElement localizable = questionnaire.getPages().get(1).getQuestions().get(0).getQuestionCategories().get(0);
    QuestionnaireModel model = new QuestionnaireModel(localizable);
    Object result = model.getObject();

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals(localizable, result);
  }

  @Test
  public void testOpenAnswerDefinition() {
    // Expect that methods are called on activeQuestionnaireAdministrationServiceMock to
    // retrieve the current locale and questionnaire.
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).times(2);

    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).times(2);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).times(2);

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IQuestionnaireElement localizable = questionnaire.getPages().get(1).getQuestions().get(0).getQuestionCategories().get(0).getCategory().getOpenAnswerDefinition();
    QuestionnaireModel model = new QuestionnaireModel(localizable);
    Object result = model.getObject();

    IQuestionnaireElement multipleLocalizable = questionnaire.getPages().get(3).getQuestions().get(0).getQuestionCategories().get(0).getCategory().getOpenAnswerDefinition().getOpenAnswerDefinitions().get(0);
    QuestionnaireModel multipleModel = new QuestionnaireModel(multipleLocalizable);
    Object multipleResult = multipleModel.getObject();

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals(localizable, result);
    Assert.assertEquals(multipleLocalizable, multipleResult);
  }

  @Test
  public void testCategory() {

    IQuestionnaireElement localizable = questionnaire.getPages().get(1).getQuestions().get(0).getQuestionCategories().get(0).getCategory();
    try {
      QuestionnaireModel model = new QuestionnaireModel(localizable);
      Assert.assertFalse(true);
    } catch(Exception e) {

    }

  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");
    builder.withSection("S1").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withCategory("2").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2").withCategory("3").withOpenAnswerDefinition("OPEN_DATE", DataType.DATE);
    builder.inQuestion("Q2").withCategory("4").withOpenAnswerDefinition("OPEN_TEXT_DEFAULT_VALUES", DataType.TEXT).setDefaultData("a", "b", "c");

    // condition test
    builder.inSection("S1").withPage("P3").withQuestion("Q3").withCategories("1", "2", "3");

    // multiple OpenAnswer test
    builder.inSection("S1").withPage("P4").withQuestion("Q4").withCategory("1").withOpenAnswerDefinition("Date", DataType.DATE).withOpenAnswerDefinition("Year", DataType.INTEGER);

    return builder.getQuestionnaire();
  }

}
