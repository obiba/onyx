package org.obiba.onyx.webapp.home.page;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.page.InterviewPage;
import org.obiba.onyx.webapp.participant.page.ParticipantSearchPage;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class HomePage extends BasePage {

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name="activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SuppressWarnings("serial")
  public HomePage() {
    super();

    add(new ParticipantSearchForm("configurationForm"));

    add(new BookmarkablePageLink("search", ParticipantSearchPage.class));

  }

  private class ParticipantSearchForm extends Form {

    private static final long serialVersionUID = 1L;

    public ParticipantSearchForm(String id) {
      super(id);

      setModel(new Model(new Participant()));

      TextField barCode = new TextField("barCode", new PropertyModel(getModelObject(), "barcode"));
      barCode.add(new RequiredFormFieldBehavior());
      barCode.setLabel(new StringResourceModel("ParticipantCode", HomePage.this, null));
      add(barCode);

      add(new Button("submit"));
    }

    @Override
    protected void onSubmit() {
      Participant template = (Participant) ParticipantSearchForm.this.getModelObject();
      Participant participant = queryService.matchOne(template);

      // Participant found, display interview page.
      if(participant != null) {
        activeInterviewService.setParticipant(participant);
        setResponsePage(InterviewPage.class);

        // Not found, display error message in feedback panel.
      } else {
        error((new StringResourceModel("ParticipantNotFound", this, ParticipantSearchForm.this.getModel())).getString());
      }
    }
  }

}
