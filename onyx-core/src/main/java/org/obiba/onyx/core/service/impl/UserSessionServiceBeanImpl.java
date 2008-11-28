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

  private Locale locale = Locale.ENGLISH;

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }
  
  public User getUser() {
    User user = new User();
    user.setEmail("test@test.com");
    user.setFirstName("firstname");
    user.setLastName("lastname");
    return user;
  }

}
