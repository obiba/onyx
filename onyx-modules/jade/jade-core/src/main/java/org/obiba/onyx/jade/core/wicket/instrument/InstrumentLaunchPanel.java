/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.OperatorSource;
import org.obiba.onyx.jade.core.domain.instrument.UnitParameterValueConverter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
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
      Data data = inputDataSourceVisitor.getData(activeInterviewService.getParticipant(), param);
      if(data != null) {
        if(!data.getType().equals(runValue.getDataType())) {
          UnitParameterValueConverter converter = new UnitParameterValueConverter();
          converter.convert(runValue, data);
        } else {
          runValue.setData(data);
        }
        activeInstrumentRunService.update(runValue);
      } else {
        log.error("The value for instrument parameter {} comes from an InputSource, but this source has not produced a value. Please correct stage dependencies or your instrument-descriptor.xml file for this instrument.", param.getDescription());
        error("An unexpected problem occurred while setting up this instrument's run. Please contact support.");
      }

    }

    RepeatingView repeat = new RepeatingView("repeat");
    add(repeat);

    // get all the input run values that requires manual capture
    InstrumentInputParameter template = new InstrumentInputParameter();
    template.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);
    template.setInstrument(instrument);

    boolean manualCaptureRequired = false;
    for(final InstrumentInputParameter param : queryService.match(template)) {

      // We don't want to display parameters that were manually entered by the user in the previous step.
      // These will be automatically sent to the instrument.
      if(!(param.getInputSource() instanceof OperatorSource)) {

        manualCaptureRequired = true;

        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        // Inject the Spring application context and the user session service
        // into the instrument parameter. NOTE: These are dependencies of
        // InstrumentParameter.getDescription().
        // param.setApplicationContext(((SpringWebApplication)
        // getApplication()).getSpringContextLocator().getSpringContext());
        // param.setUserSessionService(userSessionService);

        item.add(new Label("instruction", new StringResourceModel("TypeTheValueInTheInstrument", InstrumentLaunchPanel.this, new Model() {
          public Object getObject() {
            InstrumentRunValue runValue = activeInstrumentRunService.getInputInstrumentRunValue(param.getName());
            ValueMap map = new ValueMap("description=" + new SpringStringResourceModel(param.getDescription()).getString());
            if(runValue.getData() != null && runValue.getData().getValue() != null) {
              map.put("value", new SpringStringResourceModel(runValue.getData().getValueAsString()).getString());
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

    if(manualCaptureRequired) {
      add(new Label("instructions", new StringResourceModel("Instructions", InstrumentLaunchPanel.this, null)));
    } else {
      add(new EmptyPanel("instructions"));
    }

  }

  /**
   * Called when instrument launcher is clicked.
   */
  public abstract void onInstrumentLaunch();

}
