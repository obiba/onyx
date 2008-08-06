package org.obiba.onyx.webapp.login.page;


import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.login.panel.LoginPanel;


public class LoginPage extends BasePage {

  private static final long serialVersionUID = -3536960410263657341L;

  public LoginPage() {
    add(new LoginPanel("loginPanel"));
  }
  
 
}
