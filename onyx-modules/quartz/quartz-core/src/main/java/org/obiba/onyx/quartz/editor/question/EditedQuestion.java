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

import static org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator.ROW_COUNT_KEY;

import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.ListToGridPermutator;
import org.obiba.onyx.quartz.editor.EditedElement;

public class EditedQuestion extends EditedElement<Question> {

  private static final long serialVersionUID = 1L;

  public static enum Layout {
    SINGLE_COLUMN, GRID;
  }

  private QuestionType questionType;

  private Layout layout = Layout.SINGLE_COLUMN;

  private Integer nbRows = ListToGridPermutator.DEFAULT_ROW_COUNT;

  public EditedQuestion(Question element) {
    super(element);
    if(element != null) {
      setElement(element);
      questionType = element.getType();
    }
  }

  @Override
  public void setElement(Question question) {
    super.setElement(question);
    if(question != null) {
      ValueMap map = question.getUIArgumentsValueMap();
      if(map != null && map.containsKey(ROW_COUNT_KEY)) {
        layout = Layout.GRID;
        nbRows = map.getInt(ROW_COUNT_KEY);
      }
    }
  }

  public QuestionType getQuestionType() {
    return questionType;
  }

  public void setQuestionType(QuestionType questionType) {
    this.questionType = questionType;
  }

  public Layout getLayout() {
    return layout;
  }

  public void setLayout(Layout layout) {
    this.layout = layout;
    setLayoutInfos();
  }

  public Integer getNbRows() {
    return nbRows;
  }

  public void setNbRows(Integer nbRows) {
    this.nbRows = nbRows;
    setLayoutInfos();
  }

  @Override
  public String toString() {
    return "question: " + getElement() + ", questionType: " + questionType + ", layout: " + layout + ", nbRows: " + nbRows;
  }

  private void setLayoutInfos() {
    if(layout != null) {
      Question question = getElement();
      question.clearUIArguments();
      switch(layout) {
      case GRID:
        question.addUIArgument(ROW_COUNT_KEY, String.valueOf(nbRows));
        break;
      case SINGLE_COLUMN:
        break;
      }
    }
  }
}
