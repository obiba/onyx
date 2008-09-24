package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InstrumentLauncherPanel extends Panel {

  private static final long serialVersionUID = 2397755629651961494L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentLauncherPanel.class);

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentDescriptorService instrumentDescriptorService;

  @SuppressWarnings("serial")
  public InstrumentLauncherPanel(String id) {
    super(id);
    Instrument instrument = activeInstrumentRunService.getInstrument();
    String instrumentCodeBase = instrumentDescriptorService.getCodeBase(instrument.getBarcode());

    if (instrumentCodeBase == null) {
      log.info("No code base for instrument {}", instrument.getName());
      throw new IllegalArgumentException("No code base found for instrument " + instrument.getName());
    }
    
    final InstrumentLauncher launcher = new InstrumentLauncher(instrument, instrumentCodeBase);

    Link button = new Link("start") {

      @Override
      public void onClick() {
        launcher.launch();
        onInstrumentLaunch();
      }

    };
    button.add(new AttributeModifier("value", new StringResourceModel("Start", this, null)));
    add(button);
    button.setVisible(instrumentService.isInteractiveInstrument(instrument));

  }

  public abstract void onInstrumentLaunch();
}
