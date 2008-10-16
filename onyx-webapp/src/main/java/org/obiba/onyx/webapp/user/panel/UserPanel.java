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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
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
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.webapp.base.panel.AjaxLanguageChoicePanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

/**
 * Defines User Form for adding/editing user
 * @author acarey
 * 
 */
public class UserPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private UserService userService;

  private ModalWindow userModalWindow;

  private FeedbackPanel feedbackPanel;

  private PasswordTextField password;
  
  private AjaxLanguageChoicePanel language;

  public UserPanel(String id, IModel model, final ModalWindow modalWindow) {
    super(id, model);
    userModalWindow = modalWindow;
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

      feedbackPanel = new FeedbackPanel("feedback");
      feedbackPanel.setOutputMarkupId(true);
      add(feedbackPanel);

      TextField lastName = new TextField("lastName", new PropertyModel(getModel(), "lastName"));
      lastName.add(new RequiredFormFieldBehavior());
      add(lastName);

      TextField firstName = new TextField("firstName", new PropertyModel(getModel(), "firstName"));
      firstName.add(new RequiredFormFieldBehavior());
      add(firstName);

      TextField login = new TextField("login", new PropertyModel(getModel(), "login"));
      login.add(new RequiredFormFieldBehavior());
      login.setEnabled(false);
      add(login);

      password = new PasswordTextField("password", new Model(new String()));
      password.setRequired(getUser().getLogin() == null);
      if(getUser().getLogin() != null) password.add(new PasswordValidator());
      add(password);

      TextField email = new TextField("email", new PropertyModel(getModel(), "email"));
      email.add(new RequiredFormFieldBehavior());
      email.add(EmailAddressValidator.getInstance());
      add(email);

      ListMultipleChoice roles = new ListMultipleChoice("roles", new PropertyModel(getModel(), "roles"), userService.getRoles(SortingClause.create("name")));
      roles.setRequired(true);
      roles.setChoiceRenderer(new IChoiceRenderer() {

        private static final long serialVersionUID = 1L;

        public Object getDisplayValue(Object object) {
          return (new StringResourceModel("Role." + ((Role) object).getName(), UserPanel.this, null).getString());
        }

        public String getIdValue(Object object, int index) {
          return (((Role) object).getId().toString());
        }
      });
      add(roles);

      language = new AjaxLanguageChoicePanel("languageSelect", null, Arrays.asList(new Locale[] { Locale.FRENCH, Locale.ENGLISH })) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onLanguageUpdate(Locale language, AjaxRequestTarget target) {
          if(language == null) language = Locale.ENGLISH;
          //getUser().setLanguage(language);
        }

      };
      
      if(getUser().getLanguage() != null) language.setSelectedLanguage(getUser().getLanguage());
      add(language);

      add(new AjaxSubmitLink("submit") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form form) {
          super.onSubmit();
          User user = (User) UserPanelForm.this.getModelObject();
          String newPassword = UserPanel.this.password.getModelObjectAsString();
          Locale newLanguage = UserPanel.this.language.getSelectedLanguage();
          
          if(newPassword != "") user.setPassword(User.digest(newPassword));
          if(user.getLogin() == null) generateLogin(user);
          user.setLanguage((newLanguage == null) ?  Locale.ENGLISH : newLanguage);
          if(user.getStatus() == null) user.setStatus(Status.ACTIVE);
          user.setDeleted(false);

          userService.createOrUpdateUser(user);
          userModalWindow.close(target);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form form) {
          target.addComponent(feedbackPanel);
        }
      });

      add(new AjaxLink("cancel") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          userModalWindow.close(target);
        }
      });
    }

    private class PasswordValidator extends AbstractValidator {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onValidate(IValidatable validatable) {
        String newPassword = validatable.getValue().toString();

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

    String login = baseLogin;
    int i = 1;
    while(userService.getUserWithLogin(login) != null) {
      login = baseLogin + i;
      i++;
    }

    user.setLogin(login);
  }
}
