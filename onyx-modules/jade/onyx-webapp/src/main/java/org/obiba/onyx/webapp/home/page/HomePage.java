package org.obiba.onyx.webapp.home.page;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.interview.page.InterviewPage;

public class HomePage extends BasePage {

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  @SuppressWarnings("serial")
  public HomePage() {
    super();

    add(new ParticipantSearchForm("configurationForm"));

    add(new Link("receiveParticipants") {

      private static final long serialVersionUID = 1L;

      public void onClick() {
        // To be implemented
      }

    });

  }

  private class ParticipantSearchForm extends Form {

    private static final long serialVersionUID = 1L;

    public ParticipantSearchForm(String id) {
      super(id);

      setModel(new Model(new Participant()));

      TextField barCode = new TextField("barCode", new PropertyModel(getModelObject(), "barCode"));
      barCode.add(new RequiredFormFieldBehavior());
      add(barCode);

      add(new SubmitLink("goToParticipant") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit() {
          super.onSubmit();

          Participant template = (Participant) ParticipantSearchForm.this.getModelObject();
          Participant participant = queryService.matchOne(template);
          
          // Participant found, display interview page.
          if(participant != null) {
            activeInterviewService.setParticipant(participant);
            setResponsePage(InterviewPage.class);
            
          // Not found, display error message in feedback panel.
          } else {
            error((new StringResourceModel("participantNotFound", this, ParticipantSearchForm.this.getModel())).getString());
          }

        }

      });

    }
  }

}
