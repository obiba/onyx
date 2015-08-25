package org.obiba.onyx.core.etl.participant.impl;

import java.util.Date;
import java.util.List;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.CachedDatasource;
import org.obiba.magma.support.CachedValueTable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.obiba.onyx.core.etl.participant.IInterviewPostProcessor;
import org.obiba.onyx.core.etl.participant.IParticipantPostProcessor;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.scheduling.annotation.Async;

public class CachedDatasourceProcessor extends AbstractCachedDatasourceProcessor implements IParticipantPostProcessor,
    IInterviewPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(CachedDatasourceProcessor.class);

  private boolean proccessOnAppointment = true;

  private boolean proccessOnReception = false;

  private boolean proccessOnInterview = false;

  //
  // Settings
  //

  /**
   * Participant processing on appointment list update.
   *
   * @param proccessOnAppointment
   */
  public void setProccessOnAppointment(boolean proccessOnAppointment) {
    this.proccessOnAppointment = proccessOnAppointment;
  }

  /**
   * Participant processing on participant reception.
   *
   * @param proccessOnReception
   */
  public void setProccessOnReception(boolean proccessOnReception) {
    this.proccessOnReception = proccessOnReception;
  }

  /**
   * Participant processing on participant interview starts.
   *
   * @param proccessOnInterview
   */
  public void setProccessOnInterview(boolean proccessOnInterview) {
    this.proccessOnInterview = proccessOnInterview;
  }

  //
  // Interface processor methods
  //

  @Override
  public void process(ExecutionContext context, List<Participant> participants) {
    if(!proccessOnAppointment || participants == null || participants.isEmpty()) return;

    for (CachedDatasource datasource : getCachedDatasources()) {
      doCache(context, datasource, participants);
    }
  }

  @Override
  @Async
  public void onCreation(Interview interview) {
    if (!proccessOnReception) return;

    for (CachedDatasource datasource : getCachedDatasources()) {
      doCache(datasource, interview.getParticipant());
    }
  }

  @Override
  @Async
  public void onProgress(Interview interview) {
    if (!proccessOnInterview) return;

    for (CachedDatasource datasource : getCachedDatasources()) {
      doCache(datasource, interview.getParticipant());
    }
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
        doCache(table, variables, entity);
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
