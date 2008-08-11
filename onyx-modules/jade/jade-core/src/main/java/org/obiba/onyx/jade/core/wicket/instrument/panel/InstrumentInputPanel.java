package org.obiba.onyx.jade.core.wicket.instrument.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class InstrumentInputPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SpringBean
  private EntityQueryService queryService;

  public InstrumentInputPanel(String id, IModel instrumentModel) {
    super(id);
    setModel(new DetachableEntityModel(queryService, instrumentModel.getObject()));
    setOutputMarkupId(true);

    Instrument instrument = (Instrument) getModelObject();
    InstrumentInputParameter template = new InstrumentInputParameter();
    template.setInstrument(instrument);
    KeyValueDataPanel inputs = new KeyValueDataPanel("inputs");
    for(InstrumentInputParameter param : queryService.match(template)) {
      Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getName());
      inputs.addRow(label, new InputTextField(KeyValueDataPanel.getRowValueId()));
    }
    add(inputs);
  }

  private class InputTextField extends Fragment {

    public InputTextField(String id) {
      super(id, "textFieldFragment", InstrumentInputPanel.this);
      add(new TextField("field"));
    }

  }

}
