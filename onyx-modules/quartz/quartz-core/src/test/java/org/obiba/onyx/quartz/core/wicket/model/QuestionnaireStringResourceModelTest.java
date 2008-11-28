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

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.support.StaticMessageSource;

public class QuestionnaireStringResourceModelTest {

  private ApplicationContextMock applicationContextMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private Locale locale;

  private Questionnaire questionnaire;

  private QuestionCategory questionCategory;

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

    questionnaire = new Questionnaire("Q1", "1.0");

    questionCategory = new QuestionCategory();
    questionCategory.setQuestion(new Question("Q1"));
    questionCategory.setCategory(new Category("DONT_KNOW"));

    questionnaireBundleMock = createMock(QuestionnaireBundle.class);

    messageSource = new StaticMessageSource() {
      @Override
      protected MessageFormat createMessageFormat(String msg, Locale locale) {
        return new StringReferenceCompatibleMessageFormat((msg != null ? msg : ""), locale);
      }
    };
    messageSource.addMessage("Questionnaire.Q1.label", locale, "Test questionnaire label");
    messageSource.addMessage("Questionnaire.Q1.description", locale, "Test questionnaire description with arguments: {0}, {1}");
    messageSource.addMessage("QuestionCategory.Q1.DONT_KNOW.label", locale, "${Category.DONT_KNOW.label}");
    messageSource.addMessage("Category.DONT_KNOW.label", locale, "Don''t know");

    propertyKeyProvider = new DefaultPropertyKeyProviderImpl();

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    new WicketTester(application);
  }

  @Test
  public void testResolveStringWithNoArgs() {
    ILocalizable localizable = questionnaire;
    String property = "label";

    // Expect that methods are called on activeQuestionnaireAdministrationServiceMock to
    // retrieve the current locale and questionnaire.
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale);
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire);

    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("Q1")).andReturn(questionnaireBundleMock);

    // Expect that methods are called on questionnaireBundleMock to retrieve the message source
    // and required property key.
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource);
    expect(questionnaireBundleMock.getPropertyKey(localizable, property)).andReturn(propertyKeyProvider.getPropertyKey(localizable, property));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    QuestionnaireStringResourceModel model = new QuestionnaireStringResourceModel(localizable, property, null);
    String stringResource = (String) model.getObject();

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals("Test questionnaire label", stringResource);
  }

  @Test
  public void testResolveStringWithArgs() {
    ILocalizable localizable = questionnaire;
    String property = "description";

    // Expect that methods are called on activeQuestionnaireAdministrationServiceMock to
    // retrieve the current locale and questionnaire.
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale);
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire);

    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("Q1")).andReturn(questionnaireBundleMock);

    // Expect that methods are called on questionnaireBundleMock to retrieve the message source
    // and required property key.
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource);
    expect(questionnaireBundleMock.getPropertyKey(localizable, property)).andReturn(propertyKeyProvider.getPropertyKey(localizable, property));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    QuestionnaireStringResourceModel model = new QuestionnaireStringResourceModel(localizable, "description", new String[] { "firstarg", "secondarg" });
    String stringResource = (String) model.getObject();

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals("Test questionnaire description with arguments: firstarg, secondarg", stringResource);
  }

  @Test
  public void testResolveStringWithReferences() {
    ILocalizable localizable = questionCategory;
    String property = "label";

    // Expect that methods are called on activeQuestionnaireAdministrationServiceMock to
    // retrieve the current locale and questionnaire.
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale);
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire);

    // Expect that questionnaireBundleManagerMock is used to retrieve the current questionnaire bundle.
    expect(questionnaireBundleManagerMock.getBundle("Q1")).andReturn(questionnaireBundleMock);

    // Expect that methods are called on questionnaireBundleMock to retrieve the message source
    // and required property key.
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource);
    expect(questionnaireBundleMock.getPropertyKey(localizable, property)).andReturn(propertyKeyProvider.getPropertyKey(localizable, property));

    replay(activeQuestionnaireAdministrationServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    QuestionnaireStringResourceModel model = new QuestionnaireStringResourceModel(localizable, "label", null);
    String stringResource = (String) model.getObject();

    verify(activeQuestionnaireAdministrationServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertEquals("Don't know", stringResource);
  }
}
