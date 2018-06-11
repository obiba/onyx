/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.contraindication.IContraindicatable;
import org.obiba.onyx.wicket.contraindication.ObservedContraIndicationPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;

/**
 * Instrument selection Step.
 * 
 */
public class ObservedContraIndicationStep extends AbstractJadeContraIndicationStep {

  private static final long serialVersionUID = 4489598868219932761L;

  @SuppressWarnings("serial")
  public ObservedContraIndicationStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("ObservedContraIndication", this, null)));
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepInNext(form, target);
    setContent(target, new ObservedContraIndicationPanel(getContentId(), new PropertyModel<IContraindicatable>(this, "activeInstrumentRunService")));
  }

  //
  // AbstractJadeContraIndicationStep Methods
  //

  protected Contraindication.Type getParticipantInteractionType() {
    return Contraindication.Type.OBSERVED;
  }
}
