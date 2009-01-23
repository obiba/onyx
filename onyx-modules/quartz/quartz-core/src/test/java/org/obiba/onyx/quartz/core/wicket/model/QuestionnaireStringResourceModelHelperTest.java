/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.model;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.text.MessageFormat;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.obiba.onyx.util.data.DataType;
import org.obiba.wicket.test.MockSpringApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.StaticMessageSource;

/**
 * 
 */
public class QuestionnaireStringResourceModelHelperTest {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionnaireStringResourceModelHelperTest.class);

  private ApplicationContextMock applicationContextMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private Locale locale;

  private Questionnaire questionnaire;

  private Question question;

  private QuestionCategory questionCategory;

  private OpenAnswerDefinition openAnswerDefinition;

  private QuestionnaireBundle questionnaireBundleMock;

  private StaticMessageSource messageSource;

  private IPropertyKeyProvider propertyKeyProvider;

  @Before
  public void setUp() {
    applicationContextMock = new ApplicationContextMock();

    questionnaireBundleManagerMock = createMock(QuestionnaireBundleManager.class);
    applicationContextMock.putBean("questionnaireBundleManager", questionnaireBundleManagerMock);

    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    applicationContextMock.putBean("activeQuestionnaireAdministrationService", activeQuestionnaireAdministrationServiceMock);

    locale = new Locale("en");

    questionnaire = new Questionnaire("QUESTIONNAIRE", "1.0");

    Question parentQuestion = new Question("PARENT_QUESTION");
    question = new Question("QUESTION");
    parentQuestion.addQuestion(question);
    questionCategory = new QuestionCategory();
    Category category = new Category("CATEGORY");
    openAnswerDefinition = new OpenAnswerDefinition("OPEN", DataType.TEXT);
    category.setOpenAnswerDefinition(openAnswerDefinition);
    questionCategory.setCategory(category);
    question.addQuestionCategory(questionCategory);
    Page page = new Page("PAGE");
    page.addQuestion(parentQuestion);
    questionnaire.addPage(page);

    questionnaireBundleMock = createMock(QuestionnaireBundle.class);

    messageSource = new StaticMessageSource() {
      @Override
      protected MessageFormat createMessageFormat(String msg, Locale locale) {
        return new StringReferenceCompatibleMessageFormat((msg != null ? msg : ""), locale);
      }
    };

    propertyKeyProvider = new DefaultPropertyKeyProviderImpl();

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    new WicketTester(application);
  }

  @Test
  public void testStringResourceModelOpenLabel() {

    messageSource.addMessage("Question.QUESTION.label", locale, "question");
    messageSource.addMessage("QuestionCategory.QUESTION.CATEGORY.label", locale, "question_category");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.label", locale, "open_label");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.unitLabel", locale, "open_unit_label");

    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleManagerMock.getBundle("QUESTIONNAIRE")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "label")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "label"));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IModel model = QuestionnaireStringResourceModelHelper.getStringResourceModel(question, questionCategory, openAnswerDefinition);
    log.info(model.getObject().toString());
    Assert.assertEquals("open_label", model.getObject().toString());

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

  }

  @Test
  public void testStringResourceModelOpenInvalidLabel() {

    messageSource.addMessage("Question.QUESTION.label", locale, "question");
    messageSource.addMessage("QuestionCategory.QUESTION.CATEGORY.label", locale, "question_category");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.label", locale, ":");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.unitLabel", locale, "open_unit_label");

    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleManagerMock.getBundle("QUESTIONNAIRE")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "label")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "label"));
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "unitLabel"));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IModel model = QuestionnaireStringResourceModelHelper.getStringResourceModel(question, questionCategory, openAnswerDefinition);
    log.info(model.getObject().toString());
    Assert.assertEquals("open_unit_label", model.getObject().toString());

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

  }

  @Test
  public void testStringResourceModelOpenUnitLabel() {

    messageSource.addMessage("Question.QUESTION.label", locale, "question");
    messageSource.addMessage("QuestionCategory.QUESTION.CATEGORY.label", locale, "question_category");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.label", locale, "");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.unitLabel", locale, "open_unit_label");

    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleManagerMock.getBundle("QUESTIONNAIRE")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "label")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "label"));
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "unitLabel"));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IModel model = QuestionnaireStringResourceModelHelper.getStringResourceModel(question, questionCategory, openAnswerDefinition);
    log.info(model.getObject().toString());
    Assert.assertEquals("open_unit_label", model.getObject().toString());

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

  }

  @Test
  public void testStringResourceModelCategory() {

    messageSource.addMessage("Question.QUESTION.label", locale, "question");
    messageSource.addMessage("QuestionCategory.QUESTION.CATEGORY.label", locale, "question_category");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.label", locale, "");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.unitLabel", locale, "");

    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleManagerMock.getBundle("QUESTIONNAIRE")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(questionCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(questionCategory, "label"));
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "label")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "label"));
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "unitLabel"));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IModel model = QuestionnaireStringResourceModelHelper.getStringResourceModel(question, questionCategory, openAnswerDefinition);
    log.info(model.getObject().toString());
    Assert.assertEquals("question_category", model.getObject().toString());

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

  }

  @Test
  public void testStringResourceModelQuestion() {

    messageSource.addMessage("Question.QUESTION.label", locale, "question");
    messageSource.addMessage("QuestionCategory.QUESTION.CATEGORY.label", locale, "");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.label", locale, "");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.unitLabel", locale, "");

    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleManagerMock.getBundle("QUESTIONNAIRE")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label"));
    expect(questionnaireBundleMock.getPropertyKey(questionCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(questionCategory, "label"));
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "label")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "label"));
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "unitLabel"));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IModel model = QuestionnaireStringResourceModelHelper.getStringResourceModel(question, questionCategory, openAnswerDefinition);
    log.info(model.getObject().toString());
    Assert.assertEquals("question", model.getObject().toString());

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

  }

  @Test
  public void testStringResourceModelParentQuestion() {

    messageSource.addMessage("Question.PARENT_QUESTION.label", locale, "parent_question");
    messageSource.addMessage("Question.QUESTION.label", locale, "question");
    messageSource.addMessage("QuestionCategory.QUESTION.CATEGORY.label", locale, "");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.label", locale, "");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.unitLabel", locale, "");

    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleManagerMock.getBundle("QUESTIONNAIRE")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question.getParentQuestion(), "label")).andReturn(propertyKeyProvider.getPropertyKey(question.getParentQuestion(), "label"));
    expect(questionnaireBundleMock.getPropertyKey(question, "label")).andReturn(propertyKeyProvider.getPropertyKey(question, "label"));
    expect(questionnaireBundleMock.getPropertyKey(questionCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(questionCategory, "label"));
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "label")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "label"));
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "unitLabel")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "unitLabel"));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IModel model = QuestionnaireStringResourceModelHelper.getStringResourceModel(question.getParentQuestion(), questionCategory, openAnswerDefinition);
    log.info(model.getObject().toString());
    Assert.assertEquals("parent_question / question", model.getObject().toString());

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

  }

  @Test
  public void testStringResourceModelParentQuestionOpenLabel() {

    messageSource.addMessage("Question.PARENT_QUESTION.label", locale, "parent_question");
    messageSource.addMessage("Question.QUESTION.label", locale, "question");
    messageSource.addMessage("QuestionCategory.QUESTION.CATEGORY.label", locale, "");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.label", locale, "label");
    messageSource.addMessage("OpenAnswerDefinition.OPEN.unitLabel", locale, "");

    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleManagerMock.getBundle("QUESTIONNAIRE")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(question.getParentQuestion(), "label")).andReturn(propertyKeyProvider.getPropertyKey(question.getParentQuestion(), "label"));
    expect(questionnaireBundleMock.getPropertyKey(openAnswerDefinition, "label")).andReturn(propertyKeyProvider.getPropertyKey(openAnswerDefinition, "label"));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    IModel model = QuestionnaireStringResourceModelHelper.getStringResourceModel(question.getParentQuestion(), questionCategory, openAnswerDefinition);
    log.info(model.getObject().toString());
    Assert.assertEquals("parent_question / label", model.getObject().toString());

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

  }
}
