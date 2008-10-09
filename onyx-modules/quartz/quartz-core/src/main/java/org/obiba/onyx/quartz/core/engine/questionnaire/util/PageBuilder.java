package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

public class PageBuilder extends AbstractQuestionnaireElementBuilder<Page> {

  private PageBuilder(SectionBuilder sectionBuilder, String name) {
    super(sectionBuilder.getQuestionnaire());
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    this.element = new Page(name);
    this.questionnaire.addPage(element);
    sectionBuilder.getElement().addPage(element);
  }

  private PageBuilder(Questionnaire questionnaire, Page page) {
    super(questionnaire);
    this.element = page;
  }

  public static PageBuilder createPage(SectionBuilder parent, String name) {
    return new PageBuilder(parent, name);
  }

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

}
