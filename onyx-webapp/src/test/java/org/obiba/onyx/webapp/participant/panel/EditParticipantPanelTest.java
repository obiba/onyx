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

import java.util.Date;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.Province;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.wicket.test.MockSpringApplication;

@SuppressWarnings("serial")
public class EditParticipantPanelTest {

  WicketTester tester;

  ParticipantService mockParticipantService;

  @Before
  public void setup() {

    ApplicationContextMock mockCtx = new ApplicationContextMock();

    mockParticipantService = createMock(ParticipantService.class);

    mockCtx.putBean("participantService", mockParticipantService);

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

    EasyMock.replay(mockParticipantService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        return new EditParticipantPanel(panelId, new Model(p), new ModalWindow("bogus"));
      }
    });
    tester.dumpPage();

    FormTester formTester = tester.newFormTester("panel:editParticipantForm");

    formTester.setValue("apartment", "400");

    // formTester.submit(); WICKET-1828: does not submit through AJAX
    // tester.clickLink("panel:editParticipantForm:saveAction", true); WICKET-1828: does not submit new values

    tester.executeAjaxEvent("panel:editParticipantForm:saveAction", "onclick");

    tester.assertNoErrorMessage();

    EasyMock.verify(mockParticipantService);

    Assert.assertEquals("400", p.getApartment());
  }

  public Participant newTestParticipant() {
    Participant p = new Participant();
    p.setFirstName("Marcel");
    p.setLastName("Tremblay");
    p.setBarcode("1234");
    p.setBirthDate(new Date());
    p.setCity("Montr�al");
    p.setProvince(Province.QC);
    p.setGender(Gender.MALE);
    return p;
  }
}
