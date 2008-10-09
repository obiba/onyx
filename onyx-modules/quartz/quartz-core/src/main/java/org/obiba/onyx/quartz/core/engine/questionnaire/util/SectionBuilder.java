package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

public class SectionBuilder extends AbstractQuestionnaireElementBuilder<Section> {

  private QuestionnaireBuilder parent;

  private SectionBuilder(QuestionnaireBuilder parent, String name) {
    this.parent = parent;
    withSection(name);
  }

  public static SectionBuilder createSection(QuestionnaireBuilder parent, String name) {
    return new SectionBuilder(parent, name);
  }

  public QuestionnaireBuilder parent() {
    return parent;
  }

  /**
   * Add a top level {@link Section} to current {@link Questionnaire}, and make it the current {@link Section}
   * @param name
   * @return
   * @see #getSection()
   */
  public SectionBuilder withSection(String name) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    element = new Section(name);
    parent.getElement().addSection(element);

    return this;
  }

  /**
   * Add sub {@link Section} to given {@link Section} and make it the current {@link Section}.
   * @param section
   * @param name
   * @return
   * @see #getSection()
   */
  public SectionBuilder withSection(Section section, String name) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    Section subSection = new Section(name);
    section.addSection(subSection);
    element = subSection;

    return this;
  }

  /**
   * Add a {@link Page} to current {@link Section} and make it the current {@link Page}.
   * @param name
   * @return
   * @see #getPage()
   */
  public PageBuilder withPage(String name) {
    return PageBuilder.createPage(this, name);
  }
  
}
