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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authentication.panel.SignInPanel;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession.AuthenticateErrorCode;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.DialogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginPanel extends SignInPanel {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(LoginPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private AuthenticateErrorCode errCode = null;

  private Dialog forgotPasswordDialog;

  public LoginPanel(String id) {
    super(id, false);
    remove("feedback");

    forgotPasswordDialog = createForgotPasswordDialog();
    add(forgotPasswordDialog);

    AjaxLink link = new AjaxLink("forgotPassword") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        forgotPasswordDialog.show(target);
      }

    };
    ((MarkupContainer) get("signInForm")).add(link);
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
    userSessionService.setWorkstation(((WebRequest) getRequest()).getHttpServletRequest().getRemoteHost());
    log.info("User {} logged in from workstation {}.", userSessionService.getUser().getLogin(), userSessionService.getWorkstation());
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

  private Dialog createForgotPasswordDialog() {
    Dialog dialog = null;

    IModel titleModel = (new ResourceModel("ForgotPassword")).wrapOnAssignment(this);

    IModel messageModel = new SpringStringResourceModel("ForgotPasswordMessage", "ForgotPasswordMessage");
    Component content = new MultiLineLabel("content", messageModel);

    dialog = DialogBuilder.buildInfoDialog("forgotPasswordDialog", titleModel, content).getDialog();
    dialog.setHeightUnit("em");
    dialog.setInitialHeight(9);

    return dialog;
  }
}
