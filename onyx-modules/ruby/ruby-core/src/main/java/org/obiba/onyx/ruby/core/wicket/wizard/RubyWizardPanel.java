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

import org.apache.wicket.model.IModel;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardPanel;

public class RubyWizardPanel extends WizardPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  private RubyWizardForm wizardForm;

  //
  // Constructors
  //

  public RubyWizardPanel(String id, IModel rubyModel, StageModel stageModel, boolean resuming) {
    super(id, rubyModel);

    wizardForm.setStageModel(stageModel);
    wizardForm.initStartStep(resuming);
  }

  //
  // WizardPanel Methods
  //

  @Override
  public WizardForm createForm(String componentId) {
    wizardForm = new RubyWizardForm(componentId, getModel());

    return wizardForm;
  }

  //
  // Methods
  //

  public WizardForm getWizardForm() {
    return wizardForm;
  }

  public void setActionWindow(ActionWindow window) {
    wizardForm.setActionWindow(window);
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    wizardForm.setFeedbackWindow(feedbackWindow);
  }

  public FeedbackWindow getFeedbackWindow() {
    return wizardForm.getFeedbackWindow();
  }
}
