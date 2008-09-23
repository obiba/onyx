package org.obiba.onyx.marble.core.wicket;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarblePanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = -6692482689347742363L;

  private static final Logger log = LoggerFactory.getLogger(MarblePanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConsentService activeConsentService;

  @SpringBean
  private ModuleRegistry moduleRegistry;

  private ActionWindow actionWindow;

  private FeedbackPanel feedbackPanel;

  @SuppressWarnings("serial")
  public MarblePanel(String id, Stage stage) {
    super(id);

    final String stageName = stage.getName();

    Form form = new Form("form");
    add(form);

    form.add(new CheckBox("cb", new PropertyModel(activeConsentService, "consent")));
    AjaxButton button;
    form.add(button = new AjaxButton("submit", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        log.info("consent=" + activeConsentService.getConsent());
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
    button.add(new AttributeModifier("value", new StringResourceModel("Submit", this, null)));

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
