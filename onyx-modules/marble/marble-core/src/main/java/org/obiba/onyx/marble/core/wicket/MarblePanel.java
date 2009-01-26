/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.core.wicket.wizard.ConsentWizardForm;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardPanel;

public class MarblePanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = 1L;

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

    setModel(new StageModel(moduleRegistry, stage.getName()));

    activeConsentService.getConsent(true);

    add(new WizardPanel("content", getModel()) {

      @Override
      public WizardForm createForm(String componentId) {
        return new ConsentWizardForm(componentId, getModel()) {

          @Override
          public void onCancel(AjaxRequestTarget target) {
            IStageExecution exec = activeInterviewService.getStageExecution(getStage());
            ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
            if(actionDef != null) {
              actionWindow.show(target, MarblePanel.this.getModel(), actionDef);
            }
          }

          @Override
          public void onFinish(AjaxRequestTarget target, Form form) {

            Boolean consentIsSubmitted = activeConsentService.isConsentFormSubmitted();
            Boolean consentIsAccepted = activeConsentService.getConsent().isAccepted();
            Boolean consentIsElectronic = activeConsentService.getConsent().getMode() == ConsentMode.ELECTRONIC ? true : false;

            // Consent not submitted, inform the user that the submit button (PDF form) has to be clicked.
            if(!consentIsSubmitted) {
              error(getString("MissingConsentForm"));
              target.appendJavascript("resizeWizardContent();");

              // Invalid electronic consent.
            } else if(consentIsAccepted && consentIsElectronic && !activeConsentService.validateElectronicConsent()) {
              error(getString("InvalidConsentForm"));
              getElectronicConsentStep().setNextStep(null);
              gotoNext(target);
              this.changeWizardFormStyle("wizard-consent");
              target.appendJavascript("resizeWizardContent();");

              // Valid electronic consent, refused electronic consent, or manual consent.
            } else {
              IStageExecution exec = activeInterviewService.getStageExecution(getStage());
              ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);
              target.appendJavascript("resizeWizardContent();");
              if(actionDef != null) {
                actionWindow.show(target, MarblePanel.this.getModel(), actionDef);
              }
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

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    this.feedbackPanel = feedbackPanel;
  }

  public FeedbackPanel getFeedbackPanel() {
    return feedbackPanel;
  }

  protected Stage getStage() {
    return (Stage) getModelObject();
  }

}
