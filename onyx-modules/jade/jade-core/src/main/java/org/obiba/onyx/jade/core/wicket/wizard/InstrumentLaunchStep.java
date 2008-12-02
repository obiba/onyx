/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.wizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.validation.AbstractIntegrityCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheckType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentLaunchPanel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.wicket.application.ISpringWebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentLaunchStep extends WizardStepPanel {

  private static final long serialVersionUID = -2511672064460152210L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentLaunchStep.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private UserSessionService userSessionService;

  private boolean launched = false;

  public InstrumentLaunchStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label("title", new StringResourceModel("InstrumentApplicationLaunch", this, null)));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(getPreviousStep() != null);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

  @SuppressWarnings("serial")
  @Override
  public void onStepInNext(final WizardForm form, AjaxRequestTarget target) {
    setContent(target, new InstrumentLaunchPanel(getContentId()) {

      @Override
      public void onInstrumentLaunch() {
        log.info("onInstrumentLaunch");
        activeInstrumentRunService.setInstrumentRunStatus(InstrumentRunStatus.IN_PROGRESS);
        launched = true;
      }

    });
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    if(launched) {
      if(InstrumentRunStatus.IN_ERROR.equals(activeInstrumentRunService.getInstrumentRunStatus())) {
        error(getString("InstrumentApplicationError"));
        setNextStep(null);
      } else {
        InstrumentOutputParameter template = new InstrumentOutputParameter();
        template.setCaptureMethod(InstrumentParameterCaptureMethod.AUTOMATIC);
        template.setInstrument(activeInstrumentRunService.getInstrument());

        List<InstrumentOutputParameter> outputParams = queryService.match(template);

        boolean completed = true;

        for(InstrumentOutputParameter param : outputParams) {
          InstrumentRunValue runValue = activeInstrumentRunService.getOutputInstrumentRunValue(param.getName());
          Data data = runValue.getData();
          if(data == null || data.getValue() == null) {
            error(getString("NoInstrumentDataSaveThem"));
            setNextStep(null);
            completed = false;
            break;
          }
        }

        if(completed) {
          // Perform each output parameter's integrity checks.
          List<IntegrityCheck> failedChecks = checkIntegrity(outputParams);

          if(failedChecks.isEmpty()) {
            ((InstrumentWizardForm) form).setUpWizardFlow();
          } else {
            for(IntegrityCheck failedCheck : failedChecks) {
              // Set the integrity check's context and user session service to ensure
              // proper localization of the error message.
              failedCheck.setApplicationContext(((ISpringWebApplication) Application.get()).getSpringContextLocator().getSpringContext());
              failedCheck.setUserSessionService(userSessionService);

              error(failedCheck.getDescription(activeInstrumentRunService));
            }

            setNextStep(null);
          }
        }
      }

    } else {
      error(getString("InstrumentApplicationMustBeStarted"));
      setNextStep(null);
    }
  }

  /**
   * For each output parameter, performs all integrity checks of type <code>ERROR</code>.
   * 
   * @param outputParams output parameters
   * @return list of integrity checks that failed (empty list if none)
   */
  private List<IntegrityCheck> checkIntegrity(List<InstrumentOutputParameter> outputParams) {
    List<IntegrityCheck> failedChecks = new ArrayList<IntegrityCheck>();

    for(InstrumentOutputParameter param : outputParams) {
      List<AbstractIntegrityCheck> integrityChecks = param.getIntegrityChecks();

      for(AbstractIntegrityCheck integrityCheck : integrityChecks) {
        // Skip non-ERROR type checks.
        if(!integrityCheck.getType().equals(IntegrityCheckType.ERROR)) {
          continue;
        }

        InstrumentRunValue runValue = activeInstrumentRunService.getOutputInstrumentRunValue(param.getName());
        Data paramData = (runValue != null) ? runValue.getData() : null;

        if(!integrityCheck.checkParameterValue(paramData, null, activeInstrumentRunService)) {
          failedChecks.add(integrityCheck);
          break; // stop checking parameter after first failure (but continue checking other parameters!)
        }
      }
    }

    return failedChecks;
  }
}
