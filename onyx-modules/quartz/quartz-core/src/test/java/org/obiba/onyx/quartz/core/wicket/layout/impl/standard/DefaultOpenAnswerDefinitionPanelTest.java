/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Locale;

import junit.framework.Assert;

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
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayoutFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.wicket.test.MockSpringApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

public class DefaultOpenAnswerDefinitionPanelTest {

  static final Logger log = LoggerFactory.getLogger(DefaultOpenAnswerDefinitionPanelTest.class);

  private WicketTester tester;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private EntityQueryService queryServiceMock;

  private QuestionnaireBundle questionnaireBundleMock;

  private MessageSource messageSourceMock;

  private DefaultOpenAnswerDefinitionPanelMock openMock;

  private QuestionnaireParticipant questionnaireParticipant;

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
    pageLayoutFactoryRegistryMock.register(new DefaultPageLayoutFactory());
    mockCtx.putBean("pageLayoutFactoryRegistry", pageLayoutFactoryRegistryMock);

    QuestionPanelFactoryRegistry questionPanelFactoryRegistryMock = new QuestionPanelFactoryRegistry();
    questionPanelFactoryRegistryMock.register(new DefaultQuestionPanelFactory());
    mockCtx.putBean("questionPanelFactoryRegistry", questionPanelFactoryRegistryMock);

    MockSpringApplication application = new MockSpringApplication();
    application.setHomePage(Page.class);
    application.setApplicationContext(mockCtx);
    tester = new WicketTester(application);

    questionnaireParticipant = new QuestionnaireParticipant();
    questionnaireParticipant.setParticipant(new Participant());
  }

  @Test
  public void testOpenInteger() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousAnswer = new CategoryAnswer();
    OpenAnswer openAnswer = new OpenAnswer();
    openAnswer.setDataType(DataType.INTEGER);
    openAnswer.setData(DataBuilder.buildInteger(321l));
    previousAnswer.addOpenAnswer(openAnswer);

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey((Questionnaire) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(new String()).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant);
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer((Question) EasyMock.anyObject(), (Category) EasyMock.anyObject(), (OpenAnswerDefinition) EasyMock.anyObject())).andReturn(openAnswer);
    expect(activeQuestionnaireAdministrationServiceMock.answer((Question) EasyMock.anyObject(), (QuestionCategory) EasyMock.anyObject(), (OpenAnswerDefinition) EasyMock.anyObject(), (Data) EasyMock.anyObject())).andReturn(new CategoryAnswer()).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(Locale.FRENCH).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSourceMock).anyTimes();
    expect(messageSourceMock.getMessage("", null, Locale.FRENCH)).andReturn("").atLeastOnce();

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);
    replay(messageSourceMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {
        QuestionCategory element = QuestionnaireFinder.getInstance(questionnaire).findQuestionCategory("Q2", "Q2.1");
        return openMock = new DefaultOpenAnswerDefinitionPanelMock(panelId, new Model(element));
      }
    });

    // tester.dumpPage();

    // check previous answer is here
    Assert.assertNotNull(openMock);
    Assert.assertNotNull(openMock.getData());
    Long value = openMock.getData().getValue();
    Assert.assertEquals(321, value.intValue());

    // check onclick callback
    // tester.executeAjaxEvent("panel:form:content:open:input:field", "onclick");
    // Assert.assertEquals("No click event on open field", Boolean.TRUE, assertClickModel.getObject());

    // check and set field value
    FormTester formTester = tester.newFormTester("panel:form");
    Assert.assertEquals("321", formTester.getTextComponentValue("content:open:input:field"));
    formTester.setValue("content:open:input:field", "123");

    // check submit of value
    tester.executeAjaxEvent("panel:form:content:open:input:field", "onchange");
    Assert.assertNotNull(openMock.getData());
    value = openMock.getData().getValue();
    Assert.assertEquals(123, value.intValue());

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
    verify(messageSourceMock);
  }

  @Test
  public void testOpenTextWithDefaultValues() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousAnswer = new CategoryAnswer();
    OpenAnswer openAnswer = new OpenAnswer();
    openAnswer.setDataType(DataType.TEXT);
    openAnswer.setData(DataBuilder.buildText("c"));
    previousAnswer.addOpenAnswer(openAnswer);

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey((Questionnaire) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(new String()).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant);
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer((Question) EasyMock.anyObject(), (Category) EasyMock.anyObject(), (OpenAnswerDefinition) EasyMock.anyObject())).andReturn(openAnswer);
    expect(activeQuestionnaireAdministrationServiceMock.answer((Question) EasyMock.anyObject(), (QuestionCategory) EasyMock.anyObject(), (OpenAnswerDefinition) EasyMock.anyObject(), (Data) EasyMock.anyObject())).andReturn(new CategoryAnswer()).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(Locale.FRENCH).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSourceMock).anyTimes();
    expect(messageSourceMock.getMessage("", null, Locale.FRENCH)).andReturn("").atLeastOnce();

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);
    replay(messageSourceMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {
        QuestionCategory element = QuestionnaireFinder.getInstance(questionnaire).findQuestionCategory("Q2", "Q2.4");
        return openMock = new DefaultOpenAnswerDefinitionPanelMock(panelId, new Model(element));
      }
    });

    // tester.dumpPage();

    // check the right select option is selected
    tester.assertContains("(option selected=\"selected\" value=\"c\")");

    // check previous answer is here
    Assert.assertNotNull(openMock);
    Assert.assertNotNull(openMock.getData());
    String value = openMock.getData().getValue();
    Assert.assertEquals("c", value);

    // check and set field value
    FormTester formTester = tester.newFormTester("panel:form");
    formTester.select("content:open:input:select", 1);

    // check submit of value
    tester.executeAjaxEvent("panel:form:content:open:input:select", "onchange");
    Assert.assertNotNull(openMock.getData());
    value = openMock.getData().getValue();
    Assert.assertEquals("b", value);

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
    verify(messageSourceMock);
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");
    builder.withSection("S1").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withCategory("2").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2").withCategory("3").withOpenAnswerDefinition("OPEN_DATE", DataType.DATE);
    builder.inQuestion("Q2").withCategory("4").withOpenAnswerDefinition("OPEN_TEXT_DEFAULT_VALUES", DataType.TEXT).setDefaultData("a", "b", "c");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(Locale.FRENCH);
    q.addLocale(Locale.ENGLISH);

    return q;
  }

}
