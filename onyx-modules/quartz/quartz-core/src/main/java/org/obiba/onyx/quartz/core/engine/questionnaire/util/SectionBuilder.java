package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

public class SectionBuilder extends AbstractQuestionnaireElementBuilder<Section> {

  private SectionBuilder(QuestionnaireBuilder parent, String name) {
    super(parent.getQuestionnaire());
    this.element = new Section(name);
    this.questionnaire.addSection(element);
  }

  private SectionBuilder(Questionnaire questionnaire, Section section) {
    super(questionnaire);
    this.element = section;
  }

  public static SectionBuilder createSection(QuestionnaireBuilder parent, String name) {
    return new SectionBuilder(parent, name);
  }

  public static SectionBuilder inSection(Questionnaire questionnaire, Section section) {
    return new SectionBuilder(questionnaire, section);
  }

  /**
   * Add a {@link Section} to current {@link Section}, and make it the current {@link Section}.
   * @param name
   * @return
   */
  public SectionBuilder withSection(String name) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    Section section = new Section(name);
    element.addSection(section);
    element = section;

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
