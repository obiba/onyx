package org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.util.data.Data;

public class DefaultPropertyKeyProviderImpl implements IPropertyKeyProvider, IVisitor {

  //
  // Questionnaire element properties, with their default values.
  //
  private List<String> questionnaireProperties = Arrays.asList("label", "description", "labelNext", "imageNext", "labelPrevious", "imagePrevious", "labelStart", "labelFinish", "labelInterrupt", "labelResume", "labelCancel");

  private List<String> sectionProperties = Arrays.asList("label");

  private List<String> pageProperties = Arrays.asList("label");

  private List<String> questionProperties = Arrays.asList("label", "instructions", "caption", "help", "image");

  private List<String> categoryProperties = Arrays.asList("label", "image");

  private List<String> openAnswerDefinitionProperties = new ArrayList<String>(Arrays.asList("label", "unitLabel"));

  /**
   * The property requested for.
   */
  private String property;

  /**
   * The properties for the visited localizable.
   */
  private List<String> properties;

  public List<String> getProperties(ILocalizable localizable) {
    this.property = null;
    localizable.accept(this);
    return properties;
  }

  public String getPropertyKey(ILocalizable localizable, String property) {
    this.property = property;
    localizable.accept(this);
    return localizable.getClass().getSimpleName() + "." + localizable.getName() + "." + property;
  }

  //
  // Questionnaire element properties setters and getters.
  // 
  public void setQuestionnaireProperties(List<String> questionnaireProperties) {
    this.questionnaireProperties = questionnaireProperties;
  }

  public void setSectionProperties(List<String> sectionProperties) {
    this.sectionProperties = sectionProperties;
  }

  public void setPageProperties(List<String> pageProperties) {
    this.pageProperties = pageProperties;
  }

  public void setQuestionProperties(List<String> questionProperties) {
    this.questionProperties = questionProperties;
  }

  public void setCategoryProperties(List<String> categoryProperties) {
    this.categoryProperties = categoryProperties;
  }

  public void setOpenAnswerDefinitionProperties(List<String> openAnswerDefinitionProperties) {
    this.openAnswerDefinitionProperties = openAnswerDefinitionProperties;
  }

  protected List<String> getQuestionnaireProperties() {
    return questionnaireProperties;
  }

  protected List<String> getSectionProperties() {
    return sectionProperties;
  }

  protected List<String> getPageProperties() {
    return pageProperties;
  }

  protected List<String> getQuestionProperties() {
    return questionProperties;
  }

  protected List<String> getCategoryProperties() {
    return categoryProperties;
  }

  protected List<String> getOpenAnswerDefinitionProperties() {
    return openAnswerDefinitionProperties;
  }

  //
  // visitor methods
  //
  public void visit(Questionnaire questionnaire) {
    properties = getQuestionnaireProperties();

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(questionnaire);
    }
  }

  public void visit(Section section) {
    properties = getSectionProperties();

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(section);
    }
  }

  public void visit(Page page) {
    properties = getPageProperties();

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(page);
    }
  }

  public void visit(Question question) {
    properties = getQuestionProperties();

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(question);
    }
  }

  public void visit(QuestionCategory questionCategory) {
    visit(questionCategory.getCategory());
  }

  public void visit(Category category) {
    properties = getCategoryProperties();

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(category);
    }
  }

  public void visit(OpenAnswerDefinition openAnswerDefinition) {
    properties = new ArrayList<String>(getOpenAnswerDefinitionProperties());
    for(Data value : openAnswerDefinition.getDefaultValues()) {
      properties.add(value.getValueAsString());
    }

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(openAnswerDefinition);
    }
  }

  /**
   * Exception if a requested property is not part of questionnaire element allowed properties.
   * @param localizable
   * @return
   */
  private IllegalArgumentException invalidPropertyException(ILocalizable localizable) {
    return new IllegalArgumentException("Invalid property for class " + localizable.getClass().getName() + ": " + property);
  }

}
