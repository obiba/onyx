package org.obiba.onyx.marble.core.service;

import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;

public interface ActiveConsentService {
  
  public Consent getConsent();

  public void setConsent(Consent consent);
  
  public ConsentMode getMode();
  
}
