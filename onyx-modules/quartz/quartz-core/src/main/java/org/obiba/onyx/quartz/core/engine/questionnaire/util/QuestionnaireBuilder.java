/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.Properties;

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.AbstractQuestionnaireElementBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.ConditionBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IPropertyKeyWriter;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.OpenAnswerDefinitionBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.PageBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.PropertyKeyWriterVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.SectionBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl.PropertiesPropertyKeyWriterImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.SimplifiedUIPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.SimplifiedPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.SimplifiedQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionPanelFactory;
import org.obiba.runtime.Version;

/**
 * The {@link Questionnaire} main builder.
 * @author Yannick Marcon
 * 
 */
public class QuestionnaireBuilder extends AbstractQuestionnaireElementBuilder<Questionnaire> {

  private IPropertyKeyProvider propertyKeyProvider;

  /**
   * Constructor.
   * @param name
   * @param version
   * @throws IllegalArgumentException if name does not respect naming pattern and if version does not respect version
   * pattern.
   * @see Version
   */
  private QuestionnaireBuilder(String name, String version) {
    super(null, DefaultPageLayoutFactory.class, DefaultQuestionPanelFactory.class);
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    this.element = new Questionnaire(name, version);
    this.questionnaire = this.element;
    this.propertyKeyProvider = new DefaultPropertyKeyProviderImpl();
  }

  private QuestionnaireBuilder(Questionnaire questionnaire) {
    super(questionnaire, DefaultPageLayoutFactory.class, DefaultQuestionPanelFactory.class);
    this.element = questionnaire;
    this.propertyKeyProvider = new DefaultPropertyKeyProviderImpl();
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
    Section section = QuestionnaireFinder.getInstance(questionnaire).findSection(name);
    if(section == null) {
      throw invalidElementNameException(Section.class, name);
    }
    return SectionBuilder.inSection(this, section);
  }

  /**
   * Position the builder to the {@link Page} with the given name.
   * @param name
   * @return
   * @throws IllegalStateException if no page can be found with this name
   */
  public PageBuilder inPage(String name) {
    Page page = QuestionnaireFinder.getInstance(questionnaire).findPage(name);
    if(page == null) {
      throw invalidElementNameException(Page.class, name);
    }
    return PageBuilder.inPage(this, page);
  }

  /**
   * Position the builder to the {@link Question} with the given name.
   * @param name
   * @return
   * @throws IllegalStateException if no question can be found with this name
   */
  public QuestionBuilder inQuestion(String name) {
    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion(name);
    if(question == null) {
      throw invalidElementNameException(Question.class, name);
    }
    return QuestionBuilder.inQuestion(this, question);
  }

  /**
   * Position the builder to the {@link OpenAnswerDefinition} with the given name.
   * @param name
   * @return
   * @throws IllegalStateException if no OpenAnswerDefinition can be found with this name
   */
  public OpenAnswerDefinitionBuilder inOpenAnswerDefinition(String name) {
    OpenAnswerDefinition openAnswerDefinition = QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition(name);
    if(openAnswerDefinition == null) {
      throw invalidElementNameException(OpenAnswerDefinition.class, name);
    }
    return OpenAnswerDefinitionBuilder.inOpenAnswerDefinition(this, openAnswerDefinition);
  }

  /**
   * Build a data source representing the question answering in current questionnaire.
   * @param question
   * @return
   */
  public IDataSource newDataSource(String question) {
    return ConditionBuilder.createQuestionCondition(this, question, null, null).getElement();
  }

  /**
   * Build a data source representing the category selection in current questionnaire.
   * @param question
   * @param category
   * @return
   */
  public IDataSource newDataSource(String question, String category) {
    return ConditionBuilder.createQuestionCondition(this, question, category, null).getElement();
  }

  /**
   * Build a data source that gives the open answer in current questionnaire.
   * @param question
   * @param category
   * @param openAnswer
   * @return
   */
  public IDataSource newDataSource(String question, String category, String openAnswer) {
    return ConditionBuilder.createQuestionCondition(this, question, category, openAnswer).getElement();
  }

  /**
   * Build a data source representing the question answering in another questionnaire.
   * @param questionnaire
   * @param question
   * @return
   */
  public IDataSource newExternalDataSource(@SuppressWarnings("hiding") String questionnaire, String question) {
    return ConditionBuilder.createQuestionCondition(this, questionnaire, question, null, null).getElement();
  }

  /**
   * Build a data source representing the category selection in another questionnaire.
   * @param questionnaire
   * @param question
   * @param category
   * @return
   */
  public IDataSource newExternalDataSource(@SuppressWarnings("hiding") String questionnaire, String question, String category) {
    return ConditionBuilder.createQuestionCondition(this, questionnaire, question, category, null).getElement();
  }

  /**
   * Build a data source that gives the open answer in another questionnaire.
   * @param questionnaire
   * @param question
   * @param category
   * @param openAnswer
   * @return
   */
  public IDataSource newExternalDataSource(@SuppressWarnings("hiding") String questionnaire, String question, String category, String openAnswer) {
    return ConditionBuilder.createQuestionCondition(this, questionnaire, question, category, openAnswer).getElement();
  }

  /**
   * Write the questionnaire properties.
   * @param writer
   */
  public void writeProperties(@SuppressWarnings("hiding") IPropertyKeyProvider propertyKeyProvider, IPropertyKeyWriter writer) {
    QuestionnaireWalker walker = new QuestionnaireWalker(new PropertyKeyWriterVisitor(propertyKeyProvider, writer));
    walker.walk(questionnaire);
    writer.end();
  }

  /**
   * Create the localization properties for the current {@link Questionnaire}.
   * @return
   */
  public Properties getProperties(@SuppressWarnings("hiding") IPropertyKeyProvider propertyKeyProvider) {
    PropertiesPropertyKeyWriterImpl pWriter = new PropertiesPropertyKeyWriterImpl();

    writeProperties(propertyKeyProvider, pWriter);

    return pWriter.getProperties();
  }

  public QuestionnaireBuilder setPropertyKeyProvider(IPropertyKeyProvider propertyKeyProvider) {
    this.propertyKeyProvider = propertyKeyProvider;
    return this;
  }

  public IPropertyKeyProvider getPropertyKeyProvider() {
    return propertyKeyProvider;
  }

  public void setSimplifiedUI() {
    setDefaultPageUI(SimplifiedPageLayoutFactory.class);
    setDefaultQuestionUI(SimplifiedQuestionPanelFactory.class);

    setPropertyKeyProvider(new SimplifiedUIPropertyKeyProviderImpl());
  }

  public void setStandardUI() {
    setDefaultPageUI(DefaultPageLayoutFactory.class);
    setDefaultQuestionUI(DefaultQuestionPanelFactory.class);
    setPropertyKeyProvider(new DefaultPropertyKeyProviderImpl());
  }
}
