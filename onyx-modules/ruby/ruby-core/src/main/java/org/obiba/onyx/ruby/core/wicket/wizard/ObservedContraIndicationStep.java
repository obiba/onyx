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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.wicket.contraindication.ObservedContraIndicationPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;

public class ObservedContraIndicationStep extends AbstractRubyContraIndicationStep {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  //
  // Constructors
  //

  public ObservedContraIndicationStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("ObservedContraIndication", this, null)));
  }

  //
  // AbstractRubyContraIndicationStep Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    super.handleWizardState(form, target);

    // Disable interrupt link.
    Component interruptLink = ((RubyWizardForm) form).getInterruptLink();
    interruptLink.setEnabled(false);

    if(target != null) {
      target.addComponent(interruptLink);
    }
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    setContent(target, new ObservedContraIndicationPanel(getContentId(), new PropertyModel(activeTubeRegistrationService, "participantTubeRegistration")));
  }

  protected final Contraindication.Type getParticipantInteractionType() {
    return Contraindication.Type.OBSERVED;
  }
}
