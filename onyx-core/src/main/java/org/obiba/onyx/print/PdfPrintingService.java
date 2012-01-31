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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

  /**
   * The implementations of PdfHandler. Each instance can handle printing a PDF file onto a specific DocFlavor. They are
   * ordered is a way that if the PrintService handles PDF directly, the PDF will not be converted by this bean.
   */
  // PDF handlers were removed to force conversion to PS. See ONYX-1619.
  protected final PdfHandler[] IMPLEMENTED_FLAVORS = {/* new PdfByteArrayPdfHandler(), new PdfInputStreamPdfHandler(), */new PostscriptByteArrayPdfHandler(), new ApplicationByteArrayPdfHandler() };

  protected String printerName;

  protected PrintService printService;

  protected PdfHandler supportedHandler;

  public void afterPropertiesSet() throws Exception {
    // Try to find a PrintService instance with the specified name if any
    if(printerName != null && printerName.length() > 0) {
      log.info("Looking for a printer named '{}'", printerName);
      // Lookup all services
      PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
      for(PrintService ps : printServices) {
        if(ps != null && printerName.equalsIgnoreCase(ps.getName())) {
          log.info("Using printer '{}'", ps.getName());
          this.printService = ps;
          break;
        }
      }
      if(printService == null) {
        log.warn("Could not find printer with name '{}'. Will try default printer.", printerName);
      }
    }

    // If the printService is null, we weren't configured with a printerName or we couldn't
    // find one with the specified name
    if(printService == null) {
      printService = PrintServiceLookup.lookupDefaultPrintService();
      if(printService != null) {
        log.info("Using default printer '{}'.", printService.getName());
      }
    }

    // If the printService is null, there is no default printer installed.
    if(printService == null) {
      log.warn("No default printer found. Printing will not be available.");
    } else {
      // We have a PrintService instance. Find the first handler that this service accepts.
      for(PdfHandler handler : IMPLEMENTED_FLAVORS) {
        if(printService.isDocFlavorSupported(handler.getImplementedFlavor())) {
          supportedHandler = handler;
          break;
        }
      }

      if(supportedHandler != null) {
        log.info("Printer '{}' supports PDF printing through handler {}. PDF printing will be available.", printService.getName(), supportedHandler.getClass().getSimpleName());
      } else {
        log.warn("Printer '{}' does not support printing PDF files directly. PDF printing will not be available.", printService.getName());
        printService = null;
      }
    }
  }

  public void setPrinterName(String printerName) {
    this.printerName = printerName;
  }

  public boolean supportsPdfPrinting() {
    return supportedHandler != null;
  }

  public void printPdf(byte[] pdf) throws PrintException {
    printPdf(new ByteArrayInputStream(pdf));
  }

  public void printPdf(InputStream pdf) throws PrintException {
    supportedHandler.printPdf(pdf);
  }

  protected void print(Object source, DocFlavor flavor) throws PrintException {
    log.info("Starting print job");
    DocPrintJob printJob = printService.createPrintJob();
    printJob.print(new SimpleDoc(source, flavor, null), null);
    log.info("Print job finished");
  }

  public static void main(String[] args) throws Exception {
    PdfPrintingService pps = new PdfPrintingService();
    pps.setPrinterName(args[0]);
    pps.afterPropertiesSet();
    pps.printPdf(new FileInputStream(args[1]));
  }

  private interface PdfHandler {
    public DocFlavor getImplementedFlavor();

    public void printPdf(InputStream pdf) throws PrintException;
  }

  private class PdfByteArrayPdfHandler implements PdfHandler {

    public DocFlavor getImplementedFlavor() {
      return DocFlavor.BYTE_ARRAY.PDF;
    }

    public void printPdf(InputStream pdf) throws PrintException {
      // Adapt the input stream to a byte array
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        Streams.copy(pdf, baos);
      } catch(IOException e) {
        throw new PrintException(e);
      }
      print(baos.toByteArray(), getImplementedFlavor());
    }

  }

  private class PdfInputStreamPdfHandler implements PdfHandler {

    public DocFlavor getImplementedFlavor() {
      return DocFlavor.INPUT_STREAM.PDF;
    }

    public void printPdf(InputStream pdf) throws PrintException {
      print(pdf, getImplementedFlavor());
    }

  }

  private abstract class CustomByteArrayPdfHandler implements PdfHandler {

    public void printPdf(InputStream pdf) throws PrintException {
      // Convert the pdf to postscript
      PdfToPs pdfToPs = PdfToPs.get();
      if(pdfToPs == null) {
        throw new PrintException("Cannot convert PDF to " + getImplementedFlavor().getMimeType() + ". PDF cannot be printed.");
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        if(pdfToPs.convert(pdf, baos) == false) {
          throw new PrintException("Error converting PDF to " + getImplementedFlavor().getMimeType() + " for printing.");
        }
        baos.close();
      } catch(IOException e) {
        throw new PrintException("Error converting PDF to " + getImplementedFlavor().getMimeType() + " for printing.", e);
      }
      print(baos.toByteArray(), getImplementedFlavor());
    }

  }

  /**
   * Converts the PDF stream to a Postscript stream using {@link PdfToPs} then sends the resulting postscript to the
   * printer. This handler is used for printers and print servers that don't handle PDF files directly.
   */
  private class PostscriptByteArrayPdfHandler extends CustomByteArrayPdfHandler {
    public DocFlavor getImplementedFlavor() {
      return DocFlavor.BYTE_ARRAY.POSTSCRIPT;
    }
  }

  /**
   * This implementation was added to support a special application stream (Available on Windows) for printers that
   * don't support Postscript.
   */
  private class ApplicationByteArrayPdfHandler extends CustomByteArrayPdfHandler {
    public DocFlavor getImplementedFlavor() {
      return DocFlavor.BYTE_ARRAY.AUTOSENSE;
    }
  }
}
