/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.authentication;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;

public class OnyxRealm extends JdbcRealm {

  private EntityQueryService queryService;

  public OnyxRealm() {
    super();
    setAuthenticationQuery("select password from user where login = ? and deleted = 0");
    setUserRolesQuery("select r.name from user_roles as ur, user as u, role as r where u.login = ? and u.id = ur.user_id and ur.role_id = r.id");
    setCredentialsMatcher(new HashedCredentialsMatcher("SHA"));
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    AuthenticationInfo info = super.doGetAuthenticationInfo(token);

    User template = new User();
    template.setLogin(((UsernamePasswordToken) token).getUsername());
    template.setDeleted(false);
    User fetchedUser = queryService.matchOne(template);
    if(fetchedUser.isActive() == false) {
      throw new DisabledAccountException();
    }
    if(fetchedUser.getLanguage() != null) {
      OnyxAuthenticatedSession.get().setLocale(fetchedUser.getLanguage());
    }

    return info;
  }

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }
}
