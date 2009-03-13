/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.home.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.page.InterviewPage;
import org.obiba.onyx.webapp.participant.page.ParticipantSearchPage;
import org.obiba.onyx.webapp.participant.panel.UnlockInterviewPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class HomePage extends BasePage {

  @SpringBean
  ParticipantService participantService;

  @SpringBean
  private InterviewManager interviewManager;

  private ModalWindow unlockInterviewWindow;

  public HomePage() {
    super();

    add(new ParticipantSearchForm("searchForm"));

    add(new BookmarkablePageLink("search", ParticipantSearchPage.class));

    unlockInterviewWindow = new ModalWindow("unlockInterview");
    unlockInterviewWindow.setCssClassName("onyx");
    unlockInterviewWindow.setTitle(new ResourceModel("UnlockInterview"));
    unlockInterviewWindow.setResizable(false);
    unlockInterviewWindow.setUseInitialHeight(false);
    add(unlockInterviewWindow);

  }

  private class ParticipantSearchForm extends Form {

    private static final long serialVersionUID = 1L;

    public ParticipantSearchForm(String id) {
      super(id);

      setModel(new Model(new Participant()));

      TextField barCode = new TextField("barCode", new PropertyModel(getModel(), "barcode"));
      barCode.add(new RequiredFormFieldBehavior());
      barCode.setLabel(new StringResourceModel("ParticipantCode", HomePage.this, null));
      add(barCode);

      add(new AjaxButton("submit") {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onSubmit(AjaxRequestTarget target, Form form) {
          Participant participant = getParticipant();
          // Participant found, display interview page.
          if(participant != null) {
            if(interviewManager.isInterviewAvailable(participant)) {
              interviewManager.obtainInterview(participant);
              setResponsePage(InterviewPage.class);
            }
            unlockInterviewWindow.setContent(new UnlockInterviewPanel(unlockInterviewWindow.getContentId(), new PropertyModel(ParticipantSearchForm.this, "participant")) {

              private static final long serialVersionUID = 1L;

              @Override
              public void onCancel(AjaxRequestTarget target) {
                unlockInterviewWindow.close(target);
              }
            });
            target.appendJavascript("Wicket.Window.unloadConfirmation = false;");
            unlockInterviewWindow.show(target);
          } else {
            // Not found, display error message in feedback panel.
            error((new StringResourceModel("ParticipantNotFound", this, ParticipantSearchForm.this.getModel())).getString());
          }
        }
      });

    }

    public Participant getParticipant() {
      Participant template = (Participant) ParticipantSearchForm.this.getModelObject();
      return participantService.getParticipant(template);
    }
  }

}
