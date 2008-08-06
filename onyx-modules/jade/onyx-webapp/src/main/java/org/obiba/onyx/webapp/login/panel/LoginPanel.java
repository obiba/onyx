package org.obiba.onyx.webapp.login.panel;

import org.apache.wicket.authentication.panel.SignInPanel;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;

public class LoginPanel extends SignInPanel {

  private static final long serialVersionUID = 1L;

  public LoginPanel(String id) {
    super(id, false);
    remove("feedback");
  }

  /**
   * @see org.apache.wicket.markup.html.form.Form#onSubmit()
   */
  public boolean signIn(java.lang.String username, java.lang.String password) {
    // Get session info
    OnyxAuthenticatedSession session = (OnyxAuthenticatedSession) getSession();
    return session.authenticate(username, password);

  }

  public void onSignInSucceeded() {
    setResponsePage(getApplication().getHomePage());
  }
  
}