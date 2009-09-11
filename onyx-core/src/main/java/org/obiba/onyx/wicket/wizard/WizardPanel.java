/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.wizard;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;

public abstract class WizardPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  protected EntityQueryService queryService;

  private WizardForm wizardForm;

  public WizardPanel(String id, IModel model) {
    super(id);
    setDefaultModel(model);

    wizardForm = createForm("form");
    add(wizardForm);

  }

  public WizardForm getWizardForm() {
    return wizardForm;
  }

  public boolean isCanceled() {
    return wizardForm.isCanceled();
  }

  public abstract WizardForm createForm(String componentId);

  public EntityQueryService getQueryService() {
    return queryService;
  }

}
