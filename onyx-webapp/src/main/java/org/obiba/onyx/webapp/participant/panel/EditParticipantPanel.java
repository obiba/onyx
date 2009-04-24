/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.DateValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Group;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.reusable.Dialog;
import org.obiba.onyx.core.reusable.FeedbackWindow;
import org.obiba.onyx.core.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.core.reusable.Dialog.Status;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.webapp.participant.panel.AssignCodeToParticipantPanel.AssignCodeToParticipantForm;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditParticipantPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(EditParticipantPanel.class);

  // constant for fixed attributes
  private static String BARCODE = "barcode";

  private static String FIRST_NAME = "firstName";

  private static String LAST_NAME = "lastName";

  private static String GENDER = "gender";

  private static String BIRTH_DATE = "birthDate";

  private static final int TEXTFIELD_SIZE = 40;

  @SpringBean
  private ParticipantMetadata participantMetadata;

  @SpringBean
  private ParticipantService participantService;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private UserSessionService userSessionService;

  private FeedbackWindow feedbackWindow;

  private Page sourcePage;

  private PanelMode mode;

  AssignCodeToParticipantPanel assignCodePanel;

  IModel participantModel;

  Dialog modalWindow;

  private static enum PanelMode {
    RECEPTION, ENROLLMENT, EDIT
  }

  public EditParticipantPanel(String id, IModel participantModel, Page sourcePage) {
    super(id, participantModel);
    this.sourcePage = sourcePage;
    this.participantModel = participantModel;
    initMode();

    createEditParticipantPanel(sourcePage);

    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);
  }

  /**
   * Constructor in another panel
   * 
   * @param id
   * @param participantModel
   * @param sourcePage
   * @param editParticipantForm
   */
  public EditParticipantPanel(String id, IModel participantModel, Page sourcePage, Form editParticipantForm) {
    this(id, participantModel, sourcePage);
    addActionButtons(null, editParticipantForm);
  }

  /**
   * Constructor in a dialog window
   * 
   * @param id
   * @param participantModel
   * @param sourcePage
   * @param modalWindow
   */
  public EditParticipantPanel(String id, final IModel participantModel, final Page sourcePage, final Dialog modalWindow) {
    this(id, participantModel, sourcePage);
    this.modalWindow = modalWindow;
    addDialogActionButtons(modalWindow, participantModel);
  }

  private void addActionButtons(final Dialog modalWindow, final Form editParticipantForm) {

    @SuppressWarnings("serial")
    AjaxSubmitLink submitLink = new AjaxSubmitLink("saveAction") {
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        updateParticipant(target);
      }

      protected void onError(AjaxRequestTarget target, Form form) {
        displayFeedback(target);
      }
    };
    editParticipantForm.add(submitLink);

    @SuppressWarnings("serial")
    AjaxLink cancelLink = new AjaxLink("cancelAction") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        cancelEditParticipant(target);
      }
    };
    editParticipantForm.add(cancelLink);

  }

  @SuppressWarnings("serial")
  private void addDialogActionButtons(final Dialog modalWindow, final IModel participantModel) {
    modalWindow.setCloseButtonCallback(new CloseButtonCallback() {

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        if(status == null) return true;
        switch(status) {

        case SUCCESS:
          updateParticipant(target);
          break;

        case ERROR:
          displayFeedback(target);
          return false;

        case CANCELLED:
          cancelEditParticipant(target);
          break;

        default:
          break;

        }
        return true;
      }

    });
  }

  private void cancelEditParticipant(AjaxRequestTarget target) {
    if(mode == PanelMode.EDIT && modalWindow != null) {
      modalWindow.close(target);
    } else {
      setResponsePage(sourcePage);
    }
  }

  private void displayFeedback(AjaxRequestTarget target) {
    feedbackWindow.setContent(new FeedbackPanel("content"));
    feedbackWindow.show(target);
  }

  private void updateParticipant(AjaxRequestTarget target) {
    Participant participant = (Participant) participantModel.getObject();
    participantService.updateParticipant(participant);

    // submitting child form if it's visible
    if(assignCodePanel.isVisible()) {
      ((AssignCodeToParticipantForm) assignCodePanel.get("assignCodeToParticipantForm")).onSubmit(participant);
    }

    if(mode == PanelMode.EDIT && modalWindow != null) {
      modalWindow.close(target);
    } else {
      setResponsePage(sourcePage);
    }
  }

  private void createEditParticipantPanel(final Page sourcePage) {
    Participant participant = (Participant) participantModel.getObject();

    // set recruitmentType for participant to volunteer if it is null
    if(participant.getRecruitmentType() == null) {
      participant.setRecruitmentType(RecruitmentType.VOLUNTEER);
    }

    if(participant.getExported() == null) {
      participant.setExported(false);
    }
    if(participant.getRecruitmentType().equals(RecruitmentType.VOLUNTEER)) {
      add(new EmptyPanel("enrollmentId"));
    } else
      add(new RowFragment("enrollmentId", getModel(), "EnrollmentId", "enrollmentId"));

    if(participant.getAppointment() == null) {
      participant.setAppointment(new Appointment(participant, new Date()));
    }

    // set Assessment Center Id for participant
    if(participant.getSiteNo() == null) {
      ApplicationConfiguration appConfig = queryService.matchOne(new ApplicationConfiguration());
      participant.setSiteNo(appConfig.getSiteNo());
    }

    if(mode == PanelMode.EDIT) {
      add(new RowFragment(BARCODE, getModel(), "ParticipantCode", BARCODE));
      add(new RowFragment(FIRST_NAME, getModel(), "FirstName", FIRST_NAME));
      add(new RowFragment(LAST_NAME, getModel(), "LastName", LAST_NAME));
    } else {
      add(new EmptyPanel(BARCODE));

      FormComponent firstName = new TextField("value", new PropertyModel(getModel(), FIRST_NAME)).setRequired(true).setLabel(new ResourceModel("FirstName")).add(new StringValidator.MaximumLengthValidator(250));
      firstName.add(new StringValidator.MaximumLengthValidator(250));
      add(new TextFieldFragment(FIRST_NAME, getModel(), "FirstName*", firstName));

      FormComponent lastName = new TextField("value", new PropertyModel(getModel(), LAST_NAME)).setRequired(true).setLabel(new ResourceModel("LastName")).add(new StringValidator.MaximumLengthValidator(250));
      lastName.add(new StringValidator.MaximumLengthValidator(250));
      add(new TextFieldFragment(LAST_NAME, getModel(), "LastName*", lastName));
    }

    add(new DropDownFragment(GENDER, getModel(), "Gender*", createGenderDropDown()));

    // A birthdate is valid only if it is in the following interval -> [currentDate - 130 years, currentDate]
    FormComponent birthDate = createBirthDateField();
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.YEAR, -130);
    birthDate.add(DateValidator.range(calendar.getTime(), new Date()));
    add(new DateFragment(BIRTH_DATE, getModel(), "BirthDate*", birthDate));

    add(new AttributeGroupsFragment("configuredAttributeGroups", getModel()));

    add(assignCodePanel = new AssignCodeToParticipantPanel("assignCodeToParticipantPanel", participantModel, participantMetadata) {

      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        return mode == PanelMode.RECEPTION || mode == PanelMode.ENROLLMENT;
      }
    });

  }

  /**
   * Initializes the Panel's {@code mode} attribute. The mode is determined by examining the participant's attributes.
   */
  private void initMode() {
    Participant participant = (Participant) getModelObject();
    if(participant.getBarcode() != null) {
      // The participant has already been received/enrolled. Obviously, we are editing
      mode = PanelMode.EDIT;
    } else {
      // Mode is ENROLLMENT if the participant is a volunteer
      // Mode is RECEPTION if the participant has been enrolled
      mode = participant.getRecruitmentType() == RecruitmentType.VOLUNTEER ? PanelMode.ENROLLMENT : PanelMode.RECEPTION;
    }
  }

  // Fragment definitions for fixed fields
  private class RowFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public RowFragment(String id, IModel participantModel, String label, String field) {
      super(id, "rowFragment", EditParticipantPanel.this);
      add(new Label("label", new ResourceModel(label)));

      IModel valueModel = new PropertyModel(participantModel, field);

      // Date handling. If the field value is a date, format it using the configured
      // date format. Note: Since we cannot tell from the participant meta-data whether
      // this is a date or a date/time, currently we are assuming here that this is a date
      // and formatting accordingly.
      if(valueModel.getObject() != null && valueModel.getObject() instanceof Date) {
        final Date date = (Date) valueModel.getObject();

        valueModel = new Model() {
          private static final long serialVersionUID = 1L;

          @Override
          public Object getObject() {
            return userSessionService.getDateFormat().format(date);
          }
        };
      }

      add(new Label("value", valueModel));
    }
  }

  private class TextFieldFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public TextFieldFragment(String id, IModel participantModel, String label, FormComponent component) {
      super(id, "textFieldFragment", EditParticipantPanel.this);
      add(new Label("label", new ResourceModel(label)));
      add(component);

      addNoFocusCssClassInReceptionMode(component);
    }
  }

  private class DropDownFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public DropDownFragment(String id, IModel participantModel, String label, FormComponent component) {
      super(id, "genderFragment", EditParticipantPanel.this);
      add(new Label("label", new ResourceModel(label)));
      add(component);

      addNoFocusCssClassInReceptionMode(component);
    }
  }

  private class DateFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public DateFragment(String id, IModel participantModel, String label, FormComponent component) {
      super(id, "dateFragment", EditParticipantPanel.this);
      add(new Label("label", new ResourceModel(label)));
      add(component);

      addNoFocusCssClassInReceptionMode(component);
    }
  }

  private DateTextField createBirthDateField() {
    DateTextField birthDateField = new DateTextField("value", new PropertyModel(getModel(), BIRTH_DATE), new PatternDateConverter("yyyy-MM-dd", true));

    birthDateField.setRequired(true);
    birthDateField.setLabel(new ResourceModel("BirthDateWithFormat"));

    birthDateField.add(new DatePicker() {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean enableMonthYearSelection() {
        return true;
      }
    });

    return birthDateField;
  }

  private DropDownChoice createGenderDropDown() {
    DropDownChoice genderDropDown = new DropDownChoice(GENDER, new PropertyModel(getModel(), GENDER), Arrays.asList(Gender.values()), new GenderRenderer()) {

      private static final long serialVersionUID = 1L;

      @Override
      protected boolean localizeDisplayValues() {
        // Returning true will make the parent class lookup the value returned by the call
        // to GenderRenderer.getDisplayValue() as a key in the localizer
        // (ie: localizer.getString(renderer.getDisplayValue())
        return true;
      }
    };
    genderDropDown.setRequired(true);
    genderDropDown.setLabel(new ResourceModel("Gender"));
    genderDropDown.setOutputMarkupId(true);

    return genderDropDown;
  }

  private static class GenderRenderer implements IChoiceRenderer {

    private static final long serialVersionUID = 1L;

    public Object getDisplayValue(Object object) {
      // Prepend "Gender." to generate the proper resource bundle key
      return (object != null) ? "Gender." + object.toString() : null;
    }

    public String getIdValue(Object object, int index) {
      return object.toString();
    }
  }

  private class AttributeGroupsFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public AttributeGroupsFragment(String id, IModel participantModel) {
      super(id, "attributeGroupsFragment", EditParticipantPanel.this);

      RepeatingView repeater = new RepeatingView("groupRepeater");
      add(repeater);

      List<Group> groups = getGroups();

      for(int i = 0; i < groups.size(); i++) {
        Group group = groups.get(i);

        WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
        repeater.add(item);

        String groupNameKey = group.getName();
        String groupName = !group.isDefaultGroup() ? (new SpringStringResourceModel(groupNameKey, groupNameKey)).getString() : null;

        item.add(new Label("groupName", groupName).setVisible(groupName != null));
        item.add(new AttributeGroupFragment("group", participantModel, group));
      }
    }

    private List<Group> getGroups() {
      List<Group> groups = new ArrayList<Group>();

      if(participantMetadata.getConfiguredAttributes().size() != 0) {
        Group currentGroup = null;

        for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
          Group group = attribute.getGroup();

          if((currentGroup == null) || (group.getName() == null && currentGroup.getName() != null) || (group.getName() != null && currentGroup.getName() == null) || (group.getName() != null && currentGroup.getName() != null && !group.getName().equals(currentGroup.getName()))) {
            groups.add(group);
            currentGroup = group;
          }
        }
      }

      return groups;
    }
  }

  private class AttributeGroupFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public AttributeGroupFragment(String id, IModel participantModel, Group group) {
      super(id, "attributeGroupFragment", EditParticipantPanel.this);

      RepeatingView repeat = new RepeatingView("attributeRepeater");
      add(repeat);

      Participant participant = (Participant) participantModel.getObject();

      for(final ParticipantAttribute attribute : group.getParticipantAttributes()) {
        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        Label label = new Label("label", new SpringStringResourceModel(new PropertyModel(attribute, "name")));
        item.add(label);

        if(attribute.isMandatoryAtReception()) {
          item.add(new Label("mandatory", " *"));
        } else {
          item.add(new Label("mandatory", ""));
        }

        IModel attributeValueModel;
        if(participant.getParticipantAttributeValue(attribute.getName()) == null) {
          participant.setConfiguredAttributeValue(attribute.getName(), new Data(attribute.getType()));

          // ONYX-186
          if(participant.getId() != null) {
            participantService.updateParticipant(participant);
            attributeValueModel = new DetachableEntityModel(queryService, participant.getParticipantAttributeValue(attribute.getName()));
          } else {
            attributeValueModel = new Model(participant.getParticipantAttributeValue(attribute.getName()));
          }
        } else {
          attributeValueModel = new DetachableEntityModel(queryService, participant.getParticipantAttributeValue(attribute.getName()));
        }

        // Field is editable if the Panel's mode is EDIT and the attribute allows edition AFTER reception
        // OR the panel's mode is NOT EDIT and the attribute allows edition AT reception.
        boolean editable = false;
        if(mode == PanelMode.EDIT) {
          editable = attribute.isEditableAfterReception();
        } else if(mode == PanelMode.RECEPTION || mode == PanelMode.ENROLLMENT) {
          editable = attribute.isEditableAtReception();
        }

        if(editable) {
          final DataField field;
          if(attribute.getAllowedValues() != null && attribute.getAllowedValues().size() > 0) {
            List<Data> allowedValues = new ArrayList<Data>();
            for(String value : attribute.getAllowedValues()) {
              allowedValues.add(new Data(attribute.getType(), value));
            }

            field = new DataField("field", new PropertyModel(attributeValueModel, "data"), attribute.getType(), allowedValues, new IChoiceRenderer() {

              private static final long serialVersionUID = 1L;

              public Object getDisplayValue(Object object) {
                Data data = (Data) object;
                return (new SpringStringResourceModel(data.getValueAsString())).getString();
              }

              public String getIdValue(Object object, int index) {
                Data data = (Data) object;
                return data.getValueAsString();
              }

            }, null);

            if(!attribute.isMandatoryAtReception()) ((DropDownChoice) field.getField()).setNullValid(true);
          } else {
            field = new DataField("field", new PropertyModel(attributeValueModel, "data"), attribute.getType());

            // Add any validator defined by the current ParticipantAttribute.
            List<IValidator> validators = attribute.getValidators();
            for(IValidator validator : validators) {
              field.add(validator);
            }

            if(field.getField() instanceof TextField) {
              // ONYX-203: Although participant metadata do not currently indicate the maximum length
              // of configured text attributes, for now impose a maximum (250 characters should be safe)
              // to avoid persistence errors when the values are longer than the data source permits.
              if(validators.size() > 0) {
                field.getField().add(new DataValidator(new StringValidator.MaximumLengthValidator(250), DataType.TEXT));
              }

              field.getField().add(new AttributeModifier("size", true, new Model(TEXTFIELD_SIZE)));
            }
          }

          if(attribute.isMandatoryAtReception()) field.setRequired(true);
          field.setLabel(new SpringStringResourceModel(new PropertyModel(attribute, "name")));
          field.getField().add(new AttributeAppender("class", true, new Model("nofocus"), " "));

          item.add(field);
        } else {
          String value = (participant.getConfiguredAttributeValue(attribute.getName()) != null) ? participant.getConfiguredAttributeValue(attribute.getName()).getValueAsString() : null;
          item.add(new Label("field", new Model(value)));
        }
      }
    }
  }

  /**
   * Adds the CSS "nofocus" class to the specified form component if the the current edit mode is <code>RECEPTION</code>
   * (i.e., when the participant is being received).
   * 
   * @param component form component
   */
  private void addNoFocusCssClassInReceptionMode(FormComponent component) {
    if(mode == PanelMode.RECEPTION) {
      component.add(new AttributeAppender("class", true, new Model("nofocus"), " "));
    }
  }
}
