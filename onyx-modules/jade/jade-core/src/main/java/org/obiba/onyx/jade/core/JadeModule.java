package org.obiba.onyx.jade.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageExecution;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JadeModule implements Module, ApplicationContextAware {

  private static final Logger log = LoggerFactory.getLogger(JadeModule.class);
  
  private EntityQueryService queryService;
  
  private ApplicationContext applicationContext;

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public String getName() {
    return "jade";
  }

  public StageExecution resume(Stage stage) {
    throw new UnsupportedOperationException("resume");
  }

  public StageExecution start(Stage stage) {
    JadeStageExecution exec = getCurrentStageExecution();
    // TODO check a stage execution is not already on the way in this session scope
    exec.setStage(new JadeStage(this, getInstrumentType(stage)));
    exec.start();
    return exec;
    //return new JadeStageExecution(new JadeStage(this, getInstrumentType(stage)));
  }
  
  private JadeStageExecution getCurrentStageExecution() {
    return (JadeStageExecution)applicationContext.getBean("jadeStageExecution");
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
    for (Stage stage : stages) {
      InstrumentType type = ((JadeStage)stage).getInstrumentType();
      for (InstrumentType dependentType : type.getDependentTypes()) {
        stage.addDependentStage(map.get(dependentType));
      }
    }
    
    log.info("JadeStages=" + stages);
    
    return stages;
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
