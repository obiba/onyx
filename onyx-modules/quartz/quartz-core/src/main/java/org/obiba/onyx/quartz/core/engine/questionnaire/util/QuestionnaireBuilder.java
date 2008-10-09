package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

public class QuestionnaireBuilder extends AbstractQuestionnaireElementBuilder<Questionnaire> {

  private QuestionnaireBuilder(String name, String version) {
    super(null);
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    this.element = new Questionnaire(name, version);
    this.questionnaire = this.element;
  }

  /**
   * Create a new {@link Questionnaire}.
   * @param name
   * @param version
   * @return
   */
  public static QuestionnaireBuilder createQuestionnaire(String name, String version) {
    return new QuestionnaireBuilder(name, version);
  }

  /**
   * Add a top level {@link Section} to current {@link Questionnaire}, and make it the current {@link Section}
   * @param name
   * @return
   * @see #getSection()
   */
  public SectionBuilder withSection(String name) {
    return SectionBuilder.createSection(this, name);
  }

  public SectionBuilder inSection(String name) {
    Section section = getElement().findSection(name);
    if(section == null) {
      throw invalidElementNameException(Section.class, name);
    }
    return SectionBuilder.inSection(getQuestionnaire(), section);
  }

  public PageBuilder inPage(String name) {
    Page page = getElement().findPage(name);
    if(page == null) {
      throw invalidElementNameException(Page.class, name);
    }
    return PageBuilder.inPage(getQuestionnaire(), page);
  }

  public QuestionBuilder inQuestion(String name) {
    Question question = getElement().findQuestion(name);
    if(question == null) {
      throw invalidElementNameException(Question.class, name);
    }
    return QuestionBuilder.inQuestion(getQuestionnaire(), question);
  }

}
