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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.wicket.util.io.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a PDF file into a Postscript file using XPDF (http://www.foolabs.com/xpdf/).
 * <p>
 * Currently, only Windows is supported. The reason being that the CUPS (Linux and MacOS X) supports printing PDFs
 * directly, so there is no need to convert them to postscript first.
 */
public abstract class PdfToPs {
  private static final Logger log = LoggerFactory.getLogger(PdfToPs.class);

  private PdfToPs() {
  }

  public boolean convert(InputStream pdf, OutputStream ps) throws IOException {
    boolean exitValue = false;
    File pdfFile = File.createTempFile("pdftops", "pdf");
    File psFile = File.createTempFile("pdftops", "ps");
    try {
      FileOutputStream fos = new FileOutputStream(pdfFile);
      Streams.copy(pdf, fos);
      fos.close();
      exitValue = convert(pdfFile, psFile);
      Streams.copy(new FileInputStream(psFile), ps);
    } finally {
      if(pdfFile != null) pdfFile.delete();
      if(psFile != null) psFile.delete();
    }
    return exitValue;
  }

  /**
   * Converts a PDF file into a Postscript file. Returns true if pdftops's exit code is 0. Otherwise it returns false.
   * @param pdf
   * @param ps
   * @return
   */
  public abstract boolean convert(File pdf, File ps);

  public static PdfToPs get() {
    String osName = System.getProperty("os.name");

    // Currently only Windows is implemented
    if(osName.toLowerCase().contains("windows")) {
      return new WindowsPdfToPs();
    }
    return null;
  }

  private static class WindowsPdfToPs extends PdfToPs {

    private static File PDF_TO_PS_EXECUTABLE = null;

    WindowsPdfToPs() {
      prepareExecutable();
    }

    /**
     * Extracts the {@code pdftops} executable from the jar file onto the file system and stores the resulting File
     * instance for later invocation.
     */
    private synchronized void prepareExecutable() {
      if(PDF_TO_PS_EXECUTABLE == null) {
        InputStream is = null;
        try {
          is = PdfToPs.class.getResourceAsStream("pdftops-3.02pl2-win32.exe");
          if(is != null) {
            PDF_TO_PS_EXECUTABLE = File.createTempFile("pdftops", "exe");
            PDF_TO_PS_EXECUTABLE.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(PDF_TO_PS_EXECUTABLE);
            Streams.copy(is, fos);
            fos.close();
          }
        } catch(IOException e) {
          if(PDF_TO_PS_EXECUTABLE != null) {
            PDF_TO_PS_EXECUTABLE.delete();
          }
        } finally {
          if(is != null) {
            try {
              is.close();
            } catch(Exception e) {
            }
            ;
          }
        }
      }
    }

    public boolean convert(File pdf, File ps) {
      if(PDF_TO_PS_EXECUTABLE == null) {
        return false;
      }

      try {
        log.debug("Invoking {} {} {}", new String[] { PDF_TO_PS_EXECUTABLE.getAbsolutePath(), pdf.getAbsolutePath(), ps.getAbsolutePath() });
        int exitCode = Runtime.getRuntime().exec(new String[] { PDF_TO_PS_EXECUTABLE.getAbsolutePath(), pdf.getAbsolutePath(), ps.getAbsolutePath() }).waitFor();
        log.debug("pdftops exit code is {}", exitCode);
        return exitCode == 0;
      } catch(InterruptedException e) {
        log.error("Error invoking pdftops", e);
        return false;
      } catch(IOException e) {
        log.error("Error invoking pdftops", e);
        return false;
      }
    }

  }

}
