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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
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

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  protected ActiveTubeRegistrationService activeTubeRegistrationService;

  //
  // Constructors
  //

  public TubeRegistrationStep(String id) {
    super(id);
    setOutputMarkupId(true);

    Participant participant = activeInterviewService.getParticipant();
    add(new Label(getTitleId(), new StringResourceModel("TubeRegistration", this, new Model(new ValueMap("participant=" + participant.getFullName())))));

    add(new EmptyPanel(getContentId()));
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getNextLink().setEnabled(true);
    form.getPreviousLink().setEnabled(hasContraindications());
    form.getFinishLink().setEnabled(false);

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
