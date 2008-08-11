package org.obiba.onyx.jade.core.wicket.instrument.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;

public abstract class InstrumentSelector extends Panel {

  private static final long serialVersionUID = 3920957095572085598L;
  
  private Instrument selection = null;
  
  @SuppressWarnings("serial")
  public InstrumentSelector(String id, IModel instrumentTypeModel) {
    super(id, instrumentTypeModel);
    
    Form form = new Form("form");
    add(form);
    
    InstrumentType type = (InstrumentType)getModelObject();
    DropDownChoice select = new DropDownChoice("select", new PropertyModel(this, "selection"), type.getInstruments(), new IChoiceRenderer() {

      public Object getDisplayValue(Object object) {
        return ((Instrument)object).getName();
      }

      public String getIdValue(Object object, int index) {
        return ((Instrument)object).getId().toString();
      }
      
    });
    select.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        onInstrumentSelection(target, selection);
      }
      
    });
    form.add(select);
  }

  public Instrument getSelection() {
    return selection;
  }

  public void setSelection(Instrument selection) {
    this.selection = selection;
  }
  
  public abstract void onInstrumentSelection(AjaxRequestTarget target, Instrument instrument);
  
}
