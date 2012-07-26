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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.config.Ini;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.config.WebIniSecurityManagerFactory;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class OnyxSecurityManagerFactory implements FactoryBean<SecurityManager> {

  private final Logger log = LoggerFactory.getLogger(OnyxSecurityManagerFactory.class);

  private Set<Realm> realms;

  private String iniPath;

  private SecurityManager securityManager;

  private Map<String, List<String>> rolesMap;

  public void setRealms(Set<Realm> realms) {
    this.realms = realms;
  }

  public void setIniPath(String iniPath) {
    this.iniPath = iniPath;
  }

  /**
   * Map to onyx roles.
   * @param keyValuePairs
   */
  public void setRolesMap(String keyValuePairs) {
    rolesMap = new HashMap<String, List<String>>();
    // Get list of strings separated by the delimiter
    StringTokenizer tokenizer = new StringTokenizer(keyValuePairs, ",");
    while(tokenizer.hasMoreElements()) {
      String token = tokenizer.nextToken();
      String[] entry = token.split("=");
      if(entry.length == 2) {
        String key = entry[1].trim();
        if(rolesMap.containsKey(key) == false) {
          rolesMap.put(key, new ArrayList<String>());
        }
        rolesMap.get(key).add(entry[0].trim());
      }
    }
  }

  @Override
  public SecurityManager getObject() throws Exception {
    if(securityManager == null) {
      try {
        WebIniSecurityManagerFactory factory = new CustomWebIniSecurityManagerFactory(Ini.fromResourcePath(iniPath));
        securityManager = factory.createInstance();
      } catch(Exception e) {
        // case shiro.ini cannot be found
        log.warn("Failed loading Shiro Ini file at path '{}': {}", iniPath, e.getMessage());
        log.debug(e.getMessage(), e);
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRealms(realms);
        securityManager = manager;
      }
      // TODO ugly, need to find better
      securityManager = new OnyxWebSecurityManager((WebSecurityManager) securityManager);
    }
    return securityManager;
  }

  @Override
  public Class<?> getObjectType() {
    return WebSecurityManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  private final class CustomWebIniSecurityManagerFactory extends WebIniSecurityManagerFactory {

    private CustomWebIniSecurityManagerFactory(Ini config) {
      super(config);
    }

    @Override
    protected void applyRealmsToSecurityManager(Collection<Realm> iniRealms, SecurityManager securityManager) {
      if(realms != null) {
        super.applyRealmsToSecurityManager(ImmutableList.<Realm> builder().addAll(realms).addAll(iniRealms).build(), securityManager);
      } else {
        super.applyRealmsToSecurityManager(iniRealms, securityManager);
      }
    }
  }

  private class OnyxSecurityManager implements SecurityManager {

    private SecurityManager wrapped;

    private OnyxSecurityManager(SecurityManager wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public Subject login(Subject subject, AuthenticationToken authenticationToken) throws AuthenticationException {
      return wrapped.login(subject, authenticationToken);
    }

    @Override
    public void logout(Subject subject) {
      wrapped.logout(subject);
    }

    @Override
    public Subject createSubject(SubjectContext context) {
      return wrapped.createSubject(context);
    }

    @Override
    public AuthenticationInfo authenticate(AuthenticationToken authenticationToken) throws AuthenticationException {
      AuthenticationInfo info = wrapped.authenticate(authenticationToken);

      return info;
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
      return wrapped.isPermitted(principals, permission);
    }

    @Override
    public boolean isPermitted(PrincipalCollection subjectPrincipal, Permission permission) {
      return wrapped.isPermitted(subjectPrincipal, permission);
    }

    @Override
    public boolean[] isPermitted(PrincipalCollection subjectPrincipal, String... permissions) {
      return wrapped.isPermitted(subjectPrincipal, permissions);
    }

    @Override
    public boolean[] isPermitted(PrincipalCollection subjectPrincipal, List<Permission> permissions) {
      return wrapped.isPermitted(subjectPrincipal, permissions);
    }

    @Override
    public boolean isPermittedAll(PrincipalCollection subjectPrincipal, String... permissions) {
      return wrapped.isPermittedAll(subjectPrincipal, permissions);
    }

    @Override
    public boolean isPermittedAll(PrincipalCollection subjectPrincipal, Collection<Permission> permissions) {
      return wrapped.isPermittedAll(subjectPrincipal, permissions);
    }

    @Override
    public void checkPermission(PrincipalCollection subjectPrincipal, String permission) throws AuthorizationException {
      wrapped.checkPermission(subjectPrincipal, permission);
    }

    @Override
    public void checkPermission(PrincipalCollection subjectPrincipal, Permission permission) throws AuthorizationException {
      wrapped.checkPermission(subjectPrincipal, permission);
    }

    @Override
    public void checkPermissions(PrincipalCollection subjectPrincipal, String... permissions) throws AuthorizationException {
      wrapped.checkPermissions(subjectPrincipal, permissions);
    }

    @Override
    public void checkPermissions(PrincipalCollection subjectPrincipal, Collection<Permission> permissions) throws AuthorizationException {
      wrapped.checkPermissions(subjectPrincipal, permissions);
    }

    @Override
    public boolean hasRole(PrincipalCollection subjectPrincipal, String roleIdentifier) {
      if(wrapped.hasRole(subjectPrincipal, roleIdentifier)) return true;
      else if(rolesMap.containsKey(roleIdentifier)) {
        for(String role : rolesMap.get(roleIdentifier)) {
          if(wrapped.hasRole(subjectPrincipal, role)) return true;
        }
      }
      return false;
    }

    @Override
    public boolean[] hasRoles(PrincipalCollection subjectPrincipal, List<String> roleIdentifiers) {
      // TODO
      return wrapped.hasRoles(subjectPrincipal, roleIdentifiers);
    }

    @Override
    public boolean hasAllRoles(PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers) {
      // TODO
      return wrapped.hasAllRoles(subjectPrincipal, roleIdentifiers);
    }

    @Override
    public void checkRole(PrincipalCollection subjectPrincipal, String roleIdentifier) throws AuthorizationException {
      // TODO
      wrapped.checkRole(subjectPrincipal, roleIdentifier);
    }

    @Override
    public void checkRoles(PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers) throws AuthorizationException {
      // TODO
      wrapped.checkRoles(subjectPrincipal, roleIdentifiers);
    }

    @Override
    public void checkRoles(PrincipalCollection subjectPrincipal, String... roleIdentifiers) throws AuthorizationException {
      // TODO
      wrapped.checkRoles(subjectPrincipal, roleIdentifiers);
    }

    @Override
    public Session start(SessionContext context) {
      return wrapped.start(context);
    }

    @Override
    public Session getSession(SessionKey key) throws SessionException {
      return wrapped.getSession(key);
    }

  }

  private final class OnyxWebSecurityManager extends OnyxSecurityManager implements WebSecurityManager {

    private WebSecurityManager wrapped;

    private OnyxWebSecurityManager(WebSecurityManager wrapped) {
      super(wrapped);
      this.wrapped = wrapped;
    }

    @Override
    public boolean isHttpSessionMode() {
      return wrapped.isHttpSessionMode();
    }

  }

}
