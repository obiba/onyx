package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;

/**
 * {@link Page} builder, given a {@link Questionnaire} and a current {@link Section}.
 * @author Yannick Marcon
 * 
 */
public class PageBuilder extends AbstractQuestionnaireElementBuilder<Page> {

  /**
   * Constructor using {@link SectionBuilder} to get the {@link Section} it is applied to.
   * @param sectionBuilder
   * @param name
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  private PageBuilder(SectionBuilder sectionBuilder, String name) {
    super(sectionBuilder.getQuestionnaire());
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    if(!checkUniquePageName(name)) {
      throw invalidNameUnicityException(Page.class, name);
    }
    this.element = new Page(name);
    this.questionnaire.addPage(element);
    sectionBuilder.getElement().addPage(element);
  }

  /**
   * Constructor.
   * @param questionnaire
   * @param page
   */
  private PageBuilder(Questionnaire questionnaire, Page page) {
    super(questionnaire);
    this.element = page;
  }

  /**
   * Create a page in a {@link Section} given its name.
   * @param parent
   * @param name
   * @return
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  public static PageBuilder createPage(SectionBuilder parent, String name) {
    return new PageBuilder(parent, name);
  }

  /**
   * Set the given {@link Page} as the current one.
   * @param questionnaire
   * @param page
   * @return
   */
  public static PageBuilder inPage(Questionnaire questionnaire, Page page) {
    return new PageBuilder(questionnaire, page);
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name) {
    return QuestionBuilder.createQuestion(this, name, false);
  }

  /**
   * Add a required, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, boolean multiple) {
    return QuestionBuilder.createQuestion(this, name, multiple);
  }

  /**
   * Check page name unicity.
   * @param name
   * @return
   */
  private boolean checkUniquePageName(String name) {
    return (QuestionnaireFinder.getInstance(questionnaire).findPage(name) == null);
  }
}
