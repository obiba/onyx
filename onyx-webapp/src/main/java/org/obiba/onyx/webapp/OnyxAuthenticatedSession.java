/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.protocol.http.WebSession;

public final class OnyxAuthenticatedSession extends WebSession {

  private static final long serialVersionUID = 8437488796788626235L;

  protected OnyxAuthenticatedSession(final OnyxApplication application, Request request) {
    super(request);
    InjectorHolder.getInjector().inject(this);
  }

  /**
   * Checks the given username and password and, if they are correct, sets the session User object.
   * @param login the username
   * @param password the password
   * @return AuthenticateErrorCode if the user was authenticated, null otherwise
   */
  public final AuthenticateErrorCode authenticate(final String login, final String password) {
    if(isSignedIn() == false) {

      try {
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.login(new UsernamePasswordToken(login, password.toCharArray()));
      } catch(UnknownAccountException uae) {
        return AuthenticateErrorCode.SIGNIN_ERROR;
      } catch(IncorrectCredentialsException ice) {
        return AuthenticateErrorCode.SIGNIN_ERROR;
      } catch(DisabledAccountException lae) {
        return AuthenticateErrorCode.INACTIVE_ACCOUNT;
      } catch(ExcessiveAttemptsException eae) {
        return AuthenticateErrorCode.SIGNIN_ERROR;
      } catch(AuthenticationException ae) {
        return AuthenticateErrorCode.SIGNIN_ERROR;
      }

    }
    return null;
  }

  public final boolean authenticate(final String password) {
    try {
      Subject currentUser = SecurityUtils.getSubject();
      SecurityUtils.getSecurityManager().authenticate(new UsernamePasswordToken((String) currentUser.getPrincipal(), password.toCharArray()));
      return true;
    } catch(AuthenticationException e) {
      return false;
    }
  }

  /**
   * @return true if user is signed in
   */
  public boolean isSignedIn() {
    return SecurityUtils.getSubject().isAuthenticated();
  }

  public String getUserName() {
    Subject currentUser = SecurityUtils.getSubject();
    return currentUser.isAuthenticated() ? (String) currentUser.getPrincipal() : "";
  }

  public static OnyxAuthenticatedSession get() {
    return (OnyxAuthenticatedSession) Session.get();
  }

  /**
   * Sign the user out.
   */
  public void signOut() {
    SecurityUtils.getSubject().logout();
    invalidateNow();
  }

  public enum AuthenticateErrorCode {
    SIGNIN_ERROR, INACTIVE_ACCOUNT
  }

}
