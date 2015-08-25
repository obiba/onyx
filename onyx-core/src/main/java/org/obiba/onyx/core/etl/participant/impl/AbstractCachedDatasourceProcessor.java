package org.obiba.onyx.core.etl.participant.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaCacheExtension;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.CachedDatasource;
import org.obiba.magma.support.CachedValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BinaryType;
import org.obiba.onyx.core.data.DatasourceUtils;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Base class more warming up {@link org.obiba.magma.support.CachedDatasource}.
 */
public class AbstractCachedDatasourceProcessor {

  private static final Logger log = LoggerFactory.getLogger(AbstractCachedDatasourceProcessor.class);

  private MagmaEngine magmaEngine;

  protected Map<String, Set<String>> datasourceTables = Maps.newHashMap();

  public void setMagmaEngine(MagmaEngine magmaEngine) {
    this.magmaEngine = magmaEngine;
  }

  /**
   * Restrict caching warm up to the specified tables (no restriction if nothing is specified).
   */
  public void setTableNames(String[] tableNames) {
    if(tableNames == null || tableNames.length == 0) return;

    for(String tableName : tableNames) {
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName.trim());
      if(!datasourceTables.containsKey(resolver.getDatasourceName())) {
        datasourceTables.put(resolver.getDatasourceName(), new TreeSet<String>());
      }
      datasourceTables.get(resolver.getDatasourceName()).add(resolver.getTableName());
    }
  }

  protected boolean isApplicable(Datasource datasource) {
    return datasourceTables.isEmpty() || datasourceTables.containsKey(datasource.getName());
  }

  protected boolean isApplicable(Datasource datasource, ValueTable table) {
    return datasourceTables.isEmpty() || datasourceTables.containsKey(datasource.getName()) &&
        datasourceTables.get(datasource.getName()).contains(table.getName());
  }

  protected boolean isApplicable(ValueTable table, Variable variable) {
    return !variable.getValueType().equals(BinaryType.get());
  }

  protected List<CachedDatasource> getCachedDatasources() {
    List<CachedDatasource> datasources = Lists.newArrayList();
    if(!magmaEngine.hasExtension(MagmaCacheExtension.class)) return datasources;

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      CachedDatasource ds = DatasourceUtils.asCachedDatasource(datasource);
      if(ds != null && isApplicable(datasource)) {
        datasources.add(ds);
      }
    }
    return datasources;
  }

  /**
   * Warm-up participant's cache. If data where already cached, this allows to bring cache in memory depending
   * on the cache configuration.
   *
   * @param datasource
   * @param participant
   */
  protected void doCache(CachedDatasource datasource, Participant participant) {
    doCache(datasource, Collections.singletonList(participant), false);
  }

  protected void doCache(CachedDatasource datasource, List<Participant> participants, boolean evict) {
    try {
      for(ValueTable table : datasource.getValueTables()) {
        if(isApplicable(datasource, table)) doCache((CachedValueTable) table, participants, evict);
      }
    } catch(Exception e) {
      log.error("Unable to get tables of datasource: {}", datasource.getName(), e);
    }
  }

  protected void doCache(CachedValueTable table, List<Participant> participants, boolean evict) {
    table.getEntityType(); //cache warm-up

    if(!table.isForEntityType(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE)) return;

    Iterable<Variable> variables = table.getVariables();

    for(Participant participant : participants) {
      doCache(table, variables, participant, evict);
    }
  }

  private void doCache(CachedValueTable table, Iterable<Variable> variables, Participant participant, boolean evict) {
    VariableEntity entity = new VariableEntityBean(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE,
        participant.getEnrollmentId());

    if(evict) table.evictValues(entity);

    if(table.hasValueSet(entity)) {
      try {
        doCache(table, variables, entity);
      } catch(Exception e) {
        log.error("Unable to cache data of {}: {}", MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE,
            participant.getEnrollmentId(), e);
      }
    }
  }

  protected void doCache(CachedValueTable table, Iterable<Variable> variables, VariableEntity entity) {
    ValueSet valueSet = table.getValueSet(entity);
    for(Variable variable : variables) {
      if(isApplicable(table, variable)) { //cache warm-up
        table.getVariable(variable.getName());
        table.getVariableValueSource(variable.getName()).getVariable();
        table.getVariableValueSource(variable.getName()).getValue(valueSet);
        table.getValue(variable, valueSet);
      }
    }
  }

}
