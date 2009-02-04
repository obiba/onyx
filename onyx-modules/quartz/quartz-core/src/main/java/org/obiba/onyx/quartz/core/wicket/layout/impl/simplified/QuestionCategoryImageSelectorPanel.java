/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.BaseQuestionCategorySelectionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;

/**
 * 
 */
public class QuestionCategoryImageSelectorPanel extends BaseQuestionCategorySelectionPanel {

  private static final long serialVersionUID = 1L;

  private IModel questionModel;

  public QuestionCategoryImageSelectorPanel(String id, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
    this.questionModel = new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion());

    // add the category label css decorated with images
    AjaxLink link = new AjaxLink("link") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        // persist (or not)

        // change css style accordingly

        // fire event to other selectors in case of exclusive choice
        if(!getQuestion().isMultiple()) {

        }

        onSelection(target, getQuestionModel(), getQuestionCategoryModel());
      }

    };
    link.add(new Label("label", new QuestionnaireStringResourceModel(questionCategoryModel, "label")));
    add(link);
  }

  /**
   * Called when selector is clicked.
   * @param target
   */
  public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
  }

  protected IModel getQuestionModel() {
    return questionModel;
  }

  protected Question getQuestion() {
    return (Question) questionModel.getObject();
  }

  protected IModel getQuestionCategoryModel() {
    return getModel();
  }
}
