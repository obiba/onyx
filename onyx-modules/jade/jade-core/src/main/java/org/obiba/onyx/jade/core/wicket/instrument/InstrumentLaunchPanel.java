package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;
import org.obiba.onyx.jade.core.service.InstrumentService;
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
public abstract class InstrumentLaunchPanel extends Panel {

  private static final long serialVersionUID = 8250439838157103589L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentLaunchPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private InputDataSourceVisitor inputDataSourceVisitor;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean
  private InstrumentDescriptorService instrumentDescriptorService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  @SuppressWarnings("serial")
  public InstrumentLaunchPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    Instrument instrument = activeInstrumentRunService.getInstrument();
    log.info("instrument.name=" + instrument.getName());

    // general instructions and launcher
    add(new Label("general", new StringResourceModel("StartMeasurementWithInstrument", this, new Model(new ValueMap("name=" + instrument.getName())))));
    String instrumentCodeBase = instrumentDescriptorService.getCodeBase(instrument.getBarcode());

    if(instrumentCodeBase == null) {
      log.info("No code base for instrument {}", instrument.getName());
      throw new IllegalArgumentException("No code base found for instrument " + instrument.getName());
    }

    final InstrumentLauncher launcher = new InstrumentLauncher(instrument, instrumentCodeBase);

    add(new Link("start") {

      @Override
      public void onClick() {
        launcher.launch();
        InstrumentLaunchPanel.this.onInstrumentLaunch();
      }

    });

    // get the data from not read-only input parameters sources
    for(InstrumentInputParameter param : instrumentService.getInstrumentInputParameter(instrument, true)) {
      final InstrumentRunValue runValue = activeInstrumentRunService.getInputInstrumentRunValue(param.getName());
      runValue.setData(inputDataSourceVisitor.getData(activeInterviewService.getParticipant(), param));
      activeInstrumentRunService.update(runValue);
    }

    RepeatingView repeat = new RepeatingView("repeat");
    add(repeat);

    // get all the input run values that requires manual capture
    InstrumentInputParameter template = new InstrumentInputParameter();
    template.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);
    template.setInstrument(instrument);

    if (queryService.count(template)>0) {
      add(new Label("instructions", new StringResourceModel("Instructions", InstrumentLaunchPanel.this, null)));
    }
    else {
      add(new EmptyPanel("instructions"));
    }
    
    for(final InstrumentInputParameter param : queryService.match(template)) {
      WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
      repeat.add(item);

      // Inject the Spring application context and the user session service
      // into the instrument parameter. NOTE: These are dependencies of
      // InstrumentParameter.getDescription().
      param.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
      param.setUserSessionService(userSessionService);

      item.add(new Label("instruction", new StringResourceModel("TypeTheValueInTheInstrument", InstrumentLaunchPanel.this, new Model() {
        public Object getObject() {
          InstrumentRunValue runValue = activeInstrumentRunService.getInputInstrumentRunValue(param.getName());
          ValueMap map = new ValueMap("description=" + param.getDescription());
          if(runValue.getData() != null && runValue.getData().getValue() != null) {
            map.put("value", runValue.getData().getValueAsString());
            String unit = param.getMeasurementUnit();
            if(unit == null) {
              unit = "";
            }
            map.put("unit", unit);
          }
          return map;
        }
      })));
    }

  }

  /**
   * Called when instrument launcher is clicked.
   */
  public abstract void onInstrumentLaunch();

}
