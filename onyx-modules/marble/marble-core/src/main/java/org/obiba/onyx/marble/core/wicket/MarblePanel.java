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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.core.wicket.wizard.ConsentWizardForm;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarblePanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(MarblePanel.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConsentService activeConsentService;

  @SpringBean
  private ConsentService consentService;

  @SpringBean
  private ModuleRegistry moduleRegistry;

  private ActionWindow actionWindow;

  private FeedbackWindow feedbackWindow;

  @SuppressWarnings("serial")
  public MarblePanel(String id, Stage stage) {
    super(id);

    setModel(new StageModel(moduleRegistry, stage.getName()));

    add(new WizardPanel("content", new Model(activeConsentService.getConsent(true))) {

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

              // Invalid electronic consent.
            } else if(consentIsAccepted && consentIsElectronic && !activeConsentService.validateElectronicConsent()) {
              error(getString("InvalidConsentForm"));
              getElectronicConsentStep().setNextStep(null);
              gotoNext(target);
              this.changeWizardFormStyle("wizard-consent");

              // Valid electronic consent, refused electronic consent, or manual consent.
            } else {
              IStageExecution exec = activeInterviewService.getStageExecution(getStage());
              ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);

              // Delete previous consent (if exist) for that interview
              Consent existingConsent = consentService.getConsent(activeInterviewService.getInterview());
              if(existingConsent != null) {
                consentService.deletePreviousConsent(activeInterviewService.getInterview());
              }

              // Save the consent
              consentService.saveConsent(activeConsentService.getConsent());

              if(actionDef != null) {
                actionWindow.show(target, MarblePanel.this.getModel(), actionDef);
              }
            }

            target.appendJavascript("Resizer.resizeWizard();");
            target.appendJavascript("Resizer.resizeConsentFrame();");
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

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    this.feedbackWindow = feedbackWindow;
  }

  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

  protected Stage getStage() {
    return (Stage) getModelObject();
  }

  public void setConsentService(ConsentService consentService) {
    this.consentService = consentService;
  }

}
