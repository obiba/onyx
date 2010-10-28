/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.validation.validator.RangeValidator;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionStateHolder;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayoutFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionPanelFactory;
import org.obiba.onyx.quartz.test.ComponentTesterUtils;
import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.link.AjaxImageLink;
import org.obiba.wicket.test.MockSpringApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.StaticMessageSource;

public class SimplifiedQuestionPanelTest {

  static final Logger log = LoggerFactory.getLogger(SimplifiedQuestionPanelTest.class);

  private WicketTester tester;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private EntityQueryService queryServiceMock;

  private QuestionnaireBundle questionnaireBundleMock;

  private StaticMessageSource messageSource;

  private IPropertyKeyProvider propertyKeyProvider;

  private Locale locale = Locale.ENGLISH;

  private QuestionnaireParticipant questionnaireParticipant;

  @Before
  public void setUp() {
    ApplicationContextMock mockCtx = new ApplicationContextMock();
    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    questionnaireBundleManagerMock = createMock(QuestionnaireBundleManager.class);
    queryServiceMock = createMock(EntityQueryService.class);
    questionnaireBundleMock = createMock(QuestionnaireBundle.class);
    // messageSourceMock = createMock(MessageSource.class);

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

    messageSource = new StaticMessageSource() {
      @Override
      protected MessageFormat createMessageFormat(String msg, Locale locale1) {
        return new StringReferenceCompatibleMessageFormat((msg != null ? msg : ""), locale1);
      }
    };
    messageSource.addMessage("Questionnaire.HealthQuestionnaire.clearAll", locale, "clear all");
    messageSource.addMessage("Questionnaire.HealthQuestionnaire.clickHere", locale, "click here");

    messageSource.addMessage("Question.Q1.label", locale, "question label");
    messageSource.addMessage("Question.Q1.help", locale, "question help");
    messageSource.addMessage("Question.Q1.specifications", locale, "question specifications");
    messageSource.addMessage("Question.Q1.instructions", locale, "question instructions");
    messageSource.addMessage("Question.Q1.caption", locale, "question caption");
    messageSource.addMessage("Question.Q1.categoryOrder", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q1.1.description", locale, "Description one");
    messageSource.addMessage("QuestionCategory.Q1.2.label", locale, "Choice two");
    messageSource.addMessage("QuestionCategory.Q1.2.description", locale, "Description two");
    messageSource.addMessage("QuestionCategory.Q1.3.label", locale, "Choice three");
    messageSource.addMessage("QuestionCategory.Q1.3.description", locale, "Description three");
    messageSource.addMessage("QuestionCategory.Q1.4.label", locale, "Choice four");
    messageSource.addMessage("QuestionCategory.Q1.4.description", locale, "Description four");
    messageSource.addMessage("QuestionCategory.Q1.5.label", locale, "Choice five");
    messageSource.addMessage("QuestionCategory.Q1.5.description", locale, "Description five");
    messageSource.addMessage("QuestionCategory.Q1.6.label", locale, "Choice six");
    messageSource.addMessage("QuestionCategory.Q1.6.description", locale, "Description six");
    messageSource.addMessage("QuestionCategory.Q1.7.label", locale, "Choice seven");
    messageSource.addMessage("QuestionCategory.Q1.7.description", locale, "Description seven");
    messageSource.addMessage("QuestionCategory.Q1.8.label", locale, "Choice eight");
    messageSource.addMessage("QuestionCategory.Q1.8.description", locale, "Description eight");

    messageSource.addMessage("QuestionCategory.Q1.1.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.1.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.2.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.2.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.3.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.3.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.4.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.4.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.5.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.5.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.6.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.6.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.7.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.7.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.8.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.8.imageDeselected", locale, "");

    messageSource.addMessage("Question.Q1_MULTIPLE.label", locale, "question label");
    messageSource.addMessage("Question.Q1_MULTIPLE.help", locale, "question help");
    messageSource.addMessage("Question.Q1_MULTIPLE.specifications", locale, "question specifications");
    messageSource.addMessage("Question.Q1_MULTIPLE.instructions", locale, "question instructions");
    messageSource.addMessage("Question.Q1_MULTIPLE.caption", locale, "question caption");
    messageSource.addMessage("Question.Q1_MULTIPLE.categoryOrder", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.1.description", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.1.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.1.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.2.label", locale, "Choice two");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.2.description", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.2.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.2.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.3.label", locale, "Choice three");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.3.description", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.3.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.3.imageDeselected", locale, "");

    messageSource.addMessage("OpenAnswerDefinition.OPEN_3.label", locale, "Open three");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_3.unitLabel", locale, "Open three unit");

    messageSource.addMessage("Question.Q2.label", locale, "question2 label");
    messageSource.addMessage("Question.Q2.help", locale, "question2 help");
    messageSource.addMessage("Question.Q2.specifications", locale, "question2 specifications");
    messageSource.addMessage("Question.Q2.instructions", locale, "question2 instructions");
    messageSource.addMessage("Question.Q2.caption", locale, "question2 caption");
    messageSource.addMessage("Question.Q2.categoryOrder", locale, "");
    messageSource.addMessage("QuestionCategory.Q2.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q2.1.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q2.1.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q2.DONT_KNOW.label", locale, "Dont know");
    messageSource.addMessage("QuestionCategory.Q2.DONT_KNOW.description", locale, "");
    messageSource.addMessage("QuestionCategory.Q2.DONT_KNOW.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q2.DONT_KNOW.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.Q2.PREFER_NOT_ANSWER.label", locale, "Prefer not answer");
    messageSource.addMessage("QuestionCategory.Q2.PREFER_NOT_ANSWER.description", locale, "");
    messageSource.addMessage("QuestionCategory.Q2.PREFER_NOT_ANSWER.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.Q2.PREFER_NOT_ANSWER.imageDeselected", locale, "");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_INT.label", locale, "open label");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_INT.unitLabel", locale, "open unit label");

    messageSource.addMessage("Question.MULTIPLE_OPEN.label", locale, "question2 label");
    messageSource.addMessage("Question.MULTIPLE_OPEN.help", locale, "question2 help");
    messageSource.addMessage("Question.MULTIPLE_OPEN.specifications", locale, "question2 specifications");
    messageSource.addMessage("Question.MULTIPLE_OPEN.instructions", locale, "question2 instructions");
    messageSource.addMessage("Question.MULTIPLE_OPEN.caption", locale, "question2 caption");
    messageSource.addMessage("Question.MULTIPLE_OPEN.categoryOrder", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.DURATION.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.DURATION.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.DURATION.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.DONT_KNOW.label", locale, "Dont know");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.DONT_KNOW.description", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.DONT_KNOW.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.DONT_KNOW.imageDeselected", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.PREFER_NOT_ANSWER.label", locale, "Prefer not answer");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.PREFER_NOT_ANSWER.description", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.PREFER_NOT_ANSWER.imageSelected", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.PREFER_NOT_ANSWER.imageDeselected", locale, "");
    messageSource.addMessage("OpenAnswerDefinition.DURATION_OPEN_HOURS.label", locale, "open hours label");
    messageSource.addMessage("OpenAnswerDefinition.DURATION_OPEN_HOURS.unitLabel", locale, "open hours unit label");
    messageSource.addMessage("OpenAnswerDefinition.DURATION_OPEN_MINUTES.label", locale, "open minutes label");
    messageSource.addMessage("OpenAnswerDefinition.DURATION_OPEN_MINUTES.unitLabel", locale, "open minutes unit label");

    messageSource.addMessage("Question.MULTIPLE_MULTIPLE_OPEN.label", locale, "question2 label");
    messageSource.addMessage("Question.MULTIPLE_MULTIPLE_OPEN.help", locale, "question2 help");
    messageSource.addMessage("Question.MULTIPLE_MULTIPLE_OPEN.specifications", locale, "question2 specifications");
    messageSource.addMessage("Question.MULTIPLE_MULTIPLE_OPEN.instructions", locale, "question2 instructions");
    messageSource.addMessage("Question.MULTIPLE_MULTIPLE_OPEN.caption", locale, "question2 caption");
    messageSource.addMessage("Question.MULTIPLE_MULTIPLE_OPEN.categoryOrder", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_MULTIPLE_OPEN.MULTIPLE_DURATION.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.MULTIPLE_MULTIPLE_OPEN.DONT_KNOW.label", locale, "Dont know");
    messageSource.addMessage("QuestionCategory.MULTIPLE_MULTIPLE_OPEN.PREFER_NOT_ANSWER.label", locale, "Prefer not answer");
    messageSource.addMessage("OpenAnswerDefinition.MULTIPLE_DURATION_OPEN_HOURS.label", locale, "open hours label");
    messageSource.addMessage("OpenAnswerDefinition.MULTIPLE_DURATION_OPEN_HOURS.unitLabel", locale, "open hours unit label");
    messageSource.addMessage("OpenAnswerDefinition.MULTIPLE_DURATION_OPEN_MINUTES.label", locale, "open minutes label");
    messageSource.addMessage("OpenAnswerDefinition.MULTIPLE_DURATION_OPEN_MINUTES.unitLabel", locale, "open minutes unit label");

    messageSource.addMessage("Question.Q2_MULTIPLE.label", locale, "question2 label");
    messageSource.addMessage("Question.Q2_MULTIPLE.help", locale, "question2 help");
    messageSource.addMessage("Question.Q2_MULTIPLE.specifications", locale, "question2 specifications");
    messageSource.addMessage("Question.Q2_MULTIPLE.instructions", locale, "question2 instructions");
    messageSource.addMessage("Question.Q2_MULTIPLE.caption", locale, "question2 caption");
    messageSource.addMessage("Question.Q2_MULTIPLE.categoryOrder", locale, "");
    messageSource.addMessage("QuestionCategory.Q2_MULTIPLE.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q2_MULTIPLE.DONT_KNOW.label", locale, "Dont know");
    messageSource.addMessage("QuestionCategory.Q2_MULTIPLE.PREFER_NOT_ANSWER.label", locale, "Prefer not answer");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_TEXT.label", locale, "open label");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_TEXT.unitLabel", locale, "open unit label");

    messageSource.addMessage("Question.Q3.label", locale, "question3 label");
    messageSource.addMessage("Question.Q3.help", locale, "question3 help");
    messageSource.addMessage("Question.Q3.specifications", locale, "question3 specifications");
    messageSource.addMessage("Question.Q3.instructions", locale, "question3 instructions");
    messageSource.addMessage("Question.Q3.caption", locale, "question3 caption");
    messageSource.addMessage("Question.Q3.categoryOrder", locale, "");
    messageSource.addMessage("QuestionCategory.Q3.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q3.2.label", locale, "Choice two");
    messageSource.addMessage("QuestionCategory.Q3.3.label", locale, "Choice three");
    messageSource.addMessage("Question.Q3_1.label", locale, "question3-1 label");
    messageSource.addMessage("Question.Q3_2.label", locale, "question3-2 label");

    propertyKeyProvider = new DefaultPropertyKeyProviderImpl();

    MockSpringApplication application = new MockSpringApplication();
    application.setHomePage(Page.class);
    application.setApplicationContext(mockCtx);
    tester = new WicketTester(application);

    questionnaireParticipant = new QuestionnaireParticipant();
    questionnaireParticipant.setParticipant(new Participant());
  }

  @Test
  public void testExclusiveChoiceQuestion() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousCategoryAnswer = new CategoryAnswer();
    previousCategoryAnswer.setCategoryName("1");
    QuestionAnswer previousQuestionAnswer = new QuestionAnswer();
    previousQuestionAnswer.setQuestionName("Q1");
    previousQuestionAnswer.addCategoryAnswer(previousCategoryAnswer);

    final Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();

    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(3))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(4))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(5))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(6))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(7))).andReturn(null).atLeastOnce();
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question);
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question);
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, question.getQuestionCategories().get(1))).andReturn(new CategoryAnswer()).times(2);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, question.getQuestionCategories().get(2))).andReturn(new CategoryAnswer()).once();
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "description")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "description")).atLeastOnce();
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageSelected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageSelected")).anyTimes();
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageDeselected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageDeselected")).anyTimes();
    }

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new SimplifiedQuestionPanelMock(panelId, new Model(question));
      }
    });

    // dumpPage("testExclusiveChoiceQuestion");

    // check question message resources
    tester.assertLabel("panel:form:content:label", "question label");
    tester.assertLabel("panel:form:content:instructions", "question instructions");
    tester.assertLabel("panel:form:content:caption", "question caption");

    // check help toggle
    tester.isInvisible("panel:form:content:help");
    tester.executeAjaxEvent("panel:form:content:helpToggle:link", "onclick");
    tester.assertVisible("panel:form:content:help");

    Panel categories = (Panel) tester.getComponentFromLastRenderedPage("panel:form:content:content:openCategories");
    Assert.assertEquals(0, ComponentTesterUtils.findChildren(categories, SimplifiedOpenAnswerDefinitionPanel.class).size());

    categories = (Panel) tester.getComponentFromLastRenderedPage("panel:form:content:content:regularCategories");
    Assert.assertEquals(8, ComponentTesterUtils.findChildren(categories, QuestionCategoryLink.class).size());

    // check all expected categories are here
    checkQuestionCategoryLink("regularCategories", 1, 2, "Choice one", true);
    checkQuestionCategoryLink("regularCategories", 1, 3, "Choice six", false);
    checkQuestionCategoryLink("regularCategories", 4, 5, "Choice two", false);
    checkQuestionCategoryLink("regularCategories", 4, 6, "Choice seven", false);
    checkQuestionCategoryLink("regularCategories", 7, 8, "Choice three", false);
    checkQuestionCategoryLink("regularCategories", 7, 9, "Choice eight", false);
    checkQuestionCategoryLink("regularCategories", 10, 11, "Choice four", false);
    checkQuestionCategoryLink("regularCategories", 13, 14, "Choice five", false);

    categories = (Panel) tester.getComponentFromLastRenderedPage("panel:form:content:content:escapeCategories");
    Assert.assertEquals(0, ComponentTesterUtils.findChildren(categories, QuestionCategoryLink.class).size());

    // select category
    clickQuestionCategoryLink("regularCategories", 7, 8);
    clickQuestionCategoryLink("regularCategories", 4, 5);
    clickQuestionCategoryLink("regularCategories", 4, 5);

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  @Test
  public void testExclusiveChoiceQuestionWithOpenAnswer() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousCategoryAnswer = new CategoryAnswer();
    previousCategoryAnswer.setCategoryName("DONT_KNOW");
    QuestionAnswer previousQuestionAnswer = new QuestionAnswer();
    previousQuestionAnswer.setQuestionName("Q2");
    previousQuestionAnswer.addCategoryAnswer(previousCategoryAnswer);

    final Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q2");
    OpenAnswerDefinition open = QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("OPEN_INT");

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), open)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(questionnaire, "clickHere")).andReturn(propertyKeyProvider.getPropertyKey(questionnaire, "clickHere")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageSelected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageSelected")).anyTimes();
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageDeselected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageDeselected")).anyTimes();
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
      if(qCategory.getOpenAnswerDefinition() == null) {
        expect(questionnaireBundleMock.getPropertyKey(qCategory, "description")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "description")).atLeastOnce();
      } else {
        expect(questionnaireBundleMock.getPropertyKey(qCategory.getOpenAnswerDefinition(), "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(qCategory.getOpenAnswerDefinition(), "unitLabel")).atLeastOnce();
      }
    }

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new SimplifiedQuestionPanelMock(panelId, new Model(question));
      }
    });

    // tester.dumpPage();

    Panel categories = (Panel) tester.getComponentFromLastRenderedPage("panel:form:content:content:openCategories");
    Assert.assertEquals(1, ComponentTesterUtils.findChildren(categories, SimplifiedOpenAnswerDefinitionPanel.class).size());

    categories = (Panel) tester.getComponentFromLastRenderedPage("panel:form:content:content:escapeCategories");
    Assert.assertEquals(2, ComponentTesterUtils.findChildren(categories, QuestionCategoryLink.class).size());

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  @Test
  public void testExclusiveChoiceQuestionWithMultipleOpenAnswer() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousCategoryAnswer = new CategoryAnswer();
    previousCategoryAnswer.setCategoryName("DONT_KNOW");
    QuestionAnswer previousQuestionAnswer = new QuestionAnswer();
    previousQuestionAnswer.setQuestionName("MULTIPLE_OPEN");
    previousQuestionAnswer.addCategoryAnswer(previousCategoryAnswer);

    final Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("MULTIPLE_OPEN");
    OpenAnswerDefinition openHours = QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("DURATION_OPEN_HOURS");
    OpenAnswerDefinition openMinutes = QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("DURATION_OPEN_MINUTES");

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openHours)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openMinutes)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(questionnaire, "clickHere")).andReturn(propertyKeyProvider.getPropertyKey(questionnaire, "clickHere")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageSelected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageSelected")).anyTimes();
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageDeselected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageDeselected")).anyTimes();
      if(qCategory.getOpenAnswerDefinition() == null) {
        expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
        expect(questionnaireBundleMock.getPropertyKey(qCategory, "description")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "description")).atLeastOnce();
      } else {
        for(OpenAnswerDefinition open : qCategory.getOpenAnswerDefinition().getOpenAnswerDefinitions()) {
          expect(questionnaireBundleMock.getPropertyKey(open, "label")).andReturn(propertyKeyProvider.getPropertyKey(open, "label")).atLeastOnce();
          expect(questionnaireBundleMock.getPropertyKey(open, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(open, "unitLabel")).atLeastOnce();
        }
      }
    }

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new SimplifiedQuestionPanelMock(panelId, new Model(question));
      }
    });

    // dumpPage("testExclusiveChoiceQuestionWithMultipleOpenAnswer");

    Panel categories = (Panel) tester.getComponentFromLastRenderedPage("panel:form:content:content:openCategories");
    Assert.assertEquals(1, ComponentTesterUtils.findChildren(categories, MultipleSimplifiedOpenAnswerDefinitionPanel.class).size());
    Assert.assertEquals(2, ComponentTesterUtils.findChildren(categories, SimplifiedOpenAnswerDefinitionPanel.class).size());

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMultipleChoiceQuestion() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousCategoryAnswer1 = new CategoryAnswer();
    previousCategoryAnswer1.setCategoryName("1");
    QuestionAnswer previousQuestionAnswer = new QuestionAnswer();
    previousQuestionAnswer.setQuestionName("Q1_MULTIPLE");
    previousQuestionAnswer.addCategoryAnswer(previousCategoryAnswer1);
    CategoryAnswer previousCategoryAnswer3 = new CategoryAnswer();
    previousCategoryAnswer3.setCategoryName("3");
    previousQuestionAnswer.addCategoryAnswer(previousCategoryAnswer3);

    final Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1_MULTIPLE");

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(previousCategoryAnswer1).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(previousCategoryAnswer3).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswers(question)).andReturn(previousQuestionAnswer.getCategoryAnswers()).times(2);
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    activeQuestionnaireAdministrationServiceMock.deleteAnswer(question, question.getQuestionCategories().get(1));
    activeQuestionnaireAdministrationServiceMock.deleteAnswer(question, question.getQuestionCategories().get(2));
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, question.getQuestionCategories().get(1))).andReturn(new CategoryAnswer()).once();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageSelected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageSelected")).anyTimes();
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageDeselected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageDeselected")).anyTimes();
      if(qCategory.getOpenAnswerDefinition() == null) {
        expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
        expect(questionnaireBundleMock.getPropertyKey(qCategory, "description")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "description")).atLeastOnce();
      }
    }

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new SimplifiedQuestionPanelMock(panelId, new Model(question));
      }
    });

    // dumpPage("testMultipleChoiceQuestion");

    // check all expected categories are here
    checkQuestionCategoryLink("regularCategories", 1, 2, "Choice one", true);
    checkQuestionCategoryLink("regularCategories", 3, 4, "Choice two", false);
    checkQuestionCategoryLink("regularCategories", 5, 6, "Choice three", true);

    // click on categories
    clickQuestionCategoryLink("regularCategories", 3, 4);
    clickQuestionCategoryLink("regularCategories", 5, 6);

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  @Test
  public void testSharedCategoriesArrayQuestion() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousCategoryAnswer = new CategoryAnswer();
    previousCategoryAnswer.setCategoryName("1");
    QuestionAnswer previousQuestionAnswer = new QuestionAnswer();
    previousQuestionAnswer.setQuestionName("Q3_1");
    previousQuestionAnswer.addCategoryAnswer(previousCategoryAnswer);

    final Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q3");
    Question question1 = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q3_1");
    Question question2 = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q3_2");

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question1, question.getQuestionCategories().get(0))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question1, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question1, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();

    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question2, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question2, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question2, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();

    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question1);
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question1);
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question2);
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question2);

    expect(activeQuestionnaireAdministrationServiceMock.answer(question1, question.getQuestionCategories().get(2))).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.answer(question2, question.getQuestionCategories().get(0))).andReturn(new CategoryAnswer());

    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(questionnaire, "clearAll")).andReturn(propertyKeyProvider.getPropertyKey(questionnaire, "clearAll")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).anyTimes();
      if(qCategory.getCategory().getOpenAnswerDefinition() != null) {
        expect(questionnaireBundleMock.getPropertyKey(qCategory.getCategory().getOpenAnswerDefinition(), "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory.getCategory().getOpenAnswerDefinition(), "label")).anyTimes();
        expect(questionnaireBundleMock.getPropertyKey(qCategory.getCategory().getOpenAnswerDefinition(), "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(qCategory.getCategory().getOpenAnswerDefinition(), "unitLabel")).anyTimes();
      }
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageSelected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageSelected")).anyTimes();
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "imageDeselected")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "imageDeselected")).anyTimes();
    }
    for(Question q : question.getQuestions()) {
      expect(questionnaireBundleMock.getPropertyKey(q, "label")).andReturn(propertyKeyProvider.getPropertyKey(q, "label")).anyTimes();
    }

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new SimplifiedQuestionPanelMock(panelId, new Model(question));
      }
    });

    dumpPage("testSharedCategoriesArrayQuestion");

    // tester.getComponentFromLastRenderedPage("panel:form:content:content:array:headers:2:header:label");
    tester.assertLabel("panel:form:content:content:array:headers:2:header:label", "Choice one");
    tester.assertLabel("panel:form:content:content:array:headers:3:header:label", "Choice two");
    tester.assertLabel("panel:form:content:content:array:headers:4:header:label", "Choice three");

    // check row labels as to be child question labels
    tester.assertLabel("panel:form:content:content:array:rows:rows:1:cells:1:cell", "question3-1 label");
    tester.assertLabel("panel:form:content:content:array:rows:rows:2:cells:1:cell", "question3-2 label");

    tester.assertComponent("panel:form:content:content:clearAll", AjaxImageLink.class);

    RepeatingView line = (RepeatingView) tester.getComponentFromLastRenderedPage("panel:form:content:content:array:rows:rows:1:cells");
    Assert.assertEquals(3, ComponentTesterUtils.findChildren(line, QuestionCategoryLink.class).size());
    checkArrayQuestionCategoryLink(1, 2, true);
    checkArrayQuestionCategoryLink(1, 3, false);
    checkArrayQuestionCategoryLink(1, 4, false);

    line = (RepeatingView) tester.getComponentFromLastRenderedPage("panel:form:content:content:array:rows:rows:2:cells");
    Assert.assertEquals(3, ComponentTesterUtils.findChildren(line, QuestionCategoryLink.class).size());
    checkArrayQuestionCategoryLink(2, 2, false);
    checkArrayQuestionCategoryLink(2, 3, false);
    checkArrayQuestionCategoryLink(2, 4, false);

    // click
    clickArrayQuestionCategoryLink(1, 4);
    clickArrayQuestionCategoryLink(2, 2);

    // clear
    tester.executeAjaxEvent("panel:form:content:content:clearAll:link", "onclick");

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  private void dumpPage(String testName) {
    tester.dumpPage();
    File dump = new File("target/" + getClass().getSimpleName() + "-" + testName + ".html");
    try {
      if(!dump.exists()) dump.createNewFile();
      OutputStream out = new FileOutputStream(dump);
      out.write(tester.getServletResponse().getDocument().getBytes());
      out.flush();
      out.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void clickQuestionCategoryLink(String categoryType, int row, int col) {
    String category = "panel:form:content:content:" + categoryType + ":category";

    Component link = tester.getComponentFromLastRenderedPage(category + ":" + row + ":cols:" + col + ":input:link:link");
    tester.executeAjaxEvent(link, "onclick");
  }

  private void checkQuestionCategoryLink(String categoryType, int row, int col, String label, boolean selected) {
    String category = "panel:form:content:content:" + categoryType + ":category";

    tester.assertComponent(category + ":" + row + ":cols:" + col + ":input", QuestionCategoryLink.class);
    // tester.assertComponent(category + ":" + row + ":cols:" + col + ":input",
    // IQuestionCategorySelectionStateHolder.class);
    IQuestionCategorySelectionStateHolder stateHolder = (IQuestionCategorySelectionStateHolder) tester.getComponentFromLastRenderedPage(category + ":" + row + ":cols:" + col + ":input");
    Assert.assertEquals(selected, stateHolder.wasSelected());

    tester.assertComponent(category + ":" + row + ":cols:" + col + ":input:link", AjaxImageLink.class);
    Component link = tester.getComponentFromLastRenderedPage(category + ":" + row + ":cols:" + col + ":input:link");
    Assert.assertEquals("obiba-quartz-category", link.getMarkupAttributes().getString("class"));
    tester.assertLabel(category + ":" + row + ":cols:" + col + ":input:link:link:label", label);
  }

  private void clickArrayQuestionCategoryLink(int row, int col) {
    String rows = "panel:form:content:content:array:rows:rows";

    Component link = tester.getComponentFromLastRenderedPage(rows + ":" + row + ":cells:" + col + ":cell:link:link");
    tester.executeAjaxEvent(link, "onclick");
  }

  private void checkArrayQuestionCategoryLink(int row, int col, boolean selected) {
    String rows = "panel:form:content:content:array:rows:rows";

    tester.assertComponent(rows + ":" + row + ":cells:" + col + ":cell", QuestionCategoryLink.class);
    // tester.assertComponent(rows + ":" + row + ":cells:" + col + ":cell",
    // IQuestionCategorySelectionStateHolder.class);
    IQuestionCategorySelectionStateHolder stateHolder = (IQuestionCategorySelectionStateHolder) tester.getComponentFromLastRenderedPage(rows + ":" + row + ":cells:" + col + ":cell");
    Assert.assertEquals(selected, stateHolder.wasSelected());

    tester.assertComponent(rows + ":" + row + ":cells:" + col + ":cell:link", AjaxImageLink.class);
    Component link = tester.getComponentFromLastRenderedPage(rows + ":" + row + ":cells:" + col + ":cell:link");
    Assert.assertEquals("obiba-quartz-category", link.getMarkupAttributes().getString("class"));

  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("S1").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3", "4", "5", "6", "7", "8");
    builder.inPage("P1").withQuestion("Q1_MULTIPLE", "1", true).withCategories("1", "2", "3");
    builder.withSection("S2").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withSharedCategory("DONT_KNOW").setEscape(true);
    builder.inQuestion("Q2").withSharedCategory("PREFER_NOT_ANSWER").setEscape(true);
    builder.inPage("P2").withQuestion("Q2_MULTIPLE", "2", true).withCategory("1").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2_MULTIPLE").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    builder.withSection("S3").withPage("P3").withQuestion("Q3").withCategories("1", "2", "3");
    builder.inQuestion("Q3").withQuestion("Q3_1");
    builder.inQuestion("Q3").withQuestion("Q3_2");

    builder.withSection("S_MULTIPLE_OPEN").withPage("P_MULTIPLE_OPEN").withQuestion("MULTIPLE_OPEN").withSharedCategory("DURATION").withOpenAnswerDefinition("DURATION_OPEN", DataType.INTEGER);
    builder.inOpenAnswerDefinition("DURATION_OPEN").withOpenAnswerDefinition("DURATION_OPEN_HOURS", DataType.INTEGER).addValidator(new RangeValidator(0, 16));
    builder.inOpenAnswerDefinition("DURATION_OPEN").withOpenAnswerDefinition("DURATION_OPEN_MINUTES", DataType.INTEGER).addValidator(new RangeValidator(0, 960));
    builder.inQuestion("MULTIPLE_OPEN").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    builder.inPage("P_MULTIPLE_OPEN").withQuestion("MULTIPLE_MULTIPLE_OPEN", "3", true).withSharedCategory("MULTIPLE_DURATION").withOpenAnswerDefinition("MULTIPLE_DURATION_OPEN", DataType.INTEGER);
    builder.inOpenAnswerDefinition("MULTIPLE_DURATION_OPEN").withOpenAnswerDefinition("MULTIPLE_DURATION_OPEN_HOURS", DataType.INTEGER).addValidator(new RangeValidator(0, 16));
    builder.inOpenAnswerDefinition("MULTIPLE_DURATION_OPEN").withOpenAnswerDefinition("MULTIPLE_DURATION_OPEN_MINUTES", DataType.INTEGER).addValidator(new RangeValidator(0, 960));
    builder.inQuestion("MULTIPLE_MULTIPLE_OPEN").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(locale);

    return q;
  }

}
