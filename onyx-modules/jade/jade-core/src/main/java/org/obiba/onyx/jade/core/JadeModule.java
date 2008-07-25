package org.obiba.onyx.jade.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageExecution;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JadeModule implements Module {

  private static final Logger log = LoggerFactory.getLogger(JadeModule.class);

  private EntityQueryService queryService;

  private InstrumentRunService instrumentRunService;

  private ParticipantService participantService;
  
  private StageExecution stageExecution;

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setStageExecution(StageExecution stageExecution) {
    this.stageExecution = stageExecution;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public String getName() {
    return "jade";
  }

  public StageExecution resume(Stage stage) {
    // TODO check a stage execution is not already on the way in this session scope
    stageExecution.resume();
    return stageExecution;
  }

  public StageExecution start(Stage stage) {
    // TODO check a stage execution is not already on the way in this session scope
    stageExecution.start(new JadeStage(this, getInstrumentType(stage)));
    return stageExecution;
    // return new JadeStageExecution(new JadeStage(this, getInstrumentType(stage)));
  }

  public StageExecution getCurrentStageExecution() {
    return stageExecution;
  }

  private InstrumentType getInstrumentType(Stage stage) {
    InstrumentType template = new InstrumentType(stage.getName(), null);
    return queryService.matchOne(template);
  }

  public Collection<Stage> getStages() {
    Collection<Stage> stages = new ArrayList<Stage>();
    Map<InstrumentType, Stage> map = new HashMap<InstrumentType, Stage>();

    for(InstrumentType type : queryService.list(InstrumentType.class)) {
      Stage stage = new JadeStage(this, type);
      stages.add(stage);
      map.put(type, stage);
    }

    // set dependencies
    for(Stage stage : stages) {
      InstrumentType type = ((JadeStage) stage).getInstrumentType();
      for(InstrumentType dependentType : type.getDependentTypes()) {
        stage.addDependentStage(map.get(dependentType));
      }
    }

    log.info("JadeStages=" + stages);

    return stages;
  }

  public boolean isCompleted(Stage stage) {
    InstrumentType instrumentType = getInstrumentType(stage);

    instrumentRunService.getLastCompletedInstrumentRun(instrumentRunService.getParticipantInterview(participantService.getCurrentParticipant()), instrumentType);

    return false;
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
  }

}
