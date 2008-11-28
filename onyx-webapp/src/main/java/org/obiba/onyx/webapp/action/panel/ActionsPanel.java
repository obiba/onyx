/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.action.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;

public class ActionsPanel extends Panel {

  private static final long serialVersionUID = 5855667390712874428L;

  @SuppressWarnings( { "serial", "serial" })
  public ActionsPanel(String id, IModel stageModel, IStageExecution exec, final ActionWindow modal) {
    super(id);
    setOutputMarkupId(true);
    setModel(stageModel);

    RepeatingView repeating = new RepeatingView("repeating");
    add(repeating);

    for(final ActionDefinition actionDef : exec.getActionDefinitions()) {
      WebMarkupContainer item = new WebMarkupContainer(repeating.newChildId());
      repeating.add(item);

      AjaxLink link = new AjaxLink("link") {

        @Override
        public void onClick(AjaxRequestTarget target) {
          modal.show(target, ActionsPanel.this.getModel(), actionDef);
        }

      };
      link.add(new Label("action", new MessageSourceResolvableStringModel(actionDef.getLabel())));
      item.add(link);

    }

  }

}
