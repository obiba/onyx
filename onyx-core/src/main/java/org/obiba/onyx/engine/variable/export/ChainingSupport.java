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
import java.io.UnsupportedEncodingException;

/**
 * Utility class for {@code IChainingOnyxDataExportStrategy} that create additional entries in their delegate strategy.
 */
class ChainingSupport {

  private IOnyxDataExportStrategy delegate;

  public ChainingSupport(IOnyxDataExportStrategy delegate) {
    this.delegate = delegate;
  }

  /**
   * Adds an byte-array entry with name {@code name} to the delegate strategy.
   * 
   * @param name the name of the entry to create
   * @param value the byte-array value to add
   */
  public void addEntry(String name, byte[] value) {
    try {
      OutputStream os = delegate.newEntry(name);
      os.write(value);
      os.flush();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Adds string entry with name {@code name} to the delegate strategy.
   * 
   * @param name the name of the entry to create
   * @param value the string value to add
   */
  public void addEntry(String name, String value) {
    try {
      addEntry(name, value.getBytes("ISO-8859-1"));
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
