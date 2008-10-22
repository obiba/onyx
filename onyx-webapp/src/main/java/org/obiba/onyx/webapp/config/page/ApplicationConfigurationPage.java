/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.config.page;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfigurationPage extends BasePage {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ApplicationConfigurationPage.class);

  @SpringBean
  private ApplicationConfigurationService appConfigService;

  @SpringBean
  private UserService userService;

  public ApplicationConfigurationPage() {
    add(new ConfigurationForm("configurationForm"));
  }

  private class ConfigurationForm extends Form {

    private static final long serialVersionUID = -8937533839333908882L;

    ConfigurationFormModel model = new ConfigurationFormModel();

    FileUploadField studyLogo;

    @SuppressWarnings("serial")
    public ConfigurationForm(String name) {
      super(name);

      TextField studyName = new TextField("studyName", new PropertyModel(model, "config.studyName"));
      studyName.add(new RequiredFormFieldBehavior());
      add(studyName.add(StringValidator.maximumLength(30)));

      TextField siteName = new TextField("siteName", new PropertyModel(model, "config.siteName"));
      siteName.add(new RequiredFormFieldBehavior());
      add(siteName.add(StringValidator.maximumLength(30)));

      TextField siteNo = new TextField("siteNo", new PropertyModel(model, "config.siteNo"));
      siteNo.add(new RequiredFormFieldBehavior());
      add(siteNo.add(StringValidator.maximumLength(30)));

      TextField administratorName = new TextField("lastName", new PropertyModel(model, "user.lastName"));
      administratorName.add(new RequiredFormFieldBehavior());
      add(administratorName.add(StringValidator.maximumLength(30)));

      TextField administratorFirstName = new TextField("firstName", new PropertyModel(model, "user.firstName"));
      administratorFirstName.add(new RequiredFormFieldBehavior());
      add(administratorFirstName.add(StringValidator.maximumLength(30)));

      TextField administratorUserLogin = new TextField("login", new PropertyModel(model, "user.login"));
      administratorUserLogin.add(new RequiredFormFieldBehavior());
      add(administratorUserLogin.add(StringValidator.maximumLength(30)));

      PasswordTextField administratorUserPassword = new PasswordTextField("password", new PropertyModel(model, "user.password"));
      administratorUserPassword.add(new RequiredFormFieldBehavior());
      add(administratorUserPassword.add(StringValidator.maximumLength(30)));

      PasswordTextField administratorUserPasswordConfirm = new PasswordTextField("confirmPassword", new Model(""));
      administratorUserPasswordConfirm.add(new RequiredFormFieldBehavior());
      add(administratorUserPasswordConfirm.add(StringValidator.maximumLength(30)));

      // Validate that the password and the confirmed password are the same.
      add(new EqualPasswordInputValidator(administratorUserPassword, administratorUserPasswordConfirm));

      TextField administratorEmail = new TextField("email", new PropertyModel(model, "user.email"));
      add(administratorEmail.add(EmailAddressValidator.getInstance()));
      add(administratorEmail.add(StringValidator.maximumLength(30)));

      studyLogo = new FileUploadField("studyLogo");

      // Make sure that the file uploaded is either a "jpg", a "gif", a "jpeg" or a "bmp".
      add(studyLogo.add(new PatternValidator(".*((\\.jpg)|(\\.gif)|(\\.jpeg)|(\\.bmp))")).setRequired(true));

      // Set max size of upload to two megabytes. A logo should not be bigger than that!!
      setMaxSize(Bytes.megabytes(2));
      setMultiPart(true);

      AutoCompleteTextField participantUpdateDirectory = new AutoCompleteTextField("participantUpdateDirectory", new PropertyModel(model, "participantDirectoryPath")) {

        @SuppressWarnings("unchecked")
        protected Iterator getChoices(String input) {
          File dir = new File(input);
          List<String> choices = new ArrayList<String>();
          if(dir.exists() && dir.isDirectory()) {
            choices.add(dir.getAbsolutePath());
            for(File subDir : dir.listFiles()) {
              if(subDir.isDirectory() && !subDir.getName().startsWith(".")) {
                choices.add(subDir.getAbsolutePath());
              }
            }
          }
          return choices.iterator();
        }

      };
      participantUpdateDirectory.add(new AbstractValidator() {

        protected void onValidate(IValidatable validatable) {
          File dir = new File((String) validatable.getValue());
          if(!dir.exists()) {
            validatable.error(new ParticipantsListDirectoryValidationError("ParticipantsListRepositoryDoesNotExist"));
          } else if(!dir.isDirectory()) {
            validatable.error(new ParticipantsListDirectoryValidationError("ParticipantsListRepositoryIsNotDirectory"));
          }
        }

      });
      participantUpdateDirectory.add(new RequiredFormFieldBehavior());
      add(participantUpdateDirectory);

      add(new Button("saveButton"));
    }

    public void onSubmit() {
      saveConfiguration();
      uploadStudyLogo();
      setResponsePage(getApplication().getHomePage());
    }

    private void uploadStudyLogo() {

      // Retrieve the uploaded logo.
      FileUpload upload = studyLogo.getFileUpload();

      // Attempt to save file only if exist and was successfully transfered.
      if(upload != null) {

        // Save the logo in the "images" folder of the web application.
        ServletContext context = ((WebApplication) RequestCycle.get().getApplication()).getServletContext();
        File newFile = new File(context.getRealPath("/images"), "studyLogo.jpg");

        try {

          newFile.createNewFile();
          upload.writeTo(newFile);

        } catch(Exception e) {
          throw new IllegalStateException("Unable to write logo file", e);
        } finally {
          try {
            upload.closeStreams();
          } catch(Exception e) {
            // Ignore exception.
          }
        }
      }

    }

    private void saveConfiguration() {

      // set up the roles
      Role adminRole = userService.createRole(Role.SYSTEM_ADMINISTRATOR);
      userService.createRole(Role.PARTICIPANT_MANAGER);
      userService.createRole(Role.DATA_COLLECTION_OPERATOR);

      // Setup administrator user.
      User user = model.getUser();
      user.setPassword(User.digest(user.getPassword()));
      user.addRole(adminRole);
      user.setLanguage(Session.get().getLocale());
      user.setDeleted(false);
      user.setStatus(Status.ACTIVE);
      userService.createUser(user);

      // Save initial application configuration.
      appConfigService.createApplicationConfiguration(model.getConfig());
    }

  }

  private class ConfigurationFormModel implements Serializable {

    private static final long serialVersionUID = -3683550452902579885L;

    private User user = new User();

    private ApplicationConfiguration config = new ApplicationConfiguration();

    public ApplicationConfiguration getConfig() {
      return config;
    }

    public User getUser() {
      return user;
    }

    public void setParticipantDirectoryPath(String path) {
      String dirPath = new File(path).getAbsolutePath();
      log.info("participantDirectoryPath={}", dirPath);
      config.setParticipantDirectoryPath(dirPath);
    }

    public String getParticipantDirectoryPath() {
      return config.getParticipantDirectoryPath();
    }
  }

  @SuppressWarnings("serial")
  private class ParticipantsListDirectoryValidationError implements IValidationError, Serializable {

    private String key;

    public ParticipantsListDirectoryValidationError(String key) {
      this.key = key;
    }

    public String getErrorMessage(IErrorMessageSource messageSource) {
      return ApplicationConfigurationPage.this.getString(key);
    }

  }

}
