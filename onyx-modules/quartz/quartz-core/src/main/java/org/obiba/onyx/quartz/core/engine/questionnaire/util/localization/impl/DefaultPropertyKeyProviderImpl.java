/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyNamingStrategy;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.util.data.Data;

public class DefaultPropertyKeyProviderImpl implements IPropertyKeyProvider, IVisitor {

  private IPropertyKeyNamingStrategy propertyKeyNamingStrategy;

  //
  // Questionnaire element properties, with their default values.
  //
  private List<String> questionnaireProperties;

  private List<String> sectionProperties;

  private List<String> pageProperties;

  private List<String> questionProperties;

  private List<String> categoryProperties;

  private List<String> openAnswerDefinitionProperties;

  private List<String> variableProperties;

  /**
   * The properties for the visited localizable.
   */
  private List<String> properties;

  public DefaultPropertyKeyProviderImpl() {
    super();
    this.questionnaireProperties = new ArrayList<String>(Arrays.asList("label", "description", "labelNext", "labelPrevious", "labelStart", "labelFinish", "labelInterrupt", "labelResume", "labelCancel", "conclusion"));
    this.sectionProperties = new ArrayList<String>(Arrays.asList("label"));
    this.pageProperties = new ArrayList<String>(Arrays.asList("label"));
    this.questionProperties = new ArrayList<String>(Arrays.asList("label", "instructions", "media", "caption", "help", "specifications", "categoryOrder"));
    this.categoryProperties = new ArrayList<String>(Arrays.asList("label"));
    this.openAnswerDefinitionProperties = new ArrayList<String>(Arrays.asList("label", "unitLabel"));
    this.propertyKeyNamingStrategy = new DefaultPropertyKeyNamingStrategy();
    this.variableProperties = new ArrayList<String>(Arrays.asList("label"));
  }

  @Override
  public List<String> getProperties(IQuestionnaireElement localizable) {
    localizable.accept(this);
    return properties;
  }

  @Override
  public String getPropertyKey(IQuestionnaireElement localizable, String property) {
    return propertyKeyNamingStrategy.getPropertyKey(localizable, property);
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

  public List<String> getQuestionnaireProperties() {
    return questionnaireProperties;
  }

  public List<String> getSectionProperties() {
    return sectionProperties;
  }

  public List<String> getPageProperties() {
    return pageProperties;
  }

  public List<String> getQuestionProperties() {
    return questionProperties;
  }

  public List<String> getCategoryProperties() {
    return categoryProperties;
  }

  protected List<String> getOpenAnswerDefinitionProperties() {
    return openAnswerDefinitionProperties;
  }

  public List<String> getVariableProperties() {
    return variableProperties;
  }

  //
  // visitor methods
  //
  @Override
  public void visit(Questionnaire questionnaire) {
    properties = getQuestionnaireProperties();
  }

  @Override
  public void visit(Section section) {
    properties = getSectionProperties();
  }

  @Override
  public void visit(Page page) {
    properties = getPageProperties();
  }

  @Override
  public void visit(Question question) {
    properties = getQuestionProperties();
  }

  @Override
  public void visit(QuestionCategory questionCategory) {
    visit(questionCategory.getCategory());
  }

  @Override
  public void visit(Category category) {
    properties = getCategoryProperties();
  }

  @Override
  public void visit(OpenAnswerDefinition openAnswerDefinition) {
    properties = new ArrayList<String>(getOpenAnswerDefinitionProperties());
    for(Data value : openAnswerDefinition.getDefaultValues()) {
      properties.add(value.getValueAsString());
    }
  }

  public void setPropertyKeyNamingStrategy(IPropertyKeyNamingStrategy propertyKeyNamingStrategy) {
    this.propertyKeyNamingStrategy = propertyKeyNamingStrategy;
  }

  @Override
  public void visit(Variable variable) {
    // TODO Auto-generated method stub

  }

}
