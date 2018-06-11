/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.service.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;

/**
 *
 */
public class DefaultActiveQuestionnaireAdministrationServiceImplTest {
  //
  // Instance Variables
  //

  private DefaultActiveQuestionnaireAdministrationServiceImpl sut;

  private AnswerFinderUpdater mockAnswerFinder;

  private PersistenceManager mockPersistenceManager;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    mockAnswerFinder = createMock(AnswerFinderUpdater.class);
    mockPersistenceManager = createMock(PersistenceManager.class);

    sut = createSUT(mockAnswerFinder);
    sut.setPersistenceManager(mockPersistenceManager);
  }

  //
  // Test Methods
  //

  @Test
  public void testQuestionIsAnsweredByNoAnswerWhenAllAnswersAreDeleted() {
    // Create a question that has been answered.
    Question question = createQuestionWithNoAnswerCategory();
    List<CategoryAnswer> categoryAnswers = createCategoryAnswers("ca1", "ca2");
    expect(mockAnswerFinder.findAnswers(question)).andReturn(categoryAnswers).times(1).andReturn(new ArrayList<CategoryAnswer>()).times(1);

    // Delete both answers.
    mockPersistenceManager.delete(categoryAnswers.get(0));
    expectLastCall().atLeastOnce();
    mockPersistenceManager.delete(categoryAnswers.get(1));
    expectLastCall().atLeastOnce();

    // Make sure question is answered using a "noAnswer" category.
    expect(mockAnswerFinder.answer(question, question.getNoAnswerQuestionCategory())).andReturn(null);

    replay(mockAnswerFinder, mockPersistenceManager);

    // Exercise
    sut.deleteAnswers(question);

    // Verify
    verify(mockAnswerFinder, mockPersistenceManager);
  }

  @Test
  public void testDeleteNoAnswerIfRealAnswerIsProvided() {

    // Create a question answered a "noAnswer" category that has not been answered.
    Question question = createQuestionWithNoAnswerCategory();
    List<CategoryAnswer> categoryAnswer = createCategoryAnswers("noAnswerCategory");
    expect(mockAnswerFinder.findAnswers(question)).andReturn(categoryAnswer);

    expect(mockAnswerFinder.findAnswer(question)).andReturn(new QuestionAnswer());
    expect(mockPersistenceManager.matchOne(anyObject())).andReturn(null).atLeastOnce();
    expect(mockPersistenceManager.save(anyObject())).andReturn(null).atLeastOnce();

    // Make sure the "noAnswer" CategoryAnswer is deleted, when question is answered.
    mockPersistenceManager.delete(categoryAnswer.get(0));

    replay(mockAnswerFinder, mockPersistenceManager);

    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setQuestion(question);
    questionCategory.setCategory(new Category("testCategory"));
    sut.answer(questionCategory);

    // Verify
    verify(mockAnswerFinder, mockPersistenceManager);
  }

  @Test
  public void testNoAnswerCategoryIsUsedIfNoRealAnswerIsProvided() {

    // Create a question with a "noAnswer" category.
    Question question = createQuestionWithNoAnswerCategory();
    expect(mockAnswerFinder.findAnswer(question)).andReturn(null).atLeastOnce();
    expect(mockAnswerFinder.findAnswers(question)).andReturn(createCategoryAnswers()).atLeastOnce();

    expect(mockPersistenceManager.save(anyObject())).andReturn(null).atLeastOnce();

    // Make sure question is answered using a "noAnswer" category.
    expect(mockAnswerFinder.answer(question, question.getNoAnswerQuestionCategory())).andReturn(null);

    replay(mockAnswerFinder, mockPersistenceManager);

    sut.setActiveAnswers(question, true);

    verify(mockAnswerFinder, mockPersistenceManager);
  }

  //
  // Helper Methods
  //

  private DefaultActiveQuestionnaireAdministrationServiceImpl createSUT(final AnswerFinderUpdater finder) {
    return new DefaultActiveQuestionnaireAdministrationServiceImpl() {

      public List<OpenAnswer> findOpenAnswers(Question question, Category category) {
        return finder.findOpenAnswers(question, category);
      }

      public OpenAnswer findOpenAnswer(String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName) {
        return finder.findOpenAnswer(questionnaireName, questionName, categoryName, openAnswerDefinitionName);
      }

      public OpenAnswer findOpenAnswer(Question question, Category category, OpenAnswerDefinition openAnswerDefinition) {
        return finder.findOpenAnswer(question, category, openAnswerDefinition);
      }

      public OpenAnswer findOpenAnswer(QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition) {
        return finder.findOpenAnswer(questionCategory, openAnswerDefinition);
      }

      public List<CategoryAnswer> findAnswers(Question question) {
        return finder.findAnswers(question);
      }

      public CategoryAnswer findAnswer(String questionnaireName, String questionName, String categoryName) {
        return finder.findAnswer(questionnaireName, questionName, categoryName);
      }

      public CategoryAnswer findAnswer(Question question, Category category) {
        return finder.findAnswer(question, category);
      }

      public CategoryAnswer findAnswer(Question question, QuestionCategory questionCategory) {
        return finder.findAnswer(question, questionCategory);
      }

      public CategoryAnswer findAnswer(QuestionCategory questionCategory) {
        return finder.findAnswer(questionCategory);
      }

      public List<CategoryAnswer> findActiveAnswers(String questionnaireName, String questionName) {
        return finder.findActiveAnswers(questionnaireName, questionName);
      }

      public List<CategoryAnswer> findActiveAnswers(Question question) {
        return finder.findActiveAnswers(question);
      }

      protected QuestionAnswer findAnswer(Question question) {
        return finder.findAnswer(question);
      }

      public CategoryAnswer answer(Question question, QuestionCategory questionCategory) {
        return finder.answer(question, questionCategory);
      }

      @Override
      public QuestionnaireParticipant getQuestionnaireParticipant() {
        QuestionnaireParticipant questionnaireParticipant = new QuestionnaireParticipant();
        questionnaireParticipant.setParticipant(new Participant());
        questionnaireParticipant.setQuestionnaireName("TestQuestionnaire");
        return questionnaireParticipant;
      }

    };
  }

  private Question createQuestionWithNoAnswerCategory() {
    Question question = new Question("testQuestion");

    QuestionCategory questionCategory = new QuestionCategory();
    Category category = new Category("noAnswerCategory");
    category.setNoAnswer(true);
    questionCategory.setCategory(category);
    question.addQuestionCategory(questionCategory);

    return question;
  }

  private List<CategoryAnswer> createCategoryAnswers(String... categoryAnswerNames) {
    List<CategoryAnswer> categoryAnswers = new ArrayList<CategoryAnswer>();
    for(String name : categoryAnswerNames) {
      CategoryAnswer ca = new CategoryAnswer();
      ca.setCategoryName(name);
      categoryAnswers.add(ca);
    }

    return categoryAnswers;
  }

  //
  // Inner Classes
  //

  interface AnswerFinderUpdater {

    public List<OpenAnswer> findOpenAnswers(Question question, Category category);

    public OpenAnswer findOpenAnswer(String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName);

    public OpenAnswer findOpenAnswer(Question question, Category category, OpenAnswerDefinition openAnswerDefinition);

    public OpenAnswer findOpenAnswer(QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition);

    public List<CategoryAnswer> findAnswers(Question question);

    public CategoryAnswer findAnswer(String questionnaireName, String questionName, String categoryName);

    public CategoryAnswer findAnswer(Question question, Category category);

    public CategoryAnswer findAnswer(Question question, QuestionCategory questionCategory);

    public CategoryAnswer findAnswer(QuestionCategory questionCategory);

    public List<CategoryAnswer> findActiveAnswers(String questionnaireName, String questionName);

    public List<CategoryAnswer> findActiveAnswers(Question question);

    public QuestionAnswer findAnswer(Question question);

    public CategoryAnswer answer(Question question, QuestionCategory questionCategory);

  }
}
