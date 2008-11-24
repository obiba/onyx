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

import org.apache.wicket.markup.html.form.Form;
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
   * Returns the specified page.
   * 
   * @param name page name
   */
  public Page getPage(String name) {
    return questionnaireFinder.findPage(name);
  }

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
    if(answer == null) return;

    if(isOnCurrentPage(question)) {
      // Answer the question.
      addComment(question);
      answerQuestionImpl(question, answer);

    } else {
      // The question might be on the next page. So advance to the next page and try again.
      nextPage();

      if(isOnCurrentPage(question)) {
        // Answer the question.
        addComment(question);
        answerQuestionImpl(question, answer);
      } else {
        // Not on this page either. Stop looking and treat this as a failure.
        Assert.fail("Failed to answer question " + question.getName() + " (not found)");
      }
    }
  }

  protected void addComment(final Question question) {

    // TODO The source of the comment should be the answerProvider, instead of an hard coded comment.
    addComment(question, "test comment");
  }

  /**
   * Add a comment to a question.
   * 
   * @param question
   * @param string
   */
  @SuppressWarnings("serial")
  protected void addComment(final Question question, String comment) {
    int index = question.getPage().getQuestions().indexOf(question) + 1;

    // Display comment modal panel.
    wicketTester.executeAjaxEvent("panel:content:form:step:panel:questions:" + index + ":question:addComment", "onclick");

    // Set comment in form.
    FormTester commentForm = wicketTester.newFormTester("panel:content:form:step:panel:questions:" + index + ":question:addCommentModal:content:commentForm");
    commentForm.setValue("newComment", comment);

    // Submit comment.
    wicketTester.executeAjaxEvent("panel:content:form:step:panel:questions:" + index + ":question:addCommentModal:content:commentForm:saveComment", "onclick");

    // Make sure that comment is saved.
    Assert.assertEquals(comment, activeQuestionnaireAdministrationService.getComment(question));

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
    Page currentPage = null;

    while((currentPage = activeQuestionnaireAdministrationService.getCurrentPage()) != null) {
      log.info("Current page is " + currentPage.getName());

      if(containsNonBoilerplateQuestion(currentPage)) {
        answerQuestionsOnCurrentPage(answerProvider, toQuestion, inclusive);
      } else {
        // Need to create a new FormTester, otherwise WicketTester will attempt
        // to submit form data from the previous page...
        wicketTester.newFormTester(this.getFormPath());
      }

      if(isOnCurrentPage(toQuestion)) {
        break;
      }

      nextPage();
    }
  }

  /**
   * Equivalent to <code>answerQuestionsUpTo(answerProvider, toQuestion, true)</code>.
   * 
   * @param answerProvider answer provider
   * @param toQuestion question to stop at (inclusive)
   */
  public void answerQuestionsUpTo(AnswerProvider answerProvider, Question toQuestion) {
    answerQuestionsUpTo(answerProvider, toQuestion, true);
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

      if(!question.isBoilerPlate() && question.isToBeAnswered(activeQuestionnaireAdministrationService) && !question.hasDataSource()) {
        answerQuestion(question, answerProvider.getAnswer(question));
        log.info("Answered question " + question.getName());

        List<Question> subQuestions = question.getQuestions();

        for(Question subQuestion : subQuestions) {
          atToQuestion = subQuestion.getName().equals(toQuestion.getName());

          if(atToQuestion && !inclusive) {
            return;
          }

          if(!question.isBoilerPlate() && question.isToBeAnswered(activeQuestionnaireAdministrationService)) {
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

  public void assertCurrentPage(Page page) {
    Page currentPage = activeQuestionnaireAdministrationService.getCurrentPage();
    Assert.assertTrue((currentPage == null && page == null) || (currentPage != null && page != null && currentPage.getName().equals(page.getName())));
  }

  public void assertNextPage(Page page) {
    Page nextPage = activeQuestionnaireAdministrationService.nextPage();
    Assert.assertTrue((nextPage == null && page == null) || (nextPage != null && page != null && nextPage.getName().equals(page.getName())));
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
    // Need to create a new FormTester, otherwise WicketTester will attempt
    // to submit form data from the previous page...
    wicketTester.newFormTester(this.getFormPath());
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
   * Indicates whether a page contains at least one non-boilerplate question.
   * 
   * @param page page
   * @return <code>true</code> if at least one question on the page is not a boilerplate question
   */
  private boolean containsNonBoilerplateQuestion(Page page) {
    List<Question> questions = page.getQuestions();

    for(Question question : questions) {
      if(!question.isBoilerPlate()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Answers the specified question with the specified answer.
   * 
   * Delegates to <code>openAnswer</code> in the case of an "open" answer, and to
   * <code>radio/checkBoxButtonAnswer</code> in the case of a radio/checkBox button selection.
   * 
   * @param question question
   * @param answer answer
   */
  private void answerQuestionImpl(Question question, CategoryAnswer answer) {
    if(question.getUIFactoryName().equals("quartz.DropDownQuestionPanelFactory")) {
      // TODO open answer for dropdown
      dropDownAnswer(question, answer);
    } else if(question.getParentQuestion() == null) {
      // default question
      if(answer.getOpenAnswers() != null && answer.getOpenAnswers().size() > 0) {
        openAnswer(question, answer);
      } else if(question.isMultiple()) {
        checkBoxButtonAnswer(question, answer);
      } else {
        radioButtonAnswer(question, answer);
      }
    } else if(question.getParentQuestion().getCategories().size() == 0) {
      throw new UnsupportedOperationException("Test of sub-questions not supported yet");
      // TODO deal with sub question markup
      // if(question.isMultiple()) {
      // checkBoxButtonAnswer(question, answer);
      // } else {
      // radioButtonAnswer(question, answer);
      // }
    } else if(question.getCategories().size() == 0) {
      if(answer.getOpenAnswers() != null && answer.getOpenAnswers().size() > 0) {
        openArrayAnswer(question, answer);
      } else if(question.getParentQuestion().isMultiple()) {
        checkBoxArrayButtonAnswer(question, answer);
      } else {
        radioArrayButtonAnswer(question, answer);
      }
    } else {
      // joined
      throw new UnsupportedOperationException("Test of question with joined categories not supported yet");
    }
  }

  /**
   * Get the path to the form component.
   * @return
   */
  private String getFormPath() {
    return "panel:content:form";
  }

  private Form getForm() {
    return (Form) wicketTester.getComponentFromLastRenderedPage("panel:content:form");
  }

  //
  // default question
  //

  /**
   * Given a question and an radio button answer, selects the corresponding radio button and fires an "onchange" Ajax
   * event to trigger persistence.
   * 
   * @param question question
   * @param answer answer
   */
  private void radioButtonAnswer(Question question, CategoryAnswer answer) {
    FormTester formTester = wicketTester.newFormTester(getFormPath());

    formTester.select(getCategoryGroupComponentId(question), getCategoryIndex(question, answer));
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + getRadioButtonComponentId(question, answer), ONCHANGE_EVENT);
  }

  /**
   * Given a question and an checkbox button answer, selects the corresponding checkbox button and fires an "onchange"
   * Ajax event to trigger persistence.
   * 
   * @param question question
   * @param answer answer
   */
  private void checkBoxButtonAnswer(Question question, CategoryAnswer answer) {
    FormTester formTester = wicketTester.newFormTester(getFormPath());

    formTester.select(getCategoryGroupComponentId(question), getCategoryIndex(question, answer));
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + getCheckBoxButtonComponentId(question, answer), ONCHANGE_EVENT);
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

    Category category = question.findCategory(answer.getCategoryName());
    String openPath = getInputFieldComponentId(question, category);
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + openPath, "onclick");
    formTester.setValue(openPath, answer.getOpenAnswers().get(0).getData().getValueAsString());
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + openPath, ONBLUR_EVENT);
  }

  private String getInputFieldComponentId(Question question, Category category) {
    int categoryIndex = question.getCategories().indexOf(category) + 1;
    return getCategoryGroupComponentId(question) + ":category:" + categoryIndex + ":input:open:open:input:field";
  }

  private String getCategoryGroupComponentId(Question question) {
    int index = question.getPage().getQuestions().indexOf(question) + 1;
    return "step:panel:questions:" + index + ":question:content:categories";
  }

  private String getRadioButtonComponentId(Question question, CategoryAnswer answer) {
    return getButtonComponentId(question, answer, "radio");
  }

  private String getCheckBoxButtonComponentId(Question question, CategoryAnswer answer) {
    return getButtonComponentId(question, answer, "checkbox");
  }

  private String getButtonComponentId(Question question, CategoryAnswer answer, String type) {
    String partBeforeIndex = getCategoryGroupComponentId(question) + ":category";
    String partAfterIndex = "input:categoryLabel:" + type;
    int buttonIndex = getCategoryIndex(question, answer) + 1; // 1-based.

    if(buttonIndex == -1) {
      Assert.fail("Invalid answer [" + answer.getCategoryName() + "] for question [" + question.getName() + "] (could not locate corresponding " + type + " button)");
    }

    return partBeforeIndex + COMPONENT_ID_SEPARATOR + buttonIndex + COMPONENT_ID_SEPARATOR + partAfterIndex;
  }

  //
  // dropdown question
  // 

  private void dropDownAnswer(Question question, CategoryAnswer answer) {
    FormTester formTester = wicketTester.newFormTester(getFormPath());

    formTester.select(getDropDownComponentId(question), getCategoryIndex(question, answer));
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + getDropDownComponentId(question), ONCHANGE_EVENT);
  }

  private String getDropDownComponentId(Question question) {
    int index = question.getPage().getQuestions().indexOf(question) + 1;
    return "step:panel:questions:" + index + ":question:content:questionCategories";
  }

  //
  // shared categories question array
  //

  private void checkBoxArrayButtonAnswer(Question question, CategoryAnswer answer) {
    FormTester formTester = wicketTester.newFormTester(getFormPath());

    formTester.select(getArrayCategoryGroupComponentId(question), getCategoryIndex(question.getParentQuestion(), answer));
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + getCheckBoxArrayButtonComponentId(question, answer), ONCHANGE_EVENT);
  }

  private void radioArrayButtonAnswer(Question question, CategoryAnswer answer) {
    FormTester formTester = wicketTester.newFormTester(getFormPath());

    formTester.select(getArrayCategoryGroupComponentId(question), getCategoryIndex(question.getParentQuestion(), answer));
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + getRadioArrayButtonComponentId(question, answer), ONCHANGE_EVENT);
  }

  private String getArrayCategoryGroupComponentId(Question question) {
    Question parent = question.getParentQuestion();
    int parentIndex = parent.getPage().getQuestions().indexOf(parent) + 1;
    int index = parent.getQuestions().indexOf(question) + 1;
    return "step:panel:questions:" + parentIndex + ":question:content:array:rows:rows:" + index + ":group";
  }

  private String getRadioArrayButtonComponentId(Question question, CategoryAnswer answer) {
    return getArrayButtonComponentId(question, answer, "radio");
  }

  private String getCheckBoxArrayButtonComponentId(Question question, CategoryAnswer answer) {
    return getArrayButtonComponentId(question, answer, "checkbox");
  }

  private String getArrayButtonComponentId(Question question, CategoryAnswer answer, String type) {
    String partBeforeIndex = getArrayCategoryGroupComponentId(question) + ":cells";
    String partAfterIndex = "cell:categoryLabel:" + type;
    int buttonIndex = getCategoryIndex(question.getParentQuestion(), answer) + 2; // 2-based (first column is
    // sub-question labels).

    if(buttonIndex == -1) {
      Assert.fail("Invalid answer [" + answer.getCategoryName() + "] for question [" + question.getName() + "] (could not locate corresponding " + type + " button)");
    }

    return partBeforeIndex + COMPONENT_ID_SEPARATOR + buttonIndex + COMPONENT_ID_SEPARATOR + partAfterIndex;
  }

  private void openArrayAnswer(Question question, CategoryAnswer answer) {
    FormTester formTester = wicketTester.newFormTester(getFormPath());

    Category category = question.getParentQuestion().findCategory(answer.getCategoryName());
    String openPath = getArrayInputFieldComponentId(question, category);
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + openPath, "onclick");
    formTester.setValue(openPath, answer.getOpenAnswers().get(0).getData().getValueAsString());
    wicketTester.executeAjaxEvent(getFormPath() + COMPONENT_ID_SEPARATOR + openPath, ONBLUR_EVENT);
  }

  private String getArrayInputFieldComponentId(Question question, Category category) {
    Question parent = question.getParentQuestion();
    int categoryIndex = parent.getCategories().indexOf(category) + 2; // 2-based

    return getArrayCategoryGroupComponentId(question) + ":cells:" + categoryIndex + ":cell:open:open:input:field";
  }

  /**
   * Get the category index in given question.
   * @param question
   * @param answer
   * @return
   */
  private int getCategoryIndex(Question question, CategoryAnswer answer) {
    Category category = question.findCategory(answer.getCategoryName());
    return question.getCategories().indexOf(category);
  }

  /**
   * Returns the base path for an answer provider resource (i.e., classpath resource).
   * 
   * The base path is <code>questionnaires/[questionnaireName]/[questionnaireVersion]</code>, where
   * "[questionnaireName]" is the value returned by <code>getQuestionnaireName</code> and "[questionnaireVersion]" is
   * the value returned by <code>getQuestionnaireVersion</code>.
   * 
   * @return answer provider resource path
   */
  protected String getAnswerProviderResourcePath() {
    return "questionnaires" + "/" + getQuestionnaireName() + "/" + getQuestionnaireVersion();
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

  /**
   * Returns the version of the questionnaire under test.
   * 
   * @return version of questionnaire
   */
  protected abstract String getQuestionnaireVersion();
}