/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.reusable;

import static org.easymock.EasyMock.createMock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.print.IPrintableReport;
import org.obiba.onyx.print.PrintableReportsRegistry;
import org.obiba.onyx.print.impl.DefaultPrintableReportsRegistryTest;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

public class PrintableReportPanelTest {

  ExtendedApplicationContextMock mockApplicationContext;

  MockSpringApplication application;

  private PrintableReportsRegistry mockPrintableReportsRegistery;

  @Before
  public void setUp() throws Exception {
    mockPrintableReportsRegistery = createMock(PrintableReportsRegistry.class);
    mockApplicationContext = new ExtendedApplicationContextMock();
    mockApplicationContext.putBean("printableReportsRegistry", mockPrintableReportsRegistery);
    application = new MockSpringApplication();
    application.setApplicationContext(mockApplicationContext);
  }

  @Test
  public void printableReportPanelLabelTest() {

    boolean notLocalisble = false;
    boolean ready = true;
    IPrintableReport report = DefaultPrintableReportsRegistryTest.getPrintableReport(Collections.<Locale> emptySet(), "ConsentReport", notLocalisble, ready);

    // Set Spring asset returned by applicationContext.getMessage(). This is the localized name of the Report.
    mockApplicationContext.setMessage("Signed Consent Report");

    Set<IPrintableReport> reports = new HashSet<IPrintableReport>(1);
    reports.add(report);

    EasyMock.expect(mockPrintableReportsRegistery.availableReports()).andReturn(reports).times(1);
    EasyMock.expect(mockPrintableReportsRegistery.getReportByName("ConsentReport")).andReturn(report).times(1);
    EasyMock.replay(mockPrintableReportsRegistery);

    WicketTester tester = new WicketTester(application);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return new PrintableReportPanel(panelId);
      }
    });

    // tester.dumpPage();

    EasyMock.verify(mockPrintableReportsRegistery);

    tester.assertLabel("panel:group:table:0:rows:reportRow:checkboxLabel:name", "Signed Consent Report");
    // tester.assertLabel("panel:group:table:0:rows:reportRow:status", "Ready");
  }
}
