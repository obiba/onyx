package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;


public class ContraIndicatedPanel extends Panel {

  private static final long serialVersionUID = 9014406108097758044L;
  
  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;
  
  public ContraIndicatedPanel(String id) {
    super(id);
    
    ContraIndication ci = activeInstrumentRunService.getContraIndication();
    add(new Label("label", new StringResourceModel("ReasonForContraIndication", this, new Model(new ValueMap("ci=" + ci.getName())))));
  }

}
