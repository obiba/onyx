/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.test;

import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.DbUnitAwareTestExecutionListener;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.QuartzPanel;
import org.obiba.onyx.quartz.core.wicket.layout.IPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayoutFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.quartz.test.provider.AnswerProvider;
import org.obiba.wicket.test.MockSpringApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.SessionScope;

/**
 * Base class for questionnaire tests.
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-spring-context.xml" })
@TransactionConfiguration(transactionManager = "transactionManager")
@TestExecutionListeners(value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitAwareTestExecutionListener.class })
public abstract class AbstractQuestionnaireTest {

  //
  // Constants
  //

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractQuestionnaireTest.class);

  private static final String COMPONENT_ID_SEPARATOR = ":";

  private static final String ONBLUR_EVENT = "onblur";

  private static final String ONCHANGE_EVENT = "onchange";

  //
  // Instance Variables
  //

  private ConfigurableApplicationContext applicationContext;

  private PersistenceManager persistenceManager;

  private ActiveInterviewService activeInterviewService;

  protected ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private QuestionnaireBundleManager questionnaireBundleManager;

  private Stage questionnaireStage;

  private Questionnaire questionnaire;

  private QuestionnaireFinder questionnaireFinder;

  private WicketTester wicketTester;

  //
  // Fixture Methods
  //

  /**
   * Initializes and positions the questionnaire under test at the start page.
   */
  @Before
  public void setUp() {
    initContext();
    initWicketTester();
    initQuestionnaireStage();
    initQuestionnaire();
    initActiveInterview();
    initActiveQuestionnaire();
  }

  private void initContext() {
    // Initialize context and bean references.
    // TODO: If possible, have this class extend BaseDefaultSpringContextTestCase and
    // get a reference to the context created by the latter.
    applicationContext = new ClassPathXmlApplicationContext("test-spring-context.xml");

    // Initialize web session. Need this for session scope beans in the context.
    applicationContext.getBeanFactory().registerScope("session", new SessionScope());
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    request.setSession(session);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    // Initialize references to beans.
    persistenceManager = (PersistenceManager) applicationContext.getBean("persistenceManager");
    activeInterviewService = (ActiveInterviewService) applicationContext.getBean("activeInterviewService");
    activeQuestionnaireAdministrationService = (ActiveQuestionnaireAdministrationService) applicationContext.getBean("activeQuestionnaireAdministrationService");
    questionnaireBundleManager = (QuestionnaireBundleManager) applicationContext.getBean("questionnaireBundleManager");

    // Initialize questionnaire UI factory registries. This is normally done by
    // QuestionnaireUIFactoryRegistrationListener (a WebApplicationStartupListener) but
    // MockSpringApplication doesn't seem to support this mechanism.
    initUIFactoryRegistries();
  }

  private void initUIFactoryRegistries() {
    PageLayoutFactoryRegistry pageLayoutFactoryRegistry = (PageLayoutFactoryRegistry) applicationContext.getBean("pageLayoutFactoryRegistry");
    QuestionPanelFactoryRegistry questionPanelFactoryRegistry = (QuestionPanelFactoryRegistry) applicationContext.getBean("questionPanelFactoryRegistry");

    Map<String, IPageLayoutFactory> pageLayoutFactories = applicationContext.getBeansOfType(IPageLayoutFactory.class);
    if(pageLayoutFactories != null) {
      for(IPageLayoutFactory factory : pageLayoutFactories.values()) {

        pageLayoutFactoryRegistry.register(factory);
      }
    }

    Map<String, IQuestionPanelFactory> questionPanelFactories = applicationContext.getBeansOfType(IQuestionPanelFactory.class);
    if(questionPanelFactories != null) {
      for(IQuestionPanelFactory factory : questionPanelFactories.values()) {

        questionPanelFactoryRegistry.register(factory);
      }
    }
  }

  private void initWicketTester() {
    MockSpringApplication application = new MockSpringApplication();
    application.setHomePage(org.apache.wicket.Page.class);
    application.setApplicationContext(applicationContext);
    wicketTester = new WicketTester(application);
  }

  private void initQuestionnaireStage() {
    questionnaireStage = new Stage();
    questionnaireStage.setName(getQuestionnaireName());
  }

  private void initQuestionnaire() {
    questionnaire = questionnaireBundleManager.getBundle(questionnaireStage.getName()).getQuestionnaire();

    // Also initialize QuestionnaireFinder. (Used by getQuestion() method.)
    questionnaireFinder = new QuestionnaireFinder(questionnaire);
  }

  private void initActiveInterview() {
    Participant participant = persistenceManager.get(Participant.class, Long.valueOf("1"));
    Assert.assertNotNull(participant);
    activeInterviewService.setParticipant(participant);
  }

  private void initActiveQuestionnaire() {
    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);
  }

  //
  // Test Methods
  //

  /**
   * Returns the specified question.
   * 
   * @param name question name
   */
  public Question getQuestion(String name) {
    return questionnaireFinder.findQuestion(name);
  }

  /**
   * Responds to the specified question with the specified answer.
   * 
   * To support repeated calls to this method, the method allows that the question be present <i>either</i> on the
   * current page <i>or</i> on the page immediately following.
   * 
   * Note: In the case of an "open" answer, the answer consists of both the category name (<code>CategoryAnswer.getCategoryName()</code>)
   * and the open answer data (<code>CategoryAnswer.getData()</code>). Non-open answers consists of the category
   * name only.
   * 
   * @param question question
   * @param answer answer
   */
  public void answerQuestion(Question question, CategoryAnswer answer) {
    if(isOnCurrentPage(question)) {
      // Answer the question.
      answerQuestionImpl(question, answer);
    } else {
      // The question might be on the next page. So advance to the next page and try again.
      nextPage();

      if(isOnCurrentPage(question)) {
        // Answer the question.
        answerQuestionImpl(question, answer);
      } else {
        // Not on this page either. Stop looking and treat this as a failure.
        Assert.fail("Failed to answer question " + question.getName() + " (not found)");
      }
    }
  }

  /**
   * Responds to all questions (to be answered) up to the specified question.
   * 
   * Answers are obtained from the specified answer provider.
   * 
   * @param answerProvider answer provider
   * @param toQuestion question to stop at
   * @param inclusive if <code>true</code>, the specified question is the last question answered; if
   * <code>false</code>, the last question answered is the one before that question
   */
  public void answerQuestionsUpTo(AnswerProvider answerProvider, Question toQuestion, boolean inclusive) {
    while(activeQuestionnaireAdministrationService.getCurrentPage() != null) {
      log.info("Current page is " + activeQuestionnaireAdministrationService.getCurrentPage().getName());
      answerQuestionsOnCurrentPage(answerProvider, toQuestion, inclusive);

      if(isOnCurrentPage(toQuestion)) {
        break;
      }

      nextPage();
    }
  }

  /**
   * Responds to all questions (to be answered) on the current page, up to the specified question.
   * 
   * Answers are obtained form the specified answer provider.
   * 
   * @param answerProvider
   * @param toQuestion
   * @param inclusive if <code>true</code>, the specified question is the last question answered; if
   * <code>false</code>, the last question answered is the one before that question
   */
  public void answerQuestionsOnCurrentPage(AnswerProvider answerProvider, Question toQuestion, boolean inclusive) {
    Page currentPage = activeQuestionnaireAdministrationService.getCurrentPage();

    List<Question> questions = currentPage.getQuestions();

    for(Question question : questions) {
      boolean atToQuestion = question.getName().equals(toQuestion.getName());

      if(atToQuestion && !inclusive) {
        return;
      }

      if(question.isToBeAnswered(activeQuestionnaireAdministrationService) && !question.getCategories().isEmpty()) {
        answerQuestion(question, answerProvider.getAnswer(question));
        log.info("Answered question " + question.getName());

        List<Question> subQuestions = question.getQuestions();

        for(Question subQuestion : subQuestions) {
          atToQuestion = subQuestion.getName().equals(toQuestion.getName());

          if(atToQuestion && !inclusive) {
            return;
          }

          if(question.isToBeAnswered(activeQuestionnaireAdministrationService) && !question.getCategories().isEmpty()) {
            answerQuestion(subQuestion, answerProvider.getAnswer(subQuestion));
            log.info("Answered question " + subQuestion.getName());
          } else {
            log.info("Skipping question " + subQuestion.getName());
          }

          if(atToQuestion && inclusive) {
            return;
          }
        }
      } else {
        log.info("Skipping question " + question.getName());
      }

      if(atToQuestion && inclusive) {
        return;
      }
    }
  }

  /**
   * Returns to the page containing the specified earlier question.
   * 
   * The "Previous" button is clicked zero or more times, as necessary.
   * 
   * @param question question to return to
   */
  public void returnToEarlierQuestion(Question question) {
    while(!isOnCurrentPage(question)) {
      previousPage();
    }
  }

  /**
   * Returns to the page containing the specified later question.
   * 
   * The "Next" button is clicked zero or more times, as necessary.
   * 
   * Note: This method does not answer questions along the way.
   * 
   * @param question name of question to return to
   */
  public void returnToLaterQuestion(Question question) {
    while(!isOnCurrentPage(question)) {
      nextPage();
    }
  }

  /**
   * Asserts that the specified question is to be answered.
   * 
   * @param question question
   */
  public void assertQuestionToBeAnswered(Question question) {
    Assert.assertTrue(question.isToBeAnswered(activeQuestionnaireAdministrationService));
  }

  /**
   * Asserts that the specified question is not to be answered.
   * 
   * @param question question
   */
  public void assertQuestionNotToBeAnswered(Question question) {
    Assert.assertFalse(question.isToBeAnswered(activeQuestionnaireAdministrationService));
  }

  /**
   * Starts the questionnaire (i.e., creates the Quartz panel, selects a language (first one available), and moves to
   * the first page of the questionnaire).
   * 
   * This method should be called at the beginning of each test.
   */
  public void startQuestionnaire() {
    // Create a QuartzPanel.
    wicketTester.startPanel(new TestPanelSource() {
      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        QuartzPanel quartzPanel = new QuartzPanel(panelId, questionnaireStage, false);
        quartzPanel.setModel(new Model(questionnaire));
        return (quartzPanel);
      }
    });

    // We are currently at the language selection step. Select a language (1st available).
    FormTester formTester = wicketTester.newFormTester("panel:content:form");
    formTester.select("step:panel:localeSelect", 1);

    // Now advance to the next page, which is the first page of the questionnaire.
    nextPage();
  }

  /**
   * Positions the questionnaire at the previous page.
   */
  public void previousPage() {
    wicketTester.executeAjaxEvent("panel:content:form:previousLink", "onclick");
  }

  /**
   * Positions the questionnaire at the next page.
   */
  public void nextPage() {
    wicketTester.executeAjaxEvent("panel:content:form:nextLink", "onclick");
  }

  /**
   * Indicates whether the specified question is on the current page of the questionnaire.
   * 
   * Note: This method checks all questions and sub-questions, recursively.
   * 
   * @param question question
   * @return <code>true</code> if the question is on the current page
   */
  public boolean isOnCurrentPage(Question question) {
    boolean isOnCurrentPage = false;

    Page currentPage = activeQuestionnaireAdministrationService.getCurrentPage();

    List<Question> questions = currentPage.getQuestions();

    for(Question q : questions) {
      if(isInQuestionSubtree(q, question)) {
        isOnCurrentPage = true;
        break;
      }
    }

    return isOnCurrentPage;
  }

  /**
   * Recursively checks whether a question is contained with the specified question subtree.
   * 
   * @param root of the subtree
   * @param question question
   * @return <code>true</code> if the question is contained in the subtree
   */
  private boolean isInQuestionSubtree(Question root, Question question) {
    if(root.getName().equals(question.getName())) {
      return true;
    }

    List<Question> questions = root.getQuestions();

    for(Question q : questions) {
      if(isInQuestionSubtree(q, question)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Answers the specified question with the specified answer.
   * 
   * Delegates to <code>openAnswer</code> in the case of an "open" answer, and to <code>radioButtonAnswer</code> in
   * the case of a radio button selection.
   * 
   * @param question question
   * @param answer answer
   */
  private void answerQuestionImpl(Question question, CategoryAnswer answer) {
    if(answer.getData() != null) {
      openAnswer(question, answer);
    } else {
      radioButtonAnswer(question, answer);
    }
  }

  /**
   * Given a question and an "open" answer, sets the corresponding input field value and fires an "onblur" Ajax event to
   * trigger persistence.
   * 
   * @param question question
   * @param answer answer
   */
  private void openAnswer(Question question, CategoryAnswer answer) {
    FormTester formTester = wicketTester.newFormTester(getFormPath());

    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + getInputFieldComponentId(question), "onclick");
    formTester.setValue(getInputFieldComponentId(question), answer.getData().getValueAsString());
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + getInputFieldComponentId(question), ONBLUR_EVENT);
  }

  /**
   * Given a question and an radio button answer, selects the corresponding radio button and fires an "onchange" Ajax
   * event to trigger persistence.
   * 
   * @param question question
   * @param answer answer
   */
  private void radioButtonAnswer(Question question, CategoryAnswer answer) {
    FormTester formTester = wicketTester.newFormTester(getFormPath());

    formTester.select(getRadioGroupComponentId(question), getRadioButtonIndex(question, answer));
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + getRadioButtonComponentId(question, answer), ONCHANGE_EVENT);
  }

  private String getFormPath() {
    return "panel:content:form";
  }

  private String getInputFieldComponentId(Question question) {
    // TODO: Generate id based on question. Currently assuming one question per page.
    // Also assuming here that the input field is always the 1st category.
    return "step:panel:questions:1:question:content:categories:category:1:open:open:input:field";
  }

  private String getRadioGroupComponentId(Question question) {
    // TODO: Generate id based on question. Currently assuming one question per page.
    return "step:panel:questions:1:question:content:categories";
  }

  private String getRadioButtonComponentId(Question question, CategoryAnswer answer) {
    // TODO: Generate id based on question. Currently assuming one question per page.
    // return "step:panel:questions:1:question:content:categories:category:2:categoryLabel:input:radio";
    String partBeforeIndex = "step:panel:questions:1:question:content:categories:category";
    String partAfterIndex = "categoryLabel:input:radio";
    int radioButtonIndex = getRadioButtonIndex(question, answer);

    if(radioButtonIndex == -1) {
      Assert.fail("Invalid answer [" + answer.getCategoryName() + "] for question [" + question.getName() + "] (could not locate corresponding radio button)");
    }

    return partBeforeIndex + COMPONENT_ID_SEPARATOR + radioButtonIndex + COMPONENT_ID_SEPARATOR + partAfterIndex;
  }

  private int getRadioButtonIndex(Question question, CategoryAnswer answer) {
    int radioButtonIndex = -1;

    List<Category> categories = question.getCategories();

    for(int i = 0; i < categories.size(); i++) {
      Category category = categories.get(i);

      if(category.getName().equals(answer.getCategoryName())) {
        radioButtonIndex = i + 1; // add 1 because index is 1-based
        break;
      }
    }

    return radioButtonIndex;
  }

  //
  // Abstract Methods
  //

  /**
   * Returns the name of the questionnaire under test.
   * 
   * @return name of questionnaire
   */
  protected abstract String getQuestionnaireName();
}