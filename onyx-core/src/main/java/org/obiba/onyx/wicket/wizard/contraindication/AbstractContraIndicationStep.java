/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.wizard.contraindication;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

/**
 * Abstract base class for contra-indication steps.
 */
public abstract class AbstractContraIndicationStep extends WizardStepPanel {
  //
  // Constructors
  //

  public AbstractContraIndicationStep(String id) {
    super(id);
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(getPreviousStep() != null);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
      target.addComponent(form.getFinishLink());
    }
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    // Persist contraindicatable.
    persistContraindicatable();

    // exit if a ci is selected
    Contraindication ci = getContraindication();
    if(ci != null && ci.getType() == getParticipantInteractionType()) {
      WizardStepPanel nextStep = getContraIndicatedStep();
      // no possibility to come back
      // nextStep.setPreviousStep(this);
      setNextStep(nextStep);
      nextStep.setPreviousStep(this);
    } else {
      // do it in case of back and forth
      refreshFlow(form);
    }
  }

  //
  // Methods
  //

  /**
   * Returns the type of contra-indicatin (<code>OBSERVED</code> or <code>ASKED</code>) this step captures.
   */
  protected abstract Contraindication.Type getParticipantInteractionType();

  /**
   * Persists the contra-indicatable domain object.
   */
  protected abstract void persistContraindicatable();

  /**
   * Returns the selected contra-indication.
   * 
   * @return selected contra-indication (<code>null</code> if none selected)
   */
  protected abstract Contraindication getContraindication();

  /**
   * Returns the contra-indicated step (i.e., the step advanced to when the a contra-indication has been selected).
   * 
   * @return contra-indicated step
   */
  protected abstract WizardStepPanel getContraIndicatedStep();

  /**
   * Signals the wizard form to refresh the flow.
   * 
   * @param form wizard form
   */
  protected abstract void refreshFlow(WizardForm form);
}
