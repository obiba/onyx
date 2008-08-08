package org.obiba.onyx.wicket.wizard;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;

public abstract class WizardPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  protected EntityQueryService queryService;

  private WizardForm form;

  public WizardPanel(String id, IModel instrument) {
    super(id);
    setModel(instrument);

    form = createForm("form");
    add(form);
    
  }
  
  public boolean isCanceled() {
    return form.isCanceled();
  }

  public abstract WizardForm createForm(String componentId);

  public EntityQueryService getQueryService() {
    return queryService;
  }

  
}
