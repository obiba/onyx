package org.obiba.onyx.jade.core.wicket.seed;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.jade.core.domain.instrument.FixedSource;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.OutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantPropertySource;
import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.wicket.util.seed.XstreamResourceDatabaseSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

public class JadeDatabaseSeed extends XstreamResourceDatabaseSeed {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private PersistenceManager persistenceManager;

  private InstrumentService instrumentService;

  private InstrumentDescriptorService instrumentDescriptorService;

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public InstrumentDescriptorService getInstrumentDescriptorService() {
    return instrumentDescriptorService;
  }

  public void setInstrumentDescriptorService(InstrumentDescriptorService instrumentDescriptorService) {
    this.instrumentDescriptorService = instrumentDescriptorService;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void handleXstreamResult(Resource resource, Object result) {
    if(result != null && result instanceof List) {
      List<Object> objects = (List<Object>) result;
      for(Object entity : objects) {
        log.info("Seeding database from [" + resource + "] with entity {} of type {}", entity, entity.getClass().getSimpleName());
        if(entity instanceof Instrument) {
          Instrument instrument = (Instrument) entity;
          InstrumentType type = instrumentService.getInstrumentType(instrument.getInstrumentType().getName());
          instrument.setInstrumentType(type);
          // find code base:
          // resource is in .../<codeBase>/lib/instrument-descriptor.xml

          try {
            instrumentDescriptorService.setCodeBase(instrument.getBarcode(), resource.getFile().getParentFile().getParentFile().getName() + "/" + resource.getFile().getParentFile().getName());
          } catch(IOException cannotFindResource) {
            log.error("Cannot find resource : " + resource.getDescription());
            throw new RuntimeException(cannotFindResource);
          }

        } else if(entity instanceof OutputParameterSource) {
          OutputParameterSource source = (OutputParameterSource) entity;
          InstrumentType type = instrumentService.getInstrumentType(source.getInstrumentType().getName());
          source.setInstrumentType(type);
        }
        persistenceManager.save(entity);
      }
    }
  }

  @Override
  protected boolean shouldSeed(WebApplication application) {
    boolean seed = super.shouldSeed(application);
    return seed && (persistenceManager.list(InstrumentType.class).size() == 0);
  }

  @Override
  protected void initializeXstream(XStream xstream) {
    log.info("initializeXstream");
    super.initializeXstream(xstream);
    xstream.alias("instrumentType", InstrumentType.class);
    xstream.alias("instrument", Instrument.class);
    xstream.alias("input", InstrumentInputParameter.class);
    xstream.alias("output", InstrumentOutputParameter.class);
    xstream.alias("computedOutput", InstrumentComputedOutputParameter.class);
    xstream.alias("fixedSource", FixedSource.class);
    xstream.alias("participantPropertySource", ParticipantPropertySource.class);
    xstream.alias("outputParameterSource", OutputParameterSource.class);
  }

}
