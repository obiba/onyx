/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.action;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.Stage;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ActionWindow extends Panel {

  private static final long serialVersionUID = -3711214735708110972L;

  private static final Logger log = LoggerFactory.getLogger(ActionWindow.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private ModalWindow modal;

  @SuppressWarnings("serial")
  public ActionWindow(String id) {
    super(id);

    add(modal = new ModalWindow("modal"));
    modal.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        // same as cancel
        return true;
      }
    });

    modal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target) {
        ActionDefinitionPanel pane = (ActionDefinitionPanel) modal.get(modal.getContentId());
        if(!pane.isCancelled()) {
          Action action = pane.getAction();
          log.info("action=" + action);
          Stage stage = null;
          if(getModel() != null && getModelObject() != null) {
            stage = (Stage) getModelObject();
          }
          activeInterviewService.doAction(stage, action, activeInterviewService.getInterview().getUser());
          onActionPerformed(target, stage, action);
        }
      }
    });

  }

  /**
   * On action performed.
   * @param target
   */
  public abstract void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action);

  public void close(AjaxRequestTarget target) {
    modal.close(target);
  }

  public void setTitle(String title) {
    modal.setTitle(title);
  }

  public void setTitle(IModel title) {
    modal.setTitle(title);
  }

  @SuppressWarnings("serial")
  public void show(AjaxRequestTarget target, IModel stageModel, ActionDefinition actionDefinition) {
    setModel(stageModel);
    modal.setContent(new ActionDefinitionPanel(modal.getContentId(), actionDefinition, target) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        modal.close(target);
      }

    });

    final IModel labelModel = new MessageSourceResolvableStringModel(actionDefinition.getLabel());
    if(stageModel != null && stageModel.getObject() != null) {
      Model titleModel = new Model() {
        @Override
        public Object getObject() {
          Stage stage = (Stage) ActionWindow.this.getModelObject();
          MessageSourceResolvableStringModel stageDescriptionModel = new MessageSourceResolvableStringModel(stage.getDescription());
          return stageDescriptionModel.getObject() + ": " + labelModel.getObject();
        }
      };
      modal.setTitle(titleModel);
    } else {
      modal.setTitle(labelModel);
    }
    modal.show(target);
  }
}
