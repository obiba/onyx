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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StagePage extends BasePage {

  private static final Logger log = LoggerFactory.getLogger(StagePage.class);

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  @SuppressWarnings("serial")
  public StagePage(IModel stageModel) {
    super();

    Stage stage = (Stage) stageModel.getObject();
    IStageExecution exec = activeInterviewService.getStageExecution(stage);

    add(new ActionsPanel("action", stage, exec) {

      @Override
      public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
        IStageExecution exec = activeInterviewService.getStageExecution(stage);
        if (!exec.isInteractive()) {
          setResponsePage(InterviewPage.class);
        }
        else {
          setResponsePage(new StagePage(StagePage.this.getModel()));
        }
      }

    });

    if(!exec.isInteractive()) {
      add(new EmptyPanel("stage-component"));
    } else {
      if (exec instanceof ITransitionSource) {
        ((ITransitionSource)exec).addTransitionListener(new StageInteractionEndListener());
      }
      add(exec.getWidget("stage-component"));
    }
  }
  
  @SuppressWarnings("serial")
  private class StageInteractionEndListener implements ITransitionListener, Serializable {

    public void onTransition(IStageExecution execution, TransitionEvent event) {
      if (!execution.isInteractive()) {
        setResponsePage(InterviewPage.class);
      }
    }

    public boolean isListening() {
      return false;
    }

  }

}
