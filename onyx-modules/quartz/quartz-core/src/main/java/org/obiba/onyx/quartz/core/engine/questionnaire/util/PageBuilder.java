package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

public class PageBuilder extends AbstractQuestionnaireElementBuilder<Page> {

  private SectionBuilder parent;
  
  private PageBuilder(SectionBuilder sectionBuilder, String name) {
    this.parent = sectionBuilder;
    withPage(name);
  }
  
  public SectionBuilder parent() {
    return parent;
  }
  
  public static PageBuilder createPage(SectionBuilder sectionBuilder, String name) {
    return new PageBuilder(sectionBuilder, name);
  }
  
  /**
   * Add a {@link Page} to current {@link Section} and make it the current {@link Page}.
   * @param name
   * @return
   * @see #getPage()
   */
  public PageBuilder withPage(String name) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    element = new Page(name);
    parent.parent().getElement().addPage(element);
    parent.getElement().addPage(element);

    return this;
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
