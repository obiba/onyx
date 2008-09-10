package org.obiba.onyx.webapp.login.panel;

import org.apache.wicket.authentication.panel.SignInPanel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.wicket.util.JavascriptEventAlert;

public class LoginPanel extends SignInPanel {

  private static final long serialVersionUID = 1L;

  public LoginPanel(String id) {
    super(id, false);
    remove("feedback");

    Link link = new Link("forgotPassword"){

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
    return session.authenticate(username, password);

  }

  public void onSignInSucceeded() {
    setResponsePage(getApplication().getHomePage());
  }

}