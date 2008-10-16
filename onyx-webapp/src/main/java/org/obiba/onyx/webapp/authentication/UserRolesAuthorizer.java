/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.authentication;

import org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;

public class UserRolesAuthorizer implements IRoleCheckingStrategy {
  
  public UserRolesAuthorizer() {}
  
  public final boolean hasAnyRole(final Roles componentRoles) {
    final Roles userRoles = OnyxAuthenticatedSession.get().getRoles();
    return userRoles != null && userRoles.hasAnyRole(componentRoles);
  }
}
