package org.obiba.onyx.jade.core.wicket.instrument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InstructionsPanel extends Panel {

  private static final long serialVersionUID = 8250439838157103589L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstructionsPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  private InstrumentRun instrumentRun;

  @SuppressWarnings("serial")
  public InstructionsPanel(String id, IModel instrumentModel) {
    super(id);
    setModel(new DetachableEntityModel(queryService, instrumentModel.getObject()));
    setOutputMarkupId(true);

    Instrument instrument = (Instrument) getModelObject();

    instrumentRun = activeInstrumentRunService.getInstrumentRun();
    if(instrumentRun == null) {
      instrumentRun = activeInstrumentRunService.start(activeInterviewService.getParticipant(), instrument);
    }
    // save instrument input parameters if any for jade-remote-server
    activeInstrumentRunService.validate();

    add(new InstrumentLauncherPanel("launcher") {

      @Override
      public void onInstrumentLaunch() {
        InstructionsPanel.this.onInstrumentLaunch();
      }

    });

    final List<InstrumentRunValue> manualInputs = new ArrayList<InstrumentRunValue>();
    for(InstrumentRunValue runValue : instrumentRun.getInstrumentRunValues()) {
      if(runValue.getInstrumentParameter() instanceof InstrumentInputParameter && runValue.getCaptureMethod().equals(InstrumentParameterCaptureMethod.MANUAL)) {
        manualInputs.add(runValue);
      }
    }

    add(new Label("general", new StringResourceModel("StartMeasurementWithInstrument", this, new Model(new ValueMap("name=" + instrument.getName())))));

    add(new DataView("item", new IDataProvider() {

      @SuppressWarnings("unchecked")
      public Iterator iterator(int first, int count) {
        return manualInputs.listIterator(first);
      }

      public IModel model(Object object) {
        return new Model((Serializable) object);
      }

      public int size() {
        return manualInputs.size();
      }

      public void detach() {
      }

    }) {

      @Override
      protected void populateItem(Item item) {
        InstrumentRunValue runValue = (InstrumentRunValue) item.getModelObject();
        InstrumentParameter param = runValue.getInstrumentParameter();
        ValueMap map = new ValueMap("name=" + param.getName() + ",value=");
        if(runValue != null && runValue.getData() != null && runValue.getData().getValue() != null) {
          map.put("value", runValue.getData().getValue());
        }
        item.add(new Label("instruction", new StringResourceModel("TypeTheValueInTheInstrument", InstructionsPanel.this, new Model(map))));
      }

    });

  }

  public abstract void onInstrumentLaunch();
}
