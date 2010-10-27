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

import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

/**
 * A helper class for implementing {@link IWalkerVisitor}. All methods are empty stubs except {@code #visiteMore()}
 * which always returns true;
 */
public class DefaultWalkerVisitor implements IWalkerVisitor {

  @Override
  public boolean visiteMore() {
    return true;
  }

  @Override
  public void visit(Questionnaire questionnaire) {
  }

  @Override
  public void visit(Section section) {
  }

  @Override
  public void visit(Page page) {
  }

  @Override
  public void visit(Question question) {
  }

  @Override
  public void visit(QuestionCategory questionCategory) {
  }

  @Override
  public void visit(Category category) {
  }

  @Override
  public void visit(OpenAnswerDefinition openAnswerDefinition) {
  }

  @Override
  public void visit(Variable variable) {

  }

}
