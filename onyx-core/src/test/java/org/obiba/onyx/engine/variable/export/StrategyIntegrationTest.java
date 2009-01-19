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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.crypt.IPublicKeyFactory;

/**
 * 
 */
public class StrategyIntegrationTest {

  IOnyxDataExportStrategy mockDelegate;

  IPublicKeyFactory mockKeyFactory;

  KeyPair keyPair;

  OnyxDataExportContext context = new OnyxDataExportContext("MyDestination", new User());

  @Before
  public void setup() throws NoSuchAlgorithmException {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    // Set a small value: it's long to generate the keypair
    kpg.initialize(512);
    keyPair = kpg.generateKeyPair();
    mockKeyFactory = EasyMock.createMock(IPublicKeyFactory.class);
    EasyMock.expect(mockKeyFactory.getPublicKey(context.getDestination())).andReturn(keyPair.getPublic()).anyTimes();
    EasyMock.replay(mockKeyFactory);

    mockDelegate = EasyMock.createMock(IOnyxDataExportStrategy.class);
  }

  @Test
  public void testDigestAndZip() throws NoSuchAlgorithmException, IOException {
    TestDigestAndZipEntry entries[] = { new TestDigestAndZipEntry("First Entry"), new TestDigestAndZipEntry("Second Entry"), new TestDigestAndZipEntry("Third Entry") };

    // Pipeline is digesting -> zip -> mock
    // This should create a zip file containing two entries for each call to newEntry on "digesting"
    ZipExportStrategy zipStrategy = new ZipExportStrategy();
    zipStrategy.setDelegate(mockDelegate);
    DigestingOnyxDataExportStrategy digestStrategy = new DigestingOnyxDataExportStrategy();
    digestStrategy.setDelegate(zipStrategy);

    // The result is a Zip file
    ByteArrayOutputStream zipStream = new ByteArrayOutputStream();

    StringBuilder outputName = new StringBuilder();
    outputName.append(context.getExportYear()).append('-').append(zeroPad(context.getExportMonth(), 2)).append('-').append(zeroPad(context.getExportDay(), 2)).append("T").append(zeroPad(context.getExportHour(), 2)).append('h').append(zeroPad(context.getExportMinute(), 2)).append('m').append(zeroPad(context.getExportSecond(), 2)).append('.').append(zeroPad(context.getExportMillisecond(), 3)).append(".zip");

    mockDelegate.prepare(context);
    EasyMock.expect(mockDelegate.newEntry(outputName.toString())).andReturn(zipStream);
    mockDelegate.terminate(context);

    EasyMock.replay(mockDelegate);
    digestStrategy.prepare(context);
    for(TestDigestAndZipEntry entry : entries) {
      entry.handle(digestStrategy);
    }
    digestStrategy.terminate(context);
    EasyMock.verify(mockDelegate);

    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipStream.toByteArray()));
    for(TestDigestAndZipEntry entry : entries) {
      entry.verify(zis);
    }

  }

  @Test
  public void testEncryptAndDigestAndZip() throws NoSuchAlgorithmException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException {

    TestEncryptAndDigestAndZipEntry[] entries = { new TestEncryptAndDigestAndZipEntry("First Entry"), new TestEncryptAndDigestAndZipEntry("Second Entry"), new TestEncryptAndDigestAndZipEntry("Third Entry") };

    // Pipeline is encrypting -> digesting -> zip -> mock
    // This should create a zip file containing two entries for each call to newEntry on "digesting".
    ZipExportStrategy zipStrategy = new ZipExportStrategy();
    zipStrategy.setDelegate(mockDelegate);
    DigestingOnyxDataExportStrategy digestStrategy = new DigestingOnyxDataExportStrategy();
    digestStrategy.setDelegate(zipStrategy);
    EncryptingOnyxDataExportStrategy encryptStrategy = new EncryptingOnyxDataExportStrategy();
    encryptStrategy.setDelegate(digestStrategy);
    encryptStrategy.setPublicKeyFactory(mockKeyFactory);

    // The result is a Zip file
    ByteArrayOutputStream zipStream = new ByteArrayOutputStream();

    StringBuilder outputName = new StringBuilder();
    outputName.append(context.getExportYear()).append('-').append(zeroPad(context.getExportMonth(), 2)).append('-').append(zeroPad(context.getExportDay(), 2)).append("T").append(zeroPad(context.getExportHour(), 2)).append('h').append(zeroPad(context.getExportMinute(), 2)).append('m').append(zeroPad(context.getExportSecond(), 2)).append('.').append(zeroPad(context.getExportMillisecond(), 3)).append(".zip");

    mockDelegate.prepare(context);
    EasyMock.expect(mockDelegate.newEntry(outputName.toString())).andReturn(zipStream);
    mockDelegate.terminate(context);

    EasyMock.replay(mockDelegate);
    encryptStrategy.prepare(context);
    for(TestEncryptAndDigestAndZipEntry entry : entries) {
      entry.handle(encryptStrategy);
    }
    encryptStrategy.terminate(context);
    EasyMock.verify(mockDelegate);

    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipStream.toByteArray()));
    byte[] keyData = readDigestedZipEntryData("encryption.key", zis);
    byte[] ivData = readDigestedZipEntryData("encryption.iv", zis);
    byte[] paramData = readDigestedZipEntryData("encryption.parameters", zis);

    for(TestEncryptAndDigestAndZipEntry entry : entries) {
      entry.verify(createCipher(keyData, paramData), zis);
    }

  }

  private byte[] readDigestedZipEntryData(String name, ZipInputStream zis) throws IOException {
    byte[] entryData = readZipEntry(name, zis);
    readZipEntry(name + ".sha512", zis);
    return entryData;
  }

  private byte[] readZipEntry(String name, ZipInputStream zis) throws IOException {
    ZipEntry keyEntry = zis.getNextEntry();
    Assert.assertEquals(name, keyEntry.getName());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int count = 0;
    while((count = zis.read(buffer)) != -1) {
      baos.write(buffer, 0, count);
    }
    return baos.toByteArray();
  }

  private Cipher createCipher(byte[] keyData, byte[] parameterData) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    // Re-create the SecretKey from its encoded bytes.

    // Unwrap the keyData
    Cipher unWrapCipher = Cipher.getInstance(keyPair.getPrivate().getAlgorithm());
    unWrapCipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
    SecretKey sk = (SecretKey) unWrapCipher.unwrap(keyData, "AES", Cipher.SECRET_KEY);

    // Re-create the Algorithm parameters from its encoded bytes.
    AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
    parameters.init(parameterData);

    // Re-create the Cipher (same key + parameters), but in DECRYPT_MODE
    Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5PADDING");
    cipher.init(Cipher.DECRYPT_MODE, sk, parameters);
    return cipher;
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

  private class TestDigestAndZipEntry {
    private String name;

    private byte[] testData;

    private byte[] testDataDigest;

    TestDigestAndZipEntry(String name) throws UnsupportedEncodingException, NoSuchAlgorithmException {
      this.name = name;
      this.testData = name.getBytes("ISO-8859-1");
      this.testDataDigest = MessageDigest.getInstance("SHA-512").digest(testData);
    }

    public void expect() {
    }

    public void handle(IOnyxDataExportStrategy strategy) throws IOException {
      OutputStream digestingStream = strategy.newEntry(name);
      Assert.assertNotNull(digestingStream);
      digestingStream.write(testData);
      digestingStream.flush();
    }

    public void verify(ZipInputStream zis) throws IOException {
      byte[] entryData = readZipEntry(name, zis);
      Assert.assertArrayEquals(testData, entryData);
      byte[] digestData = readZipEntry(name + ".sha512", zis);
      Assert.assertArrayEquals(testDataDigest, digestData);
    }

  }

  private class TestEncryptAndDigestAndZipEntry {
    private String name;

    private byte[] testData;

    TestEncryptAndDigestAndZipEntry(String name) throws UnsupportedEncodingException, NoSuchAlgorithmException {
      this.name = name;
      this.testData = name.getBytes("ISO-8859-1");
    }

    public void expect() {
    }

    public void handle(IOnyxDataExportStrategy strategy) throws IOException {
      OutputStream os = strategy.newEntry(name);
      Assert.assertNotNull(os);
      os.write(testData);
      os.flush();
    }

    public void verify(Cipher cipher, ZipInputStream zis) throws IOException, IllegalBlockSizeException, BadPaddingException {
      byte[] entryData = readDigestedZipEntryData(name, zis);
      Assert.assertArrayEquals(testData, cipher.doFinal(entryData));
    }

  }
}
