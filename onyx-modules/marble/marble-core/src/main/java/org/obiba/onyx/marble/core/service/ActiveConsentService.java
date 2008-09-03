package org.obiba.onyx.marble.core.service;

public interface ActiveConsentService {

  public Boolean getConsent();
  
  public void setConsent(Boolean consent);
  
  public void validate();
  
}
