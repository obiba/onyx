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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.wicket.layout.IPageLayoutFactory;

/**
 * {@link Section} builder, given a {@link Questionnaire}.
 * @author cag-ymarcon
 * 
 */
public class SectionBuilder extends AbstractQuestionnaireElementBuilder<Section> {

  /**
   * Constructor using the {@link QuestionnaireBuilder} to get the {@link Questionnaire} it is applied to.
   * @param parent
   * @param name
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  private SectionBuilder(QuestionnaireBuilder parent, String name) {
    super(parent);
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    if(!checkUniqueSectionName(name)) {
      throw invalidNameUnicityException(Section.class, name);
    }
    this.element = new Section(name);
    this.questionnaire.addSection(element);
  }

  /**
   * Constructor.
   * @param questionnaire
   * @param section
   */
  private SectionBuilder(QuestionnaireBuilder parent, Section section) {
    super(parent);
    this.element = section;
  }

  /**
   * Create a section in the {@link Questionnaire}.
   * @param parent
   * @param name
   * @return
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  public static SectionBuilder createSection(QuestionnaireBuilder parent, String name) {
    return new SectionBuilder(parent, name);
  }

  public static SectionBuilder inSection(QuestionnaireBuilder parent, Section section) {
    return new SectionBuilder(parent, section);
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
    if(!checkUniqueSectionName(name)) {
      throw invalidNameUnicityException(Section.class, name);
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

  /**
   * Add a {@link Page} to current {@link Section} and make it the current {@link Page}.
   * @param name
   * @return
   * @see #getPage()
   */
  public PageBuilder withPage(String name, Class<? extends IPageLayoutFactory> uiFactoryClass) {
    return PageBuilder.createPage(this, name, uiFactoryClass);
  }

  /**
   * Check section name is unique.
   * @param name
   * @return
   */
  private boolean checkUniqueSectionName(String name) {
    return (QuestionnaireFinder.getInstance(questionnaire).findSection(name) == null);
  }

}
