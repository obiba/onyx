/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.wicket.wizard.InstrumentWizardForm;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class JadePanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = -6692482689347742363L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ModuleRegistry moduleRegistry;

  private ActionWindow actionWindow;

  private FeedbackPanel feedbackPanel;

  private JadeModel model;

  @SuppressWarnings("serial")
  public JadePanel(String id, Stage stage) {
    super(id);
    InstrumentType type = getInstrumentType(stage);
    setModel(model = new JadeModel(new StageModel(moduleRegistry, stage.getName()), new DetachableEntityModel(queryService, type)));

    add(new WizardPanel("content", model.getIntrumentTypeModel()) {

      @Override
      public WizardForm createForm(String componentId) {
        return new InstrumentWizardForm(componentId, getModel()) {

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
          }

          @Override
          public void onError(AjaxRequestTarget target, Form form) {
            target.addComponent(feedbackPanel);
          }

          @Override
          public FeedbackPanel getFeedbackPanel() {
            return feedbackPanel;
          }

        };
      }

    });
  }

  private InstrumentType getInstrumentType(Stage stage) {
    InstrumentType template = new InstrumentType(stage.getName(), null);
    return queryService.matchOne(template);
  }

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    this.feedbackPanel = feedbackPanel;
  }

  public FeedbackPanel getFeedbackPanel() {
    return feedbackPanel;
  }

  @SuppressWarnings("serial")
  private class JadeModel extends AbstractReadOnlyModel {

    private IModel intrumentTypeModel;

    private IModel stageModel;

    public JadeModel(IModel stageModel, IModel instrumentTypeModel) {
      this.intrumentTypeModel = instrumentTypeModel;
      this.stageModel = stageModel;
    }

    public InstrumentType getIntrumentType() {
      return (InstrumentType) intrumentTypeModel.getObject();
    }

    public Stage getStage() {
      return (Stage) stageModel.getObject();
    }

    public IModel getIntrumentTypeModel() {
      return intrumentTypeModel;
    }

    public IModel getStageModel() {
      return stageModel;
    }

    @Override
    public void detach() {
      this.stageModel.detach();
      this.intrumentTypeModel.detach();
      super.detach();
    }

    @Override
    public Object getObject() {
      return null;
    }
  }

}
