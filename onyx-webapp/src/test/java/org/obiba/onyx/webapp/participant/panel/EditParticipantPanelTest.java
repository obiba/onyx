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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.Page;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
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
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.identifier.NullIdentifierSequenceProvider;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SuppressWarnings("serial")
public class EditParticipantPanelTest implements Serializable {

  private transient WicketTester tester;

  private ParticipantService mockParticipantService;

  private EntityQueryService mockQueryService;

  private UserSessionService mockUserSessionService;

  private ParticipantMetadata participantMetadata;

  private Participant p = newTestParticipant();

  @Before
  public void setup() {
    ExtendedApplicationContextMock mockCtx = new ExtendedApplicationContextMock();

    mockParticipantService = createMock(ParticipantService.class);
    mockQueryService = createMock(EntityQueryService.class);
    mockUserSessionService = createMock(UserSessionService.class);

    mockCtx.putBean("participantService", mockParticipantService);
    mockCtx.putBean("entityQueryService", mockQueryService);
    mockCtx.putBean("userSessionService", mockUserSessionService);
    mockCtx.putBean("identifierSequenceProvider", new NullIdentifierSequenceProvider());

    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("test-spring-context.xml");
    participantMetadata = (ParticipantMetadata) context.getBean("participantMetadata");
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
    expect(mockUserSessionService.getDateFormat()).andReturn(new SimpleDateFormat("yyyy-MM-dd")).anyTimes();
    expect(mockUserSessionService.getUser()).andReturn(new User()).anyTimes();
    mockParticipantService.updateParticipant(p);
    expect(mockQueryService.matchOne((ApplicationConfiguration) EasyMock.anyObject())).andReturn(new ApplicationConfiguration());

    EasyMock.replay(mockUserSessionService);
    EasyMock.replay(mockParticipantService);
    EasyMock.replay(mockQueryService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        DummyHomePage dummyHomePage = new DummyHomePage();
        EditParticipantFormPanel panel = new EditParticipantFormPanel(panelId, new Model(p), dummyHomePage);

        // We have to remove the feedback dialog because it is confusing the WicketTester when we try to submit the form
        // in the EditParticipantFormPanel. The feedback dialog contains a useless form (all Dialogs include a form).
        panel.get("editParticipantForm:editParticipantPanel:feedback").replaceWith(new EmptyPanel("feedback"));
        return panel;
      }
    });

    FormTester formTester = tester.newFormTester("panel:editParticipantForm");
    formTester.select("editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:1:field:input:select", 3);
    formTester.setValue("editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:2:field:input:field", "Peel street");

    // test EditParticipantPanel in EDIT mode => no editable field (except gender and birthdate)
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:firstName:value", Label.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:lastName:value", Label.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:gender:gender", DropDownChoice.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:birthDate:value", DateTextField.class);
    tester.assertInvisible("panel:editParticipantForm:editParticipantPanel:assignCodeToParticipantPanel");

    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:1:field:input:select", DropDownChoice.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:2:field:input:field", TextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:3:field:input:field", TextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:4:field", Label.class);

    tester.executeAjaxEvent("panel:editParticipantForm:saveAction", "onclick");
    tester.assertNoErrorMessage();

    EasyMock.verify(mockUserSessionService);
    EasyMock.verify(mockParticipantService);
    EasyMock.verify(mockQueryService);

    // TODO Make this test work: implement the sort option on the allowed values set
    // Assert.assertEquals("NB", p.getConfiguredAttributeValue("Province").getValueAsString());
    Assert.assertEquals("Peel street", p.getConfiguredAttributeValue("Street").getValueAsString());
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
    mockParticipantService.assignCodeToParticipant(p, "B12345678", null, null);

    EasyMock.replay(mockParticipantService);
    EasyMock.replay(mockQueryService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        DummyHomePage dummyHomePage = new DummyHomePage();
        EditParticipantFormPanel panel = new EditParticipantFormPanel(panelId, new Model(p), dummyHomePage);
        panel.get("editParticipantForm:editParticipantPanel:assignCodeToParticipantPanel").replaceWith(new AssignCodeToParticipantPanelMock("assignCodeToParticipantPanel", new Model(p), new Model(participantMetadata)));

        // We have to remove the feedback dialog because it is confusing the WicketTester when we try to submit the form
        // in the EditParticipantFormPanel. The feedback dialog contains a useless form (all Dialogs include a form).
        panel.get("editParticipantForm:editParticipantPanel:feedback").replaceWith(new EmptyPanel("feedback"));
        return panel;
      }
    });

    // test EditParticipantPanel RECEPTION mode => enrollementId and assignCodeToParticipantPanel are shown, all fields
    // are editable

    FormTester formTester = tester.newFormTester("panel:editParticipantForm");
    formTester.setValue("editParticipantPanel:firstName:value", "Martine");
    formTester.select("editParticipantPanel:gender:gender", 0);
    formTester.setValue("editParticipantPanel:assignCodeToParticipantPanel:assignCodeToParticipantForm:participantCode", "B12345678");
    formTester.setValue("editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:2:field:input:field", "Peel street");
    formTester.setValue("editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:4:field:input:field", "514-398-3311 ext 00721");

    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:enrollmentId:value", Label.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:firstName:value", TextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:lastName:value", TextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:gender:gender", DropDownChoice.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:birthDate:value", DateTextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:assignCodeToParticipantPanel", AssignCodeToParticipantPanel.class);

    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:1:field:input:select", DropDownChoice.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:2:field:input:field", TextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:3:field", Label.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:4:field:input:field", TextField.class);

    tester.executeAjaxEvent("panel:editParticipantForm:saveAction", "onclick");
    tester.assertNoErrorMessage();

    EasyMock.verify(mockParticipantService);
    EasyMock.verify(mockQueryService);

    Assert.assertEquals("Martine", p.getFirstName());
    Assert.assertEquals(Gender.FEMALE, p.getGender());
    Assert.assertEquals("Peel street", p.getConfiguredAttributeValue("Street").getValueAsString());
    Assert.assertEquals("514-398-3311 ext 00721", p.getConfiguredAttributeValue("Phone").getValueAsString());
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
    mockParticipantService.assignCodeToParticipant(p, "B12345678", null, null);

    EasyMock.replay(mockParticipantService);
    EasyMock.replay(mockQueryService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        DummyHomePage dummyHomePage = new DummyHomePage();
        EditParticipantFormPanel panel = new EditParticipantFormPanel(panelId, new Model(p), dummyHomePage);
        panel.get("editParticipantForm:editParticipantPanel:assignCodeToParticipantPanel").replaceWith(new AssignCodeToParticipantPanelMock("assignCodeToParticipantPanel", new Model(p), new Model(participantMetadata)));

        // We have to remove the feedback dialog because it is confusing the WicketTester when we try to submit the form
        // in the EditParticipantFormPanel. The feedback dialog contains a useless form (all Dialogs include a form).
        panel.get("editParticipantForm:editParticipantPanel:feedback").replaceWith(new EmptyPanel("feedback"));
        return panel;
      }
    });

    // test EditParticipantPanel in ENROLLMENT mode => enrollementId not shown, all fields editable
    // assignCodeToParticipantPanel should be shown

    tester.dumpPage();
    FormTester formTester = tester.newFormTester("panel:editParticipantForm");
    formTester.setValue("editParticipantPanel:firstName:value", "Martin");
    formTester.setValue("editParticipantPanel:lastName:value", "Dupont");
    formTester.select("editParticipantPanel:gender:gender", 1);
    formTester.setValue("editParticipantPanel:birthDate:value", "1979-05-05");
    formTester.setValue("editParticipantPanel:assignCodeToParticipantPanel:assignCodeToParticipantForm:participantCode", "B12345678");
    formTester.setValue("editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:2:field:input:field", "Peel street");
    formTester.setValue("editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:4:field:input:field", "514-398-3311 ext 00721");

    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:firstName:value", TextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:lastName:value", TextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:gender:gender", DropDownChoice.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:birthDate:value", DateTextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:assignCodeToParticipantPanel", AssignCodeToParticipantPanel.class);
    tester.assertVisible("panel:editParticipantForm:editParticipantPanel:assignCodeToParticipantPanel");

    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:1:field:input:select", DropDownChoice.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:2:field:input:field", TextField.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:3:field", Label.class);
    tester.assertComponent("panel:editParticipantForm:editParticipantPanel:configuredAttributeGroups:groupRepeater:1:group:attributeRepeater:4:field:input:field", TextField.class);

    Assert.assertNull(formTester.getForm().get("enrollmentId:value"));

    tester.executeAjaxEvent("panel:editParticipantForm:saveAction", "onclick");
    tester.assertNoErrorMessage();

    EasyMock.verify(mockParticipantService);
    EasyMock.verify(mockQueryService);

    Assert.assertEquals("Martin", p.getFirstName());
    Assert.assertEquals(Gender.MALE, p.getGender());
    Assert.assertEquals("Peel street", p.getConfiguredAttributeValue("Street").getValueAsString());
    Assert.assertEquals("514-398-3311 ext 00721", p.getConfiguredAttributeValue("Phone").getValueAsString());
  }

  public Participant newTestParticipant() {
    Participant p = new Participant();
    p.setFirstName("Marcel");
    p.setLastName("Tremblay");
    p.setBirthDate(new Date());
    p.setGender(Gender.MALE);
    return p;
  }

  private static class AssignCodeToParticipantPanelMock extends AssignCodeToParticipantPanel {

    public AssignCodeToParticipantPanelMock(String id, IModel participantModel, IModel metadataModel) {
      super(id);
      add(new AssignCodeToParticipantFormMock("assignCodeToParticipantForm", participantModel, metadataModel));
    }

    private class AssignCodeToParticipantFormMock extends AssignCodeToParticipantForm {

      @SuppressWarnings("serial")
      public AssignCodeToParticipantFormMock(String id, final IModel participantModel, IModel metadataModel) {
        super(id, participantModel, (ParticipantMetadata) metadataModel.getObject());
      }

      @Override
      public void onSubmit(Participant participant) {
        // nothing
      }
    }
  }
}
