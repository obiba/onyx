package org.obiba.onyx.webapp.home.page;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.interview.page.InterviewPage;

public class HomePage extends BasePage {

  @SpringBean(name = "participantService")
  private ParticipantService participantService;

  @SuppressWarnings("serial")
  public HomePage() {
    super();

    Participant participant = participantService.getCurrentParticipant();
    Link link = new Link("link") {

      @Override
      public void onClick() {
        setResponsePage(InterviewPage.class);
      }

    };
    link.add(new Label("participant", participant.getFirstName() + " " + participant.getLastName()));
    add(link);
  }

}
