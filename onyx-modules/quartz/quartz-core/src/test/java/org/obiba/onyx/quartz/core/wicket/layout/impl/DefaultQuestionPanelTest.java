/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.validation.validator.NumberValidator;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
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
import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.obiba.onyx.util.data.DataType;
import org.obiba.wicket.test.MockSpringApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.StaticMessageSource;

public class DefaultQuestionPanelTest {

  static final Logger log = LoggerFactory.getLogger(DefaultQuestionPanelTest.class);

  private WicketTester tester;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private EntityQueryService queryServiceMock;

  private QuestionnaireBundle questionnaireBundleMock;

  private StaticMessageSource messageSource;

  private IPropertyKeyProvider propertyKeyProvider;

  private Locale locale = Locale.ENGLISH;

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
      protected MessageFormat createMessageFormat(String msg, Locale locale) {
        return new StringReferenceCompatibleMessageFormat((msg != null ? msg : ""), locale);
      }
    };
    messageSource.addMessage("Question.Q1.label", locale, "question label");
    messageSource.addMessage("Question.Q1.help", locale, "question help");
    messageSource.addMessage("Question.Q1.specifications", locale, "question specifications");
    messageSource.addMessage("Question.Q1.instructions", locale, "question instructions");
    messageSource.addMessage("Question.Q1.caption", locale, "question caption");
    messageSource.addMessage("QuestionCategory.Q1.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q1.2.label", locale, "Choice two");
    messageSource.addMessage("QuestionCategory.Q1.3.label", locale, "Choice three");

    messageSource.addMessage("Question.Q1_MULTIPLE.label", locale, "question label");
    messageSource.addMessage("Question.Q1_MULTIPLE.help", locale, "question help");
    messageSource.addMessage("Question.Q1_MULTIPLE.specifications", locale, "question specifications");
    messageSource.addMessage("Question.Q1_MULTIPLE.instructions", locale, "question instructions");
    messageSource.addMessage("Question.Q1_MULTIPLE.caption", locale, "question caption");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.2.label", locale, "Choice two");
    messageSource.addMessage("QuestionCategory.Q1_MULTIPLE.3.label", locale, "Choice three");

    messageSource.addMessage("Question.Q2.label", locale, "question2 label");
    messageSource.addMessage("Question.Q2.help", locale, "question2 help");
    messageSource.addMessage("Question.Q2.specifications", locale, "question2 specifications");
    messageSource.addMessage("Question.Q2.instructions", locale, "question2 instructions");
    messageSource.addMessage("Question.Q2.caption", locale, "question2 caption");
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
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
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

    // tester.dumpPage();

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
    tester.assertComponent("panel:form:content:content:categories:category:1:input:categoryLabel:radio", Radio.class);
    tester.assertComponent("panel:form:content:content:categories:category:2:input:categoryLabel:radio", Radio.class);
    tester.assertComponent("panel:form:content:content:categories:category:3:input:categoryLabel:radio", Radio.class);

    // check previous answer is here (radio 1)
    RadioGroup radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    Radio radio1 = (Radio) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:categoryLabel:radio");
    Assert.assertEquals(radioGroup.getModelObject(), radio1.getModelObject());

    // select radio 2
    tester.executeAjaxEvent("panel:form:content:content:categories:category:2:input:categoryLabel:radio", "onchange");
    radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    Radio radio2 = (Radio) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:2:input:categoryLabel:radio");
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
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), open)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question);
    QuestionCategory questionCategory = question.getQuestionCategories().get(0);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, questionCategory, questionCategory.getCategory().getOpenAnswerDefinition(), null)).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
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

    // tester.dumpPage();

    // check all expected radios are here
    tester.assertComponent("panel:form:content:content:categories", RadioGroup.class);
    tester.isInvisible("panel:form:content:content:categories:category:1:input:categoryLabel:radio");
    tester.assertComponent("panel:form:content:content:categories:category:2:input:categoryLabel:radio", Radio.class);
    tester.assertComponent("panel:form:content:content:categories:category:3:input:categoryLabel:radio", Radio.class);

    // check previous answer is here (radio 2)
    RadioGroup radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    Radio radio2 = (Radio) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:2:input:categoryLabel:radio");
    Assert.assertEquals(radioGroup.getModelObject(), radio2.getModelObject());
    Assert.assertTrue(radioGroup.isRequired());
    FormComponent field = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:open:input:field");
    Assert.assertFalse(field.isRequired());

    // select open field
    tester.executeAjaxEvent("panel:form:content:content:categories:category:1:input:open:open:input:field", "onclick");
    radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    Radio radio1 = (Radio) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:categoryLabel:radio");
    Assert.assertEquals(radioGroup.getModelObject(), radio1.getModelObject());
    Assert.assertTrue(radioGroup.isRequired());
    field = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:open:input:field");
    Assert.assertTrue(field.isRequired());

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
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openHours)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openMinutes)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question);
    QuestionCategory questionCategory = question.getQuestionCategories().get(0);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, questionCategory, questionCategory.getCategory().getOpenAnswerDefinition(), null)).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
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

    // check all expected radios are here
    tester.assertComponent("panel:form:content:content:categories", RadioGroup.class);
    tester.isInvisible("panel:form:content:content:categories:category:1:input:categoryLabel:radio");
    tester.assertComponent("panel:form:content:content:categories:category:2:input:categoryLabel:radio", Radio.class);
    tester.assertComponent("panel:form:content:content:categories:category:3:input:categoryLabel:radio", Radio.class);

    // check previous answer is here (radio 2)
    RadioGroup radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    Radio radio2 = (Radio) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:2:input:categoryLabel:radio");
    Assert.assertEquals(radioGroup.getModelObject(), radio2.getModelObject());
    Assert.assertTrue(radioGroup.isRequired());
    FormComponent field1 = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:repeating:1:open:open:input:field");
    Assert.assertFalse(field1.isRequired());
    FormComponent field2 = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:repeating:2:open:open:input:field");
    Assert.assertFalse(field2.isRequired());

    // select open field
    tester.executeAjaxEvent("panel:form:content:content:categories:category:1:input:open:repeating:1:open:open:input:field", "onclick");
    radioGroup = (RadioGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    Radio radio1 = (Radio) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:categoryLabel:radio");
    Assert.assertEquals(radioGroup.getModelObject(), radio1.getModelObject());
    Assert.assertTrue(radioGroup.isRequired());
    field1 = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:repeating:1:open:open:input:field");
    Assert.assertTrue(field1.isRequired());
    field2 = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:repeating:2:open:open:input:field");
    Assert.assertTrue(field2.isRequired());

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

    // check all expected radios are here
    tester.assertComponent("panel:form:content:content:categories", CheckGroup.class);
    tester.assertComponent("panel:form:content:content:categories:category:1:input:categoryLabel:checkbox", CheckBox.class);
    tester.assertComponent("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox", CheckBox.class);
    tester.assertComponent("panel:form:content:content:categories:category:3:input:categoryLabel:checkbox", CheckBox.class);

    // check previous answer is here (checkbox 1)
    CheckGroup checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    CheckBox checkbox1 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:categoryLabel:checkbox");
    Collection<IModel> selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(1, selections.size());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox1.getModel()).getQuestionCategory(), selections.iterator().next().getObject());

    // select checkbox 2
    tester.executeAjaxEvent("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox", "onchange");
    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    checkbox1 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:categoryLabel:checkbox");
    CheckBox checkbox2 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox");
    selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(2, selections.size());
    Iterator<IModel> iterator = selections.iterator();
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox1.getModel()).getQuestionCategory(), iterator.next().getObject());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox2.getModel()).getQuestionCategory(), iterator.next().getObject());

    // unselect checkbox 2
    tester.executeAjaxEvent("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox", "onchange");
    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    checkbox1 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:categoryLabel:checkbox");
    checkbox2 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox");
    selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(1, selections.size());
    iterator = selections.iterator();
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox1.getModel()).getQuestionCategory(), iterator.next().getObject());

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

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
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), open)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    // activeQuestionnaireAdministrationServiceMock.deleteAnswers(question);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, question.getQuestionCategories().get(0), question.getQuestionCategories().get(0).getCategory().getOpenAnswerDefinition(), null)).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
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

    // check all expected radios are here
    tester.assertComponent("panel:form:content:content:categories", CheckGroup.class);
    tester.assertComponent("panel:form:content:content:categories:category:1:input:categoryLabel:checkbox", CheckBox.class);
    tester.assertComponent("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox", CheckBox.class);
    tester.assertComponent("panel:form:content:content:categories:category:3:input:categoryLabel:checkbox", CheckBox.class);

    // check previous answer is here (checkbox 2)
    CheckGroup checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    CheckBox checkbox2 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox");
    Collection<IModel> selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(1, selections.size());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox2.getModel()).getQuestionCategory(), selections.iterator().next().getObject());
    FormComponent field = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:open:input:field");
    Assert.assertFalse(field.isRequired());

    // select open field
    tester.executeAjaxEvent("panel:form:content:content:categories:category:1:input:open:open:input:field", "onclick");
    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    field = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:open:input:field");
    Assert.assertTrue(field.isRequired());

    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    CheckBox checkbox1 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:categoryLabel:checkbox");
    checkbox2 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox");
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
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openHours)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer(question, question.getCategories().get(0), openMinutes)).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    QuestionCategory questionCategory = question.getQuestionCategories().get(0);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, questionCategory, questionCategory.getCategory().getOpenAnswerDefinition(), null)).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).atLeastOnce();
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

    dumpPage("testMultipleChoiceQuestionWithMultipleOpenAnswer");

    // check all expected radios are here
    tester.assertComponent("panel:form:content:content:categories", CheckGroup.class);
    tester.isInvisible("panel:form:content:content:categories:category:1:input:categoryLabel:checkbox");
    tester.assertComponent("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox", CheckBox.class);
    tester.assertComponent("panel:form:content:content:categories:category:3:input:categoryLabel:checkbox", CheckBox.class);

    // check previous answer is here (radio 2)
    CheckGroup checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    CheckBox checkbox2 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:2:input:categoryLabel:checkbox");
    Collection<IModel> selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(1, selections.size());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox2.getModel()).getQuestionCategory(), selections.iterator().next().getObject());
    FormComponent field1 = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:repeating:1:open:open:input:field");
    Assert.assertFalse(field1.isRequired());
    FormComponent field2 = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:repeating:2:open:open:input:field");
    Assert.assertFalse(field2.isRequired());

    // select open field
    tester.executeAjaxEvent("panel:form:content:content:categories:category:1:input:open:repeating:1:open:open:input:field", "onclick");
    checkGroup = (CheckGroup) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories");
    CheckBox checkbox1 = (CheckBox) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:categoryLabel:checkbox");
    selections = (Collection<IModel>) checkGroup.getModelObject();
    Assert.assertEquals(2, selections.size());
    Iterator<IModel> iterator = selections.iterator();
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox2.getModel()).getQuestionCategory(), iterator.next().getObject());
    Assert.assertEquals(((QuestionCategoryCheckBoxModel) checkbox1.getModel()).getQuestionCategory(), iterator.next().getObject());

    field1 = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:repeating:1:open:open:input:field");
    Assert.assertTrue(field1.isRequired());
    field2 = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:repeating:2:open:open:input:field");
    Assert.assertTrue(field2.isRequired());

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
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question1, question.getQuestionCategories().get(0))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question1, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question1, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question2, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question2, question.getQuestionCategories().get(1))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question2, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();

    // activeQuestionnaireAdministrationServiceMock.deleteAnswers(question);
    // expect(activeQuestionnaireAdministrationServiceMock.answer(QuestionnaireFinder.getInstance(questionnaire).findQuestionCategory("Q3",
    // "Q3.2"), null)).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "specifications")).andReturn(propertyKeyProvider.getPropertyKey(question, "specifications")).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "caption")).andReturn(propertyKeyProvider.getPropertyKey(question, "caption")).anyTimes();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).anyTimes();
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

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("S1").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");
    builder.inPage("P1").withQuestion("Q1_MULTIPLE", true).withCategories("1", "2", "3");
    builder.withSection("S2").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");
    builder.inPage("P2").withQuestion("Q2_MULTIPLE", true).withCategory("1").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2_MULTIPLE").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    builder.withSection("S3").withPage("P3").withQuestion("Q3").withCategories("1", "2", "3");
    builder.inQuestion("Q3").withQuestion("Q3_1");
    builder.inQuestion("Q3").withQuestion("Q3_2");

    builder.withSection("S_MULTIPLE_OPEN").withPage("P_MULTIPLE_OPEN").withQuestion("MULTIPLE_OPEN").withSharedCategory("DURATION").withOpenAnswerDefinition("DURATION_OPEN", DataType.INTEGER);
    builder.inOpenAnswerDefinition("DURATION_OPEN").withOpenAnswerDefinition("DURATION_OPEN_HOURS", DataType.INTEGER).addOpenAnswerDefinitionValidator(new NumberValidator.RangeValidator(0, 16));
    builder.inOpenAnswerDefinition("DURATION_OPEN").withOpenAnswerDefinition("DURATION_OPEN_MINUTES", DataType.INTEGER).addOpenAnswerDefinitionValidator(new NumberValidator.RangeValidator(0, 960));
    builder.inQuestion("MULTIPLE_OPEN").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    builder.inPage("P_MULTIPLE_OPEN").withQuestion("MULTIPLE_MULTIPLE_OPEN", true).withSharedCategory("MULTIPLE_DURATION").withOpenAnswerDefinition("MULTIPLE_DURATION_OPEN", DataType.INTEGER);
    builder.inOpenAnswerDefinition("MULTIPLE_DURATION_OPEN").withOpenAnswerDefinition("MULTIPLE_DURATION_OPEN_HOURS", DataType.INTEGER).addOpenAnswerDefinitionValidator(new NumberValidator.RangeValidator(0, 16));
    builder.inOpenAnswerDefinition("MULTIPLE_DURATION_OPEN").withOpenAnswerDefinition("MULTIPLE_DURATION_OPEN_MINUTES", DataType.INTEGER).addOpenAnswerDefinitionValidator(new NumberValidator.RangeValidator(0, 960));
    builder.inQuestion("MULTIPLE_MULTIPLE_OPEN").withSharedCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(locale);

    return q;
  }

}
