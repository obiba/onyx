/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

public abstract class Condition implements Serializable {

  private List<Question> questions;

  public List<Question> getQuestions() {
    return questions != null ? questions : (questions = new ArrayList<Question>());
  }

  public void addQuestion(Question question) {
    if(question != null) {
      getQuestions().add(question);
      question.setCondition(this);
    }
  }

  public abstract boolean isToBeAnswered();

}
