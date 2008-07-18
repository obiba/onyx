package org.obiba.onyx.jade.core.wicket.seed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.wicket.util.seed.XstreamResourceDatabaseSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

public class JadeDatabaseSeed extends XstreamResourceDatabaseSeed implements ApplicationContextAware {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private static final String INSTRUMENT_TYPES_XML = "instrumentTypes-descriptor.xml";

  private static final String MANUAL_INSTRUMENTS_XML = "manual-instruments-descriptor.xml";

  private static final String INSTRUMENT_XML = "instrument-descriptor.xml";

  private PersistenceManager persistenceManager;

  private InstrumentService instrumentService;

  private ApplicationContext applicationContext;

  private Resource instrumentsResource;
  
  private String instrumentsDir;

  private boolean notSeeded = true;

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setInstrumentsDir(String instrumentsDir) {
    this.instrumentsDir = instrumentsDir;
  }

  @Override
  public void seedDatabase(WebApplication application) {
    notSeeded = (persistenceManager.list(InstrumentType.class).size() == 0);

    instrumentsResource = getResource();

    try {
      List<Resource> xmlResources = new ArrayList<Resource>();
      
      // add resources for describig types and manual instruments
      xmlResources.add(instrumentsResource.createRelative(INSTRUMENT_TYPES_XML));
      xmlResources.add(instrumentsResource.createRelative(MANUAL_INSTRUMENTS_XML));

      // look for instruments descriptor in instrument packages
      Resource root = applicationContext.getResource(instrumentsDir + "/");
      for(File instDir : root.getFile().listFiles()) {
        Resource res = root.createRelative(instDir.getName() + "/lib/" + INSTRUMENT_XML);
        if(res.exists()) xmlResources.add(res);
      }

      log.debug("instrument resources: " + xmlResources);

      for(Resource res : xmlResources) {
        setResource(res);
        super.seedDatabase(application);
      }

    } catch(IOException e) {
      log.error("Failed loading instruments resource", e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void handleXstreamResult(Object result) {
    if(result != null && result instanceof List) {
      List<Object> objects = (List<Object>) result;
      for(Object entity : objects) {
        log.info("Seeding database with entity {} of type {}", entity, entity.getClass().getSimpleName());
        if(entity instanceof Instrument) {
          Instrument instrument = (Instrument) entity;
          InstrumentType type = instrumentService.getInstrumentType(instrument.getInstrumentType().getName());
          instrument.setInstrumentType(type);
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
    return seed && notSeeded;
  }

  @Override
  protected void initializeXstream(XStream xstream) {
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
