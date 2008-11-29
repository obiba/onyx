/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.print;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

import org.apache.wicket.util.io.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A service bean able to print PDF files to a {@link PrintService}. This class uses the javax.print API for looking up
 * a {@code PrintService}. The service to use can be specified using the {@link #printerName} attribute; if it is empty
 * or null, the default {@code PrintService} will be looked-up.
 * <p>
 * If no {@code PrintService} is found, printing will not be available and calling any {@code printPdf} method will
 * throw a PrintException.
 * <p>
 * when a {@code PrintService} is found it is inspected to determine if it supports PDF printing (sending a PDF file
 * directly to the printer). If it doesn't then printing will not be available and calling any {@code printPdf} method
 * will throw a PrintException.
 */
public class PdfPrintingService implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(PdfPrintingService.class);

  /** The DocFlavors that we've implemented for printing PDFs */
  protected static final DocFlavor[] IMPLEMENTED_FLAVORS = { DocFlavor.BYTE_ARRAY.PDF, DocFlavor.INPUT_STREAM.PDF };

  protected String printerName;

  protected PrintService printService;

  /** The subset of flavors supported by the printService instance from the set of IMPLEMENTED_FLAVORS */
  protected Set<DocFlavor> supportedFlavors = new HashSet<DocFlavor>();

  public void afterPropertiesSet() throws Exception {
    // Try to find a PrintService instance with the specified name if any
    if(printerName != null && printerName.length() > 0) {
      log.info("Looking for a printer named {}", printerName);
      // Lookup all services
      PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
      for(PrintService ps : printServices) {
        if(ps.getName().equalsIgnoreCase(printerName)) {
          log.info("Using printer {}", ps.getName());
          this.printService = ps;
          break;
        }
      }
      if(printService == null) {
        log.warn("Could not find printer with name {}. Will try default printer.", printerName);
      }
    }

    // If the printService is null, we weren't configured with a printerName or we couldn't
    // find one with the specified name
    if(printService == null) {
      log.info("Using default printer.");
      printService = PrintServiceLookup.lookupDefaultPrintService();
    }

    // If the printService is null, there is no default printer installed.
    if(printService == null) {
      log.warn("No default printer found. Printing will not be available.");
    } else {
      // We have a PrintService instance. Extract the supported flavors out of our implemented flavors.
      for(DocFlavor flavor : IMPLEMENTED_FLAVORS) {
        if(printService.isDocFlavorSupported(flavor)) {
          supportedFlavors.add(flavor);
        }
      }

      if(supportedFlavors.size() > 0) {
        log.info("Printer {} supports PDF printing. Printing will be available.", printService.getName());
      } else {
        log.warn("Printer {} does not support printing PDF files directly. Printing will not be available.", printService.getName());
        printService = null;
      }
    }
  }

  public void setPrinterName(String printerName) {
    this.printerName = printerName;
  }

  public boolean supportsByteArray() {
    return supportedFlavors.contains(DocFlavor.BYTE_ARRAY.PDF);
  }

  public boolean supportsInputStream() {
    return supportedFlavors.contains(DocFlavor.INPUT_STREAM.PDF);
  }

  public void printPdf(byte[] pdf) throws PrintException {
    if(supportsByteArray()) {
      log.debug("Sending PDF to printer as byte array");
      print(pdf, DocFlavor.BYTE_ARRAY.PDF);
    } else {
      // Adapt the byte array if the printer supports input stream printing
      if(supportsInputStream()) {
        printPdf(new ByteArrayInputStream(pdf));
      } else {
        throw new PrintException("Printer does not support PDF printing");
      }
    }
  }

  public void printPdf(InputStream pdf) throws PrintException {
    if(supportsInputStream()) {
      log.debug("Sending PDF to printer as InputStream");
      print(pdf, DocFlavor.INPUT_STREAM.PDF);
    } else {
      // Adapt the input stream to a byte array if the printer supports byte array printing
      if(supportsByteArray()) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
          Streams.copy(pdf, baos);
        } catch(IOException e) {
          throw new PrintException(e);
        }
        printPdf(baos.toByteArray());
      } else {
        throw new PrintException("Printer does not support PDF printing");
      }
    }
  }

  protected void print(Object source, DocFlavor flavor) throws PrintException {
    DocPrintJob printJob = printService.createPrintJob();
    printJob.print(new SimpleDoc(source, flavor, null), null);
  }

}
