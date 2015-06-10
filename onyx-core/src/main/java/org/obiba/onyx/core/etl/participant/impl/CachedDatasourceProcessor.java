package org.obiba.onyx.core.etl.participant.impl;

import java.util.Date;
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
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.magma.support.CachedDatasource;
import org.obiba.magma.support.CachedValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BinaryType;
import org.obiba.onyx.core.data.DatasourceUtils;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.obiba.onyx.core.etl.participant.IParticipantPostProcessor;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;

import com.google.common.collect.Maps;

public class CachedDatasourceProcessor implements IParticipantPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(CachedDatasourceProcessor.class);

  private MagmaEngine magmaEngine;

  /**
   * Restrict caching warm up to the specified tables (no restriction if nothing is specified).
   */
  private Map<String, Set<String>> datasourceTables = Maps.newHashMap();

  public void setMagmaEngine(MagmaEngine magmaEngine) {
    this.magmaEngine = magmaEngine;
  }

  public void setTableNames(String[] tableNames) {
    if(tableNames == null || tableNames.length == 0) return;

    for(String tableName : tableNames) {
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      if(!datasourceTables.containsKey(resolver.getDatasourceName())) {
        datasourceTables.put(resolver.getDatasourceName(), new TreeSet<String>());
      }
      datasourceTables.get(resolver.getDatasourceName()).add(resolver.getTableName());
    }
  }

  @Override
  public void process(ExecutionContext context, List<Participant> participants) {
    if(participants == null || participants.isEmpty() || !magmaEngine.hasExtension(MagmaCacheExtension.class)) return;

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      CachedDatasource ds = DatasourceUtils.asCachedDatasource(datasource);
      if(ds != null && isApplicable(datasource)) {
        doCache(context, ds, participants);
      }
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

  //
  // Private methods
  //

  private void doCache(ExecutionContext context, CachedDatasource datasource, List<Participant> participants) {
    try {
      for(ValueTable table : datasource.getValueTables()) {
        if(isApplicable(datasource, table)) doCache(context, (CachedValueTable) table, participants);
      }
    } catch(Exception e) {
      String msg = "Unable to get tables of datasource: " + datasource.getName();
      log(context, AppointmentUpdateLog.Level.ERROR, e, msg);
    }
  }

  private void doCache(ExecutionContext context, CachedValueTable table, List<Participant> participants) {
    table.getEntityType(); //cache warm-up

    if(!table.isForEntityType(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE)) return;

    log(context, AppointmentUpdateLog.Level.INFO,
        "Caching data from: " + table.getDatasource().getName() + "." + table.getName());
    Iterable<Variable> variables = table.getVariables();

    for(Participant participant : participants) {
      doCache(context, table, variables, participant);
    }
  }

  private void doCache(ExecutionContext context, CachedValueTable table, Iterable<Variable> variables,
      Participant participant) {
    VariableEntity entity = new VariableEntityBean(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE,
        participant.getEnrollmentId());

    table.evictValues(entity);

    if(table.hasValueSet(entity)) {
      try {
        ValueSet valueSet = table.getValueSet(entity);
        for(Variable variable : variables) {
          if(isApplicable(table, variable)) { //cache warm-up
            table.getVariable(variable.getName());
            table.getVariableValueSource(variable.getName()).getVariable();
            table.getVariableValueSource(variable.getName()).getValue(valueSet);
            table.getValue(variable, valueSet);
          }
        }
      } catch(Exception e) {
        String msg = "Unable to cache data of " + MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE + ": " +
            participant.getEnrollmentId();
        log(context, AppointmentUpdateLog.Level.ERROR, e, msg);
      }
    }
  }

  private void log(ExecutionContext context, AppointmentUpdateLog.Level level, Exception e, String message) {
    log.error(message, e);
    log(context, AppointmentUpdateLog.Level.ERROR, message);
  }

  private void log(ExecutionContext context, AppointmentUpdateLog.Level level, String message) {
    if(context == null) return;
    AppointmentUpdateLog.addLog(context, new AppointmentUpdateLog(new Date(), level, message));
  }
}
