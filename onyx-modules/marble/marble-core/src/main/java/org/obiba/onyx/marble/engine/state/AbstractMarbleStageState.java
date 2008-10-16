package org.obiba.onyx.marble.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMarbleStageState extends AbstractStageState {

  private static final Logger log = LoggerFactory.getLogger(AbstractMarbleStageState.class);
  
  private ActiveConsentService activeConsentService;

  public void setActiveConsentService(ActiveConsentService activeConsentService) {
    this.activeConsentService = activeConsentService;
  }

  protected ActiveConsentService getActiveConsentService() {
    return activeConsentService;
  }

  @Override
  public Data getData(String key) {
    if(key.equals("Consent")) {
      return DataBuilder.buildBoolean(activeConsentService.getConsent().isAccepted());
    }
    return null;
  }

}
