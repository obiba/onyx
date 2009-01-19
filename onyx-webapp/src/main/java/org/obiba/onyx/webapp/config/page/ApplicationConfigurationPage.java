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
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.crypt.IPublicKeyStore;
import org.obiba.onyx.engine.variable.export.OnyxDataExportDestination;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.crypt.X509CertificateValidator;
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

  @SpringBean
  private IPublicKeyStore publicKeyStore;

  @SpringBean(name = "onyxDataExportDestinations")
  private List<OnyxDataExportDestination> exportDestinations;

  private List<ExportDestinationCertificateModel> certificateModels = new LinkedList<ExportDestinationCertificateModel>();

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

      TextField sessionTimeout = new TextField("sessionTimeout", new PropertyModel(model, "config.sessionTimeout"));
      sessionTimeout.add(new RequiredFormFieldBehavior());
      add(sessionTimeout.add(NumberValidator.range(0, 60 * 24)));

      studyLogo = new FileUploadField("studyLogo");

      // Make sure that the file uploaded is either a "jpg", a "gif", a "jpeg" or a "bmp".
      // add(studyLogo.add(new PatternValidator(".*((\\.jpg)|(\\.gif)|(\\.jpeg)|(\\.bmp))",
      // Pattern.CASE_INSENSITIVE)).setRequired(true));

      // Set max size of upload to two megabytes. A logo should not be bigger than that!!
      setMaxSize(Bytes.megabytes(2));
      setMultiPart(true);

      RepeatingView destinations = new RepeatingView("destinations");
      for(OnyxDataExportDestination destination : exportDestinations) {
        WebMarkupContainer container = new WebMarkupContainer(destinations.newChildId());
        container.add(new Label("destinationName", destination.getName()));
        TextArea destinationCert = new TextArea("destinationCert", new ExportDestinationCertificateModel(destination.getName()));
        destinationCert.setLabel(new Model(destination.getName()));
        destinationCert.add(new RequiredFormFieldBehavior());
        destinationCert.add(new X509CertificateValidator());
        container.add(destinationCert);

        destinations.add(container);
      }
      add(destinations);

      add(new Button("saveButton"));
    }

    public void onSubmit() {
      saveConfiguration();
      uploadStudyLogo();
      saveExportDestinationCertificates();
      setResponsePage(getApplication().getHomePage());
    }

    private void saveExportDestinationCertificates() {
      for(ExportDestinationCertificateModel certificateModel : certificateModels) {
        String destination = certificateModel.getDestinationName();
        String cert = (String) certificateModel.getObject();
        publicKeyStore.setCertificate(destination, cert);
      }
    }

    private void uploadStudyLogo() {

      // Retrieve the uploaded logo.
      FileUpload upload = studyLogo.getFileUpload();

      // Attempt to save file only if exist and was successfully transfered.
      if(upload != null) {

        // Save the logo in the "images" folder of the web application.
        ServletContext context = ((WebApplication) Application.get()).getServletContext();
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

  private class ExportDestinationCertificateModel extends Model {

    private static final long serialVersionUID = 1L;

    public String destinationName;

    public String certificate;

    ExportDestinationCertificateModel(String destinationName) {
      this.destinationName = destinationName;
      certificateModels.add(this);
    }

    public String getDestinationName() {
      return destinationName;
    }

    @Override
    public void setObject(Object object) {
      certificate = (String) object;
    }

    @Override
    public Object getObject() {
      return certificate;
    }
  }

}
