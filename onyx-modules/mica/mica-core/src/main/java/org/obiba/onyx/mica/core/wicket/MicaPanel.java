/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.mica.core.wicket;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.reusable.FeedbackWindow;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.obiba.onyx.mica.core.wicket.wizard.ConclusionWizardForm;
import org.obiba.onyx.mica.domain.conclusion.Conclusion;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicaPanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(MicaPanel.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConclusionService activeConclusionService;

  @SpringBean
  private ModuleRegistry moduleRegistry;

  private ActionWindow actionWindow;

  private FeedbackWindow feedbackWindow;

  private MicaModel model;

  Conclusion interviewConclusion;

  @SuppressWarnings("serial")
  public MicaPanel(String id, Stage stage) {
    super(id);

    model = new MicaModel(new StageModel(moduleRegistry, stage.getName()), new Model(activeConclusionService.getConclusion(true)));

    add(new WizardPanel("content", model.getConclusionModel()) {

      @Override
      public WizardForm createForm(String componentId) {
        return new ConclusionWizardForm(componentId, getModel()) {

          @Override
          public void onCancel(AjaxRequestTarget target) {
            IStageExecution exec = activeInterviewService.getStageExecution(model.getStage());
            ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
            if(actionDef != null) {
              actionWindow.show(target, model.getStageModel(), actionDef);
            }
          }

          @Override
          public void onFinish(AjaxRequestTarget target, Form form) {
            IStageExecution exec = activeInterviewService.getStageExecution(model.getStage());
            ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);
            if(actionDef != null) {
              actionWindow.show(target, model.getStageModel(), actionDef);
            }
            activeConclusionService.save();
          }

          @Override
          public void onError(AjaxRequestTarget target, Form form) {
            showFeedbackWindow(target);
          }

          @Override
          public FeedbackWindow getFeedbackWindow() {
            return feedbackWindow;
          }

        };
      }

    });
  }

  @SuppressWarnings("serial")
  private class MicaModel implements Serializable {
    private IModel conclusionModel;

    private IModel stageModel;

    public MicaModel(IModel stageModel, IModel consentModel) {
      this.conclusionModel = consentModel;
      this.stageModel = stageModel;
    }

    public Conclusion getConclusion() {
      return (Conclusion) conclusionModel.getObject();
    }

    public IModel getConclusionModel() {
      return conclusionModel;
    }

    public Stage getStage() {
      return (Stage) stageModel.getObject();
    }

    public IModel getStageModel() {
      return stageModel;
    }

  }

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    this.feedbackWindow = feedbackWindow;
  }

  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }
}