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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.BaseQuestionCategorySelectionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.pad.OpenAnswerPadFactory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A link for selecting a question category, without open answers.
 */
public class QuestionCategoryLinkPanel extends BaseQuestionCategorySelectionPanel implements IQuestionCategorySelectionStateHolder {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoryLinkPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private IModel questionModel;

  private ModalWindow padWindow;

  @SuppressWarnings("serial")
  public QuestionCategoryLinkPanel(String id, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
    setOutputMarkupId(true);
    this.questionModel = new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion());

    // add the category label css decorated with images
    ImageButton link = new ImageButton("link", new QuestionnaireStringResourceModel(questionCategoryModel, "label")) {

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
          if(padWindow != null) {
            padWindow.show(target);
          }
        }

        // fire event to other selectors in case of exclusive choice
        IQuestionCategorySelectionListener listener = (IQuestionCategorySelectionListener) QuestionCategoryLinkPanel.this.findParent(IQuestionCategorySelectionListener.class);
        if(listener != null) {
          listener.onQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), !isSelected);
        }
      }

    };
    link.add(new QuestionCategorySelectionBehavior());
    add(link);

    if(getQuestionCategory().getOpenAnswerDefinition() != null) {
      addPadModal();
    } else {
      add(new EmptyPanel("padModal").setVisible(false));
    }
  }

  @SuppressWarnings("serial")
  private void addPadModal() {
    // Create modal window
    add(padWindow = new ModalWindow("padModal"));
    padWindow.setCookieName("numeric-pad");

    final AbstractOpenAnswerDefinitionPanel pad = OpenAnswerPadFactory.create(padWindow.getContentId(), getQuestionModel(), getQuestionCategoryModel(), new QuestionnaireModel(getQuestionCategory().getOpenAnswerDefinition()), padWindow);
    padWindow.setContent(pad);

    // same as cancel
    padWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        activeQuestionnaireAdministrationService.deleteAnswer(getQuestion(), getQuestionCategory());
        return true;
      }
    });

    padWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target) {
        target.addComponent(QuestionCategoryLinkPanel.this);
      }
    });

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
