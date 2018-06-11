/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.provider;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;

/**
 * 
 */
public class AllQuestionCategoriesProvider extends AbstractQuestionnaireElementProvider<QuestionCategory, Question> {

  private static final long serialVersionUID = 929587284672308157L;

  public AllQuestionCategoriesProvider(IModel<Question> model) {
    super(model);
  }

  @Override
  protected List<QuestionCategory> getElementList() {
    Question question = getProviderElement();
    List<QuestionCategory> categories;

    if(question.isArrayOfSharedCategories()) {
      categories = new ArrayList<QuestionCategory>();
    } else if(question.getParentQuestion() != null && question.getParentQuestion().isArrayOfSharedCategories()) {
      categories = question.getParentQuestion().getQuestionCategories();
    } else
      categories = question.getQuestionCategories();

    return categories;
  }

}
