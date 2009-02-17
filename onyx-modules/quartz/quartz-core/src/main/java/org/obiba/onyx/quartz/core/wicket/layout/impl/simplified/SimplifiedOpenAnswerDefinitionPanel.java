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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionStateHolder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.QuestionCategorySelectionBehavior;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.pad.OpenAnswerPadFactory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.wicket.link.AjaxImageLink;

/**
 * 
 */
public class SimplifiedOpenAnswerDefinitionPanel extends AbstractOpenAnswerDefinitionPanel implements IQuestionCategorySelectionStateHolder {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private ModalWindow padWindow;

  private boolean selected;

  /**
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   * @param openAnswerDefinitionModel
   */
  @SuppressWarnings("serial")
  public SimplifiedOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    super(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);
    setOutputMarkupId(true);

    updateState();

    add(new Label("value", new PropertyModel(this, "openValue")).setOutputMarkupId(true).add(new QuestionCategorySelectionBehavior()));
    add(new Label("label", QuestionnaireStringResourceModelHelper.getStringResourceModel(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition())));

    AjaxImageLink link = new AjaxImageLink("link", new Model("Click"), new Model("Here")) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        padWindow.show(target);
      }

    };
    link.getLink().add(new QuestionCategorySelectionBehavior());
    add(link);

    // Create modal window
    add(padWindow = new ModalWindow("padModal"));
    padWindow.setCookieName("numeric-pad");

    final AbstractOpenAnswerDefinitionPanel pad = OpenAnswerPadFactory.create(padWindow.getContentId(), getQuestionModel(), getQuestionCategoryModel(), new QuestionnaireModel(getQuestionCategory().getOpenAnswerDefinition()), padWindow);
    padWindow.setContent(pad);

    // same as cancel
    padWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        if(!getQuestion().isMultiple()) {
          activeQuestionnaireAdministrationService.deleteAnswers(getQuestion());
        } else {
          activeQuestionnaireAdministrationService.deleteAnswer(getQuestion(), getQuestionCategory());
        }
        return true;
      }
    });

    padWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target) {
        // fire event to other selectors in case of exclusive choice
        IQuestionCategorySelectionListener listener = (IQuestionCategorySelectionListener) SimplifiedOpenAnswerDefinitionPanel.this.findParent(IQuestionCategorySelectionListener.class);
        if(listener != null) {
          listener.onQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), !isQuestionCategorySelected());
        }
      }
    });
  }

  public boolean isQuestionCategorySelected() {
    return activeQuestionnaireAdministrationService.findAnswer(getQuestion(), getQuestionCategory()) != null;
  }

  public String getOpenValue() {
    OpenAnswer answer = activeQuestionnaireAdministrationService.findOpenAnswer(getQuestion(), getQuestionCategory().getCategory(), getOpenAnswerDefinition());
    if(answer == null) return null;
    if(answer.getData() != null) return answer.getData().getValueAsString();
    return null;
  }

  @Override
  public void resetField() {
    // TODO Auto-generated method stub

  }

  public boolean isSelected() {
    return activeQuestionnaireAdministrationService.findAnswer(getQuestion(), getQuestionCategory()) != null;
  }

  public boolean updateState() {
    selected = isSelected();
    return selected;
  }

  public boolean wasSelected() {
    return selected;
  }

}
