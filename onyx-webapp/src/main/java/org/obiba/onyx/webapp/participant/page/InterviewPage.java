package org.obiba.onyx.webapp.participant.page;

import java.util.Calendar;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.panel.CommentsModalPanel;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.onyx.webapp.stage.panel.StageSelectionPanel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.util.DateUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

@AuthorizeInstantiation({"SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR"})
public class InterviewPage extends BasePage {

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;
    
  private Label commentsCount;
  
  AjaxLink viewComments;
  
  public InterviewPage() {
    super();

    if(activeInterviewService.getParticipant() == null || activeInterviewService.getInterview() == null) {
      setResponsePage(WebApplication.get().getHomePage());
    } else {
      final Interview interview = activeInterviewService.setInterviewOperator(OnyxAuthenticatedSession.get().getUser());
      Participant participant = activeInterviewService.getParticipant();
      
      add(new ParticipantPanel("participant", participant,true));
           
      // Create modal comments window
      final ModalWindow commentsWindow;
      add(commentsWindow = new ModalWindow("addCommentsModal"));
      commentsWindow.setTitle(new StringResourceModel("CommentsWindow", this, null));
      commentsWindow.setInitialHeight(600);
      commentsWindow.setInitialWidth(600);

      commentsWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {

        private static final long serialVersionUID = 1L;

        public void onClose(AjaxRequestTarget target) {

        }
      });

      // Add view interview comments action
      add(viewComments=new AjaxLink("viewComments") {

        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {
          commentsWindow.setContent( new CommentsModalPanel(commentsWindow) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onAddComments(AjaxRequestTarget target) {
              InterviewPage.this.updateCommentsCount();
              target.addComponent(InterviewPage.this.commentsCount);              
            }
            
          });          
          commentsWindow.show(target);
        }
      });   
      
      // Initialize comments counter
      updateCommentsCount();
      
      // Add create interview comments action      
      add(new AjaxLink("addComments") {

        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {
          commentsWindow.setContent( new CommentsModalPanel(commentsWindow) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onAddComments(AjaxRequestTarget target) {
              InterviewPage.this.updateCommentsCount();
              target.addComponent(InterviewPage.this.commentsCount);              
            }
            
          });        
          commentsWindow.show(target);
        }
      });

      KeyValueDataPanel kvPanel = new KeyValueDataPanel("interview");
      kvPanel.addRow(new StringResourceModel("StartDate", this, null), DateUtils.getFullDateModel(new PropertyModel(interview, "startDate")));
      kvPanel.addRow(new StringResourceModel("EndDate", this, null), DateUtils.getFullDateModel(new PropertyModel(interview, "stopDate")));
      kvPanel.addRow(new StringResourceModel("Status", this, null), new StringResourceModel("InterviewStatus." + interview.getStatus(), this, null));
      add(kvPanel);
      
      final ActionDefinition cancelDef = new ActionDefinition(ActionType.STOP, new StringResourceModel("CancelInterview", this, null).getString(), new StringResourceModel("ExplainCancelInterview", this, null).getString());
      cancelDef.addReasons(new String[] {"A", "B", "C", "D", "E"});
      cancelDef.setAskPassword(true);
      
      final ActionWindow actionWindow = new ActionWindow("modal"){

        private static final long serialVersionUID = 1L;

        @Override
        public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
          Calendar todayCal = Calendar.getInstance();
          activeInterviewService.setStatus(InterviewStatus.CANCELLED, todayCal.getTime());
          setResponsePage(InterviewPage.class);
        }
        
      };
      
      add(actionWindow);
      
      AjaxLink link = new AjaxLink("cancelInterview") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          actionWindow.show(target, null, cancelDef);
        }
      };
      
      if (interview.getStatus().equals(InterviewStatus.CANCELLED))
        link.setVisible(false);
      else
        link.setVisible(true);
      
      add(link);
            
      add(new StageSelectionPanel("stage-list", getFeedbackPanel()) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onViewComments(AjaxRequestTarget target) {
          commentsWindow.setContent( new CommentsModalPanel(commentsWindow) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onAddComments(AjaxRequestTarget target) {
              InterviewPage.this.updateCommentsCount();
              target.addComponent(InterviewPage.this.commentsCount);              
            }
            
          });          
          commentsWindow.show(target);
        }

        @Override
        public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
          InterviewPage.this.updateCommentsCount();
          target.addComponent(InterviewPage.this.commentsCount); 
        }
        
      });
    }
  }
  
  public void updateCommentsCount() {
    viewComments.addOrReplace(commentsCount = new Label("commentsCount", String.valueOf(activeInterviewService.getInterviewComments().size())));  
    commentsCount.setOutputMarkupId(true);
  }



}
