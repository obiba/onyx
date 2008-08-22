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
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the input parameters that are from read-only sources and give the instructions to the operator:
 * <ul>
 * <li>General information with instrument launcher (if available)</li>
 * <li>instructions to enter manually captured input parameters (if needed)</li>
 * </ul>
 * @author Yannick Marcon
 * 
 */
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

  @SpringBean
  private InputDataSourceVisitor inputDataSourceVisitor;

  @SpringBean
  private InstrumentService instrumentService;

  @SuppressWarnings("serial")
  public InstructionsPanel(String id, IModel instrumentModel) {
    super(id);
    setModel(new DetachableEntityModel(queryService, instrumentModel.getObject()));
    setOutputMarkupId(true);

    Instrument instrument = (Instrument) getModelObject();

    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();
    if(instrumentRun == null) {
      instrumentRun = activeInstrumentRunService.start(activeInterviewService.getParticipant(), instrument);
    }

    // get the data from not read-only input parameters sources
    for(InstrumentInputParameter param : instrumentService.getInstrumentInputParameter(instrument, true)) {
      InstrumentRunValue runValue = new InstrumentRunValue();
      runValue.setCaptureMethod(param.getCaptureMethod());
      runValue.setInstrumentParameter(param);

      if(queryService.count(runValue) == 0) {
        Data data = inputDataSourceVisitor.getData(activeInterviewService.getParticipant(), param);
        runValue.setData(data);
        instrumentRun.addInstrumentRunValue(runValue);
      }
    }

    // save instrument input parameters if any for jade-remote-server
    activeInstrumentRunService.validate();

    // general instructions and launcher
    add(new Label("general", new StringResourceModel("StartMeasurementWithInstrument", this, new Model(new ValueMap("name=" + instrument.getName())))));
    add(new InstrumentLauncherPanel("launcher") {

      @Override
      public void onInstrumentLaunch() {
        InstructionsPanel.this.onInstrumentLaunch();
      }

    });

    // manual input parameters instructions
    final List<InstrumentRunValue> manualInputs = new ArrayList<InstrumentRunValue>();
    for(InstrumentRunValue runValue : instrumentRun.getInstrumentRunValues()) {
      if(runValue.getInstrumentParameter() instanceof InstrumentInputParameter && runValue.getCaptureMethod().equals(InstrumentParameterCaptureMethod.MANUAL)) {
        manualInputs.add(runValue);
      }
    }
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
        ValueMap map = new ValueMap("name=" + param.getDescription() + ",value=");
        if(runValue != null && runValue.getData() != null && runValue.getData().getValue() != null) {
          map.put("value", runValue.getData().getValue());
        }
        item.add(new Label("instruction", new StringResourceModel("TypeTheValueInTheInstrument", InstructionsPanel.this, new Model(map))));
      }

    });

  }

  /**
   * Called when instrument launcher is clicked.
   */
  public abstract void onInstrumentLaunch();

}
