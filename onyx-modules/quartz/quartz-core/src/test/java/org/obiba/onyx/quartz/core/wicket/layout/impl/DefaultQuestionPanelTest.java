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
import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
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
    messageSource.addMessage("Question.Q1.instructions", locale, "question instructions");
    messageSource.addMessage("Question.Q1.caption", locale, "question caption");
    messageSource.addMessage("QuestionCategory.Q1.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q1.2.label", locale, "Choice two");
    messageSource.addMessage("QuestionCategory.Q1.3.label", locale, "Choice three");
    messageSource.addMessage("Question.Q2.label", locale, "question2 label");
    messageSource.addMessage("Question.Q2.help", locale, "question2 help");
    messageSource.addMessage("Question.Q2.instructions", locale, "question2 instructions");
    messageSource.addMessage("Question.Q2.caption", locale, "question2 caption");
    messageSource.addMessage("QuestionCategory.Q2.1.label", locale, "Choice one");
    messageSource.addMessage("QuestionCategory.Q2.DONT_KNOW.label", locale, "Dont know");
    messageSource.addMessage("QuestionCategory.Q2.PREFER_NOT_ANSWER.label", locale, "Prefer not answer");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_INT.label", locale, "open label");
    messageSource.addMessage("OpenAnswerDefinition.OPEN_INT.unitLabel", locale, "open unit label");

    messageSource.addMessage("Question.Q3.label", locale, "question3 label");
    messageSource.addMessage("Question.Q3.help", locale, "question3 help");
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
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, question.getQuestionCategories().get(1), null)).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
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
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(0))).andReturn(null).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(1))).andReturn(previousCategoryAnswer).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, question.getQuestionCategories().get(2))).andReturn(null).atLeastOnce();
    activeQuestionnaireAdministrationServiceMock.deleteAnswers(question);
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, question.getQuestionCategories().get(0), null)).andReturn(new CategoryAnswer());
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).anyTimes();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "help")).andReturn(propertyKeyProvider.getPropertyKey(question, "help")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "instructions")).andReturn(propertyKeyProvider.getPropertyKey(question, "instructions")).atLeastOnce();
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
    Assert.assertNull(radioGroup.getModelObject());
    Assert.assertFalse(radioGroup.isRequired());
    field = (FormComponent) tester.getComponentFromLastRenderedPage("panel:form:content:content:categories:category:1:input:open:open:input:field");
    Assert.assertTrue(field.isRequired());

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

    dumpPage();

    verify(activeInterviewServiceMock);
    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);
  }

  private void dumpPage() {
    tester.dumpPage();
    File dump = new File("target/" + getClass().getSimpleName() + ".html");
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
    builder.withSection("S2").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withCategories("DONT_KNOW", "PREFER_NOT_ANSWER");

    builder.withSection("S3").withPage("P3").withQuestion("Q3").withCategories("1", "2", "3");
    builder.inQuestion("Q3").withQuestion("Q3_1");
    builder.inQuestion("Q3").withQuestion("Q3_2");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(locale);

    return q;
  }

}
