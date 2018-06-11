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

  private Layout layout = null;

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
        int rows = map.getInt(ROW_COUNT_KEY);
        if(question.getQuestionCategories().size() == rows) {
          layout = Layout.SINGLE_COLUMN;
        } else {
          layout = Layout.GRID;
        }
        nbRows = rows;
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
  }

  public Integer getNbRows() {
    return nbRows;
  }

  public void setNbRows(Integer nbRows) {
    this.nbRows = nbRows;
  }

  @Override
  public String toString() {
    return "question: " + getElement() + ", questionType: " + questionType + ", layout: " + layout + ", nbRows: " + nbRows;
  }

  public void setLayoutInfos() {
    Question question = getElement();
    question.clearUIArguments();
    if(layout != null) {
      switch(layout) {
      case GRID:
        question.addUIArgument(ROW_COUNT_KEY, String.valueOf(nbRows));
        break;
      case SINGLE_COLUMN:
        question.addUIArgument(ROW_COUNT_KEY, String.valueOf(question.getCategories().size()));
        break;
      }
    }
  }
}
