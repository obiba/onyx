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

import java.io.InputStream;
import java.util.Locale;

import javax.print.PrintException;

import org.obiba.onyx.print.PdfPrintingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * IPrintableReport implementation for PDF reports.
 */
public abstract class PdfReport extends AbstractPrintableReport {

  @Autowired
  private PdfPrintingService printingService;

  public void print(Locale locale) {
    try {
      printingService.printPdf(getReport(locale));
    } catch(PrintException e) {
      throw new RuntimeException("An error was encountered while printing the following report: " + getName(), e);
    }
  }

  protected abstract InputStream getReport(Locale locale);

}
