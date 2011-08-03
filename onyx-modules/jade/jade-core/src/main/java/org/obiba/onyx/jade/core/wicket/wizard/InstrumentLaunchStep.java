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
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentLaunchPanel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;

public class InstrumentLaunchStep extends WizardStepPanel {

  private static final long serialVersionUID = -2511672064460152210L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentLaunchStep.class);

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private boolean launched = false;

  public InstrumentLaunchStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label("title", new StringResourceModel("InstrumentApplicationLaunch", this, null)));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setVisible(getPreviousStep() != null);
    form.getFinishLink().setVisible(false);
    form.getNextLink().setVisible(isEnableNextLink(form));

    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }

  }

  private boolean isEnableNextLink(WizardForm form) {
    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    if(instrumentType.isRepeatable()) {
      // minimum is having the expected count of repeatable measures
      int currentCount = activeInstrumentRunService.getInstrumentRun().getValidMeasureCount();
      int expectedCount = instrumentType.getExpectedMeasureCount(activeInstrumentRunService.getParticipant());
      boolean skipped = ((InstrumentLaunchPanel) get(getContentId())).getSkipMeasurement();
      if(currentCount < expectedCount && !skipped) {
        return false;
      } else {
        return true;
      }
    } else {
      return true;
    }
  }

  @SuppressWarnings("serial")
  @Override
  public void onStepInNext(final WizardForm form, AjaxRequestTarget target) {
    super.onStepInNext(form, target);
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
    super.onStepOutNext(form, target);

    if(launched || instrumentRunContainsValues()) {

      if(InstrumentRunStatus.IN_ERROR.equals(activeInstrumentRunService.getInstrumentRun().getStatus())) {
        form.error(getString("InstrumentApplicationError"));
        setNextStep(null);
      } else {

        InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();

        List<InstrumentOutputParameter> outputParams = instrumentType.getOutputParameters(InstrumentParameterCaptureMethod.AUTOMATIC);

        List<InstrumentParameter> missingValueParams = new ArrayList<InstrumentParameter>();

        if(!instrumentType.isRepeatable()) {

          if(isOutputParamCapturedManually()) {
            for(InstrumentOutputParameter param : instrumentType.getOutputParameters(InstrumentParameterCaptureMethod.MANUAL)) {
              if(param.isManualCaptureAllowed()) {
                InstrumentRunValue runValue = activeInstrumentRunService.getInstrumentRunValue(param.getCode());
                if(param.isRequired(activeInstrumentRunService.getParticipant()) && runValue == null) {
                  missingValueParams.add(param);
                }
              }
            }
          } else {
            for(InstrumentOutputParameter param : outputParams) {
              InstrumentRunValue runValue = activeInstrumentRunService.getInstrumentRunValue(param.getCode());
              if(param.isRequired(activeInstrumentRunService.getParticipant())) {
                if(runValue == null) {
                  missingValueParams.add(param);
                } else {
                  Data data = runValue.getData(param.getDataType());
                  if(data == null || data.getValue() == null) {
                    missingValueParams.add(param);
                  }
                }
              }
            }
          }

          if(missingValueParams.size() == 0) {
            // Perform each output parameter's integrity checks.
            Map<IntegrityCheck, InstrumentOutputParameter> failedChecks = activeInstrumentRunService.checkIntegrity(outputParams);

            for(Map.Entry<IntegrityCheck, InstrumentOutputParameter> entry : failedChecks.entrySet()) {
              MessageSourceResolvable resolvable = entry.getKey().getDescription(entry.getValue(), activeInstrumentRunService);
              form.error((String) new MessageSourceResolvableStringModel(resolvable).getObject());
            }

            if(failedChecks.isEmpty()) {
              ((InstrumentWizardForm) form).setUpWizardFlow(null);
            } else {
              setNextStep(null);
            }
          } else {
            log.warn("Missing values for the parameters: {}", missingValueParams);
            form.error(getString("NoInstrumentDataSaveThem"));
            setNextStep(null);
          }
        } else {
          int currentCount = activeInstrumentRunService.getInstrumentRun().getValidMeasureCount();
          if(!((InstrumentLaunchPanel) get(getContentId())).getSkipMeasurement() || currentCount == 0) {

            // minimum is having the expected count of repeatable measures
            int expectedCount = instrumentType.getExpectedMeasureCount(activeInstrumentRunService.getParticipant());
            if(currentCount < expectedCount) {
              form.error(getString("MissingMeasure", new Model(new ValueMap("count=" + (expectedCount - currentCount)))));
              setNextStep(null);
            }
          }
        }
      }

    } else {
      form.error(getString("InstrumentApplicationMustBeStarted"));
      setNextStep(null);
    }

  }

  private boolean isOutputParamCapturedManually() {
    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    List<InstrumentOutputParameter> outputParams = instrumentType.getOutputParameters(InstrumentParameterCaptureMethod.MANUAL);
    for(InstrumentOutputParameter param : outputParams) {
      InstrumentRunValue runValue = activeInstrumentRunService.getInstrumentRunValue(param.getCode());
      if(param.isManualCaptureAllowed() && runValue != null && runValue.getCaptureMethod().equals(InstrumentParameterCaptureMethod.MANUAL)) {
        return true;
      }
    }
    return false;
  }

  private boolean instrumentRunContainsValues() {
    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run.getInstrumentRunValues() != null && run.getInstrumentRunValues().size() > 0) return true;
    if(run.getValidMeasureCount() > 0) return true;
    return false;
  }
}
