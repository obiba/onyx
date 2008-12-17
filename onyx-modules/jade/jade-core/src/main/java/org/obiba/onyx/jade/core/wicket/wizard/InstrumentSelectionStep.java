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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentSelector;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

/**
 * Instrument selection Step.
 * 
 */
public class InstrumentSelectionStep extends WizardStepPanel {

  private static final long serialVersionUID = 4489598868219932761L;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SuppressWarnings("serial")
  public InstrumentSelectionStep(String id, IModel instrumentTypeModel) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("InstrumentSelection", this, null)));

    add(new InstrumentSelector(getContentId(), instrumentTypeModel, new PropertyModel(activeInstrumentRunService, "instrument")));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    // No previous step
    form.getPreviousLink().setEnabled(false);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

}
