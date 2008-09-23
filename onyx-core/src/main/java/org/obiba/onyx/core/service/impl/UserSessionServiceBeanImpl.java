package org.obiba.onyx.core.service.impl;

import java.util.Locale;

import org.obiba.onyx.core.service.UserSessionService;

/**
 * An implementation of {@link UserSessionService} that stores the locale as a member variable. The default locale value
 * is Locale.ENGLISH.
 * <p>
 * This class is designed to be used during unit tests.
 * 
 * @author philippe
 * 
 */
public class UserSessionServiceBeanImpl implements UserSessionService {

  private Locale locale = Locale.ENGLISH;

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

}
