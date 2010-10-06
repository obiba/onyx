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

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategoryLinkSelectionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.behavior.QuestionCategorySelectionBehavior;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.pad.NumericPad;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A link for selecting a question category, without open answers.
 */
public class QuestionCategoryOpenAnswerDefinitionPanel extends AbstractQuestionCategoryLinkSelectionPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoryOpenAnswerDefinitionPanel.class);

  //
  // Instance Variables
  //

  private IModel openAnswerDefinitionModel;

  private AbstractOpenAnswerDefinitionPanel pad;

  private ModalWindow padWindow;

  //
  // Constructors
  //

  @SuppressWarnings("serial")
  public QuestionCategoryOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel labelModel, IModel descriptionModel) {
    super(id, questionModel, questionCategoryModel, labelModel, descriptionModel);
  }

  //
  // AbstractQuestionCategoryLinkSelectionPanel Methods
  //

  protected void addLinkComponent(IModel labelModel, IModel descriptionModel) {
    Label value = new Label("value", new PropertyModel(this, "openValue"));
    value.setOutputMarkupId(true).add(new QuestionCategorySelectionBehavior());
    value.add(new AjaxEventBehavior("onclick") {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        OpenAnswer openAnswer = getOpenAnswer();
        pad.setData(openAnswer != null ? openAnswer.getData() : null);
        padWindow.show(target);
      }

    });
    add(value);

    // Create pad modal window
    add(padWindow = createPadModalWindow("padModal"));

    padWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public void onClose(AjaxRequestTarget target) {
        if(getOpenAnswer() == null) {
          activeQuestionnaireAdministrationService.deleteAnswer(getQuestion(), getQuestionCategory());
        }
        target.addComponent(QuestionCategoryOpenAnswerDefinitionPanel.this);
        QuestionCategoryOpenAnswerDefinitionPanel.this.fireSelectionEvent(target, isSelected());
      }
    });
  }

  private ModalWindow createPadModalWindow(String padId) {
    ModalWindow newPadWindow = new ModalWindow(padId);

    DataType type = getOpenAnswerDefinition().getDataType();
    if(type.equals(DataType.INTEGER) || type.equals(DataType.DECIMAL)) {
      pad = new NumericPad(newPadWindow.getContentId(), getQuestionModel(), getQuestionCategoryModel(), getOpenAnswerDefinitionModel()) {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isRequired() {
          // is never required because in a array the pad allows deselecting the category when setting a null value
          return false;
        }
      };

      newPadWindow.setTitle(new StringResourceModel("NumericPadTitle", pad, null));
      newPadWindow.setContent(pad);
      newPadWindow.setCssClassName("onyx");
      newPadWindow.setInitialWidth(288);
      newPadWindow.setInitialHeight(365);
      newPadWindow.setResizable(false);

      // same as cancel
      newPadWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public boolean onCloseButtonClicked(AjaxRequestTarget target) {
          return true;
        }
      });

      return newPadWindow;
    }
    throw new UnsupportedOperationException("Pad for type " + type + " not supported yet.");
  }

  public OpenAnswer getOpenAnswer() {
    return activeQuestionnaireAdministrationService.findOpenAnswer(getQuestion(), getQuestionCategory().getCategory(), getOpenAnswerDefinition());
  }

  public String getOpenValue() {
    OpenAnswer answer = getOpenAnswer();
    String val = null;

    if(answer != null && answer.getData() != null) {
      val = answer.getData().getValueAsString();
    }

    return val;
  }

  @Override
  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return (OpenAnswerDefinition) getOpenAnswerDefinitionModel().getObject();
  }

  public IModel getOpenAnswerDefinitionModel() {
    if(openAnswerDefinitionModel == null) {
      openAnswerDefinitionModel = new QuestionnaireModel(getQuestionCategory().getOpenAnswerDefinition());
    }
    return openAnswerDefinitionModel;
  }

}
