package org.obiba.onyx.webapp.participant.panel;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.test.MockSpringApplication;

public class CommentsModalPanelTest implements Serializable {

  private static final long serialVersionUID = 1L;

  private transient WicketTester tester;

  private ActiveInterviewService activeInterviewServiceMock;

  private EntityQueryService queryServiceMock;

  private CommentsModalPanel commentsModalPanel;

  @Before
  public void setup() {
    ApplicationContextMock mockCtx = new ApplicationContextMock();

    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    mockCtx.putBean("activeInterviewService", activeInterviewServiceMock);

    queryServiceMock = createMock(EntityQueryService.class);
    mockCtx.putBean("queryService", queryServiceMock);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);

    tester = new WicketTester(application);
  }

  /** 
   * Tests that all types of comments -- general and stage -- are displayed in the Previous
   * Comments section, ordered by date (most recent first).
   */
  @SuppressWarnings("serial")
  @Test
  public void testCommentsDisplayedMostRecentFirst() {
    User user = createUser();
    Interview interview = createInterview(1l);
    Stage stage = createStage(1l, "marble", "CON", "Participant Consent", 1);
    Participant participant = createParticipant(1l, "Suzan", "Tremblay");
    String generalComment = "general comment";
    String stageComment = "stage execute comment";
    Date now = new Date();
    Date oneHourAgo = new Date(now.getTime() - 1000*60*60);
    
    //
    // Create a list of two comments for this test, ordered by date (most recent first).
    //
    // NOTE: The order corresponds to the expected order of comments returned by 
    // ActiveInterviewService.getInterviewComments().
    //
    List<Action> commentActions = new ArrayList<Action>();
    commentActions.add(createStageComment(user, interview, stage, ActionType.EXECUTE, now, stageComment));
    commentActions.add(createGeneralComment(user, interview, oneHourAgo, generalComment));

    // Expect that CommentsModalPanel calls ActiveInterviewService.getInterviewComments()
    // to retrieve all comments related to the current interview, and return the test comments.
    expect(activeInterviewServiceMock.getInterviewComments()).andReturn(commentActions);

    // Expect that CommentsModalPanel calls ActiveInterviewService.getParticipant() to
    // retrieve the participant currently being interviewed, and return the test participant.
    expect(activeInterviewServiceMock.getParticipant()).andReturn(participant);

    replay(activeInterviewServiceMock);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        return commentsModalPanel = new CommentsModalPanel(panelId, new ModalWindow("bogus")) {
          @Override
          public void onAddComments(AjaxRequestTarget target) {
            // do nothing
          }
        };
      }
    });

    verify(activeInterviewServiceMock);
   
    //
    // Get a reference to the FIRST comment panel and verify that it contains the most
    // recent comment -- i.e., the stage comment.
    //
    // NOTE: In the current user interface, the comment string is contained in the FOURTH row 
    // of the comment panel.
    //
    KeyValueDataPanel commentPanel = (KeyValueDataPanel) commentsModalPanel.get("previousComments:comment-list:1:comment-panel");
    Assert.assertNotNull(commentPanel);

    MultiLineLabel commentLabel = (MultiLineLabel) commentPanel.get("repeating:4:" + KeyValueDataPanel.getRowValueId());
    Assert.assertNotNull(commentLabel);
    Assert.assertEquals(stageComment, commentLabel.getModelObjectAsString());
    
    //
    // Now get a reference to the SECOND comment panel and verify that it contains the
    // earlier comment -- i.e., the general comment.
    //
    commentPanel = (KeyValueDataPanel) commentsModalPanel.get("previousComments:comment-list:2:comment-panel");
    Assert.assertNotNull(commentPanel);

    commentLabel = (MultiLineLabel) commentPanel.get("repeating:4:" + KeyValueDataPanel.getRowValueId());
    Assert.assertNotNull(commentLabel);
    Assert.assertEquals(generalComment, commentLabel.getModelObjectAsString());
  }

  private User createUser() {
    User user = new User();
    user.setId(1l);

    return user;
  }

  private Interview createInterview(long id) {
    Interview interview = new Interview();
    interview.setId(id);

    return interview;
  }

  private Stage createStage(long id, String module, String name, String description, int displayOrder) {
    Stage stage = new Stage();
    stage.setId(id);
    stage.setModule(module);
    stage.setDisplayOrder(displayOrder);

    return stage;
  }

  private Participant createParticipant(long id, String firstName, String lastName) {
    Participant participant = new Participant();
    participant.setId(id);
    participant.setFirstName(firstName);
    participant.setLastName(lastName);

    return participant;
  }

  private Action createGeneralComment(User user, Interview interview, Date date, String comment) {
    Action commentAction = new Action();

    commentAction.setActionType(ActionType.COMMENT);
    commentAction.setUser(user);
    commentAction.setInterview(interview);
    commentAction.setDateTime(date);
    commentAction.setComment(comment);

    return commentAction;
  }

  private Action createStageComment(User user, Interview interview, Stage stage, ActionType type, Date date, String comment) {
    Action commentAction = new Action();

    commentAction.setActionType(type);
    commentAction.setUser(user);
    commentAction.setInterview(interview);
    commentAction.setStage(stage);
    commentAction.setDateTime(date);
    commentAction.setComment(comment);

    return commentAction;
  }
}