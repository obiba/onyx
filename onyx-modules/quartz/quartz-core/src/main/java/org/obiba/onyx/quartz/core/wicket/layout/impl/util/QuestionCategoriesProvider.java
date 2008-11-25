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

import java.util.List;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.AbstractDataListProvider;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the question categories.
 */
public class QuestionCategoriesProvider extends AbstractDataListProvider<QuestionCategory> {

  private static final long serialVersionUID = 1L;

  public static final String ROW_COUNT_KEY = "rowCount";

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoriesProvider.class);

  private IModel questionModel;

  public QuestionCategoriesProvider(IModel questionModel) {
    this.questionModel = questionModel;

  }

  public LineToMatrixPermutation<QuestionCategory> getPermutator() {
    Question question = (Question) questionModel.getObject();
    int rowCount = LineToMatrixPermutation.DEFAULT_ROW_COUNT;
    if(question.getUIArguments() != null) {
      rowCount = question.getUIArguments().getInt(ROW_COUNT_KEY, LineToMatrixPermutation.DEFAULT_ROW_COUNT);
    }
    return new LineToMatrixPermutation<QuestionCategory>(question.getQuestionCategories(), rowCount);
  }

  @Override
  public List<QuestionCategory> getDataList() {
    return getPermutator().getMatrixList();
  }

  @Override
  public IModel model(Object object) {
    if(object != null) {
      return new QuestionnaireModel((QuestionCategory) object);
    }
    return null;
  }
}