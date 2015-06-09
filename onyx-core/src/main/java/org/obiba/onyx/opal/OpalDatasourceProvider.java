package org.obiba.onyx.opal;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaCacheExtension;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.support.CachedDatasource;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.EnrollmentIdDatasource;
import org.obiba.opal.rest.client.magma.OpalJavaClient;
import org.obiba.opal.rest.client.magma.RestDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;

public class OpalDatasourceProvider implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(OpalDatasourceProvider.class);

  private ParticipantService participantService;

  private MagmaEngine magmaEngine;

  private OpalJavaClient opalJavaClient;

  private String onyxDatasourceName;

  private String opalDatasourceName;

  private boolean withCaching = false;

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setMagmaEngine(MagmaEngine magmaEngine) {
    this.magmaEngine = magmaEngine;
  }

  public void setOpalJavaClient(OpalJavaClient opalJavaClient) {
    this.opalJavaClient = opalJavaClient;
  }

  public void setOnyxDatasourceName(String onyxDatasourceName) {
    this.onyxDatasourceName = onyxDatasourceName;
  }

  public void setOpalDatasourceName(String opalDatasourceName) {
    this.opalDatasourceName = opalDatasourceName;
  }

  public void setWithCaching(boolean withCaching) {
    this.withCaching = withCaching;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if(opalDatasourceName != null && opalDatasourceName.isEmpty() == false) {
      String localDatasourceName = onyxDatasourceName != null ? onyxDatasourceName : opalDatasourceName;
      log.info("Adding datasource: {} -> {}", localDatasourceName, opalDatasourceName);
      Datasource ds = new RestDatasource(localDatasourceName, opalJavaClient, opalDatasourceName);
      if (withCaching && MagmaEngine.get().hasExtension(MagmaCacheExtension.class)) {
        MagmaCacheExtension cacheExtension = MagmaEngine.get().getExtension(MagmaCacheExtension.class);
        log.info("Using datasource cache: {}", localDatasourceName);
        Cache cache = cacheExtension.getCacheManager().getCache("datasource-" + localDatasourceName);
        if (cache != null) ds = new CachedDatasource(ds, cache);
      }
      magmaEngine.addDatasource(new EnrollmentIdDatasource(participantService, ds));
    }
  }

}
