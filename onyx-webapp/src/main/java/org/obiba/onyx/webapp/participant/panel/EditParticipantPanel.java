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

import java.util.Arrays;
import java.util.Date;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.webapp.participant.panel.AssignCodeToParticipantPanel.AssignCodeToParticipantForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditParticipantPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(EditParticipantPanel.class);

  private static final String RECEPTION = "reception";

  private static final String ENROLLMENT = "enrollment";

  @SpringBean
  private ParticipantService participantService;

  @SpringBean
  private EntityQueryService queryService;

  private FeedbackPanel feedbackPanel;

  private Page sourcePage;

  private String mode;

  public EditParticipantPanel(String id, IModel participantModel, Page sourcePage, String mode) {
    super(id, participantModel);
    this.mode = mode;
    this.sourcePage = sourcePage;

    Form editParticipantForm = new EditParticipantForm("editParticipantForm", participantModel);
    add(editParticipantForm);

    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
  }

  private class EditParticipantForm extends Form {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public EditParticipantForm(String id, final IModel participantModel) {
      super(id, participantModel);

      Participant participant = (Participant) getModelObject();

      // set recruitmentType for participant to volunteer if it is null
      if(participant.getRecruitmentType() == null) {
        participant.setRecruitmentType(RecruitmentType.VOLUNTEER);
        add(new EmptyPanel("enrollmentId"));
      } else
        add(new RowFragment("enrollmentId", getModel()));

      if(participant.getAppointment() == null) participant.setAppointment(new Appointment(participant, new Date()));

      // set Assessment Center Id for participant
      if(participant.getSiteNo() == null) {
        ApplicationConfiguration appConfig = queryService.matchOne(new ApplicationConfiguration());
        participant.setSiteNo(appConfig.getSiteNo());
      }

      add(new TextField("firstName", new PropertyModel(getModel(), "firstName")).setRequired(true).setLabel(new ResourceModel("FirstName")));
      add(new TextField("lastName", new PropertyModel(getModel(), "lastName")).setRequired(true).setLabel(new ResourceModel("LastName")));
      add(createGenderDropDown());
      add(createBirthDateField());

      if(mode.equals(RECEPTION) || mode.equals(ENROLLMENT)) {
        add(new AssignCodeToParticipantPanel("assignCodeToParticipantPanel", participantModel));
      } else {
        add(new EmptyPanel("assignCodeToParticipantPanel"));
      }

      @SuppressWarnings("serial")
      AjaxSubmitLink submitLink = new AjaxSubmitLink("saveAction") {
        protected void onSubmit(AjaxRequestTarget target, Form form) {
          Participant participant = (Participant) EditParticipantForm.this.getModelObject();
          participantService.updateParticipant(participant);

          // submitting child form if exists
          if(form.get("assignCodeToParticipantPanel:assignCodeToParticipantForm") != null) ((AssignCodeToParticipantForm) form.get("assignCodeToParticipantPanel:assignCodeToParticipantForm")).onSubmit(participant);

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
          setResponsePage(sourcePage);
        }
      };
      add(cancelLink);
    }
  }

  @SuppressWarnings("serial")
  private class RowFragment extends Fragment {

    public RowFragment(String id, IModel participantModel) {
      super(id, "rowFragment", EditParticipantPanel.this);
      add(new Label("label", new ResourceModel("EnrollmentId")));
      add(new Label("value", new PropertyModel(participantModel, "enrollmentId")));
    }
  }

  @SuppressWarnings("serial")
  private DateTextField createBirthDateField() {
    DateTextField birthDateField = new DateTextField("birthDate", new PropertyModel(getModel(), "birthDate"), new PatternDateConverter("yyyy-MM-dd", true));

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

}
