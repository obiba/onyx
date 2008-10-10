package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.AbstractQuestionnaireElementBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IQuestionnairePropertiesWriter;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.PageBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.SectionBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.DefaultQuestionnaireLocalizationVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IQuestionnaireLocalizer;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.QuestionnaireLocalizer;
import org.obiba.runtime.Version;

/**
 * The {@link Questionnaire} main builder.
 * @author Yannick Marcon
 * 
 */
public class QuestionnaireBuilder extends AbstractQuestionnaireElementBuilder<Questionnaire> {

  private IQuestionnaireLocalizer questionnaireLocalizer;

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
  
  public void setQuestionnaireLocalizer(IQuestionnaireLocalizer questionnaireLocalizer) {
    this.questionnaireLocalizer = questionnaireLocalizer;
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
   * Write the questionnaire properties.
   * @param writer
   */
  public void writeProperties(IQuestionnairePropertiesWriter writer) {
    List<String> propertyKeys = new ArrayList<String>();

    // TODO Spring
    QuestionnaireLocalizer localizer = new QuestionnaireLocalizer();
    localizer.setQuestionnaireVisitor(new DefaultQuestionnaireLocalizationVisitor());
    setQuestionnaireLocalizer(localizer);

    addLocalizableProperties(questionnaire, null, writer, propertyKeys);

    for(Category category : questionnaire.findSharedCategories()) {
      addLocalizableProperties(category, null, writer, propertyKeys);
    }

    for(Section section : questionnaire.getSections()) {
      addSectionProperties(section, writer, propertyKeys);
    }
    writer.end();
  }

  /**
   * Create the localization properties for the current {@link Questionnaire}.
   * @return
   */
  public Properties getProperties() {
    final Properties properties = new Properties();

    writeProperties(new IQuestionnairePropertiesWriter() {

      public void endBloc() {
      }

      public void write(String key, String value) {
        properties.put(key, value);
      }

      public void end() {
      }

      public Properties getReference() {
        return null;
      }

    });

    return properties;
  }

  /**
   * Add localization properties from {@link Section}.
   * @param section
   * @param properties
   */
  private void addSectionProperties(Section section, IQuestionnairePropertiesWriter writer, List<String> propertyKeys) {
    addLocalizableProperties(section, writer, propertyKeys);
    for(Page page : section.getPages()) {
      addLocalizableProperties(page, writer, propertyKeys);
      for(Question question : page.getQuestions()) {
        addLocalizableProperties(question, writer, propertyKeys);
        for(QuestionCategory questionCategory : question.getQuestionCategories()) {
          addLocalizableProperties(questionCategory.getCategory(), writer, propertyKeys);
          addLocalizableProperties(questionCategory, questionCategory.getCategory(), writer, propertyKeys);
          if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
            addLocalizableProperties(questionCategory.getCategory().getOpenAnswerDefinition(), writer, propertyKeys);
          }
        }
      }
    }
    for(Section s : section.getSections()) {
      addSectionProperties(s, writer, propertyKeys);
    }
  }

  /**
   * Shortcut method call.
   * @param localizable
   * @param properties
   */
  private void addLocalizableProperties(ILocalizable localizable, IQuestionnairePropertiesWriter writer, List<String> propertyKeys) {
    addLocalizableProperties(localizable, null, writer, propertyKeys);
  }

  /**
   * For each of the localization keys declared by the {@link ILocalizable} add it to the properties object. Set the
   * value to null by default or to the localization interpolation key.
   * @param localizable
   * @param interpolationLocalizable
   * @param writer
   */
  private void addLocalizableProperties(ILocalizable localizable, ILocalizable interpolationLocalizable, IQuestionnairePropertiesWriter writer, List<String> propertyKeys) {
    boolean written = false;
    for(String property : questionnaireLocalizer.getProperties(localizable)) {
      String key = questionnaireLocalizer.getPropertyKey(localizable, property);
      if(!propertyKeys.contains(key)) {
        Properties ref = writer.getReference();
        if(ref != null && ref.containsKey(key)) {
          writer.write(key, ref.getProperty(key));
        } else {
          writer.write(key, interpolationLocalizable == null ? "" : "${" + questionnaireLocalizer.getPropertyKey(interpolationLocalizable, property) + "}");
        }
        propertyKeys.add(key);
        written = true;
      }
    }
    if(written) writer.endBloc();
  }
}
