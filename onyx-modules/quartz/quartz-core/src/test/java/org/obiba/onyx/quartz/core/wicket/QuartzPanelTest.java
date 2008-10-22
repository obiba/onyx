/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Locale;

import org.apache.wicket.Page;
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
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayoutFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.impl.DefaultPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.DefaultQuestionPanelFactory;
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

    PageLayoutFactoryRegistry pageLayoutFactoryRegistryMock = new PageLayoutFactoryRegistry();
    pageLayoutFactoryRegistryMock.registerFactory(new DefaultPageLayoutFactory());
    mockCtx.putBean("pageLayoutFactoryRegistry", pageLayoutFactoryRegistryMock);

    QuestionPanelFactoryRegistry questionPanelFactoryRegistryMock = new QuestionPanelFactoryRegistry();
    questionPanelFactoryRegistryMock.registerFactory(new DefaultQuestionPanelFactory());
    mockCtx.putBean("questionPanelFactoryRegistry", questionPanelFactoryRegistryMock);

    MockSpringApplication application = new MockSpringApplication();
    application.setHomePage(Page.class);
    application.setApplicationContext(mockCtx);
    tester = new WicketTester(application);
  }

  @Test
  public void testOnStepOutNext() {

    final Questionnaire questionnaire = createQuestionnaire();
    expect(activeInterviewServiceMock.getParticipant()).andReturn(newTestParticipant()).times(2);
    expect(activeInterviewServiceMock.getInterview()).andReturn(newTestInterview());
    expect((questionnaireBundleManagerMock.getBundle("HealthQuestionnaire"))).andReturn(questionnaireBundleMock).times(3);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();

    // calls for the label and description properties in LanguageSelectorPanel
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(Locale.FRENCH).times(4);
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSourceMock).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey((Questionnaire) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(new String()).atLeastOnce();
    expect(messageSourceMock.getMessage("", null, Locale.FRENCH)).andReturn("").times(3);

    expect(activeQuestionnaireAdministrationServiceMock.start((Participant) (EasyMock.anyObject()), (Locale) (EasyMock.anyObject()))).andReturn(new QuestionnaireParticipant());
    expect(activeQuestionnaireAdministrationServiceMock.startPage()).andReturn(questionnaire.getPages().get(0));

    // calls for Panel creation on next link click
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect((questionnaireBundleManagerMock.getBundle("HealthQuestionnaire"))).andReturn(questionnaireBundleMock).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(Locale.FRENCH).anyTimes();
    expect(messageSourceMock.getMessage("", null, Locale.FRENCH)).andReturn("").anyTimes();

    expect(activeQuestionnaireAdministrationServiceMock.findAnswer((QuestionCategory) EasyMock.anyObject())).andReturn(new CategoryAnswer()).times(3);

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);
    replay(queryServiceMock);
    replay(messageSourceMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {

        QuartzPanel quartzPanel = new QuartzPanel(panelId, newTestStage(), false);
        quartzPanel.setModel(new Model(questionnaire));
        return (quartzPanel);

      }
    });

    FormTester formTester = tester.newFormTester("panel:content:form");
    formTester.select("step:panel:localeSelect", 1);

    tester.executeAjaxEvent("panel:content:form:nextLink", "onclick");

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

    s.setName("HealthQuestionnaire");

    return (s);
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
