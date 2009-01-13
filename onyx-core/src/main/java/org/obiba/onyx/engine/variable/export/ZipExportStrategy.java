/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 
 */
public class ZipExportStrategy implements IOnyxDataExportStrategy {

  private IOnyxDataExportStrategy delegate;

  private ZipOutputStream zipOutputStream;

  private String currentEntryName;

  public void setDelegate(IOnyxDataExportStrategy delegate) {
    this.delegate = delegate;
  }

  public void prepare(OnyxDataExportContext context) {
    delegate.prepare(context);
    StringBuilder outputName = new StringBuilder();
    outputName.append(context.getExportYear()).append('-').append(context.getExportMonth()).append('-').append(context.getExportDay()).append("T").append(context.getExportHour()).append('h').append(context.getExportMinute()).append(".zip");
    zipOutputStream = new ZipOutputStream(delegate.newEntry(outputName.toString()));
  }

  public OutputStream newEntry(String entryName) {
    currentEntryName = entryName;
    ZipEntry entry = new ZipEntry(currentEntryName);
    try {
      zipOutputStream.putNextEntry(entry);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    return zipOutputStream;
  }

  public void terminate(OnyxDataExportContext context) {
    try {
      zipOutputStream.finish();
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      zipOutputStream = null;
    }
    delegate.terminate(context);
  }

}
