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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheckType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentOutputParameterPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class OutputParametersStep extends WizardStepPanel {

  private static final long serialVersionUID = 6617334507631332206L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private InstrumentOutputParameterPanel instrumentOutputParameterPanel;

  private WizardStepPanel conclusionStep;

  private WizardStepPanel warningsStep;

  public OutputParametersStep(String id, WizardStepPanel conclusionStep, WizardStepPanel warningsStep) {
    super(id);

    this.conclusionStep = conclusionStep;
    this.warningsStep = warningsStep;

    setOutputMarkupId(true);
    add(new Label("title", new StringResourceModel("ProvideTheFollowingInformation", OutputParametersStep.this, null)));

    add(new EmptyPanel("panel"));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getNextLink().setEnabled(true);
    form.getPreviousLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getNextLink());
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getFinishLink());
    }
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    setContent(target, instrumentOutputParameterPanel = new InstrumentOutputParameterPanel(getContentId()));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    instrumentOutputParameterPanel.saveOutputInstrumentRunValues();
    activeInstrumentRunService.computeOutputParameters();

    List<InstrumentOutputParameter> paramsWithWarnings = getParametersWithWarnings();

    if(!paramsWithWarnings.isEmpty()) {
      warn(getString("ThereAreWarnings"));
      ((WarningsStep) warningsStep).setParametersWithWarnings(paramsWithWarnings);
      setNextStep(warningsStep);
    } else {
      setNextStep(conclusionStep);
    }
  }

  private List<InstrumentOutputParameter> getParametersWithWarnings() {
    List<InstrumentOutputParameter> paramsWithWarnings = new ArrayList<InstrumentOutputParameter>();

    // Query the output parameters.
    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument(activeInstrumentRunService.getInstrument());
    template.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);

    List<InstrumentOutputParameter> outputParams = queryService.match(template);

    for(InstrumentOutputParameter param : outputParams) {
      InstrumentRunValue runValue = activeInstrumentRunService.getOutputInstrumentRunValue(param.getName());

      // Don't check parameters that haven't been assigned a value.
      if(runValue == null || runValue.getData() == null || runValue.getData().getValue() == null) {
        continue;
      }

      for(IntegrityCheck check : param.getIntegrityChecks()) {
        // Skip non-warning checks.
        if(!check.getType().equals(IntegrityCheckType.WARNING)) {
          continue;
        }

        if(!check.checkParameterValue(runValue.getData(), null, activeInstrumentRunService)) {
          paramsWithWarnings.add(param);
          break;
        }
      }
    }

    return paramsWithWarnings;
  }
}
