package org.obiba.onyx.webapp.stage.page;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.ITransitionListener;
import org.obiba.onyx.engine.state.ITransitionSource;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.webapp.action.panel.ActionsPanel;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.interview.page.InterviewPage;
import org.obiba.onyx.wicket.action.ActionWindow;

public class StagePage extends BasePage {
  
  @SpringBean
  private ActiveInterviewService activeInterviewService;

  @SuppressWarnings("serial")
  public StagePage(Stage stage) {
    super();

    IStageExecution exec = activeInterviewService.getStageExecution(stage);
    
    final ActionWindow modal;
    add(modal = new ActionWindow("modal") {

      @Override
      public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
        IStageExecution exec = activeInterviewService.getStageExecution(stage);
        if(!exec.isInteractive()) {
          setResponsePage(InterviewPage.class);
        } else {
          setResponsePage(new StagePage(stage));
        }
      }
      
    });

    add(new ActionsPanel("action", stage, exec, modal));
    
    if(!exec.isInteractive()) {
      add(new EmptyPanel("stage-component"));
    } else {
      if(exec instanceof ITransitionSource) {
        ((ITransitionSource) exec).addTransitionListener(new StageInteractionEndListener());
      }
      add(exec.getWidget("stage-component"));
    }
  }

  @SuppressWarnings("serial")
  private class StageInteractionEndListener implements ITransitionListener, Serializable {

    private boolean remove = false;

    public void onTransition(IStageExecution execution, TransitionEvent event) {
      if(!execution.isInteractive()) {
        setResponsePage(InterviewPage.class);
        remove = true;
      }
    }

    public boolean removeAfterTransition() {
      return remove;
    }

  }

}
