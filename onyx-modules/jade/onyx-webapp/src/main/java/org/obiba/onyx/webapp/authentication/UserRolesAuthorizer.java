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
