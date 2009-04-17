/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.action.panel;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionConfiguration;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

public class ActionsPanelTest {

  ExtendedApplicationContextMock mockApplicationContext;

  MockSpringApplication application;

  ActiveInterviewService mockActiveInterviewService;

  IStageExecution mockStageExecution;

  List<ActionDefinition> actionDefinitionList;

  MockActionWindow window;

  @Before
  public void setup() {
    mockActiveInterviewService = EasyMock.createMock(ActiveInterviewService.class);
    mockStageExecution = EasyMock.createMock(IStageExecution.class);

    mockApplicationContext = new ExtendedApplicationContextMock();
    mockApplicationContext.putBean("activeInterviewService", mockActiveInterviewService);

    application = new MockSpringApplication();
    application.setApplicationContext(mockApplicationContext);

    actionDefinitionList = new LinkedList<ActionDefinition>();
    actionDefinitionList.add(new ActionDefinition(ActionType.EXECUTE, "mock.EXECUTE"));
    actionDefinitionList.add(new ActionDefinition(ActionType.STOP, "mock.STOP"));

    // InterviewPage Mocks.
    mockApplicationContext.putBean("userSessionService", EasyMock.createMock(UserSessionService.class));
    mockApplicationContext.putBean(new ActionDefinitionConfiguration());
    mockApplicationContext.putBean(EasyMock.createMock(EntityQueryService.class));
    mockApplicationContext.putBean(EasyMock.createMock(InterviewManager.class));
  }

  @Test
  public void testClickActions() {

    EasyMock.expect(mockActiveInterviewService.getStageExecution((Stage) EasyMock.anyObject())).andReturn(mockStageExecution).anyTimes();
    EasyMock.expect(mockActiveInterviewService.getInteractiveStage()).andReturn(null).anyTimes();
    EasyMock.expect(mockStageExecution.getActionDefinitions()).andReturn(actionDefinitionList).anyTimes();
    EasyMock.replay(mockActiveInterviewService, mockStageExecution);

    WicketTester tester = new WicketTester(application);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        // Model may contain null since the panel only passes the value along and doesn't actually use it itself.
        return new ActionsPanel(panelId, new Model(null), window = new MockActionWindow());
      }
    });

    // tester.dumpPage();

    // Click on "EXECUTE"
    tester.executeAjaxEvent("panel:link:1", "onclick");
    Assert.assertNotNull("Expected event on modal window.", window.getClickedDefinition());
    Assert.assertEquals(actionDefinitionList.get(0).getCode(), window.getClickedDefinition().getCode());

    EasyMock.verify(mockActiveInterviewService, mockStageExecution);

    // Click on "STOP"
    tester.executeAjaxEvent("panel:link:2", "onclick");
    Assert.assertNotNull("Expected event on modal window.", window.getClickedDefinition());
    Assert.assertEquals(actionDefinitionList.get(1).getCode(), window.getClickedDefinition().getCode());

    EasyMock.verify(mockActiveInterviewService, mockStageExecution);
  }

  /**
   * Tests that the ActionsPanel checks whether the clicked ActionDefinition is still available in the StageExecution
   * before executing it.
   */
  @Test
  public void testClickActionNoLongerAvailable() {
    EasyMock.expect(mockActiveInterviewService.getStageExecution((Stage) EasyMock.anyObject())).andReturn(mockStageExecution).anyTimes();
    EasyMock.expect(mockActiveInterviewService.getInteractiveStage()).andReturn(null).anyTimes();

    // This is called by the InterviewPage upon redirect. Returning null will redirect to the HomePage.
    EasyMock.expect(mockActiveInterviewService.getParticipant()).andReturn(null).anyTimes();
    // This should be called twice: first for displaying the links, second for validating the clicked actionDefinition
    EasyMock.expect(mockStageExecution.getActionDefinitions()).andReturn(actionDefinitionList).times(2);
    EasyMock.replay(mockActiveInterviewService, mockStageExecution);

    WicketTester tester = new WicketTester(application);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        // Model may contain null since the panel only passes the value along and doesn't actually use it itself.
        return new ActionsPanel(panelId, new Model(null), window = new MockActionWindow());
      }
    });

    // Make sure both actions are present
    tester.assertVisible("panel:link:1");
    tester.assertVisible("panel:link:2");

    // Remove the first actionDefinition from the list
    // This mocks that the stage has transitioned to another state which does not allow the removed action
    actionDefinitionList.remove(0);

    // Try to click on the missing actionDefinition link
    tester.executeAjaxEvent("panel:link:1", "onclick");
    // Result is that we should NOT have a modal window event
    Assert.assertNull(window.getClickedDefinition());

    // It should have redirected to the interview page, but we didn't Mock the services properly, so the InterviewPage
    // itself then redirected to the HomePage.
    tester.assertRenderedPage(application.getHomePage());

    EasyMock.verify(mockActiveInterviewService, mockStageExecution);
  }

  @Test
  public void testActionsPanelIsInvisibleWhenInteractiveStage() {

    final Stage currentStage = new Stage();
    currentStage.setName("currentStage");

    Stage interactiveStage = new Stage();
    currentStage.setName("interactiveStage");

    EasyMock.expect(mockActiveInterviewService.getStageExecution(currentStage)).andReturn(mockStageExecution).anyTimes();

    // Mock that another stage than the one for this panel is currently interactive
    EasyMock.expect(mockActiveInterviewService.getInteractiveStage()).andReturn(interactiveStage).anyTimes();

    // This is called by the InterviewPage upon redirect. Returning null will redirect to the HomePage.
    EasyMock.expect(mockActiveInterviewService.getParticipant()).andReturn(null).anyTimes();
    // This should be called once: for displaying the links (even though they'll be invisible in the end
    EasyMock.expect(mockStageExecution.getActionDefinitions()).andReturn(actionDefinitionList).once();
    EasyMock.replay(mockActiveInterviewService, mockStageExecution);

    WicketTester tester = new WicketTester(application);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        // Model may contain null since the panel only passes the value along and doesn't actually use it itself.
        return new ActionsPanel(panelId, new LoadableDetachableModel(currentStage) {
          @Override
          protected Object load() {
            return currentStage;
          }
        }, window = new MockActionWindow());
      }
    });

    // Make sure the panel is invisible
    tester.assertInvisible("panel");
    EasyMock.verify(mockActiveInterviewService, mockStageExecution);
  }

  private static class MockActionWindow extends ActionWindow {

    private static final long serialVersionUID = 1L;

    ActionDefinition clickedDefinition = null;

    public MockActionWindow() {
      super("none");
    }

    public ActionDefinition getClickedDefinition() {
      return clickedDefinition;
    }

    @Override
    public void show(AjaxRequestTarget target, IModel stageModel, ActionDefinition actionDefinition) {
      clickedDefinition = actionDefinition;
    }

    @Override
    public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
      // Don't do anything. This is a mock.
    }
  }
}
