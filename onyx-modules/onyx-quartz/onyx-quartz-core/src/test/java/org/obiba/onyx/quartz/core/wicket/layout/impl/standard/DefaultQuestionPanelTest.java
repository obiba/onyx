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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Assert;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
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
import org.obiba.onyx.magma.MagmaInstanceProvider;
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
import org.obiba.onyx.quartz.core.wicket.layout.PageLayoutFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.quartz.test.ComponentTesterUtils;
import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.wicket.test.MockSpringApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.StaticMessageSource;

public class DefaultQuestionPanelTest {

  static final Logger log = LoggerFactory.getLogger(DefaultQuestionPanelTest.class);

  private WicketTester tester;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private MagmaInstanceProvider magmaInstanceProviderMock;

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
    magmaInstanceProviderMock = createMock(MagmaInstanceProvider.class);
    // messageSourceMock = createMock(MessageSource.class);

    mockCtx.putBean("activeInterviewService", activeInterviewServiceMock);
    mockCtx.putBean("activeQuestionnaireAdministrationService", activeQuestionnaireAdministrationServiceMock);
    mockCtx.putBean("questionnaireBundleManager", questionnaireBundleManagerMock);
    mockCtx.putBean("queryService", queryServiceMock);
    mockCtx.putBean("magmaInstanceProviderMock", magmaInstanceProviderMock);
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
    messageSource.addMessage("Question.Q1.label", locale, "question label");
    messageSource.addMessage("Question.Q1.help", locale, "question help");
    messageSource.addMessage("Question.Q1.specifications", locale, "question specifications");
    messageSource.addMessage("Question.Q1.instructions", locale, "question instructions");
    messageSource.addMessage("Question.Q1.caption", locale, "question caption");
    messageSource.addMessage("Question.Q1.categoryOrder", locale, "");
    messageSource.addMessage("Question.Q1.media", locale, "");
    messageSource.addMessage("QuestionCategory.Q1.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q1.2.label", locale, "Choice two");
    messageSource.addMessage("QuestionCategory.Q1.3.label", locale, "Choice three");
    messageSource.addMessage("QuestionCategory.Q1.4.label", locale, "Choice four");
    messageSource.addMessage("QuestionCategory.Q1.5.label", locale, "Choice five");
    messageSource.addMessage("QuestionCategory.Q1.6.label", locale, "Choice six");
    messageSource.addMessage("QuestionCategory.Q1.7.label", locale, "Choice seven");
    messageSource.addMessage("QuestionCategory.Q1.8.label", locale, "Choice height");

    messageSource.addMessage("Question.Q1_MULTIPLE.label", locale, "question label");
    messageSource.addMessage("Question.Q1_MULTIPLE.help", locale, "question help");
    messageSource.addMessage("Question.Q1_MULTIPLE.specifications", locale, "question specifications");
    messageSource.addMessage("Question.Q1_MULTIPLE.instructions", locale, "question instructions");
    messageSource.addMessage("Question.Q1_MULTIPLE.caption", locale, "question caption");
    messageSource.addMessage("Question.Q1_MULTIPLE.categoryOrder", locale, "");
    messageSource.addMessage("Question.Q1_MULTIPLE.media", locale, "");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.2.label", locale, "Choice two");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.3.label", locale, "Choice three");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_3.label", locale, "Open three");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_3.unitLabel", locale, "Open three unit");

    messageSource.addMessage("Question.Q2.label", locale, "question2 label");
    messageSource.addMessage("Question.Q2.help", locale, "question2 help");
    messageSource.addMessage("Question.Q2.specifications", locale, "question2 specifications");
    messageSource.addMessage("Question.Q2.instructions", locale, "question2 instructions");
    messageSource.addMessage("Question.Q2.caption", locale, "question2 caption");
    messageSource.addMessage("Question.Q2.categoryOrder", locale, "");
    messageSource.addMessage("Question.Q2.media", locale, "");
    messageSource.addMessage("QuestionCategory.Q2.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q2.DONT_KNOW.label", locale, "Dont know");
    messageSource.addMessage("QuestionCategory.Q2.PREFER_NOT_ANSWER.label", locale, "Prefer not answer");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_INT.label", locale, "open label");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_INT.unitLabel", locale, "open unit label");

    messageSource.addMessage("Question.MULTIPLE_OPEN.label", locale, "question2 label");
    messageSource.addMessage("Question.MULTIPLE_OPEN.help", locale, "question2 help");
    messageSource.addMessage("Question.MULTIPLE_OPEN.specifications", locale, "question2 specifications");
    messageSource.addMessage("Question.MULTIPLE_OPEN.instructions", locale, "question2 instructions");
    messageSource.addMessage("Question.MULTIPLE_OPEN.caption", locale, "question2 caption");
    messageSource.addMessage("Question.MULTIPLE_OPEN.categoryOrder", locale, "");
    messageSource.addMessage("Question.MULTIPLE_OPEN.media", locale, "");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.DURATION.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.DONT_KNOW.label", locale, "Dont know");
    messageSource.addMessage("QuestionCategory.MULTIPLE_OPEN.PREFER_NOT_ANSWER.label", locale, "Prefer not answer");
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
    messageSource.addMessage("Question.MULTIPLE_MULTIPLE_OPEN.media", locale, "");
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
    messageSource.addMessage("Question.Q2_MULTIPLE.media", locale, "");
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
    messageSource.addMessage("Question.Q3.media", locale, "");
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
    QuestionCategory questionCategory = question.getQuestionCategories().get(1);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, questionCategory)).andReturn(new CategoryAnswer());
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
    expect(questionnaireBundleMock.getPropertyKey(question, "media")).andReturn(propertyKeyProvider.getPropertyKey(question, "media")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
    }

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {

        return new DefaultQuestionPanelMock(panelId, new Model(question));
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

    // check all expected radios are here
    tester.assertComponent("panel:form:content:content:categories", RadioGroup.class);

    RadioGroup radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    // assert radio count
    Assert.assertEquals(8, ComponentTesterUtils.findChildren(radioGroup, Radio.class).size());
    // assert one of them is selected
    Assert.assertNotNull(ComponentTesterUtils.findChild(radioGroup, Radio.class, radioGroup.getModel()));
    // check previous answer is here (radio 1)
    Assert.assertEquals(radioGroup.getModel(), ComponentTesterUtils.findChildren(radioGroup, Radio.class).get(0).getDefaultModel());

    // select radio 2 (the third in markup order because of the two columns)
    tester.executeAjaxEvent(ComponentTesterUtils.findChildren(radioGroup, Radio.class).get(2), "onchange");
    radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    Radio radio2 = (Radio) ComponentTesterUtils.findChildren(radioGroup, Radio.class).get(2);
    Assert.assertEquals(radioGroup.getModelObject(), radio2.getModelObject());

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
    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), open)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findActiveAnswers(question)).andReturn(Arrays.asList(new CategoryAnswer[] { previousCategoryAnswer }));
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(null);
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(previousCategoryAnswer);
    QuestionCategory questionCategory = question.getQuestionCategories().get(0);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, questionCategory, questionCategory.getCategory().getOpenAnswerDefinition(), DataBuilder.buildInteger(1l))).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "media")).andReturn(propertyKeyProvider.getPropertyKey(question, "media")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
    }
    expect(questionnaireBundleMock.getPropertyKey(open, "label")).andReturn(propertyKeyProvider.getPropertyKey(open, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(open, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(open, "unitLabel")).atLeastOnce();

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {

        return new DefaultQuestionPanelMock(panelId, new Model(question));
      }
    });

    // tester.dumpPage();

    RadioGroup radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    // assert radio count
    Assert.assertEquals(3, ComponentTesterUtils.findChildren(radioGroup, Radio.class).size());
    // assert one of them is selected
    Assert.assertNotNull(ComponentTesterUtils.findChild(radioGroup, Radio.class, radioGroup.getModel()));
    // check previous answer is here (radio 2)
    Radio radio2 = (Radio) ComponentTesterUtils.findChildren(radioGroup, Radio.class).get(1);
    Assert.assertEquals(radioGroup.getModel(), radio2.getModel());

    FormComponent field = (FormComponent) ComponentTesterUtils.findChildren(radioGroup, TextField.class).get(0);

    // select open field
    FormTester formTester = tester.newFormTester("panel:form");
    formTester.setValue("content:content:categories:category:1:cols:2:input:open:open:input:field", "1");
    tester.executeAjaxEvent(field, "onchange");
    radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    Radio radio1 = (Radio) ComponentTesterUtils.findChildren(radioGroup, Radio.class).get(0);
    Assert.assertEquals(radioGroup.getModelObject(), radio1.getModelObject());

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
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openHours)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openMinutes)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findActiveAnswers(question)).andReturn(Arrays.asList(new CategoryAnswer[] { previousCategoryAnswer }));
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(previousCategoryAnswer);
    QuestionCategory questionCategory = question.getQuestionCategories().get(0);
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, questionCategory, questionCategory.getCategory().getOpenAnswerDefinition().getOpenAnswerDefinitions().get(0), DataBuilder.buildInteger(1l))).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "media")).andReturn(propertyKeyProvider.getPropertyKey(question, "media")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
    }
    expect(questionnaireBundleMock.getPropertyKey(openHours, "label")).andReturn(propertyKeyProvider.getPropertyKey(openHours, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(openHours, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(openHours, "unitLabel")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(openMinutes, "label")).andReturn(propertyKeyProvider.getPropertyKey(openMinutes, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(openMinutes, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(openMinutes, "unitLabel")).atLeastOnce();

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new DefaultQuestionPanelMock(panelId, new Model(question));
      }
    });

    // dumpPage("testExclusiveChoiceQuestionWithMultipleOpenAnswer");

    RadioGroup radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    // assert radio count
    Assert.assertEquals(3, ComponentTesterUtils.findChildren(radioGroup, Radio.class).size());
    // assert one of them is selected
    Assert.assertNotNull(ComponentTesterUtils.findChild(radioGroup, Radio.class, radioGroup.getModel()));
    // check previous answer is here (radio 2)
    Radio radio2 = (Radio) ComponentTesterUtils.findChildren(radioGroup, Radio.class).get(1);
    Assert.assertEquals(radioGroup.getModel(), radio2.getModel());

    FormComponent field1 = (FormComponent) ComponentTesterUtils.findChildren(radioGroup, TextField.class).get(0);

    // select open field
    FormTester formTester = tester.newFormTester("panel:form");
    formTester.setValue("content:content:categories:category:1:cols:2:input:open:repeating:1:open:open:input:field", "1");
    tester.executeAjaxEvent(field1, "onchange");
    radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    Radio radio1 = (Radio) ComponentTesterUtils.findChildren(radioGroup, Radio.class).get(0);
    Assert.assertEquals(radioGroup.getModelObject(), radio1.getModelObject());

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMultipleChoiceQuestion() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousCategoryAnswer = new CategoryAnswer();
    previousCategoryAnswer.setCategoryName("1");
    QuestionAnswer previousQuestionAnswer = new QuestionAnswer();
    previousQuestionAnswer.setQuestionName("Q1_MULTIPLE");
    previousQuestionAnswer.addCategoryAnswer(previousCategoryAnswer);

    final Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1_MULTIPLE");

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    activeQuestionnaireAdministrationServiceMock.deleteAnswer(question, question.getQuestionCategories().get(1));
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, question.getQuestionCategories().get(1), question.getQuestionCategories().get(1).getCategory().getOpenAnswerDefinition(), null)).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "media")).andReturn(propertyKeyProvider.getPropertyKey(question, "media")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
    }

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new DefaultQuestionPanelMock(panelId, new Model(question));
      }
    });

    // dumpPage("testMultipleChoiceQuestion");

    // check question message resources
    tester.assertLabel("panel:form:content:label", "question label");
    tester.assertLabel("panel:form:content:instructions", "question instructions");
    tester.assertLabel("panel:form:content:caption", "question caption");

    // check help toggle
    tester.isInvisible("panel:form:content:help");
    tester.executeAjaxEvent("panel:form:content:helpToggle:link", "onclick");
    tester.assertVisible("panel:form:content:help");

    // check previous answer is here (checkbox 1)
    CheckGroup checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    // assert radio count
    Assert.assertEquals(3, ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).size());
    // assert first of them is selected
    CheckBox checkbox1 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(0);
    Collection<IModel> selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(1, selections.size());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox1.getModel()).getQuestionCategory(), selections.iterator().next().getObject());

    // select checkbox 2
    tester.executeAjaxEvent(ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(1), "onchange");
    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    checkbox1 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(0);
    CheckBox checkbox2 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(1);
    selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(2, selections.size());
    Iterator<IModel> iterator = selections.iterator();
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox1.getModel()).getQuestionCategory(), iterator.next().getObject());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox2.getModel()).getQuestionCategory(), iterator.next().getObject());

    // unselect checkbox 2
    tester.executeAjaxEvent(checkbox2, "onchange");
    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    checkbox1 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(0);
    checkbox2 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(1);
    selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(1, selections.size());
    iterator = selections.iterator();
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox1.getModel()).getQuestionCategory(), iterator.next().getObject());

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMultipleChoiceQuestionWithOpenAnswer() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousCategoryAnswer = new CategoryAnswer();
    previousCategoryAnswer.setCategoryName("DONT_KNOW");
    QuestionAnswer previousQuestionAnswer = new QuestionAnswer();
    previousQuestionAnswer.setQuestionName("Q2_MULTIPLE");
    previousQuestionAnswer.addCategoryAnswer(previousCategoryAnswer);

    final Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q2_MULTIPLE");
    OpenAnswerDefinition open = QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("OPEN_TEXT");

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), open)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    // activeQuestionnaireAdministrationServiceMock.deleteAnswers(question);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, question.getQuestionCategories().get(0), question.getQuestionCategories().get(0).getCategory().getOpenAnswerDefinition(), DataBuilder.buildText("1"))).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "media")).andReturn(propertyKeyProvider.getPropertyKey(question, "media")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
    }
    expect(questionnaireBundleMock.getPropertyKey(open, "label")).andReturn(propertyKeyProvider.getPropertyKey(open, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(open, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(open, "unitLabel")).atLeastOnce();

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new DefaultQuestionPanelMock(panelId, new Model(question));
      }
    });

    // dumpPage("testMultipleChoiceQuestionWithOpenAnswer");

    // check previous answer is here (checkbox 1)
    CheckGroup checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    // assert radio count
    Assert.assertEquals(3, ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).size());
    // assert first of them is selected
    CheckBox checkbox2 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(1);
    Collection<IModel> selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(1, selections.size());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox2.getModel()).getQuestionCategory(), selections.iterator().next().getObject());
    FormComponent field = (FormComponent) ComponentTesterUtils.findChildren(checkGroup, TextField.class).get(0);

    // select open field
    FormTester formTester = tester.newFormTester("panel:form");
    formTester.setValue("content:content:categories:category:1:cols:2:input:open:open:input:field", "1");
    tester.executeAjaxEvent(field, "onchange");
    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    field = (FormComponent) ComponentTesterUtils.findChildren(checkGroup, TextField.class).get(0);

    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    CheckBox checkbox1 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(0);
    checkbox2 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(1);
    selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(2, selections.size());
    Iterator<IModel> iterator = selections.iterator();
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox2.getModel()).getQuestionCategory(), iterator.next().getObject());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox1.getModel()).getQuestionCategory(), iterator.next().getObject());

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMultipleChoiceQuestionWithMultipleOpenAnswer() {

    final Questionnaire questionnaire = createQuestionnaire();
    CategoryAnswer previousCategoryAnswer = new CategoryAnswer();
    previousCategoryAnswer.setCategoryName("DONT_KNOW");
    QuestionAnswer previousQuestionAnswer = new QuestionAnswer();
    previousQuestionAnswer.setQuestionName("MULTIPLE_MULTIPLE_OPEN");
    previousQuestionAnswer.addCategoryAnswer(previousCategoryAnswer);

    final Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("MULTIPLE_MULTIPLE_OPEN");
    OpenAnswerDefinition openHours = QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("MULTIPLE_DURATION_OPEN_HOURS");
    OpenAnswerDefinition openMinutes = QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("MULTIPLE_DURATION_OPEN_MINUTES");

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaire")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.isQuestionnaireDevelopmentMode()).andReturn(false).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn("").atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openHours)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openMinutes)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    QuestionCategory questionCategory = question.getQuestionCategories().get(0);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, questionCategory, questionCategory.getCategory().getOpenAnswerDefinition().getOpenAnswerDefinitions().get(0), DataBuilder.buildInteger(1l))).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "media")).andReturn(propertyKeyProvider.getPropertyKey(question, "media")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
    }
    expect(questionnaireBundleMock.getPropertyKey(openHours, "label")).andReturn(propertyKeyProvider.getPropertyKey(openHours, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(openHours, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(openHours, "unitLabel")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(openMinutes, "label")).andReturn(propertyKeyProvider.getPropertyKey(openMinutes, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(openMinutes, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(openMinutes, "unitLabel")).atLeastOnce();

    replay(activeInterviewServiceMock);
    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new DefaultQuestionPanelMock(panelId, new Model(question));
      }
    });

    // dumpPage("testMultipleChoiceQuestionWithMultipleOpenAnswer");

    // check previous answer is here (checkbox 2)
    CheckGroup checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    // assert radio count
    Assert.assertEquals(3, ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).size());
    // assert first of them is selected
    CheckBox checkbox2 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(1);
    Collection<IModel> selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(1, selections.size());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox2.getModel()).getQuestionCategory(), selections.iterator().next().getObject());
    FormComponent field1 = (FormComponent) ComponentTesterUtils.findChildren(checkGroup, TextField.class).get(0);

    // select open field
    FormTester formTester = tester.newFormTester("panel:form");
    formTester.setValue("content:content:categories:category:1:cols:2:input:open:repeating:1:open:open:input:field", "1");
    tester.executeAjaxEvent(field1, "onchange");
    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    CheckBox checkbox1 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(0);
    checkbox2 = (CheckBox) ComponentTesterUtils.findChildren(checkGroup, CheckBox.class).get(1);
    selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(2, selections.size());
    Iterator<IModel> iterator = selections.iterator();
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox2.getModel()).getQuestionCategory(), iterator.next().getObject());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox1.getModel()).getQuestionCategory(), iterator.next().getObject());

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
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findActiveAnswers(question1)).andReturn(Arrays.asList(new CategoryAnswer[] { previousCategoryAnswer }));
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question1, question.getQuestionCategories().get(0))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question1, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question1, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question1, question.getCategories().get(2), question.getCategories().get(2).getOpenAnswerDefinition())).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question2, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question2, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question2, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question2, question.getCategories().get(2), question.getCategories().get(2).getOpenAnswerDefinition())).andReturn(null).atLeastOnce();

    activeQuestionnaireAdministrationServiceMock.deleteAnswers(previousCategoryAnswer);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question1, question.getQuestionCategories().get(2), question.getCategories().get(2).getOpenAnswerDefinition(), null)).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "categoryOrder")).andReturn(propertyKeyProvider.getPropertyKey(question, "categoryOrder")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "media")).andReturn(propertyKeyProvider.getPropertyKey(question, "media")).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).anyTimes();
      if(qCategory.getCategory().getOpenAnswerDefinition() != null) {
        expect(questionnaireBundleMock.getPropertyKey(qCategory.getCategory().getOpenAnswerDefinition(), "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory.getCategory().getOpenAnswerDefinition(), "label")).anyTimes();
        expect(questionnaireBundleMock.getPropertyKey(qCategory.getCategory().getOpenAnswerDefinition(), "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(qCategory.getCategory().getOpenAnswerDefinition(), "unitLabel")).anyTimes();
      }
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

        return new DefaultQuestionPanelMock(panelId, new Model(question));
      }
    });

    // dumpPage("testSharedCategoriesArrayQuestion");

    // tester.getComponentFromLastRenderedPage("panel:form:content:content:array:headers:2:header:label");
    tester.assertLabel("panel:form:content:content:array:headers:2:header:label", "Choice one");
    tester.assertLabel("panel:form:content:content:array:headers:3:header:label", "Choice two");
    tester.assertLabel("panel:form:content:content:array:headers:4:header:label", "Choice three");

    // check row labels as to be child question labels
    tester.assertLabel("panel:form:content:content:array:rows:rows:1:group:cells:1:cell", "question3-1 label");
    tester.assertLabel("panel:form:content:content:array:rows:rows:2:group:cells:1:cell", "question3-2 label");

    // check all expected radios are here
    tester.assertComponent("panel:form:content:content:array:rows:rows:1:group", RadioGroup.class);
    tester.assertComponent("panel:form:content:content:array:rows:rows:1:group:cells:2:cell:categoryLabel:radio", Radio.class);
    tester.assertComponent("panel:form:content:content:array:rows:rows:1:group:cells:3:cell:categoryLabel:radio", Radio.class);
    tester.assertComponent("panel:form:content:content:array:rows:rows:1:group:cells:4:cell:categoryLabel:radio", Radio.class);
    tester.assertComponent("panel:form:content:content:array:rows:rows:2:group", RadioGroup.class);
    tester.assertComponent("panel:form:content:content:array:rows:rows:2:group:cells:2:cell:categoryLabel:radio", Radio.class);
    tester.assertComponent("panel:form:content:content:array:rows:rows:2:group:cells:3:cell:categoryLabel:radio", Radio.class);
    tester.assertComponent("panel:form:content:content:array:rows:rows:2:group:cells:4:cell:categoryLabel:radio", Radio.class);

    tester.assertLabel("panel:form:content:content:array:rows:rows:1:group:cells:4:cell:open:label", "Open three");
    tester.assertComponent("panel:form:content:content:array:rows:rows:1:group:cells:4:cell:open:open", DataField.class);

    // check previous answer is here (radio 1.1)
    RadioGroup radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:array:rows:rows:1:group");
    Radio radio11 = (Radio) tester.getComponentFromLastRenderedPage("panel:form:content:content:array:rows:rows:1:group:cells:2:cell:categoryLabel:radio");
    Assert.assertEquals(radioGroup.getModelObject(), radio11.getModelObject());

    // select open field
    FormComponent field = (FormComponent) ComponentTesterUtils.findChildren(radioGroup, TextField.class).get(0);
    field.setModel(new Model("1"));
    tester.executeAjaxEvent(field, "onchange");

    radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:array:rows:rows:1:group");
    Radio radio13 = (Radio) tester.getComponentFromLastRenderedPage("panel:form:content:content:array:rows:rows:1:group:cells:4:cell:categoryLabel:radio");
    Assert.assertEquals(radioGroup.getModelObject(), radio13.getModelObject());

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  @SuppressWarnings("unused")
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
      e.printStackTrace();
    }
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("S1").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3", "4", "5", "6", "7", "8");
    builder.inPage("P1").withQuestion("Q1_MULTIPLE", "1", true).withCategories("1", "2", "3");
    builder.withSection("S2").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");
    builder.inPage("P2").withQuestion("Q2_MULTIPLE", "2", true).withCategory("1").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2_MULTIPLE").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    builder.withSection("S3").withPage("P3").withQuestion("Q3").withCategories("1", "2").withCategory("3").withOpenAnswerDefinition("OPEN_3", DataType.TEXT);
    builder.inQuestion("Q3").withQuestion("Q3_1");
    builder.inQuestion("Q3").withQuestion("Q3_2");

    builder.withSection("S_MULTIPLE_OPEN").withPage("P_MULTIPLE_OPEN").withQuestion("MULTIPLE_OPEN").withSharedCategory("DURATION").withOpenAnswerDefinition("DURATION_OPEN", DataType.INTEGER);
    builder.inOpenAnswerDefinition("DURATION_OPEN").withOpenAnswerDefinition("DURATION_OPEN_HOURS", DataType.INTEGER).addValidator(new RangeValidator(0l, 16l));
    builder.inOpenAnswerDefinition("DURATION_OPEN").withOpenAnswerDefinition("DURATION_OPEN_MINUTES", DataType.INTEGER).addValidator(new RangeValidator(0l, 960l));
    builder.inQuestion("MULTIPLE_OPEN").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    builder.inPage("P_MULTIPLE_OPEN").withQuestion("MULTIPLE_MULTIPLE_OPEN", "3", true).withSharedCategory("MULTIPLE_DURATION").withOpenAnswerDefinition("MULTIPLE_DURATION_OPEN", DataType.INTEGER);
    builder.inOpenAnswerDefinition("MULTIPLE_DURATION_OPEN").withOpenAnswerDefinition("MULTIPLE_DURATION_OPEN_HOURS", DataType.INTEGER).addValidator(new RangeValidator(0l, 16l));
    builder.inOpenAnswerDefinition("MULTIPLE_DURATION_OPEN").withOpenAnswerDefinition("MULTIPLE_DURATION_OPEN_MINUTES", DataType.INTEGER).addValidator(new RangeValidator(0l, 960l));
    builder.inQuestion("MULTIPLE_MULTIPLE_OPEN").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(locale);

    return q;
  }

}
