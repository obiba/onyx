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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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
public class EncryptingOnyxDataExportStrategyTest {

  EncryptingOnyxDataExportStrategy strategy;

  IPublicKeyFactory mockKeyFactory;

  KeyPair keyPair;

  IOnyxDataExportStrategy mockDelegate;

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
    strategy = new EncryptingOnyxDataExportStrategy();
    strategy.setDelegate(mockDelegate);
    strategy.setPublicKeyFactory(mockKeyFactory);

    strategy.setAlgorithm("AES");
    strategy.setMode("CFB");
    strategy.setPadding("PKCS5PADDING");
  }

  @Test
  public void testStrategyContract() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

    byte[] testData = "Test Entry Data".getBytes("ISO-8859-1");

    ByteArrayOutputStream entryStream = new ByteArrayOutputStream();
    ByteArrayOutputStream keyStream = new ByteArrayOutputStream();
    ByteArrayOutputStream parametersStream = new ByteArrayOutputStream();

    // The digesting strategy should delegate all the calls but also call an extra "newEntry" on the delegate to add the
    // digest
    mockDelegate.prepare(context);
    EasyMock.expect(mockDelegate.newEntry("encryption.key")).andReturn(keyStream);
    EasyMock.expect(mockDelegate.newEntry("encryption.parameters")).andReturn(parametersStream);
    EasyMock.expect(mockDelegate.newEntry("testEntry.dat")).andReturn(entryStream);
    mockDelegate.terminate(context);

    EasyMock.replay(mockDelegate);
    strategy.prepare(context);
    OutputStream encryptedStream = strategy.newEntry("testEntry.dat");
    Assert.assertNotNull(encryptedStream);
    encryptedStream.write(testData);
    encryptedStream.flush();
    strategy.terminate(context);
    EasyMock.verify(mockDelegate);

    Cipher cipher = createCipher(keyStream.toByteArray(), parametersStream.toByteArray());
    // Make sure we find the data we wrote after decrypting it
    Assert.assertArrayEquals(testData, cipher.doFinal(entryStream.toByteArray()));
  }

  @Test
  public void testMultipleEntries() throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

    TestEntry entries[] = { new TestEntry("First Entry"), new TestEntry("Second Entry"), new TestEntry("Third Entry") };

    ByteArrayOutputStream keyStream = new ByteArrayOutputStream();
    ByteArrayOutputStream parametersStream = new ByteArrayOutputStream();

    mockDelegate.prepare(context);
    EasyMock.expect(mockDelegate.newEntry("encryption.key")).andReturn(keyStream);
    EasyMock.expect(mockDelegate.newEntry("encryption.parameters")).andReturn(parametersStream);
    for(TestEntry entry : entries) {
      entry.expect();
    }
    mockDelegate.terminate(context);

    EasyMock.replay(mockDelegate);
    strategy.prepare(context);
    for(TestEntry entry : entries) {
      entry.handle();
    }
    strategy.terminate(context);
    EasyMock.verify(mockDelegate);

    Cipher cipher = createCipher(keyStream.toByteArray(), parametersStream.toByteArray());
    for(TestEntry entry : entries) {
      entry.verify(cipher);
    }
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

  private class TestEntry {
    private String name;

    private byte[] testData;

    private ByteArrayOutputStream entryStream;

    TestEntry(String name) throws UnsupportedEncodingException, NoSuchAlgorithmException {
      this.name = name;
      this.testData = name.getBytes("ISO-8859-1");
      this.entryStream = new ByteArrayOutputStream();
    }

    public void expect() {
      EasyMock.expect(mockDelegate.newEntry(name)).andReturn(entryStream);
    }

    public void handle() throws IOException {
      OutputStream digestingStream = strategy.newEntry(name);
      Assert.assertNotNull(digestingStream);
      digestingStream.write(testData);
      digestingStream.flush();
    }

    public void verify(Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
      // Make sure we find the data we wrote
      Assert.assertArrayEquals(testData, cipher.doFinal(entryStream.toByteArray()));
    }

  }
}
