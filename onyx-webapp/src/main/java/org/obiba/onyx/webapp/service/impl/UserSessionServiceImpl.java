/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;

public class UserSessionServiceImpl implements UserSessionService {

  private UserService userService;

  private String datePattern = DEFAULT_DATE_FORMAT_PATTERN;

  private String dateTimePattern = DEFAULT_DATETIME_FORMAT_PATTERN;

  private String userLogin;

  private String sessionId;

  private Locale locale;

  /**
   * Initializes this bean with the current {@code OnyxAuthenticatedSession} instance. It is necessary to copy the data
   * from the session in case calls to this bean's methods are made outside a request cycle. It is not possible to
   * determine which session to return when calling {@code OnyxAuthenticatedSession#get()} when no HTTP request is bound
   * to the thread.
   * <p>
   * Copying the values allows this implementation to be used in back-end services that may not be currently bound to a
   * HTTP request.
   */
  public void initialize() {
    OnyxAuthenticatedSession session = OnyxAuthenticatedSession.get();

    userLogin = session.getUser().getLogin();
    sessionId = session.getId();
    locale = session.getLocale();
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public void setDatePattern(String pattern) {
    this.datePattern = pattern;
  }

  public void setDateTimePattern(String pattern) {
    this.dateTimePattern = pattern;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    // Try to set the current session's locale. This may fail if we're not in a request cycle (it's not possible to find
    // the session outside a request cycle).
    OnyxAuthenticatedSession.get().setLocale(locale);
    this.locale = locale;
  }

  public User getUser() {
    return userService.getUserWithLogin(userLogin);
  }

  public String getSessionId() {
    return sessionId;
  }

  public DateFormat getDateFormat() {
    return new SimpleDateFormat(datePattern, getLocale());
  }

  public DateFormat getDateTimeFormat() {
    return new SimpleDateFormat(dateTimePattern, getLocale());
  }

}
