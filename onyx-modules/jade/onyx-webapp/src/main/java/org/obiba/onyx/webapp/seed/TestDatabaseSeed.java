package org.obiba.onyx.webapp.seed;

import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.application.AppConfiguration;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.engine.Stage;
import org.obiba.wicket.util.seed.XstreamResourceDatabaseSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public class TestDatabaseSeed extends XstreamResourceDatabaseSeed {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private PersistenceManager persistenceManager;

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
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
    return seed && (persistenceManager.count(Stage.class) == 0);
  }
  
  @Override
  protected void initializeXstream(XStream xstream) {
    super.initializeXstream(xstream);
    
    System.out.println("TEST MARTIN!!!");
    
    xstream.alias("config", AppConfiguration.class);
    xstream.alias("user", User.class);    
  }
}
