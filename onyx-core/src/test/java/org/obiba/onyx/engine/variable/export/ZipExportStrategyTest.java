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
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.user.User;

/**
 * 
 */
public class ZipExportStrategyTest {

  ZipExportStrategy strategy;

  IOnyxDataExportStrategy mockDelegate;

  OnyxDataExportContext context = new OnyxDataExportContext("MyDestination", new User());

  @Before
  public void setup() {
    mockDelegate = EasyMock.createMock(IOnyxDataExportStrategy.class);
    strategy = new ZipExportStrategy();
    strategy.setDelegate(mockDelegate);
  }

  @Test
  public void testStrategyContract() throws IOException, NoSuchAlgorithmException {

    String entryName = "testEntry.xml";
    byte[] testData = "Test Entry Data".getBytes("ISO-8859-1");

    ByteArrayOutputStream zipStream = new ByteArrayOutputStream();

    StringBuilder outputName = new StringBuilder();
    outputName.append(context.getExportYear()).append('-').append(context.getExportMonth()).append('-').append(context.getExportDay()).append("T").append(context.getExportHour()).append('h').append(context.getExportMinute()).append(".zip");

    mockDelegate.prepare(context);
    EasyMock.expect(mockDelegate.newEntry(outputName.toString())).andReturn(zipStream);
    mockDelegate.terminate(context);

    EasyMock.replay(mockDelegate);
    strategy.prepare(context);

    OutputStream entryStream = strategy.newEntry(entryName);
    Assert.assertNotNull(entryStream);
    entryStream.write(testData);
    entryStream.flush();

    strategy.terminate(context);
    EasyMock.verify(mockDelegate);

    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipStream.toByteArray()));
    ZipEntry entry = zis.getNextEntry();
    Assert.assertEquals(entryName, entry.getName());
    byte[] entryData = new byte[testData.length];
    zis.read(entryData);
    Assert.assertArrayEquals(testData, entryData);
  }

  @Test
  public void testMultipleEntries() throws IOException {

    TestEntry entries[] = { new TestEntry("firstEntry.xml"), new TestEntry("secondEntry.xml"), new TestEntry("thirdEntry.xml") };

    ByteArrayOutputStream zipStream = new ByteArrayOutputStream();

    StringBuilder outputName = new StringBuilder();
    outputName.append(context.getExportYear()).append('-').append(context.getExportMonth()).append('-').append(context.getExportDay()).append("T").append(context.getExportHour()).append('h').append(context.getExportMinute()).append(".zip");

    mockDelegate.prepare(context);
    EasyMock.expect(mockDelegate.newEntry(outputName.toString())).andReturn(zipStream);
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

    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipStream.toByteArray()));
    for(TestEntry entry : entries) {
      entry.verify(zis);
    }
  }

  private class TestEntry {
    private String name;

    private byte[] testData;

    TestEntry(String name) throws UnsupportedEncodingException {
      this.name = name;
      this.testData = name.getBytes("ISO-8859-1");
    }

    public void expect() {
    }

    public void handle() throws IOException {
      OutputStream digestingStream = strategy.newEntry(name);
      Assert.assertNotNull(digestingStream);
      digestingStream.write(testData);
      digestingStream.flush();
    }

    public void verify(ZipInputStream zis) throws IOException {
      ZipEntry entry = zis.getNextEntry();
      Assert.assertEquals(name, entry.getName());

      byte[] entryData = new byte[testData.length];
      zis.read(entryData);
      Assert.assertArrayEquals(testData, entryData);
    }

  }
}
