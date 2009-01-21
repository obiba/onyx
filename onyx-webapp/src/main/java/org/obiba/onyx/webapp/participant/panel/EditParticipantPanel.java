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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.webapp.participant.panel.AssignCodeToParticipantPanel.AssignCodeToParticipantForm;
import org.obiba.onyx.wicket.data.DataField;
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

  private FeedbackPanel feedbackPanel;

  private Page sourcePage;

  private PanelMode mode;

  private static enum PanelMode {
    RECEPTION, ENROLLMENT, EDIT
  }

  /**
   * Constructor in another panel
   * 
   * @param id
   * @param participantModel
   * @param sourcePage
   * @param mode
   */
  public EditParticipantPanel(String id, IModel participantModel, Page sourcePage) {
    super(id, participantModel);
    this.sourcePage = sourcePage;
    initMode();

    Form editParticipantForm = new EditParticipantForm("editParticipantForm", participantModel, null);
    add(editParticipantForm);

    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
  }

  /**
   * Constructor in a modal window
   * 
   * @param id
   * @param participantModel
   * @param sourcePage
   * @param mode
   * @param modalWindow
   */
  public EditParticipantPanel(String id, IModel participantModel, Page sourcePage, ModalWindow modalWindow) {
    super(id, participantModel);
    this.sourcePage = sourcePage;
    initMode();

    Form editParticipantForm = new EditParticipantForm("editParticipantForm", participantModel, modalWindow);
    add(editParticipantForm);

    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
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

  private class EditParticipantForm extends Form {

    private static final long serialVersionUID = 1L;

    public EditParticipantForm(String id, final IModel participantModel, final ModalWindow modalWindow) {
      super(id, participantModel);

      Participant participant = (Participant) getModelObject();

      // set recruitmentType for participant to volunteer if it is null
      if(participant.getRecruitmentType() == null) {
        participant.setRecruitmentType(RecruitmentType.VOLUNTEER);
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
        add(new RowFragment(GENDER, getModel(), "Gender", GENDER));
        add(new RowFragment(BIRTH_DATE, getModel(), "BirthDate", BIRTH_DATE));
      } else {
        add(new EmptyPanel(BARCODE));
        add(new TextFieldFragment(FIRST_NAME, getModel(), "FirstName*", new TextField("value", new PropertyModel(getModel(), FIRST_NAME)).setRequired(true).setLabel(new ResourceModel("FirstName"))));
        add(new TextFieldFragment(LAST_NAME, getModel(), "LastName*", new TextField("value", new PropertyModel(getModel(), LAST_NAME)).setRequired(true).setLabel(new ResourceModel("LastName"))));
        add(new DropDownFragment(GENDER, getModel(), "Gender*", createGenderDropDown()));
        add(new DateFragment(BIRTH_DATE, getModel(), "BirthDate*", createBirthDateField()));
      }

      add(new MetadataFragment("metadata", getModel()));

      add(new AssignCodeToParticipantPanel("assignCodeToParticipantPanel", participantModel) {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean isVisible() {
          return mode == PanelMode.RECEPTION || mode == PanelMode.ENROLLMENT;
        }
      });

      @SuppressWarnings("serial")
      AjaxSubmitLink submitLink = new AjaxSubmitLink("saveAction") {
        protected void onSubmit(AjaxRequestTarget target, Form form) {
          Participant participant = (Participant) EditParticipantForm.this.getModelObject();
          participantService.updateParticipant(participant);

          // submitting child form if it's visible
          AssignCodeToParticipantPanel panel = (AssignCodeToParticipantPanel) form.get("assignCodeToParticipantPanel");
          if(panel.isVisible()) {
            ((AssignCodeToParticipantForm) panel.get("assignCodeToParticipantForm")).onSubmit(participant);
          }

          if(mode == PanelMode.EDIT) {
            modalWindow.close(target);
          } else {
            setResponsePage(sourcePage);
          }
        }

        protected void onError(AjaxRequestTarget target, Form form) {
          target.addComponent(EditParticipantPanel.this.feedbackPanel);
        }
      };
      add(submitLink);

      @SuppressWarnings("serial")
      AjaxLink cancelLink = new AjaxLink("cancelAction") {
        @Override
        public void onClick(AjaxRequestTarget target) {
          if(mode == PanelMode.EDIT) {
            modalWindow.close(target);
          } else {
            setResponsePage(sourcePage);
          }
        }
      };
      add(cancelLink);
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
      return "Gender." + object.toString();
    }

    public String getIdValue(Object object, int index) {
      return object.toString();
    }
  }

  // Fragment definition for Metadata fields
  private class MetadataFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public MetadataFragment(String id, IModel participantModel) {
      super(id, "metadataFragment", EditParticipantPanel.this);

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      Participant participant = (Participant) participantModel.getObject();

      for(final ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
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
          DataField field;
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

            if(field.getField() instanceof TextField) {
              field.getField().add(new AttributeModifier("size", true, new Model(TEXTFIELD_SIZE)));
            }
          }

          if(attribute.isMandatoryAtReception()) field.setRequired(true);
          field.setLabel(new SpringStringResourceModel(new PropertyModel(attribute, "name")));
          field.getField().add(new AttributeAppender("class", true, new Model("nofocus"), " "));

          item.add(field);
        } else {
          item.add(new Label("field", new Model(participant.getConfiguredAttributeValue(attribute.getName()))));
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
