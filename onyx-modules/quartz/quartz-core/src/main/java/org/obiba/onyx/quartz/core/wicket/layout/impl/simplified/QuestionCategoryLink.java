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
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.BaseQuestionCategorySelectionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A link for selecting a question category, without open answers.
 */
public class QuestionCategoryLink extends BaseQuestionCategorySelectionPanel implements IQuestionCategorySelectionStateHolder {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoryLink.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private IModel questionModel;

  @SuppressWarnings("serial")
  public QuestionCategoryLink(String id, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
    setOutputMarkupId(true);
    this.questionModel = new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion());

    // add the category label css decorated with images
    AjaxImageLink link = new AjaxImageLink("link", new QuestionnaireStringResourceModel(questionCategoryModel, "label")) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        // persist (or not)
        // if it was selected, deselect it
        boolean isSelected = isQuestionCategorySelected();
        if(!getQuestion().isMultiple() || getQuestionCategory().isEscape()) {
          // exclusive choice, only one answer per question
          activeQuestionnaireAdministrationService.deleteAnswers(getQuestion());
        } else {
          activeQuestionnaireAdministrationService.deleteAnswer(getQuestion(), getQuestionCategory());
        }
        if(!isSelected) {
          activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory());
        }

        // fire event to other selectors in case of exclusive choice
        IQuestionCategorySelectionListener listener = (IQuestionCategorySelectionListener) QuestionCategoryLink.this.findParent(IQuestionCategorySelectionListener.class);
        if(listener != null) {
          listener.onQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), !isSelected);
        }
      }

    };
    link.add(new QuestionCategorySelectionBehavior());
    add(link);

  }

  public boolean isQuestionCategorySelected() {
    return activeQuestionnaireAdministrationService.findAnswer(getQuestion(), getQuestionCategory()) != null;
  }

  protected IModel getQuestionModel() {
    return questionModel;
  }

  public Question getQuestion() {
    return (Question) questionModel.getObject();
  }

  protected IModel getQuestionCategoryModel() {
    return getModel();
  }

  public QuestionCategory getQuestionCategory() {
    return (QuestionCategory) getModel().getObject();
  }
}
