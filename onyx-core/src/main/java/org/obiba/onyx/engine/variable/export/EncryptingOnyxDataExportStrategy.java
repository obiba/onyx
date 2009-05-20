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
 * A {@code IOnyxDataExportStrategy} that encrypts the data passing through it.
 * <p>
 * Encryption is done using a randomly generated {@code SecretKey} the {@code #prepare(OnyxDataExportContext)} method is
 * called. The generated key's algorithm can be specified through {@code #setAlgorithm(String)} and the key size through
 * {@code #setKeySize(int)}. <br>
 * The key is added as an entry to the exported data. Before being added, the key is wrapped (encrypted) using a
 * {@code PublicKey}. The public key is obtained through the specified {@code IPublicKeyFactory}. The public key is
 * looked-up using the destination's name.
 * <p>
 * A {@code Cipher} instance is created with using the {@code SecretKey} and a configurable transformation
 * (Algorithm/Mode/Padding). The transform can be configured through {@code #setAlgorithm(String)},
 * {@code #setMode(String)} and {@code #setPadding(String)} methods. The default value is "AES/CFB/NoPadding".
 * <p>
 * The following values are stored in <code>encryption.xml<code> which is added to the exported data:
 * <ul>
 * <li>publicKey: the public key used for wrapping the secret key. See {@code PublicKey#getEncoded()}.</li>
 * <li>publicKeyFormat: the format of the encoded public key. See {@code PublicKey#getFormat()}.</li>
 * <li>key: wrapped secret key. Format is raw bytes.</li>
 * <li>iv: IV of the {@code Cipher}. Format is raw bytes.</li>
 * <li>algorithmParameters: the value of {@code AlgorithmParameters#getEncoded()}. Useful for Java only.</li>
 * <li>transformation: the transformation used to create the cipher e.g.: AES/CFB/NoPadding</li>
 * </ul>
 *
 */
public class EncryptingOnyxDataExportStrategy implements IChainingOnyxDataExportStrategy {

  /**
   * The name of the created entry containing the {@code EncryptionData} XML.
   */
  public static final String ENCRYPTION_DATA_XML_ENTRY = "encryption.xml";

  /**
   * The name of the created entry containing the encrypted {@code SecretKey}
   * @deprecated this key is now present in the {@code #ENCRYPTION_DATA_XML_ENTRY}
   */
  public static final String ENCRYPTION_KEY_ENTRY = "encryption.key";

  /**
   * The name of the created entry containing the IV of the generated {@code SecretKey}
   * @deprecated this key is now present in the {@code #ENCRYPTION_DATA_XML_ENTRY}
   */
  public static final String ENCRYPTION_IV_ENTRY = "encryption.iv";

  /**
   * The key value for the public key entry in {@code EncryptionData}. Stores the public key used for wrapping the
   * secret key.
   */
  public static final String PUBLIC_KEY = "publicKey";

  /**
   * The key value for the public key format entry in {@code EncryptionData}. Stores the format that the public key
   * entry uses.
   */
  public static final String PUBLIC_KEY_FORMAT = "publicKeyFormat";

  /**
   * The key value for the public key algorithm entry in {@code EncryptionData}. Stores the public key's algorithm.
   */
  public static final String PUBLIC_KEY_ALGORITHM = "publicKeyAlgorithm";

  /**
   * The key value for the secret key entry in {@code EncryptionData}. Stores the secret key.
   */
  public static final String SECRET_KEY = "key";

  /**
   * The key value for the IV entry in {@code EncryptionData}. Stores the IV.
   */
  public static final String SECRET_KEY_IV = "iv";

  /**
   * The key value for the algorithm parameters entry in {@code EncryptionData}. Stores the {@code AlgorithmParameter}.
   */
  public static final String ALGORITHM_PARAMETERS = "algorithmParameters";

  /**
   * The key value for the transformation string entry in {@code EncryptionData}. Stores the transformation,
   * {@see Cipher#getInstance(String)}.
   */
  public static final String CIPHER_TRANSFORMATION = "transformation";

  private IOnyxDataExportStrategy delegate;

  private ChainingSupport chainingSupport;

  private IPublicKeyFactory publicKeyFactory;

  private String algorithm = "AES";

  private String mode = "CFB";

  // CFB Mode supports no padding.
  private String padding = "NoPadding";

  // Larger key size requires installing "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy
  // Files" which can be downloaded from Sun
  private int keySize = 128;

  private EncryptionData encryptionData;

  private Cipher cipher;

  // Keep this around to handle the end of the stream
  private OutputStream currentEntryStream;

  public void setDelegate(IOnyxDataExportStrategy delegate) {
    this.delegate = delegate;
    this.chainingSupport = new ChainingSupport(delegate);
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
    encryptionData = new EncryptionData();
    SecretKey sk = prepareExportKey(context);
    prepareCipher(sk);

    chainingSupport.addEntry(ENCRYPTION_DATA_XML_ENTRY, encryptionData.toXml());
    encryptionData = null;
  }

  public void terminate(OnyxDataExportContext context) {
    if(context.isFailed() == false) {
      endCurrentEntry();
    }
    currentEntryStream = null;
    cipher = null;
    encryptionData = null;
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
      encryptionData.addEntry(SECRET_KEY, keyData);

      // Add the entry also for backward compatibility
      chainingSupport.addEntry(ENCRYPTION_KEY_ENTRY, keyData);

      return sk;
    } catch(GeneralSecurityException e) {
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

      if(pk.getEncoded() != null) {
        encryptionData.addEntry(PUBLIC_KEY, pk.getEncoded());
        encryptionData.addEntry(PUBLIC_KEY_FORMAT, pk.getFormat());
        encryptionData.addEntry(PUBLIC_KEY_ALGORITHM, pk.getAlgorithm());
      }

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

      encryptionData.addEntry(CIPHER_TRANSFORMATION, transformation.toString());

      byte[] iv = cipher.getIV();

      // Write the IV (useful for using something else than Java to decrypt)
      if(iv != null) {
        encryptionData.addEntry(SECRET_KEY_IV, iv);

        // Add the entry also for backward compatibility
        chainingSupport.addEntry(ENCRYPTION_IV_ENTRY, iv);
      }

      // Write the AlgorithmParameters (useful for using Java to decrypt)
      AlgorithmParameters parameters = cipher.getParameters();
      if(parameters != null) {
        encryptionData.addEntry(ALGORITHM_PARAMETERS, parameters.getEncoded());
      }

    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
