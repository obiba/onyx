package org.obiba.onyx.jade.core;

import java.util.ArrayList;
import java.util.List;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageExecution;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;

public class JadeModule implements Module {

  private EntityQueryService queryService;
  
  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public String getName() {
    return "jade";
  }

  public StageExecution resume(Stage stage) {
    throw new UnsupportedOperationException("resume");
  }

  public StageExecution startStage(Stage stage) {
    return new JadeStageExecution(new JadeStage(this, getInstrumentType(stage)));
  }
  
  private InstrumentType getInstrumentType(Stage stage) {
    InstrumentType template = new InstrumentType(stage.getName(), null);
    return queryService.matchOne(template);
  }

  public List<Stage> getStages() {
    List<Stage> stages = new ArrayList<Stage>();
    for (InstrumentType type : queryService.list(InstrumentType.class)) {
      stages.add(new JadeStage(this, type));
    }
    return stages;
  }

}
