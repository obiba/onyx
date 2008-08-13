package org.obiba.onyx.jade.core.wicket.instrument.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;

public abstract class InstrumentSelector extends Panel {

  private static final long serialVersionUID = 3920957095572085598L;

  private Instrument selection = null;

  @SpringBean
  private EntityQueryService queryService;

  @SuppressWarnings("serial")
  public InstrumentSelector(String id, IModel instrumentTypeModel) {
    super(id, instrumentTypeModel);

    // get only active instruments in this type.
    Instrument template = new Instrument();
    template.setInstrumentType((InstrumentType) getModelObject());
    template.setStatus(InstrumentStatus.ACTIVE);

    DropDownChoice select = new DropDownChoice("select", new PropertyModel(this, "selection"), queryService.match(template), new IChoiceRenderer() {

      public Object getDisplayValue(Object object) {
        return ((Instrument) object).getName();
      }

      public String getIdValue(Object object, int index) {
        return ((Instrument) object).getId().toString();
      }

    });
    select.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        onInstrumentSelection(target, selection);
      }

    });
    add(select);
  }

  public Instrument getSelection() {
    return selection;
  }

  public void setSelection(Instrument selection) {
    this.selection = selection;
  }

  public abstract void onInstrumentSelection(AjaxRequestTarget target, Instrument instrument);

}
