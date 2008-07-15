package org.obiba.onyx.jade.core.wicket.seed;

import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.wicket.util.seed.XstreamResourceDatabaseSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public class JadeDatabaseSeed extends XstreamResourceDatabaseSeed {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private PersistenceManager persistenceManager;
  
  private InstrumentService instrumentService;
  
  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void handleXstreamResult(Object result) {
    if(result != null && result instanceof List) {
      List<Object> objects = (List<Object>) result;
      for(Object entity : objects) {
        log.info("Seeding database with entity {} of type {}", entity, entity.getClass().getSimpleName());
        persistenceManager.save(entity);
      }
    }
  }

  @Override
  protected boolean shouldSeed(WebApplication application) {
    boolean seed = super.shouldSeed(application);
    return seed;
  }
  
  @Override
  protected void initializeXstream(XStream xstream) {
    super.initializeXstream(xstream);
    xstream.alias("type", InstrumentType.class);
  }
  
}
