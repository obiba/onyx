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

import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.Condition;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.MultipleCondition;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.NotCondition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

/**
 * Walks through the tree of {@link Questionnaire} elements, and call for a {@link IWalkerVisitor} at each node.
 * @author Yannick Marcon
 * 
 */
public class QuestionnaireWalker implements IVisitor {

  /**
   * A walk in which each parent node is traversed before its children is called a pre-order walk.
   */
  boolean preOrder;

  /**
   * Final visitor that performs real job at each node visit.
   */
  private IWalkerVisitor visitor;

  /**
   * Constructor, given a questionnaire visitor that will perform real operation at each node visit.
   * @param visitor
   */
  public QuestionnaireWalker(IWalkerVisitor visitor) {
    this.visitor = visitor;
  }

  /**
   * Go through the questionnaire.
   * @param questionnaire
   * @param preOrder a walk in which each parent node is traversed before its children is called a pre-order walk.
   */
  public void walk(Questionnaire questionnaire, boolean preOrder) {
    this.preOrder = preOrder;
    questionnaire.accept(this);
  }

  /**
   * Go through the questionnaire, in a pre-order walk.
   * @param questionnaire
   */
  public void walk(Questionnaire questionnaire) {
    walk(questionnaire, true);
  }

  /**
   * Shall we continue walking through the questionnaire ?
   */
  private boolean visiteMore() {
    return visitor.visiteMore();
  }

  public final void visit(Questionnaire questionnaire) {
    if(preOrder) questionnaire.accept(visitor);
    for(Section section : questionnaire.getSections()) {
      section.accept(this);
      if(!visiteMore()) break;
    }
    if(!preOrder) questionnaire.accept(visitor);
  }

  public final void visit(Section section) {
    if(preOrder) section.accept(visitor);
    if(visiteMore()) {
      for(Page page : section.getPages()) {
        page.accept(this);
        if(!visiteMore()) break;
      }
    }
    if(visiteMore()) {
      for(Section sectionChild : section.getSections()) {
        sectionChild.accept(this);
      }
    }
    if(!preOrder) section.accept(visitor);
  }

  public final void visit(Page page) {
    if(preOrder) page.accept(visitor);
    if(visiteMore()) {
      for(Question question : page.getQuestions()) {
        question.accept(this);
        if(!visiteMore()) break;
      }
    }
    if(!preOrder) page.accept(visitor);
  }

  public final void visit(Question question) {
    if(preOrder) question.accept(visitor);
    if(visiteMore()) {
      for(QuestionCategory questionCategory : question.getQuestionCategories()) {
        questionCategory.accept(this);
        if(!visiteMore()) break;
      }
    }
    if(visiteMore()) {
      for(Question questionChild : question.getQuestions()) {
        questionChild.accept(this);
        if(!visiteMore()) break;
      }
    }
    if(visiteMore() && question.getCondition() != null) {
      question.getCondition().accept(this);
    }
    if(!preOrder) question.accept(visitor);
  }

  public final void visit(QuestionCategory questionCategory) {
    if(preOrder) questionCategory.accept(visitor);
    questionCategory.getCategory().accept(this);
    if(!preOrder) questionCategory.accept(visitor);
  }

  public final void visit(Category category) {
    if(preOrder) category.accept(visitor);
    if(visiteMore() && category.getOpenAnswerDefinition() != null) {
      category.getOpenAnswerDefinition().accept(this);
    }
    if(!preOrder) category.accept(visitor);
  }

  public final void visit(OpenAnswerDefinition openAnswerDefinition) {
    if(preOrder) openAnswerDefinition.accept(visitor);
    if(visiteMore()) {
      for(OpenAnswerDefinition openAnswerDefinitionChild : openAnswerDefinition.getOpenAnswerDefinitions()) {
        openAnswerDefinitionChild.accept(this);
        if(!visiteMore()) break;
      }
    }
    if(!preOrder) openAnswerDefinition.accept(visitor);
  }

  public final void visit(Condition condition) {
    if(preOrder) condition.accept(visitor);
    if(visiteMore() && (condition instanceof MultipleCondition) && ((MultipleCondition) condition).getConditions() != null) {
      for(Condition childCondition : ((MultipleCondition) condition).getConditions()) {
        childCondition.accept(this);
        if(!visiteMore()) break;
      }
    }
    if(visiteMore() && (condition instanceof NotCondition) && ((NotCondition) condition).getCondition() != null) {
      ((NotCondition) condition).getCondition().accept(this);
    }
    if(!preOrder) condition.accept(visitor);
  }

}
