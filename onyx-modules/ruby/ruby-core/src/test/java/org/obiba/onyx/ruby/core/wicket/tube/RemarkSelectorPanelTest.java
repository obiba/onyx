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

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class RemarkSelectorPanelTest {

  private WicketTester tester;

  private ActiveTubeRegistrationService activeTubeRegistrationServiceMock;

  @Before
  public void setup() {

    ExtendedApplicationContextMock mockCtx = new ExtendedApplicationContextMock();

    activeTubeRegistrationServiceMock = createMock(ActiveTubeRegistrationService.class);
    mockCtx.putBean("activeTubeRegistrationService", activeTubeRegistrationServiceMock);

    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("META-INF/messages");
    mockCtx.putBean("messageSource", messageSource);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);

    tester = new WicketTester(application);

    Session.get().setLocale(new Locale("en"));

  }

  @Test
  public void testUpdateTubeRemark() {

    final RegisteredParticipantTube registeredParticipantTube = new RegisteredParticipantTube();
    registeredParticipantTube.setBarcode("tubeBarcode001");

    activeTubeRegistrationServiceMock.setTubeRemark((String) EasyMock.anyObject(), (Remark) EasyMock.anyObject());

    EasyMock.replay(activeTubeRegistrationServiceMock);

    tester.startPanel(new TestPanelSource() {
      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return new RemarkSelectorPanelMock(panelId, new Model(registeredParticipantTube));
      }
    });

    FormTester formTester = tester.newFormTester("panel:form");
    formTester.select("content:remarkSelect", 1);

    tester.executeAjaxEvent("panel:form:content:remarkSelect", "onchange");
    tester.assertNoErrorMessage();

    EasyMock.verify(activeTubeRegistrationServiceMock);
  }

}
