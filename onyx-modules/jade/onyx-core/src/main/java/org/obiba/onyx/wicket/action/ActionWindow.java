package org.obiba.onyx.wicket.action;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ActionWindow extends Panel {

  private static final long serialVersionUID = -3711214735708110972L;

  private static final Logger log = LoggerFactory.getLogger(ActionWindow.class);

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  private ModalWindow modal;
  
  private Stage stage;

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
          activeInterviewService.doAction(stage, action);
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
  public void show(AjaxRequestTarget target, Stage stage, ActionDefinition actionDefinition) {
    this.stage = stage;
    modal.setContent(new ActionDefinitionPanel(modal.getContentId(), actionDefinition) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        modal.close(target);
      }
      
    });
    modal.setTitle("New Action.");
    modal.show(target);
  }

}
