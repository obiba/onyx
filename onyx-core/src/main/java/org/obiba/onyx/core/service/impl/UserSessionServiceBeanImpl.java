/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.obiba.onyx.core.domain.user.User;
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

  private static int SESSION_COUNTER = 0;

  private Locale locale = Locale.ENGLISH;

  private String datePattern = DEFAULT_DATE_FORMAT_PATTERN;

  private String dateTimePattern = DEFAULT_DATETIME_FORMAT_PATTERN;

  private int sessionId = SESSION_COUNTER++;

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
    this.locale = locale;
  }

  public User getUser() {
    User user = new User();
    user.setId(new Long(1));
    user.setLogin("admin");
    user.setEmail("test@test.com");
    user.setFirstName("firstname");
    user.setLastName("lastname");
    return user;
  }

  public String getSessionId() {
    return Integer.toString(sessionId);
  };

  public DateFormat getDateFormat() {
    return new SimpleDateFormat(datePattern, getLocale());
  }

  public DateFormat getDateTimeFormat() {
    return new SimpleDateFormat(dateTimePattern, getLocale());
  }
}