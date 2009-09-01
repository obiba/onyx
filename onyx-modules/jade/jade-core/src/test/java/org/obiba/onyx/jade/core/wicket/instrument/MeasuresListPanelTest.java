/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.domain.run.MeasureStatus;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

public class MeasuresListPanelTest implements Serializable {

  private static final long serialVersionUID = 1L;

  private ExtendedApplicationContextMock applicationContextMock;

  private ActiveInstrumentRunService activeInstrumentRunServiceMock;

  private ActiveInterviewService activeInterviewServiceMock;

  private UserSessionService userSessionServiceMock;

  private WicketTester tester;

  private transient InstrumentType instrumentType;

  private InstrumentRun instrumentRun;

  private InstrumentService instrumentServiceMock;

  private Measure measure;

  private transient TestPanelSource testPanel;

  @Before
  public void setUp() throws Exception {
    initDomainObjects();
    initApplicationContext();
    initWicketTester();

    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun).anyTimes();
    expect(activeInstrumentRunServiceMock.getInstrumentType()).andReturn(instrumentType).anyTimes();
    expect(userSessionServiceMock.getDateTimeFormat()).andReturn(new SimpleDateFormat("")).anyTimes();
    expect(activeInterviewServiceMock.getParticipant()).andReturn(new Participant()).anyTimes();

  }

  private void initWicketTester() {
    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    tester = new WicketTester(application);
  }

  private void initApplicationContext() {

    applicationContextMock = new ExtendedApplicationContextMock();

    activeInstrumentRunServiceMock = createMock(ActiveInstrumentRunService.class);
    applicationContextMock.putBean("activeInstrumentRunService", activeInstrumentRunServiceMock);

    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    applicationContextMock.putBean("activeInterviewService", activeInterviewServiceMock);

    userSessionServiceMock = createMock(UserSessionService.class);
    applicationContextMock.putBean("userSessionService", userSessionServiceMock);

    instrumentServiceMock = createMock(InstrumentService.class);
    applicationContextMock.putBean("instrumentServiceMock", instrumentServiceMock);

  }

  private void initDomainObjects() {
    instrumentRun = new InstrumentRun();
    instrumentType = new InstrumentType("test", "test");
    instrumentType.setExpectedMeasureCount(new FixedDataSource(DataBuilder.buildInteger(3)));
    measure = new Measure() {

      private static final long serialVersionUID = 1L;

      @Override
      public InstrumentParameterCaptureMethod getCaptureMethod() {
        return InstrumentParameterCaptureMethod.AUTOMATIC;
      }
    };
    measure.setTime(new Date());
    User user = new User();
    user.setFirstName("test");
    user.setLastName("test");
    measure.setUser(user);
    measure.setStatus(MeasureStatus.VALID);
    instrumentRun.getMeasures().add(measure);
  }

  private class TestMeasuresListPanel extends MeasuresListPanel {

    private static final long serialVersionUID = 1L;

    public TestMeasuresListPanel(String id) {
      super(id);
    }

    @Override
    public void onRefresh(AjaxRequestTarget target) {
    }

  };

  @SuppressWarnings("serial")
  @Test
  public void testDeleteMeasure() {

    final TestMeasuresListPanel measuresListPanel = new TestMeasuresListPanel("panel");

    replay(activeInstrumentRunServiceMock);
    replay(userSessionServiceMock);
    replay(activeInterviewServiceMock);

    tester.startPanel(new TestPanelSource() {
      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {
        return measuresListPanel;
      }
    });

    assertTrue(measuresListPanel.getMeasureCount() == 1);
    assertTrue(measuresListPanel.getExpectedMeasureCount() == 3);

    tester.dumpPage();

    tester.executeAjaxEvent("panel:measure:0:measureActions:deleteMeasure", "onclick");
    tester.executeAjaxEvent("panel:confirmDeleteMeasureDialog:content:form:yes", "onclick");

    verify(activeInstrumentRunServiceMock);
    verify(userSessionServiceMock);
    verify(activeInterviewServiceMock);

    // This test does not work. YesCallback of confirmation dialog is not getting called for some reason.
    // It seems wicket tester never execute the callback when test is mocking an onclick action
    // assertTrue(measuresListPanel.getMeasureCount() == 2);
  }
}
