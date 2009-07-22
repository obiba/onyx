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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ValidationStep extends WizardStepPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final String FEWER_TUBES_THAN_EXPECTED_COLLECTED_RESOURCE_KEY = "Ruby.Warning.FewerTubesThanExpectedCollected";

  private static final String MORE_TUBES_THAN_EXPECTED_COLLECTED_RESOURCE_KEY = "Ruby.Warning.MoreTubesThanExpectedCollected";

  //
  // Instance Variables
  //

  @SpringBean(name = "activeTubeRegistrationService")
  protected ActiveTubeRegistrationService activeTubeRegistrationService;

  //
  // Constructors
  //

  public ValidationStep(String id) {
    super(id);

    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("Validation", ValidationStep.this, null)));

    add(new ValidationPanel(getContentId()));
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getNextLink().setEnabled(false);
    form.getPreviousLink().setEnabled(true);
    form.getFinishLink().setEnabled(true);

    if(target != null) {
      target.addComponent(form);
    }
  }

  @Override
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    super.onStepOutPrevious(form, target);
    ((RubyWizardForm) form).setUpWizardFlow();
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepInNext(form, target);
    int registeredTubeCount = activeTubeRegistrationService.getRegisteredTubeCount();
    int expectedTubeCount = activeTubeRegistrationService.getExpectedTubeCount();

    String warningMessage = null;

    if(registeredTubeCount < expectedTubeCount) {
      warningMessage = new SpringStringResourceModel(FEWER_TUBES_THAN_EXPECTED_COLLECTED_RESOURCE_KEY, new Object[] { registeredTubeCount, expectedTubeCount }, FEWER_TUBES_THAN_EXPECTED_COLLECTED_RESOURCE_KEY).getString();
    } else if(registeredTubeCount > expectedTubeCount) {
      warningMessage = new SpringStringResourceModel(MORE_TUBES_THAN_EXPECTED_COLLECTED_RESOURCE_KEY, new Object[] { registeredTubeCount, expectedTubeCount }, MORE_TUBES_THAN_EXPECTED_COLLECTED_RESOURCE_KEY).getString();
    }

    if(warningMessage != null) {
      warn(warningMessage);
    }
  }
}