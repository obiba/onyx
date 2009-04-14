/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.wicket.layout.IPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.util.data.DataType;

/**
 * {@link Page} builder, given a {@link Questionnaire} and a current {@link Section}.
 * @author Yannick Marcon
 * 
 */
public class PageBuilder extends AbstractQuestionnaireElementBuilder<Page> {

  /**
   * Constructor using {@link SectionBuilder} to get the {@link Section} it is applied to.
   * @param parent
   * @param name
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  private PageBuilder(SectionBuilder parent, String name, Class<? extends IPageLayoutFactory> uiFactoryClass) {
    super(parent);
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    if(!checkUniquePageName(name)) {
      throw invalidNameUnicityException(Page.class, name);
    }
    this.element = new Page(name);
    try {
      this.element.setUIFactoryName(uiFactoryClass.newInstance().getBeanName());
    } catch(Exception e) {
      throw invalidPageLayoutFactoryException(uiFactoryClass, e);
    }
    this.questionnaire.addPage(element);
    parent.getElement().addPage(element);
  }

  /**
   * Constructor.
   * @param questionnaire
   * @param page
   */
  private PageBuilder(QuestionnaireBuilder parent, Page page) {
    super(parent);
    this.element = page;
  }

  /**
   * Create a page in a {@link Section} given its name.
   * @param parent
   * @param name
   * @return
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  public static PageBuilder createPage(SectionBuilder parent, String name) {
    return new PageBuilder(parent, name, parent.getDefaultPageUI());
  }

  /**
   * Create a page in a {@link Section} given its name.
   * @param parent
   * @param name
   * @param uiFactoryClass
   * @return
   */
  public static PageBuilder createPage(SectionBuilder parent, String name, Class<? extends IPageLayoutFactory> uiFactoryClass) {
    return new PageBuilder(parent, name, uiFactoryClass);
  }

  /**
   * Set the given {@link Page} as the current one.
   * @param questionnaire
   * @param page
   * @return
   */
  public static PageBuilder inPage(QuestionnaireBuilder parent, Page page) {
    return new PageBuilder(parent, page);
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name) {
    return QuestionBuilder.createQuestion(this, name, null, false);
  }

  /**
   * Add a required, non multiple, with number {@link Question} to current {@link Page} and make it current
   * {@link Question}.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, String number) {
    return QuestionBuilder.createQuestion(this, name, number, false);
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @param uiFactoryClass
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    return QuestionBuilder.createQuestion(this, name, null, false, uiFactoryClass);
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @param uiFactoryClass
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, String number, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    return QuestionBuilder.createQuestion(this, name, number, false, uiFactoryClass);
  }

  /**
   * Add a required, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, boolean multiple) {
    return QuestionBuilder.createQuestion(this, name, null, multiple);
  }

  /**
   * Add a required, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, String number, boolean multiple) {
    return QuestionBuilder.createQuestion(this, name, number, multiple);
  }

  /**
   * Add a required, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @param uiFactoryName
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, String number, boolean multiple, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    return QuestionBuilder.createQuestion(this, name, number, multiple, uiFactoryClass);
  }

  /**
   * Check page name unicity.
   * @param name
   * @return
   */
  private boolean checkUniquePageName(String name) {
    return (QuestionnaireFinder.getInstance(questionnaire).findPage(name) == null);
  }

  private IllegalArgumentException invalidPageLayoutFactoryException(Class<? extends IPageLayoutFactory> uiFactoryClass, Exception e) {
    return new IllegalArgumentException("Unable to get page layout factory name from " + uiFactoryClass.getName(), e);
  }

  /**
   * Add Timestamp question to page.
   * @return
   */
  public PageBuilder addTimestamp() {
    // String timestampName = "TIMESTAMP_" + element.getName();
    // withQuestion(timestampName).withSharedCategory("TIMESTAMP").withOpenAnswerDefinition(timestampName,
    // DataType.DATE).setOpenAnswerDefinitionAnswerSource(new TimestampSource());
    return addTimestamp(element.getName());
  }

  /**
   * Add Timestamp question to page.
   * @param name
   * @return
   */
  public PageBuilder addTimestamp(String name) {
    String timestampName = "TIMESTAMP_" + name;
    withQuestion(timestampName).withCategory("TIMESTAMP").withOpenAnswerDefinition(timestampName, DataType.DATE).setTimestampSource();
    return this;
  }

}
