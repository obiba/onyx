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

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.onyx.wicket.wizard.contraindication.AbstractContraIndicationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Termination Step after contra-indication has been selected.
 * 
 */
public abstract class AbstractJadeContraIndicationStep extends AbstractContraIndicationStep {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractJadeContraIndicationStep.class);

  @SpringBean
  protected ActiveInstrumentRunService activeInstrumentRunService;

  public AbstractJadeContraIndicationStep(String id) {
    super(id);
  }

  //
  // AbstractContraIndicatedStep Methods
  //

  protected void persistContraindicatable() {
  }

  protected Contraindication getContraindication() {
    return activeInstrumentRunService.getContraindication();
  }

  protected WizardStepPanel getContraIndicatedStep() {
    return new ContraIndicatedStep(WizardForm.getStepId());
  }

  protected void refreshFlow(WizardForm form) {
    ((InstrumentWizardForm) form).setUpWizardFlow();
  }

  //
  // Methods
  //

  public ActiveInstrumentRunService getActiveInstrumentRunService() {
    return activeInstrumentRunService;
  }

}
