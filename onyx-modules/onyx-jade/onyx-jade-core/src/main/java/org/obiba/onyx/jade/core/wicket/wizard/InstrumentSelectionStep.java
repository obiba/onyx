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
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentSelector;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instrument selection Step.
 * 
 */
public class InstrumentSelectionStep extends WizardStepPanel {

  private static final long serialVersionUID = 4489598868219932761L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentSelectionStep.class);

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private Instrument instrument;

  private boolean instrumentSelected = false;

  public InstrumentSelectionStep(String id, IModel<InstrumentType> instrumentTypeModel) {
    super(id, instrumentTypeModel);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("InstrumentSelection", this, null)));

    add(new InstrumentSelector(getContentId(), instrumentTypeModel, new PropertyModel(this, "instrument")));

  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    // No previous step
    form.getPreviousLink().setVisible(false);
    form.getNextLink().setVisible(true);
    form.getFinishLink().setVisible(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepOutNext(form, target);
    if(instrumentSelected) {
      activeInstrumentRunService.setInstrument(instrument);
      log.debug("Updated instrument with the instrument type [" + getInstrumentType().getName() + "] and user selected barcode [" + instrument.getBarcode() + "].");
    } else {
      activeInstrumentRunService.start(activeInterviewService.getParticipant(), instrument, getInstrumentType());
      instrumentSelected = true;
      log.debug("Starting a new InstrumentRun with the instrument type [" + getInstrumentType().getName() + "] and user selected barcode [" + instrument.getBarcode() + "].");
    }
  }

  private InstrumentType getInstrumentType() {
    return (InstrumentType) getDefaultModelObject();
  }

  public void setInstrumentSelected(boolean instrumentSelected) {
    this.instrumentSelected = instrumentSelected;
  }

}
