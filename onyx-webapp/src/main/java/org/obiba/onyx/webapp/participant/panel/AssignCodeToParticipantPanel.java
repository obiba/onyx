package org.obiba.onyx.webapp.participant.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.participant.page.ParticipantSearchPage;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class AssignCodeToParticipantPanel extends Panel {

  @SpringBean
  private ParticipantService participantService;

  private static final long serialVersionUID = 1L;

  public AssignCodeToParticipantPanel(String id, DetachableEntityModel participant) {

    super(id);

    add(new AssignCodeToParticipantForm("assignCodeToParticipantForm", participant));

  }

  private class AssignCodeToParticipantForm extends Form {

    private static final long serialVersionUID = 1L;

    public AssignCodeToParticipantForm(String id, DetachableEntityModel participant) {
      super(id);

      setModel(new Model(new Participant()));

      TextField participantCode = new TextField("participantCode", new PropertyModel(getModelObject(), "barcode"));
      participantCode.add(new RequiredFormFieldBehavior());
      add(participantCode);

      final Model receptionCommentModel = new Model();
      add(new TextArea("comment", receptionCommentModel));

      add(new Button("submit", participant ) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit() {
          super.onSubmit();

          Participant participant = (Participant)AssignCodeToParticipantForm.this.getModelObject();
          
          participantService.assignCodeToParticipant((Participant) getModelObject(), participant.getBarcode(), (String)receptionCommentModel.getObject(), OnyxAuthenticatedSession.get().getUser());

          setResponsePage(ParticipantSearchPage.class);
        }
      });

      add(new AjaxLink("cancel") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          setResponsePage(ParticipantSearchPage.class);
        }

      });

    }
  }

}
