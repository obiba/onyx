package org.obiba.onyx.core.data;

import org.obiba.magma.Datasource;
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.magma.support.CachedDatasource;

public class DatasourceUtils {
  public static CachedDatasource asCachedDatasource(Datasource datasource) {
    if(datasource instanceof CachedDatasource) return (CachedDatasource) datasource;
    if(datasource instanceof AbstractDatasourceWrapper)
      return asCachedDatasource(((AbstractDatasourceWrapper) datasource).getWrappedDatasource());
    return null;
  }
}
