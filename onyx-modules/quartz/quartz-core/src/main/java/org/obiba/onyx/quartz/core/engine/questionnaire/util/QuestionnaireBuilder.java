package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.runtime.Version;

/**
 * The {@link Questionnaire} main builder.
 * @author Yannick Marcon
 * 
 */
public class QuestionnaireBuilder extends AbstractQuestionnaireElementBuilder<Questionnaire> {

  /**
   * Constructor.
   * @param name
   * @param version
   * @throws IllegalArgumentException if name does not respect naming pattern and if version does not respect
   * versionning pattern.
   * @see Version
   */
  private QuestionnaireBuilder(String name, String version) {
    super(null);
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    this.element = new Questionnaire(name, version);
    this.questionnaire = this.element;
  }

  private QuestionnaireBuilder(Questionnaire questionnaire) {
    super(questionnaire);
    this.element = questionnaire;
  }

  /**
   * Create a new {@link Questionnaire}.
   * @param name
   * @param version
   * @return
   * @throws IllegalArgumentException if name does not respect naming pattern and if version does not respect
   * versionning pattern.
   * @see Version
   */
  public static QuestionnaireBuilder createQuestionnaire(String name, String version) {
    return new QuestionnaireBuilder(name, version);
  }

  /**
   * Get an instance on the builder given a questionnaire. 
   * @param questionnaire
   * @return
   */
  public static QuestionnaireBuilder getInstance(Questionnaire questionnaire) {
    return new QuestionnaireBuilder(questionnaire);
  }

  /**
   * Add a top level {@link Section} to current {@link Questionnaire}, and make it the current {@link Section}
   * @param name
   * @return
   */
  public SectionBuilder withSection(String name) {
    return SectionBuilder.createSection(this, name);
  }

  /**
   * Position the builder to the {@link Section} with the given name.
   * @param name
   * @return
   * @throws IllegalStateException if no section can be found with this name
   */
  public SectionBuilder inSection(String name) {
    Section section = getElement().findSection(name);
    if(section == null) {
      throw invalidElementNameException(Section.class, name);
    }
    return SectionBuilder.inSection(getQuestionnaire(), section);
  }

  /**
   * Position the builder to the {@link Page} with the given name.
   * @param name
   * @return
   * @throws IllegalStateException if no page can be found with this name
   */
  public PageBuilder inPage(String name) {
    Page page = getElement().findPage(name);
    if(page == null) {
      throw invalidElementNameException(Page.class, name);
    }
    return PageBuilder.inPage(getQuestionnaire(), page);
  }

  /**
   * Position the builder to the {@link Question} with the given name.
   * @param name
   * @return
   * @throws IllegalStateException if no question can be found with this name
   */
  public QuestionBuilder inQuestion(String name) {
    Question question = getElement().findQuestion(name);
    if(question == null) {
      throw invalidElementNameException(Question.class, name);
    }
    return QuestionBuilder.inQuestion(getQuestionnaire(), question);
  }

  /**
   * Create the localization properties for the current {@link Questionnaire}.
   * @return
   */
  public Properties getProperties() {
    Properties properties = new Properties();
    addLocalizableProperties(questionnaire, null, properties);
    for(Section section : questionnaire.getSections()) {
      addSectionProperties(section, properties);
    }
    return properties;
  }

  /**
   * Add localization properties from {@link Section}.
   * @param section
   * @param properties
   */
  private void addSectionProperties(Section section, Properties properties) {
    addLocalizableProperties(section, properties);
    for(Page page : section.getPages()) {
      addLocalizableProperties(page, properties);
      for(Question question : page.getQuestions()) {
        addLocalizableProperties(question, properties);
        for(QuestionCategory questionCategory : question.getQuestionCategories()) {
          addLocalizableProperties(questionCategory.getCategory(), properties);
          addLocalizableProperties(questionCategory, questionCategory.getCategory(), properties);
          if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
            addLocalizableProperties(questionCategory.getCategory().getOpenAnswerDefinition(), properties);
          }
        }
      }
    }
    for(Section s : section.getSections()) {
      addSectionProperties(s, properties);
    }
  }

  /**
   * Shortcut method call.
   * @param localizable
   * @param properties
   */
  private void addLocalizableProperties(ILocalizable localizable, Properties properties) {
    addLocalizableProperties(localizable, null, properties);
  }

  /**
   * For each of the localization keys declared by the {@link ILocalizable} add it to the properties object.
   * Set the value to null by default or to the localization interpolation key. 
   * @param localizable
   * @param interpolationLocalizable
   * @param properties
   */
  private void addLocalizableProperties(ILocalizable localizable, ILocalizable interpolationLocalizable, Properties properties) {
    for(String property : localizable.getProperties()) {
      String key = localizable.getPropertyKey(property);
      if(!properties.contains(key)) {
        properties.put(key, interpolationLocalizable == null ? "" : "${" + interpolationLocalizable.getPropertyKey(property) + "}");
      }
    }
  }
}
