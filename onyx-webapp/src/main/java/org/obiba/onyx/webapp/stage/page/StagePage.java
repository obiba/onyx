/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.page.InterviewPage;
import org.obiba.onyx.webapp.stage.panel.StageMenuBar;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.action.ActionWindowProvider;
import org.obiba.onyx.wicket.behavior.ajaxbackbutton.HistoryAjaxBehavior;
import org.obiba.onyx.wicket.behavior.ajaxbackbutton.HistoryIFrame;
import org.obiba.onyx.wicket.behavior.ajaxbackbutton.IHistoryAjaxBehaviorOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation({ "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class StagePage extends BasePage implements IHistoryAjaxBehaviorOwner, ActionWindowProvider {

  private static final Logger log = LoggerFactory.getLogger(StagePage.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private HistoryAjaxBehavior historyAjaxBehavior;

  private ActionWindow actionWindow;

  @SuppressWarnings("serial")
  public StagePage(final IModel<Stage> stageModel) {
    super();
    setDefaultModel(stageModel);

    Participant participant = activeInterviewService.getParticipant();

    if(participant == null) {
      setResponsePage(WebApplication.get().getHomePage());
    } else {
      //
      // Modify header.
      //
      remove("header");
      add(new EmptyPanel("header"));

      //
      // Modify menu bar.
      //
      remove("menuBar");
      add(new StageMenuBar("menuBar", stageModel));

      add(actionWindow = new ActionWindow("modal") {

        @Override
        public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
          IStageExecution exec = activeInterviewService.getStageExecution(stage);
          if(!exec.isInteractive()) {
            log.debug("BACK from Stage {}/{}/{}", new Object[] { stage.getName(), action.getDateTime(), exec.getName() });
            setResponsePage(InterviewPage.class);
          } else {
            log.debug("STAY in stage");
            setResponsePage(new StagePage(stageModel));
          }
        }

      });

      // The hidden (CSS position) iframe which will capture the back/forward button clicks
      HistoryIFrame historyIFrame = new HistoryIFrame("historyIframe", getPageMap());
      add(historyIFrame);

      // The Ajax behavior which will be called after each click on back/forward buttons
      historyAjaxBehavior = new HistoryAjaxBehavior() {

        private static final long serialVersionUID = 1L;

        @Override
        public void onAjaxHistoryEvent(final AjaxRequestTarget target, final String componentId) {
          log.info("onAjaxHistoryEvent.component={}", componentId);
          setResponsePage(new InvalidStagePage(StagePage.this));
        }

      };
      add(historyAjaxBehavior);

      initializeStageComponent();
    }
  }

  private void initializeStageComponent() {

    IStageExecution exec = activeInterviewService.getStageExecution((Stage) getDefaultModelObject());

    if(!exec.isInteractive()) {
      add(new EmptyPanel("stage-component"));
    } else {
      Component stageComponent = exec.getWidget("stage-component");
      if(stageComponent == null) {
        throw new IllegalStateException("stage is interactive but dit not provide a Component to display");
      }
      add(stageComponent);
    }
  }

  @Override
  public ActionWindow getActionWindow() {
    return actionWindow;
  }

  public HistoryAjaxBehavior getHistoryAjaxBehavior() {
    return historyAjaxBehavior;
  }

}
