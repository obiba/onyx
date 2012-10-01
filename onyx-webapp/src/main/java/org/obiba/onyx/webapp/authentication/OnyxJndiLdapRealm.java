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

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OnyxJndiLdapRealm extends JndiLdapRealm {

  private final static Logger logger = LoggerFactory.getLogger(OnyxJndiLdapRealm.class);

  private String searchBase;
  private String userGroupAttribute;
  private String groupNameAttribute;
  private Map<String, String> groupRolesMap;

  /**
   * Get groups from LDAP.
   *
   * @param principals
   * @param ldapContextFactory
   * @return
   * @throws NamingException
   */
  @Override
  protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principals,
      LdapContextFactory ldapContextFactory) throws NamingException {

    Set<String> roleNames = new HashSet<String>();
    String username = (String) getAvailablePrincipal(principals);

    LdapContext systemLdapCtx = null;
    try {
      systemLdapCtx = ldapContextFactory.getSystemLdapContext();

      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

      NamingEnumeration answer = systemLdapCtx.search(searchBase, userGroupAttribute + "=" + username, constraints);
      while(answer.hasMore()) {
        SearchResult sr = (SearchResult) answer.next();
        for(NamingEnumeration attributesEnum = sr.getAttributes().getAll(); attributesEnum.hasMore(); ) {
          Attribute attr = (Attribute) attributesEnum.next();
          if(attr.getID().equalsIgnoreCase(groupNameAttribute)) {
            NamingEnumeration e = attr.getAll();
            while(e.hasMore()) {
              String role = groupRolesMap.get(e.next());
              if(role != null) roleNames.add(role);
            }
          }
        }
      }

    } finally {
      LdapUtils.closeContext(systemLdapCtx);
    }

    logger.info("Role for {}: {}", username, roleNames);

    return new SimpleAuthorizationInfo(roleNames);
  }

  public void setSearchBase(String searchBase) {
    this.searchBase = searchBase;
  }

  public void setUserGroupAttribute(String userGroupAttribute) {
    this.userGroupAttribute = userGroupAttribute;
  }

  public void setGroupNameAttribute(String groupNameAttribute) {
    this.groupNameAttribute = groupNameAttribute;
  }

  public void setGroupRolesMap(Map<String, String> groupRolesMap) {
    this.groupRolesMap = groupRolesMap;
  }
}
