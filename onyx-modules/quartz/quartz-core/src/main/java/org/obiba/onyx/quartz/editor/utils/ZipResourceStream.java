/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;

/**
 *
 */
public class ZipResourceStream implements IResourceStream {

  private static final long serialVersionUID = 1L;

  private FileResourceStream fileResource;

  public ZipResourceStream(File zip) {
    fileResource = new FileResourceStream(zip);
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
