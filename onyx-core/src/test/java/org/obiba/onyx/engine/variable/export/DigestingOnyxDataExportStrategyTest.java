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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.user.User;

/**
 * 
 */
public class DigestingOnyxDataExportStrategyTest {

  DigestingOnyxDataExportStrategy strategy;

  IOnyxDataExportStrategy mockDelegate;

  OnyxDataExportContext context = new OnyxDataExportContext("MyDestination", new User());

  @Before
  public void setup() {
    mockDelegate = EasyMock.createMock(IOnyxDataExportStrategy.class);
    strategy = new DigestingOnyxDataExportStrategy();
    strategy.setDelegate(mockDelegate);
  }

  @Test
  public void testStrategyContract() throws IOException, NoSuchAlgorithmException {

    byte[] testData = "Test Entry Data".getBytes("ISO-8859-1");
    byte[] testDataDigest = MessageDigest.getInstance("SHA-512").digest(testData);

    ByteArrayOutputStream entryStream = new ByteArrayOutputStream();
    ByteArrayOutputStream digestStream = new ByteArrayOutputStream();

    // The digesting strategy should delegate all the calls but also call an extra "newEntry" on the delegate to add the
    // digest
    mockDelegate.prepare(context);
    EasyMock.expect(mockDelegate.newEntry("testEntry.dat")).andReturn(entryStream);
    EasyMock.expect(mockDelegate.newEntry("testEntry.dat.sha512")).andReturn(digestStream);
    mockDelegate.terminate(context);

    EasyMock.replay(mockDelegate);
    strategy.prepare(context);
    OutputStream digestingStream = strategy.newEntry("testEntry.dat");
    Assert.assertNotNull(digestingStream);
    digestingStream.write(testData);
    digestingStream.flush();
    strategy.terminate(context);
    EasyMock.verify(mockDelegate);

    // Make sure we find the data we wrote
    Assert.assertArrayEquals(testData, entryStream.toByteArray());
    // Make sure the computed digest is as expected
    Assert.assertArrayEquals(testDataDigest, digestStream.toByteArray());

  }

  @Test
  public void testMultipleEntries() throws IOException, NoSuchAlgorithmException {

    TestEntry entries[] = { new TestEntry("First Entry"), new TestEntry("Second Entry"), new TestEntry("Third Entry") };
    mockDelegate.prepare(context);
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

    for(TestEntry entry : entries) {
      entry.verify();
    }
  }

  private class TestEntry {
    private String name;

    private byte[] testData;

    private byte[] testDataDigest;

    private ByteArrayOutputStream entryStream;

    private ByteArrayOutputStream digestStream;

    TestEntry(String name) throws UnsupportedEncodingException, NoSuchAlgorithmException {
      this.name = name;
      this.testData = name.getBytes("ISO-8859-1");
      this.testDataDigest = MessageDigest.getInstance("SHA-512").digest(testData);
      this.entryStream = new ByteArrayOutputStream();
      this.digestStream = new ByteArrayOutputStream();
    }

    public void expect() {
      EasyMock.expect(mockDelegate.newEntry(name)).andReturn(entryStream);
      EasyMock.expect(mockDelegate.newEntry(name + ".sha512")).andReturn(digestStream);
    }

    public void handle() throws IOException {
      OutputStream digestingStream = strategy.newEntry(name);
      Assert.assertNotNull(digestingStream);
      digestingStream.write(testData);
      digestingStream.flush();
    }

    public void verify() {
      // Make sure we find the data we wrote
      Assert.assertArrayEquals(testData, entryStream.toByteArray());
      // Make sure the computed digest is as expected
      Assert.assertArrayEquals(testDataDigest, digestStream.toByteArray());
    }

  }
}
