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

import java.util.Set;

import javax.naming.NamingException;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;

/**
 *
 */
public class OnyxJndiLdapRealm extends JndiLdapRealm {

  @Override
  protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principals, LdapContextFactory ldapContextFactory) throws NamingException {
    Set<String> roleNames = null;
    // TODO get groups from ldap
    JndiLdapContextFactory contextFactory = (JndiLdapContextFactory) getContextFactory();
    contextFactory.getSystemUsername();
    contextFactory.getSystemPassword();

    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
    return info;
  }

}
