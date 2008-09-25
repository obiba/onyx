package org.obiba.onyx.core.service;

import java.util.Locale;

import org.obiba.onyx.core.domain.user.User;

public interface UserSessionService {

  public Locale getLocale();
  
  public void setLocale(Locale locale);
  
  public User getUser();
}
