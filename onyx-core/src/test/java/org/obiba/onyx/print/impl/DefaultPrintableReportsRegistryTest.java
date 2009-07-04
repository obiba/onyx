/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.print.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.print.IPrintableReport;
import org.obiba.onyx.print.PrintableReportsRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSourceResolvable;

public class DefaultPrintableReportsRegistryTest {

  private PrintableReportsRegistry printableReportsRegistry;

  private boolean ready = true;

  private boolean localisable = true;

  private boolean notLocalisble = false;

  @Before
  public void setUp() throws Exception {
    printableReportsRegistry = new DefaultPrintableReportsRegistry();
    ApplicationContextMock mockCtx = new ApplicationContextMock();
    mockCtx.putBean("consentReport", getPrintableReport(Collections.<Locale> emptySet(), "ConsentReport", notLocalisble, ready));
    Set<Locale> locales = new HashSet<Locale>();
    locales.add(Locale.CANADA_FRENCH);
    mockCtx.putBean("participantReport", getPrintableReport(locales, "ParticipantReport", localisable, ready));
    printableReportsRegistry.setApplicationContext(mockCtx);
  }

  @Test
  public void availableReportsTest() {
    Set<IPrintableReport> reports = printableReportsRegistry.availableReports();
    for(IPrintableReport report : reports) {
      if(report.getName().equals("ConsentReport")) {
        Assert.assertEquals(ready, report.isReady());
        Assert.assertEquals(notLocalisble, report.isLocalisable());
        Assert.assertEquals(0, report.availableLocales().size());
        return;
      }
    }
    Assert.assertFalse("Expected to find the 'ConsentReport'", true);
  }

  @Test
  public void getReportByNameTest() {
    IPrintableReport report = printableReportsRegistry.getReportByName("ParticipantReport");
    Assert.assertNotNull(report);
    Assert.assertEquals(ready, report.isReady());
    Assert.assertEquals(localisable, report.isLocalisable());
    Assert.assertEquals(1, report.availableLocales().size());
  }

  public static IPrintableReport getPrintableReport(final Set<Locale> locales, final String name, final boolean localisable, final boolean ready) {
    return new IPrintableReport() {

      public Set<Locale> availableLocales() {
        return locales;
      }

      public MessageSourceResolvable getLabel() {
        return null;
      }

      public String getName() {
        return name;
      }

      public boolean isLocalisable() {
        return localisable;
      }

      public boolean isReady() {
        return ready;
      }

      public void print(Locale locale) {
      }

      public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      }

    };
  }
}
