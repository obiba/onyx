/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.onyx.wicket.wizard.contraindication.AbstractContraIndicationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for Ruby contra-indication steps.
 */
public abstract class AbstractRubyContraIndicationStep extends AbstractContraIndicationStep {
  //
  // Constants
  //

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractRubyContraIndicationStep.class);

  //
  // Instance Variables
  //

  @SpringBean
  protected ActiveTubeRegistrationService activeTubeRegistrationService;

  //
  // Constructors
  //

  public AbstractRubyContraIndicationStep(String id) {
    super(id);
  }

  //
  // AbstractContraIndicationStep Methods
  //

  protected void persistContraindicatable() {
    activeTubeRegistrationService.persistParticipantTubeRegistration();
  }

  @Override
  protected WizardStepPanel getContraIndicatedStep() {
    return new ContraIndicatedStep(WizardForm.getStepId());
  }

  @Override
  protected Contraindication getContraindication() {
    return activeTubeRegistrationService.getContraindication();
  }

  @Override
  protected void refreshFlow(WizardForm form) {
    ((RubyWizardForm) form).setUpWizardFlow();
  }
}
