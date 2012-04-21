/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.page;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.obiba.onyx.core.etl.participant.impl.AbstractFileBasedParticipantReader;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.obiba.onyx.webapp.participant.panel.UpdateParticipantListPanel;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class UpdateParticipantListWindowTest {
  //
  // Instance Variables
  //

  private AppointmentManagementService appointmentManagementServiceMock;

  private ParticipantReaderTest participantReaderMock = new ParticipantReaderTest();

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    ApplicationContextMock applicationContextMock = new ExtendedApplicationContextMock();
    appointmentManagementServiceMock = createMock(AppointmentManagementService.class);
    applicationContextMock.putBean("appointmentManagementService", appointmentManagementServiceMock);

    applicationContextMock.putBean("participantReader", participantReaderMock);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    new WicketTester(application);
  }

  //
  // Test Methods
  //

  @Test
  public void testShowConfirmationOnCreation() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");
    Component confirmationFragment = getContentFragment(window);
    Assert.assertTrue(confirmationFragment instanceof UpdateParticipantListPanel.ConfirmationFragment);
  }

  @Test
  public void testShowConfirmation() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");
    window.showConfirmation();
    Component confirmationFragment = getContentFragment(window);
    Assert.assertTrue(confirmationFragment instanceof UpdateParticipantListPanel.ConfirmationFragment);
  }

  @Test
  public void testShowProgress() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");
    window.showProgress();
    Component progressFragment = getContentFragment(window);
    Assert.assertTrue(progressFragment instanceof UpdateParticipantListPanel.ProgressFragment);
  }

  @Test
  public void testShowResultSucceeded() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");

    expect(appointmentManagementServiceMock.getLastAppointmentUpdateStats()).andReturn(new AppointmentUpdateStats());

    replay(appointmentManagementServiceMock);
    window.showResult(true);
    verify(appointmentManagementServiceMock);

    Fragment resultFragment = getContentFragment(window);
    Assert.assertTrue(resultFragment instanceof UpdateParticipantListPanel.ResultFragment);

    Label resultLabel = (Label) resultFragment.get("resultLabel");
    Assert.assertEquals("ParticipantsListSuccessfullyUpdated", resultLabel.getDefaultModelObject());
  }

  @Test
  public void testShowResultFailed() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");
    window.showResult(false);
    Fragment resultFragment = getContentFragment(window);
    Assert.assertTrue(resultFragment instanceof UpdateParticipantListPanel.ResultFragment);

    Label resultLabel = (Label) resultFragment.get("resultLabel");
    Assert.assertEquals("ParticipantListUpdateFailed", resultLabel.getDefaultModelObject());
  }

  //
  // Helper Methods
  //

  private Fragment getContentFragment(UpdateParticipantListWindow window) {
    UpdateParticipantListPanel panel = window.getWindowContent();
    return (Fragment) panel.get("contentFragment");
  }

  private class ParticipantReaderTest extends AbstractFileBasedParticipantReader {

    public ParticipantReaderTest() {
      super();
    }

    @Override
    public String getFilePattern() {
      // TODO Auto-generated method stub
      return null;
    }

    public Participant read() throws Exception, UnexpectedInputException, ParseException {
      // TODO Auto-generated method stub
      return null;
    }
  }

}
