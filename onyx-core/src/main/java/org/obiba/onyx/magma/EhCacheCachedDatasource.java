package org.obiba.onyx.magma;

import org.obiba.magma.Datasource;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.CachedDatasource;
import org.springframework.cache.ehcache.EhCacheCache;

public class EhCacheCachedDatasource extends CachedDatasource {

  net.sf.ehcache.Cache ehCache;

  public EhCacheCachedDatasource(Datasource wrapped, EhCacheCache cache) {
    super(wrapped, cache);
    this.ehCache = (net.sf.ehcache.Cache)cache.getNativeCache();
  }

  public void flushCache() {
    this.ehCache.flush();
  }

  @Override
  public void evictValues(VariableEntity variableEntity) {
    super.evictValues(variableEntity);
    this.ehCache.flush();
  }
}
