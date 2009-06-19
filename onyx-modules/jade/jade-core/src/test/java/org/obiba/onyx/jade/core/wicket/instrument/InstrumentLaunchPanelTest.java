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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

/**
 * Unit tests for {@link InstrumentLaunchPanel}.
 */
public class InstrumentLaunchPanelTest {
  //
  // Instance Variables
  //

  private ExtendedApplicationContextMock applicationContextMock;

  private ActiveInstrumentRunService activeInstrumentRunServiceMock;

  private InstrumentService instrumentServiceMock;

  private TestInstrumentType instrumentType;

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
  public void testPresenceOfManualButtonWhenManualCaptureAllowed() {
    instrumentType.setManualCaptureAllowed(true);

    expect(activeInstrumentRunServiceMock.getInstrumentType()).andReturn(instrumentType).anyTimes();
    expect(activeInstrumentRunServiceMock.updateReadOnlyInputParameterRunValue()).andReturn(null).anyTimes();

    replay(activeInstrumentRunServiceMock);

    InstrumentLaunchPanel instrumentLaunchPanel = createInstrumentLaunchPanel();

    verify(activeInstrumentRunServiceMock);

    // Verify visibility of the "enter values manually" button (this implies the visibility of its parent).
    AjaxLink manualButton = (AjaxLink) instrumentLaunchPanel.get("manualButtonBlock:manualButton");
    assertNotNull(manualButton);
    MarkupContainer parent = manualButton.getParent();
    assertNotNull(parent);
    assertTrue(manualButton.isVisible());
    assertTrue(parent.isVisible());
  }

  @Test
  public void testAbsenceOfManualButtonWhenManualCaptureNotAllowed() {
    instrumentType.setManualCaptureAllowed(false);

    expect(activeInstrumentRunServiceMock.getInstrumentType()).andReturn(instrumentType).anyTimes();
    expect(activeInstrumentRunServiceMock.updateReadOnlyInputParameterRunValue()).andReturn(null).anyTimes();

    replay(activeInstrumentRunServiceMock);

    InstrumentLaunchPanel instrumentLaunchPanel = createInstrumentLaunchPanel();

    verify(activeInstrumentRunServiceMock);

    // Verify invisibility of the "enter values manually" button. At least one of the following
    // must be true: Either the button is itself invisible or its parent is (effectively making
    // the button invisible).
    AjaxLink manualButton = (AjaxLink) instrumentLaunchPanel.get("manualButtonBlock:manualButton");
    assertNotNull(manualButton);
    MarkupContainer parent = manualButton.getParent();
    assertNotNull(parent);
    assertTrue(!manualButton.isVisible() || !parent.isVisible());
  }

  @Test
  public void testPresenceOfManualEntryDialog() {
    instrumentType.setManualCaptureAllowed(false);

    expect(activeInstrumentRunServiceMock.getInstrumentType()).andReturn(instrumentType).anyTimes();
    expect(activeInstrumentRunServiceMock.updateReadOnlyInputParameterRunValue()).andReturn(null).anyTimes();

    replay(activeInstrumentRunServiceMock);

    InstrumentLaunchPanel instrumentLaunchPanel = createInstrumentLaunchPanel();

    verify(activeInstrumentRunServiceMock);

    // Verify presence of manual entry dialog.
    Dialog manualEntryDialog = (Dialog) instrumentLaunchPanel.get("manualEntryDialog");
    assertNotNull(manualEntryDialog);
  }

  //
  // Methods
  //

  private void initWicketTester() {
    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    new WicketTester(application);
  }

  private void initApplicationContext() {
    applicationContextMock = new ExtendedApplicationContextMock();

    activeInstrumentRunServiceMock = createMock(ActiveInstrumentRunService.class);
    applicationContextMock.putBean("activeInstrumentRunService", activeInstrumentRunServiceMock);

    instrumentServiceMock = createMock(InstrumentService.class);
    applicationContextMock.putBean("instrumentService", instrumentServiceMock);
  }

  private void initDomainObjects() {
    instrumentType = new TestInstrumentType();
    instrumentType.setName("testInstrumentType");
  }

  private InstrumentLaunchPanel createInstrumentLaunchPanel() {
    InstrumentLaunchPanel instrumentLaunchPanel = new InstrumentLaunchPanel("panel") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onInstrumentLaunch() {
        // do nothing
      }
    };

    return instrumentLaunchPanel;
  }

  //
  // Inner Classes
  //

  private static class TestInstrumentType extends InstrumentType {

    private boolean manualCaptureAllowed;

    public void setManualCaptureAllowed(boolean manualCaptureAllowed) {
      this.manualCaptureAllowed = manualCaptureAllowed;
    }

    public boolean isManualCaptureAllowed() {
      return manualCaptureAllowed;
    }
  }
}