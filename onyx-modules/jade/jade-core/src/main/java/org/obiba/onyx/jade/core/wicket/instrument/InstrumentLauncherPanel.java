package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;

public abstract class InstrumentLauncherPanel extends Panel {

  private static final long serialVersionUID = 2397755629651961494L;

  private Instrument instrument;

  private String instrumentCodeBase;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentDescriptorService instrumentDescriptorService;

  @SuppressWarnings("serial")
  public InstrumentLauncherPanel(String id) {
    super(id);

    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run != null) {
      instrument = run.getInstrument();
      instrumentCodeBase = instrumentDescriptorService.getCodeBase(instrument.getBarcode());
    }

    final InstrumentLauncher launcher = new InstrumentLauncher(instrument, instrumentCodeBase);

    Link button = new Link("start") {

      @Override
      public void onClick() {
        launcher.launch();
        onInstrumentLaunch();
      }

    };
    PopupSettings popup = new PopupSettings();
    button.setPopupSettings(popup);
    add(button);
    button.setEnabled(instrument != null && instrumentCodeBase != null);

  }

  public abstract void onInstrumentLaunch();
}
