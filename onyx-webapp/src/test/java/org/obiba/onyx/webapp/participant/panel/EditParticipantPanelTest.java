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

import junit.framework.Assert;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.wicket.test.MockSpringApplication;

@SuppressWarnings("serial")
public class EditParticipantPanelTest {

  private WicketTester tester;

  private ParticipantService mockParticipantService;

  private EntityQueryService mockQueryService;

  @Before
  public void setup() {

    ApplicationContextMock mockCtx = new ApplicationContextMock();

    mockParticipantService = createMock(ParticipantService.class);
    mockQueryService = createMock(EntityQueryService.class);

    mockCtx.putBean("participantService", mockParticipantService);
    mockCtx.putBean("entityQueryService", mockQueryService);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);
    application.setHomePage(Page.class);

    tester = new WicketTester(application);
  }

  @SuppressWarnings("serial")
  @Test
  public void testUpdateParticipantInfo() {
    final Participant p = newTestParticipant();

    // We expect the updateParticipant method to be called once
    mockParticipantService.updateParticipant(p);
    expect(mockQueryService.matchOne((ApplicationConfiguration) EasyMock.anyObject())).andReturn(new ApplicationConfiguration());

    EasyMock.replay(mockParticipantService);
    EasyMock.replay(mockQueryService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        DummyHomePage dummyHomePage = new DummyHomePage();
        return new EditParticipantPanel(panelId, new Model(p), dummyHomePage, "edit");
      }
    });
    tester.dumpPage();

    FormTester formTester = tester.newFormTester("panel:editParticipantForm");
    formTester.select("gender", 0);

    tester.executeAjaxEvent("panel:editParticipantForm:saveAction", "onclick");
    tester.assertNoErrorMessage();

    EasyMock.verify(mockParticipantService);
    EasyMock.verify(mockQueryService);

    Assert.assertEquals(Gender.FEMALE, p.getGender());
  }

  public Participant newTestParticipant() {
    Participant p = new Participant();
    p.setFirstName("Marcel");
    p.setLastName("Tremblay");
    p.setBarcode("1234");
    p.setBirthDate(new Date());
    p.setGender(Gender.MALE);
    p.setEnrollmentId("10001010");
    p.setRecruitmentType(RecruitmentType.ENROLLED);
    return p;
  }
}
