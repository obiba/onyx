/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.obiba.onyx.ruby.core.domain.parser.impl.RegularExpressionBarcodePartParser;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.ruby.core.wicket.wizard.RubyWizardForm;
import org.obiba.onyx.ruby.core.wicket.wizard.RubyWizardPanel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

public class RubyPanelTest {
  //
  // Instance Variables
  //

  private RubyPanel rubyPanel;

  private WicketTester wicketTester;

  private ExtendedApplicationContextMock applicationContextMock;

  private ModuleRegistry moduleRegistry;

  private EntityQueryService entityQueryServiceMock;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveTubeRegistrationService activeTubeRegistrationServiceMock;

  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  private ParticipantTubeRegistration participantTubeRegistration;

  private Stage stage;

  private User user;

  private Participant participant;

  private Interview interview;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() throws Exception {
    initDomainObjects();
    initApplicationContext();
    initWicketTester();
  }

  //
  // Test Methods
  //

  @Test
  public void testSetActionWindow() {
    recordAndReplayCommonExpectations();

    startWicketPanel(false);

    ActionWindow actionWindow = new ActionWindow("actionWindow") {
      private static final long serialVersionUID = 1L;

      public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
        ;
      }
    };
    rubyPanel.setActionWindow(actionWindow);

    verifyCommonMocks();

    // Verify that the specified action window passed to the RubyWizardForm.
    RubyWizardPanel wizardPanel = (RubyWizardPanel) rubyPanel.get("content");
    Assert.assertNotNull(wizardPanel);
    RubyWizardForm wizardForm = (RubyWizardForm) wizardPanel.get("form");
    Assert.assertNotNull(wizardForm);
    ActionWindow formActionWindow = wizardForm.getActionWindow();
    Assert.assertNotNull(formActionWindow);
    Assert.assertEquals(actionWindow, formActionWindow);
  }

  @Test
  public void testSetFeedbackPanel() {
    recordAndReplayCommonExpectations();

    startWicketPanel(false);

    FeedbackWindow feedbackWindow = new FeedbackWindow("feedback");
    rubyPanel.setFeedbackWindow(feedbackWindow);

    verifyCommonMocks();

    // Verify that the specified feedback panel was passed to the RubyWizardForm.
    RubyWizardPanel wizardPanel = (RubyWizardPanel) rubyPanel.get("content");
    Assert.assertNotNull(wizardPanel);
    RubyWizardForm wizardForm = (RubyWizardForm) wizardPanel.get("form");
    Assert.assertNotNull(wizardForm);
    FeedbackWindow formFeedbackWindow = wizardForm.getFeedbackWindow();
    Assert.assertNotNull(formFeedbackWindow);
    Assert.assertEquals(feedbackWindow, formFeedbackWindow);
  }

  @Test
  public void testGetFeedbackPanel() {
    recordAndReplayCommonExpectations();

    startWicketPanel(false);

    FeedbackWindow feedbackWindow = new FeedbackWindow("feedbackPanel");
    rubyPanel.setFeedbackWindow(feedbackWindow);
    verifyCommonMocks();

    Assert.assertEquals(feedbackWindow, rubyPanel.getFeedbackWindow());
  }

  //
  // Helper Methods
  //

  private void initDomainObjects() {
    user = createUser(1l, "user", "test");
    participant = createParticipant(1l, "participant", "test");

    interview = createInterview(1l);
    interview.setParticipant(participant);

    stage = new Stage();
    stage.setName("SamplesCollection");

    tubeRegistrationConfiguration = createTubeRegistrationConfiguration();

    participantTubeRegistration = new ParticipantTubeRegistration();
    participantTubeRegistration.setTubeRegistrationConfig(tubeRegistrationConfiguration);
    participantTubeRegistration.setInterview(interview);
  }

  private void initApplicationContext() {
    applicationContextMock = new ExtendedApplicationContextMock();

    moduleRegistry = new ModuleRegistry();
    applicationContextMock.putBean("moduleRegistry", moduleRegistry);

    entityQueryServiceMock = createMock(EntityQueryService.class);
    applicationContextMock.putBean("entityQueryService", entityQueryServiceMock);

    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    applicationContextMock.putBean("activeInterviewService", activeInterviewServiceMock);

    activeTubeRegistrationServiceMock = createMock(ActiveTubeRegistrationService.class);
    applicationContextMock.putBean("activeTubeRegistrationService", activeTubeRegistrationServiceMock);

    applicationContextMock.putBean("tubeRegistrationConfiguration", tubeRegistrationConfiguration);
  }

  private void initWicketTester() {
    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    wicketTester = new WicketTester(application);
  }

  private void startWicketPanel(final boolean resuming) {
    wicketTester.startPanel(new TestPanelSource() {
      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        rubyPanel = new RubyPanel(panelId, stage, resuming);
        return rubyPanel;
      }
    });
  }

  private User createUser(long id, String firstName, String lastName) {
    User user = new User();
    user.setId(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);

    return user;
  }

  private Participant createParticipant(long id, String firstName, String lastName) {
    Participant participant = new Participant();
    participant.setId(id);
    participant.setFirstName(firstName);
    participant.setLastName(lastName);

    return participant;
  }

  private Interview createInterview(long id) {
    Interview interview = new Interview();
    interview.setId(id);

    return interview;
  }

  private TubeRegistrationConfiguration createTubeRegistrationConfiguration() {
    TubeRegistrationConfiguration tubeRegistrationConfiguration = new TubeRegistrationConfiguration();

    RegularExpressionBarcodePartParser parser = new RegularExpressionBarcodePartParser();
    parser.setSize(10);
    parser.setExpression(".*");

    List<IBarcodePartParser> parserList = new ArrayList<IBarcodePartParser>();
    parserList.add(parser);

    BarcodeStructure barcodeStructure = new BarcodeStructure();
    barcodeStructure.setParsers(parserList);

    tubeRegistrationConfiguration.setBarcodeStructure(barcodeStructure);

    return tubeRegistrationConfiguration;
  }

  private void recordAndReplayCommonExpectations() {
    expect(activeInterviewServiceMock.getParticipant()).andReturn(participant).anyTimes();
    expect(activeInterviewServiceMock.getOperator()).andReturn(user).anyTimes();
    expect(activeInterviewServiceMock.getInterview()).andReturn(interview).anyTimes();
    expect(activeTubeRegistrationServiceMock.hasContraindications(Contraindication.Type.OBSERVED)).andReturn(false).anyTimes();
    expect(activeTubeRegistrationServiceMock.hasContraindications(Contraindication.Type.ASKED)).andReturn(false).anyTimes();
    expect(activeTubeRegistrationServiceMock.getParticipantTubeRegistration()).andReturn(participantTubeRegistration).anyTimes();
    expect(activeTubeRegistrationServiceMock.getRegisteredTubeCount()).andReturn(1).anyTimes();

    replay(activeInterviewServiceMock);
    replay(activeTubeRegistrationServiceMock);
  }

  private void verifyCommonMocks() {
    verify(activeInterviewServiceMock);
    verify(activeTubeRegistrationServiceMock);
  }
}
