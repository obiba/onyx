package org.obiba.onyx.quartz.core.wicket;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Locale;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.MessageSource;


public class QuartzPanelTest {

  private WicketTester tester;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private EntityQueryService queryServiceMock;
  
  private QuestionnaireBundle questionnaireBundleMock;
  
  private MessageSource messageSourceMock;
  
  @Before
  public void setUp() {
    ApplicationContextMock mockCtx = new ApplicationContextMock();
    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    questionnaireBundleManagerMock = createMock(QuestionnaireBundleManager.class);
    queryServiceMock = createMock(EntityQueryService.class);
    questionnaireBundleMock = createMock(QuestionnaireBundle.class);
    messageSourceMock = createMock(MessageSource.class);
    
    mockCtx.putBean("activeInterviewService", activeInterviewServiceMock);
    mockCtx.putBean("activeQuestionnaireAdministrationService", activeQuestionnaireAdministrationServiceMock);
    mockCtx.putBean("questionnaireBundleManager", questionnaireBundleManagerMock);
    mockCtx.putBean("queryService", queryServiceMock);
    mockCtx.putBean("moduleRegistry", new ModuleRegistry());

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);
    tester = new WicketTester(application);
  }

  @Test
  public void testOnStepOutNext() {
    
    expect(activeInterviewServiceMock.getParticipant()).andReturn(newTestParticipant());
    expect(activeInterviewServiceMock.getInterview()).andReturn(newTestInterview());
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(newTestQuestionnaire()).times(4);
    expect((questionnaireBundleManagerMock.getBundle("QUE1"))).andReturn(questionnaireBundleMock).times(3);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(newTestQuestionnaire());
    activeQuestionnaireAdministrationServiceMock.setQuestionnaire((Questionnaire) EasyMock.anyObject());
    
    //calls for the label and description properties in LanguageSelectorPanel
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(Locale.FRENCH).times(4);
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSourceMock).times(2);
    expect(questionnaireBundleMock.getPropertyKey((Questionnaire) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(new String()).times(2);
    expect(messageSourceMock.getMessage("", null, Locale.FRENCH)).andReturn("").times(2);
    
    //expect(activeQuestionnaireAdministrationServiceMock.start((Participant) (EasyMock.anyObject()), (Locale) (EasyMock.anyObject()))).andReturn(new QuestionnaireParticipant());

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);
    replay(queryServiceMock);
    replay(messageSourceMock);
    
    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {

        QuartzPanel quartzPanel = new QuartzPanel(panelId, newTestStage());
        quartzPanel.setModel(new Model(newTestQuestionnaire()));
        return (quartzPanel);

      }
    });

    FormTester formTester = tester.newFormTester("panel:content:form");
    formTester.select("step:panel:localeSelect", 1);

    //tester.executeAjaxEvent("panel:content:form:nextLink", "onclick");

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
    verify(queryServiceMock);
    verify(messageSourceMock);
  }

  private Participant newTestParticipant() {
    Participant p = new Participant();

    p.setId(1);
    p.setFirstName("Natasha");
    p.setLastName("Dupont");
    p.setBarcode("barcode1");

    return (p);
  }

  private Interview newTestInterview() {
    Interview i = new Interview();
    User u = new User();

    u.setId(1);
    u.setLastName("Administrator");
    u.setFirstName("Onyx");
    i.setUser(u);

    return (i);
  }

  private Stage newTestStage() {
    Stage s = new Stage();

    s.setName("QUE1");

    return (s);
  }
  
  private Questionnaire newTestQuestionnaire() {
    Questionnaire q = new Questionnaire("QUE1", "1.0");
    
    q.addLocale(Locale.FRENCH);
    q.addLocale(Locale.ENGLISH);
    
    return q;
  }
  
}
