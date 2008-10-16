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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

public class WarningsStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private transient EntityQueryService queryService;
  
  @SpringBean
  private transient ActiveInstrumentRunService activeInstrumentRunService;
  
  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;
  
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
    
    for (InstrumentOutputParameter param : paramsWithWarnings) {
      // Inject the Spring application context and the user session service
      // into the instrument parameter. NOTE: These are dependencies of
      // InstrumentParameter.getDescription().
      param.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
      param.setUserSessionService(userSessionService);
      
      // Get the parameter's run value.
      InstrumentRunValue runValue = activeInstrumentRunService.getOutputInstrumentRunValue(param.getName());
      String valueAsString = runValue.getData().getValueAsString();
      if (valueAsString == null) {
        valueAsString = "";
      }
      
      warningsPanel.addRow(new Label(KeyValueDataPanel.getRowKeyId(), param.getDescription()), new Label(KeyValueDataPanel.getRowValueId(), valueAsString + " "+param.getMeasurementUnit()));
    }
    
    setContent(target, warningsPanel);
  }
}
