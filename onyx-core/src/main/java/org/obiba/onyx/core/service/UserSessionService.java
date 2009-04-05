/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import java.text.DateFormat;
import java.util.Locale;

import org.obiba.onyx.core.domain.user.User;

public interface UserSessionService {
  //
  // Constants
  //

  public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd";

  public static final String DEFAULT_TIME_FORMAT_PATTERN = "HH:mm";

  public static final String DEFAULT_DATETIME_FORMAT_PATTERN = DEFAULT_DATE_FORMAT_PATTERN + " " + DEFAULT_TIME_FORMAT_PATTERN;

  //
  // Methods
  //

  public Locale getLocale();

  public void setLocale(Locale locale);

  public User getUser();

  public String getSessionId();

  public DateFormat getDateFormat();

  public DateFormat getTimeFormat();

  public DateFormat getDateTimeFormat();
}
