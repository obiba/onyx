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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.webapp.participant.page.InterviewPage;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.wicket.markup.html.border.SeparatorMarkupComponentBorder;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionsPanel extends Panel {

  private static final long serialVersionUID = 5855667390712874428L;

  private static final Logger log = LoggerFactory.getLogger(ActionsPanel.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  public ActionsPanel(String id, IModel stageModel, final ActionWindow modal) {
    super(id, stageModel);
    setOutputMarkupId(true);

    RepeatingView repeating = new RepeatingView("link");
    add(repeating);
    SeparatorMarkupComponentBorder border = new SeparatorMarkupComponentBorder();
    for(ActionDefinition actionDef : getStageExecution().getActionDefinitions()) {

      AjaxLink link = new AjaxLink(repeating.newChildId(), new Model(actionDef.getCode())) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          // Before allowing the action, make sure our actionDefinition is sill available
          // in the IStageExecution. This protects concurrent interview administration.
          // See ONYX-154
          IStageExecution stageExec = getStageExecution();
          String code = (String) getModelObject();
          ActionDefinition actionDefinition = null;
          for(ActionDefinition definition : stageExec.getActionDefinitions()) {
            if(code.equals(definition.getCode())) {
              actionDefinition = definition;
              break;
            }
          }
          if(actionDefinition != null) {
            modal.show(target, (IModel<Stage>) ActionsPanel.this.getDefaultModel(), actionDefinition);
          } else {
            log.warn("Concurrent interview administration. Session {} tried to execute ActionDefinition {} on stage {}, yet that ActionDefinition is not available for the current stage's state.", new Object[] { WebSession.get().getId(), getModelObject(), getStage() });
            setResponsePage(InterviewPage.class);
          }
        }

      };
      link.add(new Label("action", new MessageSourceResolvableStringModel(actionDef.getLabel())).setRenderBodyOnly(true));
      link.setComponentBorder(border);
      repeating.add(link);
    }
  }

  @Override
  public boolean isVisible() {
    // If the this panel is not for the current interactive stage, then it should NOT be visible.
    // Otherwise, we let the super class determine our visibility

    Stage interactiveStage = activeInterviewService.getInteractiveStage();
    if(interactiveStage != null) {
      // Test that this panel's stage is not for the interactive stage
      if(getStage().getName().equals(interactiveStage.getName()) == false) {
        return false;
      }
    }
    return super.isVisible();
  }

  private Stage getStage() {
    return (Stage) getDefaultModelObject();
  }

  private IStageExecution getStageExecution() {
    return activeInterviewService.getStageExecution(getStage());
  }
}
