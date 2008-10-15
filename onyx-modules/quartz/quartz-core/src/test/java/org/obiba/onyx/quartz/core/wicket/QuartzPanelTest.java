package org.obiba.onyx.quartz.core.wicket;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Locale;

import org.apache.wicket.markup.html.WebPage;
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
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.wicket.test.MockSpringApplication;

public class QuartzPanelTest {

  private WicketTester tester;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private EntityQueryService queryServiceMock;

  @Before
  public void setUp() {
    ApplicationContextMock mockCtx = new ApplicationContextMock();
    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    questionnaireBundleManagerMock = createMock(QuestionnaireBundleManager.class);
    queryServiceMock = createMock(EntityQueryService.class);

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
    QuestionnaireBundle questionnaireBundleMock = createMock(QuestionnaireBundle.class);

    expect(activeInterviewServiceMock.getParticipant()).andReturn(newTestParticipant());//.times(2);
    expect(activeInterviewServiceMock.getInterview()).andReturn(newTestInterview());
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(new Questionnaire("QUE1", "1.0")).times(2);
    expect(questionnaireBundleManagerMock.getBundle("QUE1")).andReturn(questionnaireBundleMock);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(new Questionnaire("QUE1", "1.0"));
    activeQuestionnaireAdministrationServiceMock.setQuestionnaire((Questionnaire) EasyMock.anyObject());
    //expect(activeQuestionnaireAdministrationServiceMock.start((Participant) (EasyMock.anyObject()), (Locale) (EasyMock.anyObject()))).andReturn(new QuestionnaireParticipant());
    //expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(Locale.FRENCH);

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(queryServiceMock);
    
    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {

        QuartzPanel quartzPanel = new QuartzPanel(panelId, newTestStage());
        quartzPanel.setModel(new Model(new Questionnaire("QUE1", "1.0")));
        return (quartzPanel);

      }
    });

    FormTester formTester = tester.newFormTester("panel:content:form");
    formTester.select("step:panel:localeSelect", 0);

    //tester.executeAjaxEvent("panel:content:form:nextLink", "onclick");

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(queryServiceMock);
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
}
