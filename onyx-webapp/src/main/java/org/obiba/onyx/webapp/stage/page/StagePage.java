package org.obiba.onyx.webapp.stage.page;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.webapp.action.panel.ActionsPanel;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.page.InterviewPage;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.action.ActionWindow;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class StagePage extends BasePage {

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  @SuppressWarnings("serial")
  public StagePage(IModel stageModel) {
    super();
    setModel(stageModel);

    Participant participant = activeInterviewService.getParticipant();

    if(participant == null) {
      setResponsePage(WebApplication.get().getHomePage());
    } else {

      IStageExecution exec = activeInterviewService.getStageExecution((Stage) getModelObject());

      final ActionWindow modal;
      add(modal = new ActionWindow("modal") {

        @Override
        public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
          IStageExecution exec = activeInterviewService.getStageExecution(stage);
          if(!exec.isInteractive()) {
            setResponsePage(InterviewPage.class);
          } else {
            setResponsePage(new StagePage(StagePage.this.getModel()));
          }
        }

      });

      add(new ActionsPanel("action", getModel(), exec, modal));

      if(!exec.isInteractive()) {
        add(new EmptyPanel("stage-component"));
      } else {
        Component stageComponent = exec.getWidget("stage-component");
        if(stageComponent instanceof IEngineComponentAware) {
          IEngineComponentAware comp = (IEngineComponentAware) stageComponent;
          comp.setActionWindwon(modal);
          comp.setFeedbackPanel(getFeedbackPanel());
        }
        add(stageComponent);
      }
    }
  }

}
