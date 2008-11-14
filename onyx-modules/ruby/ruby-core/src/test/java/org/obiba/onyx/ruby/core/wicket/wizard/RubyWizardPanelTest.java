/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
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
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;

import com.thoughtworks.xstream.XStream;

public class RubyWizardPanelTest {
  //
  // Instance Variables
  //

  private WicketTester wicketTester;

  private RubyWizardPanel rubyWizardPanel;

  private ExtendedApplicationContextMock applicationContextMock;

  private ModuleRegistry moduleRegistry;

  private EntityQueryService entityQueryServiceMock;

  private UserSessionService userSessionServiceMock;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveTubeRegistrationService activeTubeRegistrationServiceMock;

  private MessageSource messageSource;

  private User user;

  private Locale enLocale = new Locale("en");

  private Participant participant;

  private Interview interview;

  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  private List<Contraindication> contraIndicationsObservedOnly;

  private List<Contraindication> contraIndicationsAskedOnly;

  private ParticipantTubeRegistration participantTubeRegistration;

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
  public void testStartsAtTubeRegistrationStepIfNoContraindicationsConfigured() {
    // Configure NO contra-indications.
    tubeRegistrationConfiguration.setContraindications(null);

    recordAndReplayCommonExpectations(false, false);

    startWicketPanel();

    verifyCommonMocks();

    // Verify that the wizard is positioned at the Tube Registration step.
    WizardStepPanel stepPanel = (WizardStepPanel) rubyWizardPanel.getWizardForm().get("step");
    Assert.assertNotNull(stepPanel);
    Assert.assertTrue(stepPanel instanceof TubeRegistrationStep);
  }

  @Test
  public void testStartsAtObservedContraIndicationStepIfSomeConfigured() {
    // Configure OBSERVED contra-indications only.
    tubeRegistrationConfiguration.setContraindications(contraIndicationsObservedOnly);

    recordAndReplayCommonExpectations(true, false);

    startWicketPanel();

    verifyCommonMocks();

    // Verify that the wizard is positioned at the Observed Contra-indications step.
    WizardStepPanel stepPanel = (WizardStepPanel) rubyWizardPanel.getWizardForm().get("step");
    Assert.assertNotNull(stepPanel);
    Assert.assertTrue(stepPanel instanceof ObservedContraIndicationStep);
  }

  @Test
  public void testStartsAtAskedContraIndicationStepIfSomeConfigured() {
    // Configure ASKED contra-indications only.
    tubeRegistrationConfiguration.setContraindications(contraIndicationsAskedOnly);

    recordAndReplayCommonExpectations(false, true);

    startWicketPanel();

    verifyCommonMocks();

    // Verify that the wizard is positioned at the Asked Contra-indications step.
    WizardStepPanel stepPanel = (WizardStepPanel) rubyWizardPanel.getWizardForm().get("step");
    Assert.assertNotNull(stepPanel);
    Assert.assertTrue(stepPanel instanceof AskedContraIndicationStep);
  }

  //
  // Helper Methods
  //

  private void initDomainObjects() throws Exception {
    user = createUser(1l, "user", "test");
    participant = createParticipant(1l, "participant", "test");

    interview = createInterview(1l);
    interview.setUser(user);
    interview.setParticipant(participant);

    tubeRegistrationConfiguration = createTubeRegistrationConfiguration();

    contraIndicationsObservedOnly = loadContraindications("contra-indications_observedOnly.xml");
    contraIndicationsAskedOnly = loadContraindications("contra-indications_askedOnly.xml");

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

    userSessionServiceMock = createMock(UserSessionService.class);
    applicationContextMock.putBean("userSessionService", userSessionServiceMock);

    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    applicationContextMock.putBean("activeInterviewService", activeInterviewServiceMock);

    activeTubeRegistrationServiceMock = createMock(ActiveTubeRegistrationService.class);
    applicationContextMock.putBean("activeTubeRegistrationService", activeTubeRegistrationServiceMock);

    applicationContextMock.putBean("tubeRegistrationConfiguration", tubeRegistrationConfiguration);

    messageSource = createMessageSource();
    applicationContextMock.putBean("messageSource", messageSource);
  }

  private void initWicketTester() {
    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    wicketTester = new WicketTester(application);
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

  private MessageSource createMessageSource() {
    StaticMessageSource messageSource = new StaticMessageSource();
    return messageSource;
  }

  private TubeRegistrationConfiguration createTubeRegistrationConfiguration() throws Exception {
    return new TubeRegistrationConfiguration();
  }

  @SuppressWarnings("unchecked")
  private List<Contraindication> loadContraindications(String fileName) {
    List<Contraindication> contraIndications = null;

    InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);

    if(is != null) {
      XStream xstream = new XStream();
      xstream.alias("contraindication", Contraindication.class);

      contraIndications = (List<Contraindication>) xstream.fromXML(is);
    }

    return contraIndications;
  }

  private void recordAndReplayCommonExpectations(boolean hasObservedContraindications, boolean hasAskedContraindications) {
    expect(userSessionServiceMock.getLocale()).andReturn(enLocale).anyTimes();
    expect(activeInterviewServiceMock.getParticipant()).andReturn(participant).anyTimes();
    expect(activeInterviewServiceMock.getInterview()).andReturn(interview).anyTimes();
    expect(activeTubeRegistrationServiceMock.getRegisteredTubeCount()).andReturn(0).anyTimes();
    expect(activeTubeRegistrationServiceMock.getExpectedTubeCount()).andReturn(0).anyTimes();
    expect(activeTubeRegistrationServiceMock.getParticipantTubeRegistration()).andReturn(participantTubeRegistration).anyTimes();
    expect(activeTubeRegistrationServiceMock.hasContraindications(Contraindication.Type.OBSERVED)).andReturn(hasObservedContraindications).anyTimes();
    expect(activeTubeRegistrationServiceMock.hasContraindications(Contraindication.Type.ASKED)).andReturn(hasAskedContraindications).anyTimes();

    replay(userSessionServiceMock);
    replay(activeInterviewServiceMock);
    replay(activeTubeRegistrationServiceMock);
  }

  private void verifyCommonMocks() {
    verify(userSessionServiceMock);
    verify(activeInterviewServiceMock);
    verify(activeTubeRegistrationServiceMock);
  }

  private void startWicketPanel() {
    wicketTester.startPanel(new TestPanelSource() {
      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        rubyWizardPanel = new RubyWizardPanel(panelId, new Model(), new StageModel(moduleRegistry, "SamplesCollection"), false);
        return rubyWizardPanel;
      }
    });
  }
}
