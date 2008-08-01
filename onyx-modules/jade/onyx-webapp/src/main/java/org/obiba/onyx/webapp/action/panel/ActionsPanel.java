package org.obiba.onyx.webapp.action.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.webapp.stage.page.StagePage;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public abstract class ActionsPanel extends Panel {

  private static final long serialVersionUID = 5855667390712874428L;
  
  @SpringBean
  private EntityQueryService queryService;
  
  @SpringBean
  private ActiveInterviewService activeInterviewService;

  public ActionsPanel(String id, final Stage stage, IStageExecution exec) {
    super(id);
    setOutputMarkupId(true);
    setModel(new DetachableEntityModel(queryService, stage));
    
    RepeatingView repeating = new RepeatingView("repeating");
    add(repeating);
    
    for (final ActionDefinition actionDef : exec.getActions()) {
      WebMarkupContainer item = new WebMarkupContainer(repeating.newChildId());
      repeating.add(item);
      
      AjaxLink link = new AjaxLink("link") {

        @Override
        public void onClick(AjaxRequestTarget target) {
          Action action = new Action(actionDef);
          activeInterviewService.doAction(stage, action);
          onActionPerformed(target, stage, action);
          IStageExecution exec = activeInterviewService.getStageExecution(stage);
          if (exec.isInteractive()) {
            setResponsePage(new StagePage(ActionsPanel.this.getModel()));
          }
          else {
            target.addComponent(ActionsPanel.this);
          }
        }
        
      };
      link.add(new Label("action", new Model(actionDef.getLabel())));
      item.add(link);
      
    }
    
  }
  
  /**
   * On action performed.
   * @param target
   */
  public abstract void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action);

}
