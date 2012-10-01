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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.util.JdbcUtils;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;

public class OnyxRealm extends JdbcRealm {

  /**
   * The default query used to retrieve account status for the user.
   */
  protected static final String USER_PROPERTIES_QUERY = "select status,language from user where login = ? and deleted = 0";

  public OnyxRealm() {
    setAuthenticationQuery("select password from user where login = ? and deleted = 0");
    setUserRolesQuery("select r.name from user_roles as ur, user as u, role as r where u.login = ? and u.id = ur.user_id and ur.role_id = r.id");
    setCacheManager(new MemoryConstrainedCacheManager());
  }

  public void setPasswordHashAlgorithm(String algo) {
    setCredentialsMatcher(new HashedCredentialsMatcher(algo));
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    AuthenticationInfo info = super.doGetAuthenticationInfo(token);

    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      UsernamePasswordToken upToken = (UsernamePasswordToken) token;
      String username = upToken.getUsername();
      String[] result = getPropertiesForUser(conn, username);
      if(Status.INACTIVE.toString().equals(result[0])) {
        throw new DisabledAccountException();
      }
      if(result[1] != null) {
        OnyxAuthenticatedSession.get().setLocale(new Locale(result[1]));
      }
    } catch(SQLException e) {
      throw new AuthenticationException(e);
    } finally {
      JdbcUtils.closeConnection(conn);
    }

    return info;
  }

  /**
   * 
   * @param conn
   * @param username
   * @return status,language
   * @throws SQLException
   */
  private String[] getPropertiesForUser(Connection conn, String username) throws SQLException {

    String[] result = new String[] { Status.INACTIVE.toString(), null };
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(USER_PROPERTIES_QUERY);
      ps.setString(1, username);

      // Execute query
      rs = ps.executeQuery();

      // Loop over results - although we are only expecting one result, since usernames should be unique
      boolean foundResult = false;
      while(rs.next()) {

        // Check to ensure only one row is processed
        if(foundResult) {
          throw new AuthenticationException("More than one user row found for user [" + username + "]. Usernames must be unique.");
        }

        result[0] = rs.getString(1);
        result[1] = rs.getString(2);

        foundResult = true;
      }
    } finally {
      JdbcUtils.closeResultSet(rs);
      JdbcUtils.closeStatement(ps);
    }

    return result;
  }

}
