package org.obiba.onyx.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code KeyStore} to use for Onyx's cryptographic purposes.
 * <p>
 * A single instance of this class should be created. Two properties are required before using the instance:
 * <ul>
 * <li>{@link #keyStoreFile}: the file to load and write the KeyStore</li>
 * <li>{@link #keyStorePassword}: the password to use when loading or storing the {@code KeyStore}</li>
 * </ul>
 * The {@link #open()} method must be called before using any other methods. The {@link close} method should be called
 * in order to persist any modifications made to the store (ie: key or certificate addition).
 * <p>
 * This class hides all checked exceptions under the common {@link KeyStoreRuntimeException} in order to reduce the
 * amount of throws declaration. Java's Security API has lots of checked exceptions that could be treated in different
 * ways, but for Onyx's purposes, any one of them is probably fatal. The actual checked exception will be available in
 * the runtime's {@code cause} attribute.
 */
public final class OnyxKeyStore implements IPublicKeyFactory {

  private static final Logger log = LoggerFactory.getLogger(OnyxKeyStore.class);

  /** Password to use for checking the KeyStore's integrity */
  private String keyStorePassword;

  /** File to load the KeyStore from */
  private File keyStoreFile;

  /** True when the store was modified after being opened */
  private boolean updated = false;

  /** Initialised on call to {@link #open()} */
  private KeyStore keyStore = null;

  public void setKeyStoreFile(File keyStoreFile) {
    this.keyStoreFile = keyStoreFile;
  }

  public void setKeyStorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
  }

  /**
   * Opens the KeyStore from the specified file using the specified password.
   * 
   * @throws KeyStoreRuntimeException
   */
  public void open() throws KeyStoreRuntimeException {
    log.info("Opening Onyx KeyStore");
    log.debug("Loading KeyStore from file {}", keyStoreFile.getAbsolutePath());
    FileInputStream fis = null;
    try {
      keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(fis = new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());
    } catch(Exception e) {
      throw new KeyStoreRuntimeException(e);
    } finally {
      if(fis != null) try {
        fis.close();
      } catch(IOException e) {
        // Ignore, but report as warning
        log.warn("Ignoring non-fatal exception when closing InputStream.", e);
      }
    }
  }

  public void close() throws KeyStoreRuntimeException {
    log.info("Closing Onyx KeyStore.");
    if(updated == true) {
      log.info("Onyx KeyStore is being written to disk as it was modified during execution.");
      log.debug("Storing KeyStore to file {}", keyStoreFile.getAbsolutePath());
      try {
        keyStore.store(new FileOutputStream(keyStoreFile), keyStorePassword.toCharArray());
      } catch(Exception e) {
        throw new KeyStoreRuntimeException(e);
      }
    }
    keyStore = null;
  }

  public List<String> getAliases() throws KeyStoreRuntimeException {
    checkKeyStore();
    try {
      List<String> aliases = new LinkedList<String>();
      Enumeration<String> keyStoreAliases = keyStore.aliases();
      while(keyStoreAliases.hasMoreElements()) {
        String alias = keyStoreAliases.nextElement();
        aliases.add(alias);
      }
      return Collections.unmodifiableList(aliases);
    } catch(KeyStoreException e) {
      throw new KeyStoreRuntimeException(e);
    }
  }

  public boolean containsAlias(String alias) throws KeyStoreRuntimeException {
    try {
      return keyStore.containsAlias(alias);
    } catch(KeyStoreException e) {
      throw new KeyStoreRuntimeException(e);
    }
  }

  public Certificate getCertificate(String alias) throws KeyStoreRuntimeException {
    checkKeyStore();
    try {
      return keyStore.getCertificate(alias);
    } catch(KeyStoreException e) {
      throw new KeyStoreRuntimeException(e);
    }
  }

  public void setCertificate(String alias, File certFile) throws KeyStoreRuntimeException {
    FileInputStream fis = null;
    try {
      log.debug("Reading certificate from file {}", certFile.getAbsolutePath());
      fis = new FileInputStream(certFile);
      setCertificate(alias, fis);
    } catch(IOException e) {
      throw new KeyStoreRuntimeException(e);
    } finally {
      try {
        if(fis != null) fis.close();
      } catch(IOException e) {
        // Ignore, but report as warning
        log.warn("Ignoring non-fatal exception when closing InputStream.", e);
      }
    }
  }

  public void setCertificate(String alias, InputStream certStream) throws KeyStoreRuntimeException {
    try {
      log.debug("Generating certificate from stream.");
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      Certificate cert = cf.generateCertificate(certStream);
      setCertificate(alias, cert);
    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      throw new KeyStoreRuntimeException(e);
    }
  }

  public void setCertificate(String alias, Certificate cert) throws KeyStoreRuntimeException {
    checkKeyStore();
    try {
      log.debug("Assigning alias {} to certificate {}", alias, cert);
      keyStore.setCertificateEntry(alias, cert);
      updated = true;
    } catch(KeyStoreException e) {
      throw new KeyStoreRuntimeException(e);
    }
  }

  public void setKey(String alias, Key key, String keyPassword) throws KeyStoreRuntimeException {
    try {
      keyStore.setKeyEntry(alias, key, keyPassword.toCharArray(), null);
    } catch(KeyStoreException e) {
      throw new KeyStoreRuntimeException(e);
    }
  }

  public PublicKey getPublicKey(String name) {
    Certificate cert = getCertificate(name);
    if(cert != null) {
      return cert.getPublicKey();
    }
    return null;
  }

  private void checkKeyStore() {
    if(keyStore == null) {
      throw new KeyStoreRuntimeException("Cannot access Onyx KeyStore. Call open() method first.");
    }
  }
}
