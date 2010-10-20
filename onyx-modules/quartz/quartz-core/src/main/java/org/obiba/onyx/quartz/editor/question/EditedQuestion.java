/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.editor.EditedElement;
import org.obiba.onyx.quartz.editor.question.condition.Conditions;

public class EditedQuestion extends EditedElement<Question> {

  private static final long serialVersionUID = 1L;

  private QuestionType questionType;

  private Conditions conditions;

  public EditedQuestion() {
    super(new Question(null));
  }

  public EditedQuestion(Question element) {
    super(element);
    questionType = element.getType();
  }

  public Conditions getConditions() {
    return conditions;
  }

  public void setConditions(Conditions conditions) {
    this.conditions = conditions;
  }

  public QuestionType getQuestionType() {
    return questionType;
  }

  public void setQuestionType(QuestionType questionType) {
    this.questionType = questionType;
  }

}
