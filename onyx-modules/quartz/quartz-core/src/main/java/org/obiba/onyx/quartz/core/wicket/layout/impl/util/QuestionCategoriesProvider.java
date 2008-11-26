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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the question categories.
 */
public class QuestionCategoriesProvider extends AbstractDataListProvider<QuestionCategory> {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoriesProvider.class);

  private IModel questionModel;

  public QuestionCategoriesProvider(IModel questionModel) {
    this(questionModel, null);
  }

  public QuestionCategoriesProvider(IModel questionModel, IDataListFilter<QuestionCategory> filter) {
    this(questionModel, filter, null);
  }

  public QuestionCategoriesProvider(IModel questionModel, IDataListFilter<QuestionCategory> filter, IDataListPermutator<QuestionCategory> permutator) {
    super(filter, permutator);
    this.questionModel = questionModel;
    getDataList();
  }

  @Override
  public List<QuestionCategory> getDataList() {
    Question question = (Question) questionModel.getObject();
    List<QuestionCategory> categories;
    if(filter == null) {
      categories = question.getQuestionCategories();
    } else {
      categories = new ArrayList<QuestionCategory>();
      for(QuestionCategory questionCategory : question.getQuestionCategories()) {
        if(filter.accept(questionCategory)) categories.add(questionCategory);
      }
    }
    IDataListPermutator<QuestionCategory> permutator = getDataListPermutator();
    if(permutator != null) {
      return permutator.permute(categories);
    } else {
      return categories;
    }
  }

  @Override
  public IModel model(Object object) {
    if(object != null) {
      return new QuestionnaireModel((QuestionCategory) object);
    }
    return null;
  }

}