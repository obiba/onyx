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
