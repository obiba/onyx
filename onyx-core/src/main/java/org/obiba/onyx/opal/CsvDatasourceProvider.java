package org.obiba.onyx.opal;

import java.io.File;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaCacheExtension;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.support.CachedDatasource;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.EnrollmentIdDatasource;
import org.obiba.onyx.magma.EhCacheCachedDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;

public class CsvDatasourceProvider implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(CsvDatasourceProvider.class);

  private static final String DATASOURCE_CACHE_PREFIX = "datasource-";

  private ParticipantService participantService;

  private MagmaEngine magmaEngine;

  private File file;

  private String entityType;

  private String tableName;

  private String datasourceName;

  private boolean withCaching = false;

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setMagmaEngine(MagmaEngine magmaEngine) {
    this.magmaEngine = magmaEngine;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public void setWithCaching(boolean withCaching) {
    this.withCaching = withCaching;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if(datasourceName != null && datasourceName.isEmpty() == false) {
      CsvDatasource csvDs = new CsvDatasource(datasourceName);
      Datasource ds = csvDs;
      if(file.isDirectory()) {
        csvDs.addValueTable(file);
      } else {
        String table = tableName == null ? file.getName().substring(0, file.getName().lastIndexOf('.')) : tableName;
        csvDs.addValueTable(table, file, entityType == null ? "Participant" : entityType);
      }
      log.info("Adding datasource: {}", datasourceName);

      if (withCaching && MagmaEngine.get().hasExtension(MagmaCacheExtension.class)) {
        MagmaCacheExtension cacheExtension = MagmaEngine.get().getExtension(MagmaCacheExtension.class);
        log.info("Using datasource cache: {}", datasourceName);
        Cache cache = cacheExtension.getCacheManager().getCache(DATASOURCE_CACHE_PREFIX + datasourceName);
        if(cache != null) ds = cache instanceof EhCacheCache
            ? new EhCacheCachedDatasource(ds, (EhCacheCache) cache)
            : new CachedDatasource(ds, cache);
      }

      if(participantService != null) {
        magmaEngine.addDatasource(new EnrollmentIdDatasource(participantService, ds));
      } else {
        magmaEngine.addDatasource(ds);
      }
    }
  }
}
