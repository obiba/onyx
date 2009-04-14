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

import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.reusable.Dialog;
import org.obiba.onyx.core.reusable.FeedbackWindow;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.wicket.markup.html.form.LocaleDropDownChoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines User Form for adding/editing user
 */
public class UserPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(UserPanel.class);

  @SpringBean
  private UserService userService;

  private ModalWindow userModalWindow;

  private FeedbackWindow feedbackWindow;

  private final FeedbackPanel feedbackPanel;

  private PasswordTextField password;

  public UserPanel(String id, IModel model, final ModalWindow modalWindow) {
    super(id, model);
    userModalWindow = modalWindow;

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    add(new UserPanelForm("userPanelForm", model));
  }

  /**
   * Expect User entity in IModel {@link User}
   */
  private class UserPanelForm extends Form {

    private static final long serialVersionUID = 1L;

    public UserPanelForm(String id, final IModel model) {
      super(id);
      setModel(model);

      TextField lastName = new TextField("lastName", new PropertyModel(getModel(), "lastName"));
      lastName.add(new RequiredFormFieldBehavior());
      lastName.add(new StringValidator.MaximumLengthValidator(20));
      add(lastName);

      TextField firstName = new TextField("firstName", new PropertyModel(getModel(), "firstName"));
      firstName.add(new RequiredFormFieldBehavior());
      firstName.add(new StringValidator.MaximumLengthValidator(20));
      add(firstName);

      TextField login = new TextField("login", new PropertyModel(getModel(), "login"));
      login.add(new LoginUnicityValidator());
      login.add(new StringValidator.LengthBetweenValidator(2, 12));
      login.add(new PatternValidator("[a-zA-Z0-9_#.]+"));
      if(((User) getModel().getObject()).getId() != null) login.setEnabled(false);
      add(login);

      password = new PasswordTextField("password", new Model(new String()));
      password.setRequired(getUser().getLogin() == null);
      password.add(new StringValidator.MaximumLengthValidator(250));
      if(getUser().getLogin() != null) password.add(new PasswordValidator());
      add(password);

      TextField email = new TextField("email", new PropertyModel(getModel(), "email"));
      email.add(new RequiredFormFieldBehavior());
      email.add(new StringValidator.MaximumLengthValidator(250));
      email.add(EmailAddressValidator.getInstance());
      add(email);

      ListMultipleChoice roles = new ListMultipleChoice("roles", new PropertyModel(getModel(), "roles"), userService.getRoles(SortingClause.create("name")));
      roles.setRequired(true);
      roles.setChoiceRenderer(new IChoiceRenderer() {

        private static final long serialVersionUID = 1L;

        public Object getDisplayValue(Object object) {
          return getString("Role." + ((Role) object).getName());
        }

        public String getIdValue(Object object, int index) {
          return (((Role) object).getId().toString());
        }
      });
      add(roles);

      LocaleDropDownChoice ddc = new LocaleDropDownChoice("language", new PropertyModel(getModel(), "language"), Arrays.asList(new Locale[] { Locale.FRENCH, Locale.ENGLISH }));
      ddc.setUseSessionLocale(true);
      // ddc.setLabel(new StringResourceModel("Language", this, null));
      ddc.add(new RequiredFormFieldBehavior());
      add(ddc);

      add(new AjaxButton("save", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form form) {
          super.onSubmit();
          User user = (User) UserPanelForm.this.getModelObject();
          String newPassword = UserPanel.this.password.getModelObjectAsString();
          // password is changed only if new password is entered
          if(newPassword != null && newPassword.length() > 0) {
            user.setPassword(User.digest(newPassword));
          }

          if(user.getLogin() == null) generateLogin(user);
          if(user.getStatus() == null) user.setStatus(Status.ACTIVE);
          user.setDeleted(false);

          userService.createOrUpdateUser(user);
          userModalWindow.close(target);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.setWindowClosedCallback(new Dialog.WindowClosedCallback() {

            public void onClose(AjaxRequestTarget target, Dialog.Status status) {
            }

          });

          feedbackWindow.show(target);
        }
      });

      // A cancel button: use an AjaxButton and disable the "defaultFormProcessing". Seems that this is the best
      // practice for this type of behaviour
      add(new AjaxButton("cancel", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form form) {
          userModalWindow.close(target);
        }
      }.setDefaultFormProcessing(false));
    }

    private class LoginUnicityValidator extends AbstractValidator {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onValidate(IValidatable validatable) {
        User existUser = userService.getUserWithLogin(validatable.getValue().toString());
        if(existUser != null) feedbackPanel.error(new StringResourceModel("LoginAlreadyUsed", UserPanel.this, null).getString());
      }
    }

    private class PasswordValidator extends AbstractValidator {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onValidate(IValidatable validatable) {
        String newPassword = validatable.getValue().toString();

        // TODO validate the password's length and strenght

        if(getUser().getPassword().equals(User.digest(newPassword))) {
          feedbackPanel.error(new StringResourceModel("PasswordPreviouslyUsed", UserPanel.this, null).getString());
        }
      }
    }

    protected User getUser() {
      return (User) getModelObject();
    }
  }

  /**
   * Generate a unique user name, only for non existing user.
   * @param user
   */
  private void generateLogin(User user) {
    if(user.getId() != null) return;

    String baseLogin = "";

    if(user.getFirstName() != null && user.getFirstName().length() > 0) baseLogin = user.getFirstName().substring(0, 1).toLowerCase();
    if(user.getLastName() != null && user.getLastName().length() > 0) baseLogin += user.getLastName().toLowerCase();

    String login = (baseLogin.length() > 12) ? baseLogin.substring(0, 12) : baseLogin;
    int i = 1;
    while(userService.getUserWithLogin(login) != null) {
      login = ((baseLogin.length() > 12) ? baseLogin.substring(0, (12 - (String.valueOf(i)).length())) : baseLogin) + i;
      i++;
    }

    user.setLogin(login);
  }
}
