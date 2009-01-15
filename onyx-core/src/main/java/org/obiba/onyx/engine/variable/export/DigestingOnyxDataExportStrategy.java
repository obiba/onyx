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
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 */
public class DigestingOnyxDataExportStrategy implements IChainingOnyxDataExportStrategy {

  private IOnyxDataExportStrategy delegate;

  private String digestType = "SHA-512";

  private String entrySuffix = ".sha512";

  private String currentEntryName;

  private MessageDigest digest;

  private DigestOutputStream digestStream;

  public void setDelegate(IOnyxDataExportStrategy delegate) {
    this.delegate = delegate;
  }

  public void setDigestType(String digestType) {
    this.digestType = digestType;
  }

  public void setEntrySuffix(String entrySuffix) {
    this.entrySuffix = entrySuffix;
  }

  public void prepare(OnyxDataExportContext context) {
    getDigest();
    delegate.prepare(context);
  }

  public OutputStream newEntry(String name) {
    if(digestStream != null) {
      addDigest();
    }
    getDigest().reset();
    currentEntryName = name;
    return digestStream = new DigestOutputStream(delegate.newEntry(name), getDigest());
  }

  public void terminate(OnyxDataExportContext context) {
    if(digestStream != null) {
      if(context.isFailed() == false) {
        addDigest();
      } else {
        currentEntryName = null;
        digestStream = null;
      }
    }
    delegate.terminate(context);
  }

  protected void addDigest() {
    byte[] entryDigest = getDigest().digest();
    OutputStream os = delegate.newEntry(currentEntryName + entrySuffix);
    try {
      os.write(entryDigest);
      os.flush();
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      currentEntryName = null;
      digestStream = null;
    }
  }

  protected MessageDigest getDigest() {
    if(digest == null) {
      try {
        digest = MessageDigest.getInstance(digestType);
      } catch(NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }

    }
    return digest;
  }
}
