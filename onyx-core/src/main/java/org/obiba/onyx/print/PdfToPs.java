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
import java.util.Map;

import org.apache.wicket.util.io.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

/**
 * Converts a PDF file into a Postscript file using XPDF (http://www.foolabs.com/xpdf/).
 * <p>
 * Currently, only Windows is supported. The reason being that the CUPS (Linux and MacOS X) supports printing PDFs
 * directly, so there is no need to convert them to postscript first.
 * <p>
 * Due to ONYX-1619, this class now supports Linux 32 and 64 bit architecture. An issue in CUPS' pdftopdf filter causes
 * form values to be emptied during the printing. Converting to ps ourselves fixes this problem.
 */
public abstract class PdfToPs {

  private static final Logger log = LoggerFactory.getLogger(PdfToPs.class);

  private PdfToPs() {
  }

  public boolean convert(InputStream pdf, OutputStream ps) throws IOException {
    boolean exitValue = false;
    File pdfFile = File.createTempFile("pdftops", ".pdf");
    File psFile = File.createTempFile("pdftops", ".ps");
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
    String osArch = System.getProperty("os.arch");

    // Currently only Windows is implemented
    PdfToPs pdfToPs = null;
    if(osName.toLowerCase().contains("windows")) {
      pdfToPs = new WindowsPdfToPs();
    } else if(osName.toLowerCase().contains("linux")) {
      if(osArch.toLowerCase().contains("amd64") || osArch.toLowerCase().contains("x86_64")) {
        pdfToPs = new Linux64PdfToPs();
      } else if(osArch.toLowerCase().contains("x86")) {
        pdfToPs = new Linux32PdfToPs();
      }
    }

    if(pdfToPs != null) {
      log.debug("Using {} for converting to PS", pdfToPs.getClass().getName());
    } else {
      log.warn("No pdftops found: os.name={} os.arch={}", osName, osArch);
    }
    return pdfToPs;
  }

  private static abstract class AbstractPdfToPs extends PdfToPs {

    private static final Map<String, File> executableCache = Maps.newHashMap();

    private File executable() {
      if(executableCache.containsKey(executableResourceName()) == false) {
        cacheExecutable();
      }
      return executableCache.get(executableResourceName());
    }

    protected abstract String executableResourceName();

    protected void onExecutable(File executable) {
      // do nothing by default
    }

    /**
     * Extracts the {@code pdftops} executable from the jar file onto the file system and stores the resulting File
     * instance for later invocation.
     */
    private synchronized void cacheExecutable() {
      InputStream is = null;
      try {
        is = PdfToPs.class.getResourceAsStream(executableResourceName());
        if(is != null) {
          File executable = File.createTempFile("pdftops", ".bin");
          log.debug("Extracting executable {} to {}", executableResourceName(), executable.getAbsolutePath());
          executable.deleteOnExit();
          FileOutputStream fos = new FileOutputStream(executable);
          Streams.copy(is, fos);
          fos.close();
          onExecutable(executable);
          executableCache.put(executableResourceName(), executable);
        } else {
          log.error("Executable {} not found in classpath", executableResourceName());
        }
      } catch(IOException e) {
        log.error("Error extracting pdftops executable", e);
      } finally {
        Closeables.closeQuietly(is);
      }
    }

    public boolean convert(File pdf, File ps) {
      File executable = executable();
      if(executable == null) {
        log.error("No pdftops executable to invoke.");
        return false;
      }

      try {
        log.debug("Invoking {} {} {}", new String[] { executable.getAbsolutePath(), pdf.getAbsolutePath(), ps.getAbsolutePath() });
        int exitCode = Runtime.getRuntime().exec(new String[] { executable.getAbsolutePath(), pdf.getAbsolutePath(), ps.getAbsolutePath() }).waitFor();
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

  private static class WindowsPdfToPs extends AbstractPdfToPs {

    @Override
    protected String executableResourceName() {
      return "pdftops-3.03-win32.exe";
    }

  }

  private static abstract class AbstractLinuxPdfToPs extends AbstractPdfToPs {

    @Override
    protected void onExecutable(File executable) {
      try {
        // chmod to enable execution
        int exitCode = Runtime.getRuntime().exec(new String[] { "chmod", "u+x", executable.getAbsolutePath() }).waitFor();
        if(exitCode != 0) {
          log.error("Error making pdftops executable. Exit code was " + exitCode);
        }
      } catch(InterruptedException e) {
        log.error("Error making pdftops executable", e);
      } catch(IOException e) {
        log.error("Error making pdftops executable", e);
      }
    }
  }

  private static class Linux32PdfToPs extends AbstractLinuxPdfToPs {

    @Override
    protected String executableResourceName() {
      return "pdftops-3.03-linux32";
    }

  }

  private static class Linux64PdfToPs extends AbstractLinuxPdfToPs {

    @Override
    protected String executableResourceName() {
      return "pdftops-3.03-linux64";
    }

  }
}
