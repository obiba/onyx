package org.obiba.onyx.mica.core.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicaPanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = -6692482689347742363L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(MicaPanel.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConclusionService activeConclusionService;

  @SpringBean
  private ModuleRegistry moduleRegistry;

  private ActionWindow actionWindow;

  private FeedbackPanel feedbackPanel;

  @SuppressWarnings("serial")
  public MicaPanel(String id, Stage stage) {
    super(id);

    final String stageName = stage.getName();

    Form form = new Form("form");
    add(form);

    form.add(new CheckBox("cb", new PropertyModel(activeConclusionService, "conclusion")));

    form.add(new AjaxButton("submit", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        log.info("conclusion=" + activeConclusionService.getConclusion());
        IStageExecution exec = activeInterviewService.getStageExecution(stageName);
        ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);
        if(actionDef != null) {
          actionWindow.show(target, new StageModel(moduleRegistry, stageName), actionDef);
        }
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        target.addComponent(feedbackPanel);
      }

    });

    form.add(new AjaxLink("cancel") {

      public void onClick(AjaxRequestTarget target) {
        IStageExecution exec = activeInterviewService.getStageExecution(stageName);
        ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
        if(actionDef != null) {
          actionWindow.show(target, new StageModel(moduleRegistry, stageName), actionDef);
        }
      }

    });
  }

  public void setActionWindwon(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    this.feedbackPanel = feedbackPanel;
  }

  public FeedbackPanel getFeedbackPanel() {
    return feedbackPanel;
  }

}
