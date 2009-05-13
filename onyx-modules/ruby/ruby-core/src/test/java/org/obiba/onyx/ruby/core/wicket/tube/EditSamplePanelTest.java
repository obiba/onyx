/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.tube;

import static org.easymock.EasyMock.createMock;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.ruby.core.wicket.wizard.EditBarcodePanel;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

public class EditSamplePanelTest {

  private WicketTester tester;

  private ActiveTubeRegistrationService activeTubeRegistrationServiceMock;

  Remark inapLabeling;

  Remark hemolyzed;

  Remark clotted;

  Remark lipemic;

  @Before
  public void setup() {

    ExtendedApplicationContextMock mockCtx = new ExtendedApplicationContextMock();

    activeTubeRegistrationServiceMock = createMock(ActiveTubeRegistrationService.class);
    mockCtx.putBean("activeTubeRegistrationService", activeTubeRegistrationServiceMock);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);

    tester = new WicketTester(application);

    final RegisteredParticipantTube registeredParticipantTube = new RegisteredParticipantTube();
    registeredParticipantTube.setBarcode("tubeBarcode001");

    tester.startPanel(new TestPanelSource() {
      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {

        TubeRegistrationConfiguration tubeRegistrationConfiguration = new TubeRegistrationConfiguration();

        List<Remark> remarks = new ArrayList<Remark>();
        remarks.add(inapLabeling = new Remark("TubeRegistration.Remark.Inappropriate_labeling"));
        remarks.add(hemolyzed = new Remark("TubeRegistration.Remark.Hemolyzed_sampling"));
        remarks.add(clotted = new Remark("TubeRegistration.Remark.Clotted_sample"));
        remarks.add(lipemic = new Remark("TubeRegistration.Remark.Lipemic_sample"));

        tubeRegistrationConfiguration.setAvailableRemarks(remarks);
        tubeRegistrationConfiguration.setBarcodeStructure(new BarcodeStructure());

        return new EditBarcodePanel(panelId, new Model(registeredParticipantTube), tubeRegistrationConfiguration);

      }
    });

    tester.executeAjaxEvent("panel:link", "onclick");

  }

  @Test
  public void testSaveEditSample() {

    EditSamplePanel editSamplePanel = (EditSamplePanel) tester.getComponentFromLastRenderedPage("panel:editSampleDialog:content:form:content");
    editSamplePanel.get("feedback").replaceWith(new EmptyPanel("feedback"));

    activeTubeRegistrationServiceMock.setTubeComment("tubeBarcode001", "test comment");
    ArrayList<Remark> tubeRemarks = new ArrayList<Remark>();
    tubeRemarks.add(inapLabeling);
    tubeRemarks.add(clotted);
    activeTubeRegistrationServiceMock.setTubeRemark("tubeBarcode001", tubeRemarks);

    EasyMock.replay(activeTubeRegistrationServiceMock);

    FormTester formTester = tester.newFormTester("panel:editSampleDialog:content:form");
    formTester.setValue("content:comment:comment", "test comment");
    formTester.selectMultiple("content:remark:remarkSelect", new int[] { 0, 2 });

    tester.executeAjaxEvent("panel:editSampleDialog:content:form:ok", "onclick");
    tester.assertNoErrorMessage();

    EasyMock.verify(activeTubeRegistrationServiceMock);

  }

  @Test
  public void testCancelEditSample() {
    EasyMock.replay(activeTubeRegistrationServiceMock);

    FormTester formTester = tester.newFormTester("panel:editSampleDialog:content:form");
    formTester.setValue("content:comment:comment", "test comment");
    formTester.selectMultiple("content:remark:remarkSelect", new int[] { 0, 2 });

    tester.executeAjaxEvent("panel:editSampleDialog:content:form:cancel", "onclick");

    EasyMock.verify(activeTubeRegistrationServiceMock);

  }

  @Test
  public void testSaveEditSampleInvalidComment() {

    StringBuffer comment = new StringBuffer();
    for(int i = 0; i < 3000; i++) {
      comment.append("c");
    }

    EasyMock.replay(activeTubeRegistrationServiceMock);

    FormTester formTester = tester.newFormTester("panel:editSampleDialog:content:form");
    formTester.setValue("content:comment:comment", comment.toString());

    // TODO This test cannot be completed because of the feedback window. The feedback window includes a Form which
    // is itself located inside a Dialog (which also includes a form). This confuses the WicketTester so we had to
    // remove the feedback window for previous tests. In this specific test the feedback window is needed, so we
    // need a better solution...

    // tester.executeAjaxEvent("panel:editSampleDialog:content:form:ok", "onclick");
    // tester.assertErrorMessages(new String[] { "StringValidator.maximum" });

    EasyMock.verify(activeTubeRegistrationServiceMock);

  }

}
