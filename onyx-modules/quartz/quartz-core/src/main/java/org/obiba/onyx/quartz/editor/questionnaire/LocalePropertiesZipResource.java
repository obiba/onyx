/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;

/**
 *
 */
public class LocalePropertiesZipResource implements IResourceStream {

  private static final long serialVersionUID = 1L;

  private FileResourceStream fileResource;

  public LocalePropertiesZipResource(QuestionnaireBundle bundle) throws IOException {

    File tmpFile = File.createTempFile(bundle.getName() + "-locales", ".zip");
    tmpFile.deleteOnExit();

    OutputStream os = new FileOutputStream(tmpFile);
    ZipOutputStream zos = new ZipOutputStream(os);
    byte[] buffer = new byte[1024];
    for(Locale locale : bundle.getAvailableLanguages()) {
      ZipEntry zip = new ZipEntry("language_" + locale.getLanguage() + ".properties");
      zos.putNextEntry(zip);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Properties properties = bundle.getLanguage(locale);
      properties.store(out, "Languages properties for questionnaire " + bundle.getName() + " for " + locale.getDisplayLanguage());
      InputStream fis = new ByteArrayInputStream(out.toByteArray());
      int read = 0;
      while((read = fis.read(buffer)) != -1) {
        zos.write(buffer, 0, read);
      }
      zos.closeEntry();
    }
    zos.close();

    fileResource = new FileResourceStream(tmpFile);
  }

  @Override
  public Time lastModifiedTime() {
    return fileResource.lastModifiedTime();
  }

  @Override
  public String getContentType() {
    return "application/zip";
  }

  @Override
  public long length() {
    return fileResource.length();
  }

  @Override
  public InputStream getInputStream() throws ResourceStreamNotFoundException {
    return fileResource.getInputStream();
  }

  @Override
  public void close() throws IOException {
    fileResource.close();
  }

  @Override
  public Locale getLocale() {
    return fileResource.getLocale();
  }

  @Override
  public void setLocale(Locale locale) {
    fileResource.setLocale(locale);
  }

}
