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
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;

/**
 * Get the question categories, performing ordering, filtering and permutation when applicable.
 */
public class QuestionCategoriesProvider extends AbstractDataListProvider<QuestionCategory> {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(QuestionCategoriesProvider.class);

  private IDataListFilter<QuestionCategory> filter;

  private IModel<Question> questionModel;

  private List<IModel<QuestionCategory>> dataList;

  public QuestionCategoriesProvider(IModel<Question> questionModel) {
    this(questionModel, null);
  }

  public QuestionCategoriesProvider(IModel<Question> questionModel, IDataListFilter<QuestionCategory> filter) {
    this(questionModel, filter, null);
  }

  public QuestionCategoriesProvider(IModel<Question> questionModel, IDataListFilter<QuestionCategory> filter, IDataListPermutator<IModel<QuestionCategory>> permutator) {
    super(permutator);
    this.filter = filter;
    this.questionModel = questionModel;
    getDataList();
  }

  @Override
  public List<IModel<QuestionCategory>> getDataList() {
    if(dataList != null) {
      log.debug("QuestionCategoriesProvider.getDataList() CACHED");
      return dataList;
    }

    log.debug("QuestionCategoriesProvider.getDataList() START");
    Question question = (Question) questionModel.getObject();

    List<IModel<QuestionCategory>> categories = new ArrayList<IModel<QuestionCategory>>();

    // ordering by locale
    String categoryOrder = null;
    try {
      categoryOrder = new QuestionnaireStringResourceModel(questionModel, "categoryOrder").getString();
      // log.info("{}.categoryOrder={}", question, categoryOrder);
    } catch(NoSuchMessageException e) {
      log.debug("categoryOrder not defined for question {}", question);
    }
    if(categoryOrder != null && categoryOrder.trim().length() > 0) {
      for(String categoryName : categoryOrder.split(",")) {
        QuestionCategory found = question.findQuestionCategory(categoryName.trim());
        if(found != null) {
          if(accept(found)) {
            categories.add(new QuestionnaireModel<QuestionCategory>(found));
          }
        }
      }
      // make sure no one is missing
      if(categories.size() < question.getQuestionCategories().size()) {
        for(QuestionCategory questionCategory : question.getQuestionCategories()) {
          if(accept(questionCategory)) {
            IModel<QuestionCategory> questionCategoryModel = new QuestionnaireModel<QuestionCategory>(questionCategory);
            if(!categories.contains(questionCategoryModel)) {
              categories.add(questionCategoryModel);
            }
          }
        }
      }
    } else {
      for(QuestionCategory questionCategory : question.getQuestionCategories()) {
        if(accept(questionCategory)) {
          categories.add(new QuestionnaireModel<QuestionCategory>(questionCategory));
        }
      }
    }

    // permutation
    IDataListPermutator<IModel<QuestionCategory>> permutator = getDataListPermutator();
    if(permutator != null) {
      dataList = permutator.permute(categories);
    } else {
      dataList = categories;
    }
    log.debug("QuestionCategoriesProvider.getDataList() END");
    return dataList;
  }

  /**
   * Question category model filtering.
   * @param item
   * @return
   */
  private boolean accept(QuestionCategory item) {

    // Filter out the "no-answer" categories. These will not be displayed.
    if(item.getCategory().isNoAnswer()) {
      return false;
    }

    if(filter != null) {
      return filter.accept(item);
    }
    return true;
  }

  @Override
  public IModel<QuestionCategory> model(QuestionCategory object) {
    if(object != null) {
      return new QuestionnaireModel<QuestionCategory>(object);
    }
    return null;
  }

}