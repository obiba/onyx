package org.obiba.onyx.marble.core.service.impl;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultActiveConsentServiceImpl extends PersistenceManagerAwareService implements ActiveConsentService {
  
  private static final Logger log = LoggerFactory.getLogger(DefaultActiveConsentServiceImpl.class);
  
  private Boolean consent = true;
  
  @Override
  public Boolean getConsent() {
    return consent;
  }
  
  @Override
  public void setConsent(Boolean consent) {
    this.consent = consent;
  }

  @Override
  public void validate() {
    // TODO Auto-generated method stub
    
  }

}
