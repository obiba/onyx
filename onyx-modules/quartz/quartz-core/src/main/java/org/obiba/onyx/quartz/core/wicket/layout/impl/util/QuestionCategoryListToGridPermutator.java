/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.util;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;

/**
 * Turns the list of question categories, in a grid representation.
 */
public class QuestionCategoryListToGridPermutator extends ListToGridPermutator<QuestionCategory> {

  private static final long serialVersionUID = 1L;

  public static final String ROW_COUNT_KEY = "rowCount";

  public QuestionCategoryListToGridPermutator(IModel questionModel) {
    super();
    Question question = (Question) questionModel.getObject();
    ValueMap arguments = question.getUIArgumentsValueMap();
    if(arguments != null) {
      setRowCount(arguments.getInt(ROW_COUNT_KEY, ListToGridPermutator.DEFAULT_ROW_COUNT));
    }
  }

  public QuestionCategoryListToGridPermutator(IModel questionModel, int rowCount) {
    super();
    setRowCount(rowCount);
  }

}
