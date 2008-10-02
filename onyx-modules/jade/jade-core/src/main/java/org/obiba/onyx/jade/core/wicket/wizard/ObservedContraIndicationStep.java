package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.wicket.instrument.ObservedContraIndicationPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;

/**
 * Instrument selection Step.
 * 
 */
public class ObservedContraIndicationStep extends AbstractContraIndicationStep {

  private static final long serialVersionUID = 4489598868219932761L;

  @SuppressWarnings("serial")
  public ObservedContraIndicationStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("ObservedContraIndication", this, null)));
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    setContent(target, new ObservedContraIndicationPanel(getContentId()));
  }

  protected ParticipantInteractionType getParticipantInteractionType() {
    return ParticipantInteractionType.OBSERVED;
  }

}
