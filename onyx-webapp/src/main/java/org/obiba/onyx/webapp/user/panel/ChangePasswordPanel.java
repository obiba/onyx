/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.user.panel;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.Dialog.Option;

/**
 * Defines form for password changing
 * @author acarey
 * 
 */
public abstract class ChangePasswordPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private UserService userService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private Dialog conclusionWindow;

  public ChangePasswordPanel(String id, int previousPageId) {
    super(id);
    addConclusionWindow();
    add(new ChangePasswordForm("changePasswordForm", previousPageId));
  }

  private void addConclusionWindow() {
    conclusionWindow = new Dialog("conclusionWindow");
    conclusionWindow.setOptions(Option.CLOSE_OPTION);
    conclusionWindow.setHeightUnit("em");
    conclusionWindow.setWidthUnit("em");
    conclusionWindow.setInitialHeight(10);
    conclusionWindow.setInitialWidth(34);
    conclusionWindow.setTitle(new StringResourceModel("ChangePassword", this, null));
    conclusionWindow.setOutputMarkupId(true);
    add(conclusionWindow);
  }

  private class ChangePasswordForm extends Form {

    private static final long serialVersionUID = 1L;

    ChangePasswordPanelModel model = new ChangePasswordPanelModel();

    public ChangePasswordForm(String id, final int previousPageId) {
      super(id);

      PasswordTextField password = new PasswordTextField("password", new PropertyModel(model, "password"));
      password.add(new RequiredFormFieldBehavior());
      add(password);

      PasswordTextField confirmPassword = new PasswordTextField("confirmPassword", new PropertyModel(model, "confirmPassword"));
      confirmPassword.add(new RequiredFormFieldBehavior());
      add(confirmPassword);

      // Validate that the password and the confirmed password are the same.
      add(new EqualPasswordInputValidator(password, confirmPassword));

      add(new AjaxButton("submit") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form form) {
          super.onSubmit();

          String password = model.getPassword();

          User user = userSessionService.getUser();
          if(userService.isNewPassword(user, User.digest(password))) {
            try {
              userService.updatePassword(user, User.digest(password));
              onSuccess(target);
            } catch(Exception e) {
              e.printStackTrace();
              onFailure(target);
            }
          } else {
            error(new StringResourceModel("PasswordPreviouslyUsed", this, null).getString());
            onFailure(target);
          }
        }

      });

      add(new AjaxLink("cancel") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          setResponsePage(getPage().getPageMap().get(previousPageId, -1));
        }
      });
    }
  }

  /**
   * Called when password is successfully changed
   * 
   */
  public abstract void onSuccess(AjaxRequestTarget target);

  /**
   * Called when password check or update failed.
   * 
   */
  public abstract void onFailure(AjaxRequestTarget target);

  private class ChangePasswordPanelModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String password;

    private String confirmPassword;

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getConfirmPassword() {
      return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
      this.confirmPassword = confirmPassword;
    }
  }

  public Dialog getConclusionWindow() {
    return conclusionWindow;
  }
}
