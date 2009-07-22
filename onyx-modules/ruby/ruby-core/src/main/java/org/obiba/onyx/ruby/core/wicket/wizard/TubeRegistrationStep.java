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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class TubeRegistrationStep extends WizardStepPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean(name = "activeTubeRegistrationService")
  protected ActiveTubeRegistrationService activeTubeRegistrationService;

  //
  // Constructors
  //

  public TubeRegistrationStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new TubeRegistrationTitlePanel(getTitleId()));

    add(new TubeRegistrationPanel(getContentId()));
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getNextLink().setEnabled(true);
    form.getPreviousLink().setEnabled(!((RubyWizardForm) form).isResuming() && hasContraindications());
    form.getFinishLink().setEnabled(false);

    ((RubyWizardForm) form).getInterruptLink().setEnabled(true);

    if(target != null) {
      target.addComponent(form);
    }
  }

  //
  // Methods
  //

  /**
   * Indicates whether any contra-indications are configured for this cohort.
   * 
   * @return <code>true</code> if any contra-indications are configure for the cohort, <code>false</code> otherwise
   */
  private boolean hasContraindications() {
    return activeTubeRegistrationService.hasContraindications(Contraindication.Type.OBSERVED) || activeTubeRegistrationService.hasContraindications(Contraindication.Type.ASKED);
  }

}
