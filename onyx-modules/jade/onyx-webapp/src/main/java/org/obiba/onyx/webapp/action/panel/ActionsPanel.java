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
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class ActionsPanel extends Panel {

  private static final long serialVersionUID = 5855667390712874428L;

  @SpringBean
  private EntityQueryService queryService;

  @SuppressWarnings( { "serial", "serial" })
  public ActionsPanel(String id, final Stage stage, IStageExecution exec, final ActionWindow modal) {
    super(id);
    setOutputMarkupId(true);
    setModel(new DetachableEntityModel(queryService, stage));
    
    RepeatingView repeating = new RepeatingView("repeating");
    add(repeating);

    for(final ActionDefinition actionDef : exec.getActionDefinitions()) {
      WebMarkupContainer item = new WebMarkupContainer(repeating.newChildId());
      repeating.add(item);

      AjaxLink link = new AjaxLink("link") {

        @Override
        public void onClick(AjaxRequestTarget target) {
          modal.show(target, stage, actionDef);
        }

      };
      link.add(new Label("action", new Model(actionDef.getLabel())));
      item.add(link);

    }

  }

}
