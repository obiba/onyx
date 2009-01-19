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
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.obiba.onyx.crypt.IPublicKeyFactory;

/**
 * 
 */
public class EncryptingOnyxDataExportStrategy implements IChainingOnyxDataExportStrategy {

  private IOnyxDataExportStrategy delegate;

  private IPublicKeyFactory publicKeyFactory;

  private String algorithm = "AES";

  private String mode = "CFB";

  private String padding = "PKCS5PADDING";

  // Larger key size requires installing "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy
  // Files" which can be downloaded from Sun
  private int keySize = 128;

  private Cipher cipher;

  // Keep this around to handle the end of the stream
  private OutputStream currentEntryStream;

  public void setDelegate(IOnyxDataExportStrategy delegate) {
    this.delegate = delegate;
  }

  public void setPublicKeyFactory(IPublicKeyFactory publicKeyFactory) {
    this.publicKeyFactory = publicKeyFactory;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public void setPadding(String padding) {
    this.padding = padding;
  }

  public void setKeySize(int keySize) {
    this.keySize = keySize;
  }

  public OutputStream newEntry(String name) {
    endCurrentEntry();
    currentEntryStream = delegate.newEntry(name);
    return new CipherOutputStream(currentEntryStream, cipher);
  }

  public void prepare(OnyxDataExportContext context) {
    delegate.prepare(context);
    SecretKey sk = prepareExportKey(context);
    prepareCipher(sk);
  }

  public void terminate(OnyxDataExportContext context) {
    if(context.isFailed() == false) {
      endCurrentEntry();
    }
    currentEntryStream = null;
    delegate.terminate(context);
  }

  protected void endCurrentEntry() {
    try {
      if(currentEntryStream != null) {
        // Handle the last block(s) of encrypted data. We need to do this because CipherOutputStream only does this on
        // close() call,
        // which we don't want to call at this point.
        byte bytes[] = cipher.doFinal();
        if(bytes != null) {
          currentEntryStream.write(bytes);
          currentEntryStream.flush();
        }
      }

    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
    currentEntryStream = null;
  }

  protected SecretKey prepareExportKey(OnyxDataExportContext context) {
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
      keyGen.init(keySize);
      SecretKey sk = keyGen.generateKey();

      byte[] keyData = wrapKey(context, sk);
      OutputStream os = delegate.newEntry("encryption.key");
      os.write(keyData);
      os.flush();
      return sk;
    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected byte[] wrapKey(OnyxDataExportContext context, SecretKey sk) {
    String destination = context.getDestination();
    PublicKey pk = publicKeyFactory.getPublicKey(destination);
    if(pk == null) {
      throw new IllegalStateException("No PublicKey found for destination " + destination);
    }
    try {
      Cipher cipher = Cipher.getInstance(pk.getAlgorithm());
      cipher.init(Cipher.WRAP_MODE, pk);
      return cipher.wrap(sk);
    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  protected void prepareCipher(SecretKey sk) {
    StringBuilder transformation = new StringBuilder(algorithm);
    if(mode != null) {
      transformation.append('/').append(mode);
      if(padding != null) {
        transformation.append('/').append(padding);
      }
    }

    try {
      cipher = Cipher.getInstance(transformation.toString());
      cipher.init(Cipher.ENCRYPT_MODE, sk);

      byte[] iv = cipher.getIV();

      // Write the IV (useful for using something else than Java to decrypt)
      if(iv != null) {
        OutputStream os = delegate.newEntry("encryption.iv");
        os.write(iv);
        os.flush();
      }

      // Write the AlgorithmParameters (useful for using Java to decrypt)
      AlgorithmParameters parameters = cipher.getParameters();
      if(parameters != null) {
        OutputStream os = delegate.newEntry("encryption.parameters");
        os.write(parameters.getEncoded());
        os.flush();
      }

    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
