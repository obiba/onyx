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

  // reception step for previously enrolled participants
  private static final String RECEPTION = "reception";

  // reception step for volunteer participants
  private static final String ENROLLMENT = "enrollment";

  // edit step for participants
  private static final String EDIT = "edit";

  @SpringBean
  private ParticipantMetadata participantMetadata;

  @SpringBean
  private ParticipantService participantService;

  @SpringBean
  private EntityQueryService queryService;

  private FeedbackPanel feedbackPanel;

  private Page sourcePage;

  private String mode;

  /**
   * Constructor in another panel
   * 
   * @param id
   * @param participantModel
   * @param sourcePage
   * @param mode
   */
  public EditParticipantPanel(String id, IModel participantModel, Page sourcePage, String mode) {
    super(id, participantModel);
    this.mode = mode;
    this.sourcePage = sourcePage;

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
  public EditParticipantPanel(String id, IModel participantModel, Page sourcePage, String mode, ModalWindow modalWindow) {
    super(id, participantModel);
    this.mode = mode;
    this.sourcePage = sourcePage;

    Form editParticipantForm = new EditParticipantForm("editParticipantForm", participantModel, modalWindow);
    add(editParticipantForm);

    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
  }

  private class EditParticipantForm extends Form {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public EditParticipantForm(String id, final IModel participantModel, final ModalWindow modalWindow) {
      super(id, participantModel);

      Participant participant = (Participant) getModelObject();

      // set recruitmentType for participant to volunteer if it is null
      if(participant.getRecruitmentType() == null) participant.setRecruitmentType(RecruitmentType.VOLUNTEER);
      if(participant.getRecruitmentType().equals(RecruitmentType.VOLUNTEER)) add(new EmptyPanel("enrollmentId"));
      else
        add(new RowFragment("enrollmentId", getModel(), "EnrollmentId", "enrollmentId"));

      if(participant.getAppointment() == null) participant.setAppointment(new Appointment(participant, new Date()));

      // set Assessment Center Id for participant
      if(participant.getSiteNo() == null) {
        ApplicationConfiguration appConfig = queryService.matchOne(new ApplicationConfiguration());
        participant.setSiteNo(appConfig.getSiteNo());
      }

      if(mode.equals(EDIT)) {
        add(new RowFragment("barcode", getModel(), "ParticipantCode", "barcode"));
        add(new RowFragment("firstName", getModel(), "FirstName", "firstName"));
        add(new RowFragment("lastName", getModel(), "LastName", "lastName"));
        add(new RowFragment("gender", getModel(), "Gender", "gender"));
        add(new RowFragment("birthDate", getModel(), "BirthDate", "birthDate"));
      } else {
        add(new EmptyPanel("barcode"));
        add(new TextFieldFragment("firstName", getModel(), "FirstName*", new TextField("value", new PropertyModel(getModel(), "firstName")).setRequired(true).setLabel(new ResourceModel("FirstName"))));
        add(new TextFieldFragment("lastName", getModel(), "LastName*", new TextField("value", new PropertyModel(getModel(), "lastName")).setRequired(true).setLabel(new ResourceModel("LastName"))));
        add(new DropDownFragment("gender", getModel(), "Gender*", createGenderDropDown()));
        add(new DateFragment("birthDate", getModel(), "BirthDate*", createBirthDateField()));
      }

      if(participantMetadata.getConfiguredAttributes().size() == 0) add(new EmptyPanel("metadata"));
      else
        add(new MetadataFragment("metadata", getModel(), participantMetadata.getConfiguredAttributes()));

      if(mode.equals(RECEPTION) || mode.equals(ENROLLMENT)) add(new AssignCodeToParticipantPanel("assignCodeToParticipantPanel", participantModel));
      else
        add(new EmptyPanel("assignCodeToParticipantPanel"));

      @SuppressWarnings("serial")
      AjaxSubmitLink submitLink = new AjaxSubmitLink("saveAction") {
        protected void onSubmit(AjaxRequestTarget target, Form form) {
          Participant participant = (Participant) EditParticipantForm.this.getModelObject();
          participantService.updateParticipant(participant);

          // submitting child form if exists
          if(form.get("assignCodeToParticipantPanel:assignCodeToParticipantForm") != null) ((AssignCodeToParticipantForm) form.get("assignCodeToParticipantPanel:assignCodeToParticipantForm")).onSubmit(participant);

          if(mode.equals(EDIT)) modalWindow.close(target);
          else
            setResponsePage(sourcePage);
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
          if(mode.equals(EDIT)) modalWindow.close(target);
          else
            setResponsePage(sourcePage);
        }
      };
      add(cancelLink);
    }
  }

  // Fragment definitions for fixed fields
  @SuppressWarnings("serial")
  private class RowFragment extends Fragment {
    public RowFragment(String id, IModel participantModel, String label, String field) {
      super(id, "rowFragment", EditParticipantPanel.this);
      add(new Label("label", new ResourceModel(label)));
      add(new Label("value", new PropertyModel(participantModel, field)));
    }
  }

  @SuppressWarnings("serial")
  private class TextFieldFragment extends Fragment {
    public TextFieldFragment(String id, IModel participantModel, String label, FormComponent component) {
      super(id, "textFieldFragment", EditParticipantPanel.this);
      add(new Label("label", new ResourceModel(label)));
      add(component);
    }
  }

  @SuppressWarnings("serial")
  private class DropDownFragment extends Fragment {
    public DropDownFragment(String id, IModel participantModel, String label, FormComponent component) {
      super(id, "genderFragment", EditParticipantPanel.this);
      add(new Label("label", new ResourceModel(label)));
      add(component);
    }
  }

  @SuppressWarnings("serial")
  private class DateFragment extends Fragment {
    public DateFragment(String id, IModel participantModel, String label, FormComponent component) {
      super(id, "dateFragment", EditParticipantPanel.this);
      add(new Label("label", new ResourceModel(label)));
      add(component);
    }
  }

  @SuppressWarnings("serial")
  private DateTextField createBirthDateField() {
    DateTextField birthDateField = new DateTextField("value", new PropertyModel(getModel(), "birthDate"), new PatternDateConverter("yyyy-MM-dd", true));

    birthDateField.setRequired(true);
    birthDateField.setLabel(new ResourceModel("BirthDateWithFormat"));

    birthDateField.add(new DatePicker() {
      @Override
      public boolean enableMonthYearSelection() {
        return true;
      }
    });

    return birthDateField;
  }

  @SuppressWarnings("serial")
  private DropDownChoice createGenderDropDown() {
    DropDownChoice genderDropDown = new DropDownChoice("gender", new PropertyModel(getModel(), "gender"), Arrays.asList(Gender.values()), new GenderRenderer()) {

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

  @SuppressWarnings("serial")
  private class GenderRenderer implements IChoiceRenderer {

    public Object getDisplayValue(Object object) {
      // Prepend "Gender." to generate the proper resource bundle key
      return "Gender." + object.toString();
    }

    public String getIdValue(Object object, int index) {
      return object.toString();
    }
  }

  // Fragment definition for Metadata fields
  @SuppressWarnings("serial")
  private class MetadataFragment extends Fragment {

    public MetadataFragment(String id, IModel participantModel, List<ParticipantAttribute> attributes) {
      super(id, "metadataFragment", EditParticipantPanel.this);

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      Participant participant = (Participant) participantModel.getObject();

      for(final ParticipantAttribute attribute : attributes) {
        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        Label label = new Label("label", (new SpringStringResourceModel(new PropertyModel(attribute, "name"))).getString());
        item.add(label);

        if(attribute.isMandatoryAtReception() == true) item.add(new Label("mandatory", " *"));
        else
          item.add(new Label("mandatory", ""));

        IModel attributeValueModel;
        if(participant.getParticipantAttributeValue(attribute.getName()) == null) {
          participant.setConfiguredAttributeValue(attribute.getName(), new Data(attribute.getType()));
          attributeValueModel = new Model(participant.getParticipantAttributeValue(attribute.getName()));
        } else {
          attributeValueModel = new DetachableEntityModel(queryService, participant.getParticipantAttributeValue(attribute.getName()));
        }

        if((mode.equals(EDIT) && attribute.isEditableAfterReception() == true) || (!mode.equals(EDIT) && attribute.isEditableAtReception())) {
          DataField field;
          if(attribute.getAllowedValues() != null && attribute.getAllowedValues().size() > 0) {
            List<Data> allowedValues = new ArrayList<Data>();
            for(String value : attribute.getAllowedValues()) {
              allowedValues.add(new Data(attribute.getType(), value));
            }

            field = new DataField("field", new PropertyModel(attributeValueModel, "data"), attribute.getType(), allowedValues, new IChoiceRenderer() {

              public Object getDisplayValue(Object object) {
                Data data = (Data) object;
                return (new SpringStringResourceModel(data.getValueAsString())).getString();
              }

              public String getIdValue(Object object, int index) {
                Data data = (Data) object;
                return data.getValueAsString();
              }

            }, null);

            if(attribute.isMandatoryAtReception() == false) ((DropDownChoice) field.getField()).setNullValid(true);
          } else {
            field = new DataField("field", new PropertyModel(attributeValueModel, "data"), attribute.getType());
          }

          if(attribute.isMandatoryAtReception() == true) field.setRequired(true);
          field.setLabel(new SpringStringResourceModel(new PropertyModel(attribute, "name")));
          field.getField().add(new AttributeAppender("class", true, new Model("nofocus"), " "));

          item.add(field);
        } else {
          item.add(new Label("field", new Model(participant.getConfiguredAttributeValue(attribute.getName()))));
        }
      }
    }
  }
}
