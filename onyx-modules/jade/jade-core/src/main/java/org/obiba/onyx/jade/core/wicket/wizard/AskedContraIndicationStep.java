package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.wicket.instrument.AskedContraIndicationPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;

/**
 * Instrument selection Step.
 * 
 */
public class AskedContraIndicationStep extends AbstractContraIndicationStep {

  private static final long serialVersionUID = 4489598868219932761L;

  private AskedContraIndicationPanel askedContraIndicationPanel;

  @SuppressWarnings("serial")
  public AskedContraIndicationStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("AskedContraIndication", this, null)));
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    setContent(target, askedContraIndicationPanel = new AskedContraIndicationPanel(getContentId()));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    askedContraIndicationPanel.saveContraIndicationSelection();
    super.onStepOutNext(form, target);
  }

  protected ParticipantInteractionType getParticipantInteractionType() {
    return ParticipantInteractionType.ASKED;
  }

}
