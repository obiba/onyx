package org.obiba.onyx.mica.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMicaStageState extends AbstractStageState {

  private static final Logger log = LoggerFactory.getLogger(AbstractMicaStageState.class);
  
  private ActiveConclusionService activeConclusionService;

  public void setActiveConclusionService(ActiveConclusionService activeConclusionService) {
    this.activeConclusionService = activeConclusionService;
  }

  protected ActiveConclusionService getActiveConclusionService() {
    return activeConclusionService;
  }

  @Override
  public Data getData(String key) {
    if(key.equals("Consent")) {
      log.info("getData(" + key +")=" + activeConclusionService.getConclusion());
      return DataBuilder.buildBoolean(activeConclusionService.getConclusion());
    }
    return null;
  }
  
  @Override
  protected Boolean areDependenciesCompleted() {
    return super.areDependenciesCompleted();
  }

}
