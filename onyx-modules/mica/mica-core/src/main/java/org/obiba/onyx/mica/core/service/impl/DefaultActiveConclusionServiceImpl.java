package org.obiba.onyx.mica.core.service.impl;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultActiveConclusionServiceImpl extends PersistenceManagerAwareService implements ActiveConclusionService {
  
  private static final Logger log = LoggerFactory.getLogger(DefaultActiveConclusionServiceImpl.class);
  
  private Boolean conclusion = true;
  
  public Boolean getConclusion() {
    return conclusion;
  }
  
  public void setConclusion(Boolean conclusion) {
    this.conclusion = conclusion;
  }

  public void validate() {
    // TODO Auto-generated method stub
    
  }

}
