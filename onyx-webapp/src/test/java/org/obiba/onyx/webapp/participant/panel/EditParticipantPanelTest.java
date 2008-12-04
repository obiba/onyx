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

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.Page;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.wicket.test.MockSpringApplication;

@SuppressWarnings("serial")
public class EditParticipantPanelTest implements Serializable {

  private transient WicketTester tester;

  private ParticipantService mockParticipantService;

  private EntityQueryService mockQueryService;

  private ParticipantMetadata participantMetadata;

  private Participant p = newTestParticipant();

  @Before
  public void setup() {

    ApplicationContextMock mockCtx = new ApplicationContextMock();

    mockParticipantService = createMock(ParticipantService.class);
    mockQueryService = createMock(EntityQueryService.class);

    mockCtx.putBean("participantService", mockParticipantService);
    mockCtx.putBean("entityQueryService", mockQueryService);

    participantMetadata = new ParticipantMetadata();
    mockCtx.putBean("participantMetadata", participantMetadata);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);
    application.setHomePage(Page.class);

    tester = new WicketTester(application);
  }

  @SuppressWarnings("serial")
  @Test
  public void testEditParticipant() {
    p.setEnrollmentId("10001010");
    p.setBarcode("1234");
    p.setRecruitmentType(RecruitmentType.ENROLLED);

    // We expect the updateParticipant method to be called once
    mockParticipantService.updateParticipant(p);
    expect(mockQueryService.matchOne((ApplicationConfiguration) EasyMock.anyObject())).andReturn(new ApplicationConfiguration());

    EasyMock.replay(mockParticipantService);
    EasyMock.replay(mockQueryService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        DummyHomePage dummyHomePage = new DummyHomePage();
        return new EditParticipantPanel(panelId, new Model(p), dummyHomePage, "edit", new ModalWindow("windowMock"));
      }
    });

    tester.executeAjaxEvent("panel:editParticipantForm:saveAction", "onclick");
    tester.assertNoErrorMessage();

    // test EditParticipantPanel en mode EDIT => aucun champ n'est éditable
    tester.assertComponent("panel:editParticipantForm:firstName:value", Label.class);
    tester.assertComponent("panel:editParticipantForm:lastName:value", Label.class);
    tester.assertComponent("panel:editParticipantForm:gender:value", Label.class);
    tester.assertComponent("panel:editParticipantForm:birthDate:value", Label.class);
    tester.assertComponent("panel:editParticipantForm:assignCodeToParticipantPanel", EmptyPanel.class);

    EasyMock.verify(mockParticipantService);
    EasyMock.verify(mockQueryService);
  }

  @SuppressWarnings("serial")
  @Test
  public void testReceiveParticipant() {
    p.setBarcode(null);
    p.setRecruitmentType(RecruitmentType.ENROLLED);

    // We expect the updateParticipant method to be called once
    mockParticipantService.updateParticipant(p);
    expect(mockQueryService.matchOne((ApplicationConfiguration) EasyMock.anyObject())).andReturn(new ApplicationConfiguration());
    expect(mockQueryService.count((Participant) EasyMock.anyObject())).andReturn(0);

    EasyMock.replay(mockParticipantService);
    EasyMock.replay(mockQueryService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        DummyHomePage dummyHomePage = new DummyHomePage();
        EditParticipantPanel panel = new EditParticipantPanel(panelId, new Model(p), dummyHomePage, "reception");
        panel.get("editParticipantForm:assignCodeToParticipantPanel").replaceWith(new AssignCodeToParticipantPanelMock("assignCodeToParticipantPanel", new Model(p)));
        return panel;
      }
    });

    // test EditParticipantPanel en mode RECEPTION => enrollementId et assignCodeToParticipantPanel apparaissent, tous
    // champs éditables

    FormTester formTester = tester.newFormTester("panel:editParticipantForm");
    formTester.setValue("firstName:value", "Martine");
    formTester.select("gender:gender", 0);
    formTester.setValue("assignCodeToParticipantPanel:assignCodeToParticipantForm:participantCode", "1234");

    tester.assertComponent("panel:editParticipantForm:enrollmentId:value", Label.class);
    tester.assertComponent("panel:editParticipantForm:firstName:value", TextField.class);
    tester.assertComponent("panel:editParticipantForm:lastName:value", TextField.class);
    tester.assertComponent("panel:editParticipantForm:gender:gender", DropDownChoice.class);
    tester.assertComponent("panel:editParticipantForm:birthDate:value", DateTextField.class);
    tester.assertComponent("panel:editParticipantForm:assignCodeToParticipantPanel", AssignCodeToParticipantPanel.class);

    tester.executeAjaxEvent("panel:editParticipantForm:saveAction", "onclick");
    tester.assertNoErrorMessage();

    EasyMock.verify(mockParticipantService);
    EasyMock.verify(mockQueryService);

    Assert.assertEquals("Martine", p.getFirstName());
    Assert.assertEquals(Gender.FEMALE, p.getGender());
  }

  @SuppressWarnings("serial")
  @Test
  public void testEnrollVolunteerParticipant() {
    p.setEnrollmentId(null);
    p.setBarcode(null);
    p.setRecruitmentType(RecruitmentType.VOLUNTEER);

    // We expect the updateParticipant method to be called once
    mockParticipantService.updateParticipant(p);
    expect(mockQueryService.matchOne((ApplicationConfiguration) EasyMock.anyObject())).andReturn(new ApplicationConfiguration());
    expect(mockQueryService.count((Participant) EasyMock.anyObject())).andReturn(0);

    EasyMock.replay(mockParticipantService);
    EasyMock.replay(mockQueryService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        DummyHomePage dummyHomePage = new DummyHomePage();
        EditParticipantPanel panel = new EditParticipantPanel(panelId, new Model(p), dummyHomePage, "enrollment");
        panel.get("editParticipantForm:assignCodeToParticipantPanel").replaceWith(new AssignCodeToParticipantPanelMock("assignCodeToParticipantPanel", new Model(p)));
        return panel;
      }
    });

    // test EditParticipantPanel en mode ENROLLMENT => enrollementId n'apparaît pas, tous champs éditables,
    // assignCodeToParticipantPanel apparaît

    FormTester formTester = tester.newFormTester("panel:editParticipantForm");
    formTester.setValue("firstName:value", "Martin");
    formTester.setValue("lastName:value", "Dupont");
    formTester.select("gender:gender", 1);
    formTester.setValue("birthDate", "05-05-1979");
    formTester.setValue("assignCodeToParticipantPanel:assignCodeToParticipantForm:participantCode", "1234");

    tester.assertComponent("panel:editParticipantForm:firstName:value", TextField.class);
    tester.assertComponent("panel:editParticipantForm:lastName:value", TextField.class);
    tester.assertComponent("panel:editParticipantForm:gender:gender", DropDownChoice.class);
    tester.assertComponent("panel:editParticipantForm:birthDate:value", DateTextField.class);
    tester.assertComponent("panel:editParticipantForm:assignCodeToParticipantPanel", AssignCodeToParticipantPanel.class);

    Assert.assertNull(formTester.getForm().get("enrollmentId:value"));

    tester.executeAjaxEvent("panel:editParticipantForm:saveAction", "onclick");
    tester.assertNoErrorMessage();

    EasyMock.verify(mockParticipantService);
    EasyMock.verify(mockQueryService);

    Assert.assertEquals("Martin", p.getFirstName());
    Assert.assertEquals(Gender.MALE, p.getGender());
  }

  public Participant newTestParticipant() {
    Participant p = new Participant();
    p.setFirstName("Marcel");
    p.setLastName("Tremblay");
    p.setBirthDate(new Date());
    p.setGender(Gender.MALE);
    return p;
  }

  private class AssignCodeToParticipantPanelMock extends AssignCodeToParticipantPanel {

    public AssignCodeToParticipantPanelMock(String id, IModel participantModel) {
      super(id);
      add(new AssignCodeToParticipantFormMock("assignCodeToParticipantForm", participantModel));
    }

    private class AssignCodeToParticipantFormMock extends AssignCodeToParticipantForm {

      @SuppressWarnings("serial")
      public AssignCodeToParticipantFormMock(String id, final IModel participantModel) {
        super(id, participantModel);
      }

      @Override
      public void onSubmit(Participant participant) {
        // nothing
      }
    }
  }
}
