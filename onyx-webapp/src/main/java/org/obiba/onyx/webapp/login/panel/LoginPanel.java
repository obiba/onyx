/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.login.panel;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.authentication.panel.SignInPanel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession.AuthenticateErrorCode;
import org.obiba.onyx.wicket.util.JavascriptEventAlert;

public class LoginPanel extends SignInPanel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private EntityQueryService queryService;

  private AuthenticateErrorCode errCode = null;

  public LoginPanel(String id) {
    super(id, false);
    remove("feedback");

    Link link = new Link("forgotPassword") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        // TODO Auto-generated method stub
        return;
      }

    };
    link.add(new JavascriptEventAlert("onclick", new StringResourceModel("ForgotPasswordMessage", LoginPanel.this, null)));
    add(link);

  }

  /**
   * @see org.apache.wicket.markup.html.form.Form#onSubmit()
   */
  public boolean signIn(java.lang.String username, java.lang.String password) {
    // Get session info
    OnyxAuthenticatedSession session = (OnyxAuthenticatedSession) getSession();
    errCode = session.authenticate(username, password);
    return (errCode != null) ? false : true;

  }

  @Override
  protected void onSignInFailed() {

    if(errCode.equals(AuthenticateErrorCode.INACTIVE_ACCOUNT)) error(getLocalizer().getString("inactiveAccount", this, "Sign in failed"));
    if(errCode.equals(AuthenticateErrorCode.SIGNIN_ERROR)) error(getLocalizer().getString("signInFailed", this, "Sign in failed"));

  }

  @Override
  public void onSignInSucceeded() {
    setSessionTimeout();
    setResponsePage(getApplication().getHomePage());
  }

  private void setSessionTimeout() {
    ApplicationConfiguration appConfig = queryService.matchOne(new ApplicationConfiguration());

    if(appConfig != null) {
      Integer sessionTimeoutInMinutes = appConfig.getSessionTimeout();

      if(sessionTimeoutInMinutes != null) {
        ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest().getSession().setMaxInactiveInterval(sessionTimeoutInMinutes * 60);
      }
    }
  }
}
