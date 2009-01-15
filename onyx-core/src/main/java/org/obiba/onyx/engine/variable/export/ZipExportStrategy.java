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
public class ZipExportStrategy implements IChainingOnyxDataExportStrategy {

  private IOnyxDataExportStrategy delegate;

  private ZipOutputStream zipOutputStream;

  private String currentEntryName;

  public void setDelegate(IOnyxDataExportStrategy delegate) {
    this.delegate = delegate;
  }

  public void prepare(OnyxDataExportContext context) {
    delegate.prepare(context);
    StringBuilder outputName = new StringBuilder();
    outputName.append(context.getExportYear()).append('-').append(zeroPad(context.getExportMonth(), 2)).append('-').append(zeroPad(context.getExportDay(), 2)).append("T").append(zeroPad(context.getExportHour(), 2)).append('h').append(zeroPad(context.getExportMinute(), 2)).append('m').append(zeroPad(context.getExportSecond(), 2)).append('.').append(zeroPad(context.getExportMillisecond(), 3)).append(".zip");
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
      if(context.isFailed() == false) {
        zipOutputStream.finish();
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      zipOutputStream = null;
    }
    delegate.terminate(context);
  }

  private String zeroPad(int value, int size) {
    return zeroPad(Integer.toString(value), size);
  }

  private String zeroPad(String value, int size) {
    StringBuilder sb = new StringBuilder(value);
    while(sb.length() < size) {
      sb.insert(0, '0');
    }
    return sb.toString();
  }
}
