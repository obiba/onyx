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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;

public class WarningsStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private List<InstrumentOutputParameter> paramsWithWarnings;

  public WarningsStep(String id) {
    super(id);

    setOutputMarkupId(true);
    add(new Label("title", new StringResourceModel("WarningsTitle", WarningsStep.this, null)));

    add(new EmptyPanel("panel"));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(true);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);

    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

  void setParametersWithWarnings(List<InstrumentOutputParameter> paramsWithWarnings) {
    this.paramsWithWarnings = paramsWithWarnings;
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    KeyValueDataPanel warningsPanel = new KeyValueDataPanel(getContentId());
    warningsPanel.setOutputMarkupId(true);

    for(InstrumentOutputParameter param : paramsWithWarnings) {
      // Get the parameter's run value.
      InstrumentRunValue runValue = activeInstrumentRunService.getInstrumentRunValue(param);

      String valueAsString = runValue.getData().getValueAsString();
      if(valueAsString == null) {
        valueAsString = "";
      }

      IModel key = new MessageSourceResolvableStringModel(param.getLabel());
      IModel value = new Model(valueAsString + " " + param.getMeasurementUnit());
      warningsPanel.addRow(key, value);
    }

    setContent(target, warningsPanel);
  }
}
