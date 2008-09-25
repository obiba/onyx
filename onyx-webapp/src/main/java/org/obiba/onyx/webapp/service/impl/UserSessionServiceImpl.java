package org.obiba.onyx.webapp.service.impl;

import java.util.Locale;

import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;

public class UserSessionServiceImpl implements UserSessionService {
 
  public Locale getLocale() {
    return OnyxAuthenticatedSession.get().getLocale();
  }

  public void setLocale(Locale locale) {
    OnyxAuthenticatedSession.get().setLocale(locale);
  }
  
  public User getUser() {
    return OnyxAuthenticatedSession.get().getUser();
  }

}
