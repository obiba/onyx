/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import java.util.Date;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.webapp.participant.page.InterviewPage;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

@SuppressWarnings("serial")
public class UnlockInterviewPanelTest {

  private WicketTester tester;

  private InterviewManager interviewManager;

  private Participant p = newTestParticipant();

  private User u = new User();

  @Before
  public void setup() {
    ExtendedApplicationContextMock mockCtx = new ExtendedApplicationContextMock();

    interviewManager = createMock(InterviewManager.class);

    mockCtx.putBean("interviewManager", interviewManager);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);
    application.setHomePage(Page.class);

    tester = new WicketTester(application);

    u.setFirstName("FirstName");
    u.setLastName("LastName");
  }

  // @Test
  // This test is commented due to the fact that the panel redirects to the InterviewPage. It is a difficult page to
  // render because it has so many dependencies...
  public void testUnlock() {
    expect(interviewManager.getInterviewer(p)).andReturn(u).once();
    expect(interviewManager.overrideInterview(p)).andReturn(null).once();
    EasyMock.replay(interviewManager);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        return new UnlockInterviewPanel(panelId, new Model(p));
      }
    });
    tester.dumpPage();

    tester.executeAjaxEvent("panel:ok", "onclick");
    tester.assertNoErrorMessage();

    EasyMock.verify(interviewManager);
    tester.assertRenderedPage(InterviewPage.class);
  }

  @Test
  public void testCancelUnlock() {
    // We expect the updateParticipant method to be called once
    expect(interviewManager.getInterviewer(p)).andReturn(u).once();
    EasyMock.replay(interviewManager);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        return new UnlockInterviewPanel(panelId, new Model(p));
      }
    });

    tester.dumpPage();
    tester.executeAjaxEvent("panel:cancel", "onclick");
    tester.assertNoErrorMessage();

    EasyMock.verify(interviewManager);
  }

  public Participant newTestParticipant() {
    Participant p = new Participant();
    p.setFirstName("Marcel");
    p.setLastName("Tremblay");
    p.setBirthDate(new Date());
    p.setGender(Gender.MALE);
    return p;
  }
}
