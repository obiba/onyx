/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.IWalkerVisitor;

/**
 *
 */
public class MissingLocalePropertiesVisitor implements IWalkerVisitor {

  @Override
  public void visit(Questionnaire questionnaire) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Section section) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Page page) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Question question) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(QuestionCategory questionCategory) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Category category) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(OpenAnswerDefinition openAnswerDefinition) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean visiteMore() {
    return true;  // no stop
  }

}
