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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Container of action buttons based on the ones defined by the wizard form.
 */
public class QuestionnaireWizardAdministrationPanel extends Panel {

  private static final long serialVersionUID = 1L;

  /**
   * @param id
   */
  public QuestionnaireWizardAdministrationPanel(String id) {
    super(id);

  }

  public QuestionnaireWizardAdministrationPanel setInterrupt(Component interrupt, boolean enabled, boolean visible) {
    interrupt.setEnabled(enabled);
    interrupt.setVisible(visible);
    add(interrupt);
    return this;
  }

  public QuestionnaireWizardAdministrationPanel setFinish(Component cancel, boolean enabled, boolean visible) {
    cancel.setEnabled(enabled);
    cancel.setVisible(visible);
    add(cancel);
    return this;
  }

  public QuestionnaireWizardAdministrationPanel setCancel(Component finish, boolean enabled, boolean visible) {
    finish.setEnabled(enabled);
    finish.setVisible(visible);
    add(finish);
    return this;
  }
}
