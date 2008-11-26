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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;

/**
 * 
 */
public class QuestionCategoriesToMatrixPermutator extends ListToMatrixPermutator<QuestionCategory> {

  private static final long serialVersionUID = 1L;

  public static final String ROW_COUNT_KEY = "rowCount";

  public QuestionCategoriesToMatrixPermutator(IModel questionModel) {
    super();
    Question question = (Question) questionModel.getObject();
    if(question.getUIArguments() != null) {
      setRowCount(question.getUIArguments().getInt(ROW_COUNT_KEY, ListToMatrixPermutator.DEFAULT_ROW_COUNT));
    }
  }

}
