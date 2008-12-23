/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.engine.variable.impl;

import org.obiba.onyx.engine.variable.Entity;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.engine.variable.IQuestionToVariableMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DefaultQuestionToVariableMappingStrategy implements IQuestionToVariableMappingStrategy {

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionToVariableMappingStrategy.class);

  public Entity getEntity(Questionnaire questionnaire) {
    // another possibility is to add the questionnaire version as a sub entity
    return new Entity(questionnaire.getName());
  }

  public Entity getEntity(Question question) {
    Entity entity = null;

    // simple question
    if(question.getQuestions().size() == 0) {
      Variable variable = new Variable(question.getName());
      entity = variable;
      for(Category category : question.getCategories()) {
        variable.addCategory(category.getName());
        if(category.getOpenAnswerDefinition() != null) {
          OpenAnswerDefinition open = category.getOpenAnswerDefinition();
          variable.addEntity(new Variable(open.getName()).setDataType(open.getDataType()).setUnit(open.getUnit()));
        }
      }
    } else if(question.getQuestionCategories().size() == 0) {
      // sub questions
      entity = new Entity(question.getName());
      for(Question subQuestion : question.getQuestions()) {
        entity.addEntity(getEntity(subQuestion));
      }
    } else {
      boolean shared = true;
      for(Question child : question.getQuestions()) {
        if(child.getCategories().size() > 0) {
          shared = false;
          break;
        }
      }
      if(shared) {
        // shared categories question
        entity = new Entity(question.getName());
        for(Question subQuestion : question.getQuestions()) {
          Variable variable = new Variable(subQuestion.getName());
          for(Category category : question.getCategories()) {
            variable.addCategory(category.getName());
            if(category.getOpenAnswerDefinition() != null) {
              OpenAnswerDefinition open = category.getOpenAnswerDefinition();
              variable.addEntity(new Variable(open.getName()).setDataType(open.getDataType()).setUnit(open.getUnit()));
            }
          }
          entity.addEntity(variable);
        }
      } else {
        // joined categories question
        throw new UnsupportedOperationException("Joined categories question array not supported yet.");
      }
    }

    log.debug("getEntity({})={}", question, entity);

    return entity;
  }

  public ILocalizable getQuestionnaireElement(Questionnaire questionnaire, Entity entity) {
    // TODO Auto-generated method stub
    return null;
  }

}
