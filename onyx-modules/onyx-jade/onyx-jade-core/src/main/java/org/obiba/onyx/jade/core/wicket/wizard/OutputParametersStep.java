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

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentLaunchPanel;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentOutputParameterPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class OutputParametersStep extends WizardStepPanel {

  private static final long serialVersionUID = 6617334507631332206L;

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
    add(new EmptyPanel(getTitleId()).setVisible(false));

    // add(new Label("title", new StringResourceModel("ProvideTheFollowingInformation", OutputParametersStep.this,
    // null)));
    // add(new EmptyPanel("panel"));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {

    form.getNextLink().setVisible(isEnableNextLink(form));

    // Disable previous button when not needed.
    WizardStepPanel previousStep = this.getPreviousStep();
    if(previousStep == null || previousStep.equals(this)) {
      form.getPreviousLink().setVisible(false);
    } else {
      form.getPreviousLink().setVisible(true);
    }

    form.getFinishLink().setVisible(false);
    if(target != null) {
      target.addComponent(form.getNextLink());
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getFinishLink());
    }
  }

  private boolean isEnableNextLink(WizardForm form) {
    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    if(instrumentType.isRepeatable()) {
      // minimum is having the expected count of repeatable measures
      int currentCount = activeInstrumentRunService.getInstrumentRun().getValidMeasureCount();
      int expectedCount = instrumentType.getExpectedMeasureCount(activeInstrumentRunService.getParticipant());
      boolean skipped = ((InstrumentOutputParameterPanel) get(getContentId())).isSkipMeasurement();
      if(currentCount < expectedCount && !skipped) {
        return false;
      } else {
        return true;
      }
    } else {
      return true;
    }
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepInNext(form, target);
    setContent(target, instrumentOutputParameterPanel = new InstrumentOutputParameterPanel(getContentId()));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepOutNext(form, target);
    instrumentOutputParameterPanel.saveOutputInstrumentRunValues();

    List<InstrumentOutputParameter> paramsWithWarnings = activeInstrumentRunService.getParametersWithWarning();

    if(!paramsWithWarnings.isEmpty()) {
      warn(getString("ThereAreWarnings"));
      ((WarningsStep) warningsStep).setParametersWithWarnings(paramsWithWarnings);
      setNextStep(warningsStep);
    } else {
      setNextStep(conclusionStep);
    }
  }
}
