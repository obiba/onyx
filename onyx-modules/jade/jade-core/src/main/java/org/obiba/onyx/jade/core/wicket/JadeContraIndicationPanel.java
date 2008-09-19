package org.obiba.onyx.jade.core.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;

public class JadeContraIndicationPanel extends Panel {

  private static final long serialVersionUID = -7360139138848577104L;

  @SpringBean
  private EntityQueryService queryService;
  
  public JadeContraIndicationPanel(String id) {
    super(id);
    
    Form form = new Form("form");
    add(form);
    
    RepeatingView repeating = new RepeatingView("repeating");
    form.add(repeating);
    
    // get all contra-indications
    queryService.list(ContraIndication.class);
    
    AjaxButton button;
    form.add(button = new AjaxButton("submit", form) {
      
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        // TODO Auto-generated method stub
        
      }
      
    });
    form.add(button);
    
  }

}
