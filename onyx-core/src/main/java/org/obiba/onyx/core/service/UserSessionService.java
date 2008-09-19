package org.obiba.onyx.core.service;

import java.util.Locale;

public interface UserSessionService {

  public Locale getLocale();
  
  public void setLocale(Locale locale);
}
