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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionStateHolder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.behavior.QuestionCategorySelectionBehavior;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.pad.NumericPad;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.link.AjaxImageLink;
import org.springframework.util.StringUtils;

/**
 * Simplified UI for entering open answers: a popup appears on link clicked with a pad, and on pad entry validation the
 * open value is displayed read-only.
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
    add(new Label("label", getCategoryLabelResourceModel()));
    QuestionnaireStringResourceModel unitLabelModel = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), "unitLabel");
    add(new Label("unit", unitLabelModel).setVisible(StringUtils.hasLength(unitLabelModel.getString())));

    AjaxImageLink link = new AjaxImageLink("link", new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "clickHere")) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        padWindow.show(target);
      }

    };
    link.getLink().add(new QuestionCategorySelectionBehavior());
    link.getLink().add(new NoDragBehavior());
    add(link);

    // Create modal window
    add(padWindow = new ModalWindow("padModal"));
    padWindow.setCssClassName("onyx");
    padWindow.setInitialWidth(288);
    padWindow.setInitialHeight(365);
    padWindow.setResizable(false);

    final AbstractOpenAnswerDefinitionPanel pad = createPad(padWindow);
    padWindow.setContent(pad);

    // same as cancel
    padWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
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
        target.addComponent(SimplifiedOpenAnswerDefinitionPanel.this);
      }
    });
  }

  private QuestionnaireStringResourceModel getCategoryLabelResourceModel() {
    // Create and add the label for the numeric input field.
    OpenAnswerDefinition parentOpenAnswer = getOpenAnswerDefinition().getParentOpenAnswerDefinition();
    IModel labelModel;
    if(parentOpenAnswer != null && parentOpenAnswer.getOpenAnswerDefinitions().size() > 0) {
      labelModel = getOpenAnswerDefinitionModel();
    } else {
      labelModel = getQuestionCategoryModel();
    }
    return new QuestionnaireStringResourceModel(labelModel, "label");
  }

  public boolean isQuestionCategorySelected() {
    return activeQuestionnaireAdministrationService.findAnswer(getQuestion(), getQuestionCategory()) != null;
  }

  public String getOpenValue() {
    OpenAnswer answer = activeQuestionnaireAdministrationService.findOpenAnswer(getQuestion(), getQuestionCategory().getCategory(), getOpenAnswerDefinition());
    String val = null;

    if(answer != null && answer.getData() != null) {
      val = answer.getData().getValueAsString();
    }

    return val;
  }

  @Override
  public void resetField() {
    // TODO Auto-generated method stub

  }

  public boolean isSelected() {
    return activeQuestionnaireAdministrationService.findOpenAnswer(getQuestion(), getQuestionCategory().getCategory(), getOpenAnswerDefinition()) != null;
  }

  public boolean updateState() {
    selected = isSelected();
    return selected;
  }

  public boolean wasSelected() {
    return selected;
  }

  public AbstractOpenAnswerDefinitionPanel createPad(ModalWindow padWindow) {
    DataType type = getOpenAnswerDefinition().getDataType();
    if(type.equals(DataType.INTEGER) || type.equals(DataType.DECIMAL)) {
      NumericPad pad = new NumericPad(padWindow.getContentId(), getQuestionModel(), getQuestionCategoryModel(), getOpenAnswerDefinitionModel());
      padWindow.setTitle(new StringResourceModel("NumericPadTitle", pad, null));
      return pad;
    } else {
      throw new UnsupportedOperationException("Pad for type " + type + " not supported yet.");
    }
  }

}
