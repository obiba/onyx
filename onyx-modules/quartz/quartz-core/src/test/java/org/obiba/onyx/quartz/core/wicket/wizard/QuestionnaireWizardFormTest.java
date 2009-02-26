package org.obiba.onyx.quartz.core.wicket.wizard;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Locale;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;

public class QuestionnaireWizardFormTest {

  private WicketTester tester;

  private ApplicationContextMock applicationContextMock;

  private ModuleRegistry moduleRegistry;

  private EntityQueryService entityQueryServiceMock;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private QuestionnaireWizardPanel wizardPanel;

  private QuestionnaireBundle questionnaireBundleMock;

  private MessageSource messageSource;

  private Questionnaire questionnaire;

  private User user;

  private Participant participant;

  private Interview interview;

  @Before
  public void setUp() {
    //
    // Create test user, participant, interview, and questionnaire.
    //
    user = createUser(1l, "user", "test");
    participant = createParticipant(1l, "participant", "test");

    interview = createInterview(1l);
    interview.setUser(user);
    interview.setParticipant(participant);

    questionnaire = createQuestionnaire("testQuestionnaire", "1.0");

    //
    // Create test application context and add to it the necessary mocks.
    //
    applicationContextMock = new ApplicationContextMock();

    moduleRegistry = new ModuleRegistry();
    applicationContextMock.putBean("moduleRegistry", moduleRegistry);

    entityQueryServiceMock = createMock(EntityQueryService.class);
    applicationContextMock.putBean("entityQueryService", entityQueryServiceMock);

    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    applicationContextMock.putBean("activeInterviewService", activeInterviewServiceMock);

    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    applicationContextMock.putBean("activeQuestionnaireAdministrationService", activeQuestionnaireAdministrationServiceMock);

    questionnaireBundleManagerMock = createMock(QuestionnaireBundleManager.class);
    applicationContextMock.putBean("questionnaireBundleManager", questionnaireBundleManagerMock);

    questionnaireBundleMock = createMock(QuestionnaireBundle.class);

    messageSource = createMessageSource();

    //
    // Create the mock Spring application and create the Wicket tester.
    //
    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    tester = new WicketTester(application);
  }

  @Test
  public void testInitStartStepWhenResuming() {
    testInitStartStepWhenResuming(true);
  }

  @Test
  public void testInitStartStepWhenNotResuming() {
    testInitStartStepWhenResuming(false);
  }

  private void testInitStartStepWhenResuming(final boolean resuming) {
    expect(activeInterviewServiceMock.getParticipant()).andReturn(participant).anyTimes();
    expect(activeInterviewServiceMock.getInterview()).andReturn(interview).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(new Locale("en")).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getResumePage()).andReturn(new Page("p2")).anyTimes();
    expect(questionnaireBundleManagerMock.getBundle(questionnaire.getName())).andReturn(questionnaireBundleMock).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(questionnaire, "label")).andReturn("labelKey").anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(questionnaire, "description")).andReturn("descriptionKey").anyTimes();

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {
      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        wizardPanel = new QuestionnaireWizardPanel(panelId, new QuestionnaireModel(questionnaire), new StageModel(moduleRegistry, questionnaire.getName()), resuming);
        return wizardPanel;
      }
    });

    // Verify that the mock objects were used as expected.
    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    // Verify that the questionnaire wizard form's start step was initialized to the required step
    // (if resuming, to ConfirmResumeStep; if not resuming, to LanguageSelectionStep).
    WizardStepPanel stepPanel = (WizardStepPanel) wizardPanel.getWizardForm().get("step");

    Assert.assertNotNull(stepPanel);

    if(resuming) {
      Assert.assertTrue(stepPanel instanceof ConfirmResumeStep);
    } else {
      Assert.assertTrue(stepPanel instanceof LanguageSelectionStep);
    }
  }

  private Questionnaire createQuestionnaire(String name, String version) {
    return new Questionnaire(name, version);
  }

  private User createUser(long id, String firstName, String lastName) {
    User user = new User();
    user.setId(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);

    return user;
  }

  private Participant createParticipant(long id, String firstName, String lastName) {
    Participant participant = new Participant();
    participant.setId(id);
    participant.setFirstName(firstName);
    participant.setLastName(lastName);

    return participant;
  }

  private Interview createInterview(long id) {
    Interview interview = new Interview();
    interview.setId(id);

    return interview;
  }

  private MessageSource createMessageSource() {
    StaticMessageSource messageSource = new StaticMessageSource();

    Locale enLocale = new Locale("en");

    messageSource.addMessage("labelKey", enLocale, "test");
    messageSource.addMessage("descriptionKey", enLocale, "test");

    return messageSource;
  }
}