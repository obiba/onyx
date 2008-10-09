package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

public class QuestionnaireBuilder extends AbstractQuestionnaireElementBuilder<Questionnaire> {

  private Map<String, Category> sharedCategories = new HashMap<String, Category>();

  private QuestionnaireBuilder(String name, String version) {
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

  public CategoryBuilder createSharedCategory(String name) {
    CategoryBuilder builder = CategoryBuilder.createCategory(null, name);
    if(sharedCategories.get(name) == null) {
      sharedCategories.put(name, builder.getElement());
    }

    return builder;
  }
  
  public Category getSharedCategory(String name) {
    return sharedCategories.get(name);
  }

}
