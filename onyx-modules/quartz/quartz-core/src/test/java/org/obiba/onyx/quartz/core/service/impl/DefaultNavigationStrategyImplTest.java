package org.obiba.onyx.quartz.core.service.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.TimestampSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.service.INavigationStrategy;
import org.obiba.onyx.util.data.DataType;

public class DefaultNavigationStrategyImplTest {

  private ActiveQuestionnaireAdministrationService serviceMock;

  private Questionnaire questionnaire;

  private QuestionnaireParticipant questionnaireParticipant;

  private Page page1;

  private Page page2;

  private Page page3;

  private Page page4;

  private Page page5;

  private ConfigurableQuestion q1;

  private ConfigurableQuestion q2;

  private ConfigurableQuestion q3;

  private ConfigurableQuestion q4;

  private ConfigurableQuestion q5;

  private ConfigurableQuestion q6;

  private ConfigurableQuestion q7;

  private List<CategoryAnswer> q1Answers;

  private List<CategoryAnswer> q2Answers;

  private List<CategoryAnswer> q3Answers;

  private List<CategoryAnswer> q4Answers;

  private List<CategoryAnswer> q5Answers;

  private List<CategoryAnswer> q6Answers;

  private List<CategoryAnswer> q7Answers;

  private INavigationStrategy navigationStrategy;

  @Before
  public void setUp() {
    // Create the mock ActiveQuestionnaireAdministrationService.
    serviceMock = createMock(ActiveQuestionnaireAdministrationService.class);

    // Create the test questionnaire.
    questionnaire = createQuestionnaire();

    // Create the test participant.
    questionnaireParticipant = createParticipant(questionnaire);

    // Create the test navigation strategy.
    navigationStrategy = new DefaultNavigationStrategyImpl();
  }

  @Test
  public void testGetPageOnStart() {
    //
    // Initialize answers.
    //
    q1Answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer q1Answer = new CategoryAnswer();
    q1Answer.setDataType(DataType.INTEGER);
    q1Answer.setIntegerValue(1l);
    q1Answers.add(q1Answer);

    q2Answers = new ArrayList<CategoryAnswer>();
    q3Answers = new ArrayList<CategoryAnswer>();
    q4Answers = new ArrayList<CategoryAnswer>();
    q5Answers = new ArrayList<CategoryAnswer>();
    q6Answers = new ArrayList<CategoryAnswer>();
    q7Answers = new ArrayList<CategoryAnswer>();

    // Expect that the questionnaire is retrieved from the service.
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);

    replay(serviceMock);

    Page startPage = navigationStrategy.getPageOnStart(serviceMock);

    // Verify that the mock was used as expected.
    verify(serviceMock);

    // Verify that the start page returned was p2 (informational page).
    Assert.assertEquals(page2, startPage);
  }

  @Test
  public void testGetPageOnNext() {
    //
    // Initialize answers.
    //
    q1Answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer q1Answer = new CategoryAnswer();
    q1Answer.setDataType(DataType.INTEGER);
    q1Answer.setIntegerValue(1l);
    q1Answers.add(q1Answer);

    q2Answers = new ArrayList<CategoryAnswer>();
    q3Answers = new ArrayList<CategoryAnswer>();
    q4Answers = new ArrayList<CategoryAnswer>();
    q5Answers = new ArrayList<CategoryAnswer>();
    q6Answers = new ArrayList<CategoryAnswer>();
    q7Answers = new ArrayList<CategoryAnswer>();

    // Test that from p2, we go to p3.
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);

    replay(serviceMock);

    Page pageAfterP2 = navigationStrategy.getPageOnNext(serviceMock, page2);

    verify(serviceMock);

    Assert.assertEquals(page3, pageAfterP2);

    // Reset mocks.
    reset(serviceMock);

    // Test that from p3, we go to p5.
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);

    replay(serviceMock);

    Page pageAfterP3 = navigationStrategy.getPageOnNext(serviceMock, page3);

    verify(serviceMock);

    Assert.assertEquals(page5, pageAfterP3);
  }

  @Test
  public void testGetPageOnPrevious() {
    //
    // Initialize answers.
    //
    q1Answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer q1Answer = new CategoryAnswer();
    q1Answer.setDataType(DataType.INTEGER);
    q1Answer.setIntegerValue(1l);
    q1Answers.add(q1Answer);

    q2Answers = new ArrayList<CategoryAnswer>();
    q3Answers = new ArrayList<CategoryAnswer>();
    q4Answers = new ArrayList<CategoryAnswer>();
    q5Answers = new ArrayList<CategoryAnswer>();
    q6Answers = new ArrayList<CategoryAnswer>();
    q7Answers = new ArrayList<CategoryAnswer>();

    // Test that from p5, we go to p3.
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);

    replay(serviceMock);

    Page pageBeforeP5 = navigationStrategy.getPageOnPrevious(serviceMock, page5);

    verify(serviceMock);

    Assert.assertEquals(page3, pageBeforeP5);

    // Reset mocks.
    reset(serviceMock);

    // Test that from p3, we go to p2.
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);

    replay(serviceMock);

    Page pageBeforeP3 = navigationStrategy.getPageOnPrevious(serviceMock, page3);

    verify(serviceMock);

    Assert.assertEquals(page2, pageBeforeP3);
  }

  @Test
  public void testGetPageOnResume() {
    // Initialize participant's resume page to p2.
    questionnaireParticipant.setResumePage(page2.getName());

    // Test that questionnaire is resumed on p2.
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);

    replay(serviceMock);

    Page resumePage = navigationStrategy.getPageOnResume(serviceMock, questionnaireParticipant);

    verify(serviceMock);

    Assert.assertEquals(page2, resumePage);

    // Reset mocks.
    reset(serviceMock);

    // Initialize participant's resume page to p3.
    questionnaireParticipant.setResumePage(page3.getName());

    // Test that questionnaire is resumed on p3.
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);

    replay(serviceMock);

    resumePage = navigationStrategy.getPageOnResume(serviceMock, questionnaireParticipant);

    verify(serviceMock);

    Assert.assertEquals(page3, resumePage);

    // Reset mocks.
    reset(serviceMock);

    // Initialize participant's resume page to p5.
    questionnaireParticipant.setResumePage(page5.getName());

    // Test that questionnaire is resumed on p5.
    expect(serviceMock.getQuestionnaire()).andReturn(questionnaire);

    replay(serviceMock);

    resumePage = navigationStrategy.getPageOnResume(serviceMock, questionnaireParticipant);

    verify(serviceMock);

    Assert.assertEquals(page5, resumePage);
  }

  private Questionnaire createQuestionnaire() {
    Questionnaire questionnaire = new Questionnaire("testQuestionnaire", "1.0.0");

    //
    // Page 1 : Contains just one question, with an answer source.
    //
    page1 = new Page("p1");
    questionnaire.addPage(page1);

    q1 = new ConfigurableQuestion("q1");

    // Add Timestamp answer source to q1
    Category cat = new Category("1");
    OpenAnswerDefinition def = new OpenAnswerDefinition("timestamp", DataType.DATE);
    def.setAnswerSource(new TimestampSource());
    cat.setOpenAnswerDefinition(def);
    QuestionCategory qcat = new QuestionCategory();
    qcat.setCategory(cat);
    q1.addQuestionCategory(qcat);

    page1.addQuestion(q1);

    //
    // Page 2 : Informational page (no questions).
    //
    page2 = new Page("p2");
    questionnaire.addPage(page2);

    //
    // Page 3 : Contains two questions.
    //
    page3 = new Page("p3");
    questionnaire.addPage(page3);

    q2 = new ConfigurableQuestion("q2");
    page3.addQuestion(q2);

    q3 = new ConfigurableQuestion("q3");
    page3.addQuestion(q3);

    //
    // Page 4 : Contains two questions, not to be answered.
    //
    page4 = new Page("p4");
    questionnaire.addPage(page4);

    q4 = new ConfigurableQuestion("q4");
    q4.setToBeAnswered(false);
    page4.addQuestion(q4);

    q5 = new ConfigurableQuestion("q5");
    q5.setToBeAnswered(false);
    page4.addQuestion(q5);

    //
    // Page 5 : Contains two questions.
    //
    page5 = new Page("p5");
    questionnaire.addPage(page5);

    q6 = new ConfigurableQuestion("q6");
    page5.addQuestion(q6);

    q7 = new ConfigurableQuestion("q7");
    page5.addQuestion(q7);

    return questionnaire;
  }

  private QuestionnaireParticipant createParticipant(Questionnaire questionnaire) {
    QuestionnaireParticipant participant = new QuestionnaireParticipant();

    participant.setQuestionnaireName(questionnaire.getName());
    participant.setQuestionnaireVersion(questionnaire.getVersion());

    return participant;
  }

  class ConfigurableQuestion extends Question {

    private static final long serialVersionUID = 1L;

    private boolean toBeAnswered;

    public ConfigurableQuestion(String name) {
      super(name);
      toBeAnswered = true; // default to true
    }

    public void setToBeAnswered(boolean toBeAnswered) {
      this.toBeAnswered = toBeAnswered;
    }

    public boolean isToBeAnswered(ActiveQuestionnaireAdministrationService service) {
      return toBeAnswered;
    }
  }
}
