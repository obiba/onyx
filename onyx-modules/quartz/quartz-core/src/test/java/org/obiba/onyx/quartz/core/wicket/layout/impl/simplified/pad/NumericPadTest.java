/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.pad;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.support.StaticMessageSource;

public class NumericPadTest {

  private WicketTester tester;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private QuestionnaireBundleManager questionnaireBundleManagerMock;

  private QuestionnaireBundle questionnaireBundleMock;

  private StaticMessageSource messageSource;

  private IPropertyKeyProvider propertyKeyProvider;

  private Locale locale = Locale.ENGLISH;

  private QuestionnaireParticipant questionnaireParticipant;

  private static Boolean modalIsShown;

  Questionnaire questionnaire;

  Question question;

  static class MyModalWindow extends ModalWindow {

    private static final long serialVersionUID = 1L;

    public MyModalWindow(String id) {
      super(id);
    }

    @Override
    public void close(AjaxRequestTarget target) {
      modalIsShown = false;
    }

  }

  @Before
  public void setUp() {

    ApplicationContextMock mockCtx = new ApplicationContextMock();
    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    questionnaireBundleManagerMock = createMock(QuestionnaireBundleManager.class);
    questionnaireBundleMock = createMock(QuestionnaireBundle.class);

    mockCtx.putBean("activeQuestionnaireAdministrationService", activeQuestionnaireAdministrationServiceMock);
    mockCtx.putBean("questionnaireBundleManager", questionnaireBundleManagerMock);

    messageSource = new StaticMessageSource() {
      @Override
      protected MessageFormat createMessageFormat(String msg, Locale locale) {
        return new StringReferenceCompatibleMessageFormat((msg != null ? msg : ""), locale);
      }
    };
    messageSource.addMessage("Questionnaire.HealthQuestionnaireSelfAdministered.ok", locale, "Ok");
    messageSource.addMessage("Questionnaire.HealthQuestionnaireSelfAdministered.cancel", locale, "Cancel");
    messageSource.addMessage("Questionnaire.HealthQuestionnaireSelfAdministered.reset", locale, "Clear");
    messageSource.addMessage("QuestionCategory.Q1.1.label", locale, "Choice one");

    propertyKeyProvider = new DefaultPropertyKeyProviderImpl();

    MockSpringApplication application = new MockSpringApplication();
    application.setHomePage(Page.class);
    application.setApplicationContext(mockCtx);
    tester = new WicketTester(application);

    questionnaireParticipant = new QuestionnaireParticipant();
    questionnaireParticipant.setParticipant(new Participant());

    setupNumericPad();
  }

  private void setupNumericPad() {

    questionnaire = createQuestionnaire();
    question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");

    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).times(1);
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).times(4);

    expect(questionnaireBundleManagerMock.getBundle("HealthQuestionnaireSelfAdministered")).andReturn(questionnaireBundleMock).atLeastOnce();
    expect(questionnaireBundleMock.getMessageSource()).andReturn(messageSource).atLeastOnce();
    for(QuestionCategory qCategory : question.getQuestionCategories()) {
      expect(questionnaireBundleMock.getPropertyKey(qCategory, "label")).andReturn(propertyKeyProvider.getPropertyKey(qCategory, "label")).atLeastOnce();
    }
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(questionnaire).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(questionnaire, "reset")).andReturn(propertyKeyProvider.getPropertyKey(questionnaire, "reset")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(questionnaire, "ok")).andReturn(propertyKeyProvider.getPropertyKey(questionnaire, "ok")).atLeastOnce();
    expect(questionnaireBundleMock.getPropertyKey(questionnaire, "cancel")).andReturn(propertyKeyProvider.getPropertyKey(questionnaire, "cancel")).atLeastOnce();

    replay(activeQuestionnaireAdministrationServiceMock, questionnaireBundleMock, questionnaireBundleManagerMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {
        QuestionCategory category = question.getQuestionCategories().get(0);
        ModalWindow modal = new MyModalWindow("modal");

        return new NumericPad("panel", new Model(question), new Model(question.getQuestionCategories().get(0)), new Model(category.getOpenAnswerDefinition())) {
          @Override
          public Locale getLocale() {
            // for error messages in english
            return Locale.ENGLISH;
          }
        };
      }
    });

    verify(activeQuestionnaireAdministrationServiceMock, questionnaireBundleMock, questionnaireBundleManagerMock);
  }

  private Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaireSelfAdministered", "1.0");

    builder.withSection("S1").withPage("P1").withQuestion("Q1").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER).setRequired(true);

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(locale);

    return q;
  }

  @Test
  public void testNumericPadButtons() {

    // Enter "123456789" on the numeric pad.
    tester.executeAjaxEvent("panel:1:button:link", "onclick");
    tester.executeAjaxEvent("panel:2:button:link", "onclick");
    tester.executeAjaxEvent("panel:3:button:link", "onclick");
    tester.executeAjaxEvent("panel:4:button:link", "onclick");
    tester.executeAjaxEvent("panel:5:button:link", "onclick");
    tester.executeAjaxEvent("panel:6:button:link", "onclick");
    tester.executeAjaxEvent("panel:7:button:link", "onclick");
    tester.executeAjaxEvent("panel:8:button:link", "onclick");
    tester.executeAjaxEvent("panel:9:button:link", "onclick");

    // Validate that the value has been entered correctly.
    tester.assertModelValue("panel:form:value:input:field", DataBuilder.buildText("123456789"));

    // Press the clear button.
    tester.executeAjaxEvent("panel:form:clear:link", "onclick");

    // Verify that the value has been cleared.
    tester.assertModelValue("panel:form:value:input:field", null);

    // Press the cancel button.
    tester.executeAjaxEvent("panel:form:cancel:link", "onclick");

  }

  @Test
  public void testValidNumericPadSubmit() {

    // Enter "1973" on the numeric pad.
    tester.executeAjaxEvent("panel:1:button:link", "onclick");
    tester.executeAjaxEvent("panel:9:button:link", "onclick");
    tester.executeAjaxEvent("panel:7:button:link", "onclick");
    tester.executeAjaxEvent("panel:3:button:link", "onclick");

    EasyMock.reset(activeQuestionnaireAdministrationServiceMock);

    List<CategoryAnswer> answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer category = new CategoryAnswer();
    category.setCategoryName("1");
    answers.add(category);
    expect(activeQuestionnaireAdministrationServiceMock.findAnswers(question)).andReturn(answers);
    QuestionCategory questionCategory = question.findQuestionCategory("1");

    // Insure that "1973" is saved (set has the answer to the question).
    expect(activeQuestionnaireAdministrationServiceMock.answer(question, questionCategory, questionCategory.getOpenAnswerDefinition(), DataBuilder.buildInteger("1973"))).andReturn(category);

    replay(activeQuestionnaireAdministrationServiceMock);

    // Press the OK button.
    tester.executeAjaxEvent("panel:form:ok:link", "onclick");

    verify(activeQuestionnaireAdministrationServiceMock);

  }

  @Test
  public void testInvalidNumericPadSubmit() {

    EasyMock.reset(activeQuestionnaireAdministrationServiceMock);

    expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale);
    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire);

    replay(activeQuestionnaireAdministrationServiceMock);

    // Press the OK button.
    tester.executeAjaxEvent("panel:form:ok:link", "onclick");

    // Verify that an error has been generated (this field is required).
    Assert.assertEquals(1, tester.getMessages(FeedbackMessage.ERROR).size());
    // warning: error message is localized
    tester.assertErrorMessages(new String[] { "Field 'Choice one' is required." });

    verify(activeQuestionnaireAdministrationServiceMock);

  }

}
