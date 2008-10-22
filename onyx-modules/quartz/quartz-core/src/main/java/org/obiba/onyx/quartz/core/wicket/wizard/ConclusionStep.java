/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ConclusionStep extends WizardStepPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Constructors
  //

  public ConclusionStep(String id) {
    super(id);

    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("Conclusion", ConclusionStep.this, null)));

    add(new EmptyPanel(getContentId()));
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getNextLink().setEnabled(false);
    form.getPreviousLink().setEnabled(true);
    ((QuestionnaireWizardForm) form).getInterruptLink().setEnabled(false);
    form.getFinishLink().setEnabled(true);

    if(target != null) {
      target.addComponent(form);
    }
  }

  @Override
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    setPreviousStep(questionnaireWizardForm.getPreviousStep());
  }
}