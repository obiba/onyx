package org.obiba.onyx.webapp.participant.panel;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.Province;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.webapp.converter.GenderConverter;
import org.obiba.onyx.webapp.converter.ProvinceConverter;

public class EditParticipantPanel extends Panel {

  private static final long serialVersionUID = 1L;

  /**
   * Regular expression used to validate postal codes.
   * 
   * Note: - No postal code includes the letters D, F, I, O, Q, or U. - The letters W and Z are used, but are not
   * currently used as the first letter.
   */
  private static final String POSTAL_CODE_REGEX = "[A-Z&&[^DFIOQUWZ]]\\d[A-Z&&[^DFIOQU]] \\d[A-Z&&[^DFIOQU]]\\d";

  /**
   * Regular expression used to validate phone numbers.
   * 
   * Note: - Dashes are required between different parts of the phone number. - This is obviously very basic validation.
   * More could be done here to rule out numbers that confirm to this format but are invalid for other reasons (e.g.,
   * 000-000-0000 is not a valid phone number).
   */
  private static final String PHONE_REGEX = "\\d{3}-\\d{3}-\\d{4}";

  private static final String[] GENDER_OPTIONS = { "F", "M" };

  @SpringBean
  private ParticipantService participantService;

  private ParticipantPanel participantPanel;

  private ModalWindow parentWindow;

  private Form editParticipantForm;

  private FeedbackPanel feedbackPanel;

  public EditParticipantPanel(String id, Participant participant, ParticipantPanel participantPanel, ModalWindow parentWindow) {
    super(id);

    this.participantPanel = participantPanel;
    this.parentWindow = parentWindow;

    editParticipantForm = new EditParticipantForm("editParticipantForm", participant);
    add(editParticipantForm);

    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
  }

  private class EditParticipantForm extends Form {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public EditParticipantForm(String id, Participant participant) {
      super(id);

      setModel(new Model(participant));

      add(new TextField("firstName", new PropertyModel(getModelObject(), "firstName")).setRequired(true).setLabel(new StringResourceModel("FirstName", null)));
      add(new TextField("lastName", new PropertyModel(getModelObject(), "lastName")).setRequired(true).setLabel(new StringResourceModel("LastName", null)));
      add(createGenderDropDown());
      add(new DateField("birthDate", new PropertyModel(getModelObject(), "birthDate")).setRequired(true).setLabel(new StringResourceModel("BirthDate", null)));
      add(new TextField("street", new PropertyModel(getModelObject(), "street")));
      add(new TextField("apartment", new PropertyModel(getModelObject(), "apartment")));
      add(new TextField("city", new PropertyModel(getModelObject(), "city")));
      add(createProvinceDropDown());
      add(new TextField("country", new PropertyModel(getModelObject(), "country")));
      add(new TextField("postalCode", new PropertyModel(getModelObject(), "postalCode")).add(new PatternValidator(POSTAL_CODE_REGEX)));
      add(new TextField("phone", new PropertyModel(getModelObject(), "phone")).add(new PatternValidator(PHONE_REGEX)));

      @SuppressWarnings("serial")
      AjaxSubmitLink submitLink = new AjaxSubmitLink("saveAction") {
        protected void onSubmit(AjaxRequestTarget target, Form form) {
          target.addComponent(EditParticipantPanel.this.participantPanel);
          EditParticipantPanel.this.parentWindow.close(target);
        }

        protected void onError(AjaxRequestTarget target, Form form) {
          target.addComponent(EditParticipantPanel.this.feedbackPanel);
        }
      };
      add(submitLink);
    }

    public void onSubmit() {
      Participant participant = (Participant) getModelObject();
      participantService.updateParticipant(participant);
    }

    @SuppressWarnings("serial")
    private DropDownChoice createGenderDropDown() {
      DropDownChoice genderDropDown = new DropDownChoice("gender", new PropertyModel(getModelObject(), "gender"), Arrays.asList(Gender.values()), new GenderRenderer()) {
        @SuppressWarnings("unchecked")
        @Override
        public IConverter getConverter(Class type) {
          return new GenderConverter();
        }
      };
      genderDropDown.setType(Gender.class);
      genderDropDown.setRequired(true);
      genderDropDown.setLabel(new StringResourceModel("Gender", null));
      genderDropDown.setOutputMarkupId(true);

      return genderDropDown;
    }
    
    @SuppressWarnings("serial")
    private DropDownChoice createProvinceDropDown() {
      DropDownChoice provinceDropDown = new DropDownChoice("province", new PropertyModel(getModelObject(), "province"), Arrays.asList(Province.values()), new ProvinceRenderer()) {
        @SuppressWarnings("unchecked")
        @Override
        public IConverter getConverter(Class type) {
          return new ProvinceConverter();
        }
      };
      provinceDropDown.setType(Province.class);
      provinceDropDown.setRequired(true);
      provinceDropDown.setOutputMarkupId(true);

      return provinceDropDown;
    }
  }

  @SuppressWarnings("serial")
  private class GenderRenderer implements IChoiceRenderer {

    @Override
    public Object getDisplayValue(Object object) {
      Locale locale = EditParticipantPanel.this.getLocale();
      ResourceBundle resourceBundle = ResourceBundle.getBundle("org.obiba.onyx.webapp.OnyxApplication", locale);
      return resourceBundle.getString("Gender."+object.toString());
    }

    @Override
    public String getIdValue(Object object, int index) {
      return object.toString();
    }
  }
  
  @SuppressWarnings("serial")
  private class ProvinceRenderer implements IChoiceRenderer {

    @Override
    public Object getDisplayValue(Object object) {
      return object.toString();
    }

    @Override
    public String getIdValue(Object object, int index) {
      return object.toString();
    }
  }
}