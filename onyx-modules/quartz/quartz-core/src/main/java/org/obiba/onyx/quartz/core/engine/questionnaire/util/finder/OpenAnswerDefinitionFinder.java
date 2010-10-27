/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.finder;

import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

/**
 * Class for finding {@link OpenAnswerDefinition}.
 * @author Yannick Marcon
 * 
 */
public class OpenAnswerDefinitionFinder extends AbstractFinderVisitor<OpenAnswerDefinition> {

  /**
   * Constructor, for searching first {@link OpenAnswerDefinition} with given name.
   * @param name
   */
  public OpenAnswerDefinitionFinder(String name) {
    super(name);
  }

  /**
   * Constructor, for searching {@link OpenAnswerDefinition} with given name.
   * @param name
   * @param stopAtFirst
   */
  public OpenAnswerDefinitionFinder(String name, boolean stopAtFirst) {
    super(name, stopAtFirst);
  }

  public void visit(Questionnaire questionnaire) {
  }

  public void visit(Section section) {
  }

  public void visit(Page page) {
  }

  public void visit(Question question) {
  }

  public void visit(QuestionCategory questionCategory) {
  }

  public void visit(Category category) {
  }

  public void visit(OpenAnswerDefinition openAnswerDefinition) {
    visitElement(openAnswerDefinition);
  }

  @Override
  public void visit(Variable variable) {
  }

}
