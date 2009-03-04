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
import org.apache.wicket.model.IModel;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

/**
 * Questionnaire step base class.
 */
public abstract class QuestionnaireWizardStepPanel extends WizardStepPanel {

  /**
   * @param id
   */
  public QuestionnaireWizardStepPanel(String id) {
    super(id);
  }

  /**
   * @param id
   * @param model
   */
  public QuestionnaireWizardStepPanel(String id, IModel model) {
    super(id, model);
  }

  /**
   * Call this after page step previous / next occured.
   * @param target
   */
  protected void onPageStep(AjaxRequestTarget target) {
    target.appendJavascript("Resizer.resizeWizard();");
  }

}
