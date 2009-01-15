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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ApplicationConfigurationService;

/**
 * 
 */
public class DirectoryDataExportStrategyTest {

  DirectoryDataExportStrategy strategy;

  OnyxDataExportContext context = new OnyxDataExportContext("MyDestination", new User());

  ApplicationConfigurationService mockConfigService;

  File outputDir = new File("target");

  String testSiteCode = "Site-001";

  @Before
  public void setup() {
    mockConfigService = EasyMock.createMock(ApplicationConfigurationService.class);
    ApplicationConfiguration conf = new ApplicationConfiguration();
    conf.setSiteNo(testSiteCode);
    EasyMock.expect(mockConfigService.getApplicationConfiguration()).andReturn(conf).anyTimes();
    EasyMock.replay(mockConfigService);

    strategy = new DirectoryDataExportStrategy();
    strategy.setOutputRootDirectory(outputDir);
    strategy.setApplicationConfigurationService(mockConfigService);
  }

  @Test
  public void testStrategyContract() throws IOException {
    String entryName = "testEntry.zip";
    byte[] testData = "Test Entry Data".getBytes("ISO-8859-1");

    strategy.prepare(context);
    File exportDir = getExportDir();
    Assert.assertTrue(exportDir.exists());
    OutputStream entryStream = strategy.newEntry(entryName);
    Assert.assertNotNull(entryStream);
    entryStream.write(testData);
    entryStream.flush();

    File entryFile = new File(exportDir, entryName);
    Assert.assertTrue(entryFile.exists());

    strategy.terminate(context);

    FileInputStream fis = new FileInputStream(entryFile);
    byte[] entryData = new byte[testData.length];
    fis.read(entryData);
    Assert.assertArrayEquals(testData, entryData);
  }

  @Test
  public void testMultipleEntries() throws IOException {

    TestEntry entries[] = { new TestEntry("firstEntry.xml"), new TestEntry("secondEntry.xml"), new TestEntry("thirdEntry.xml") };

    for(TestEntry entry : entries) {
      entry.expect();
    }

    strategy.prepare(context);
    for(TestEntry entry : entries) {
      entry.handle();
    }
    strategy.terminate(context);

    for(TestEntry entry : entries) {
      entry.verify(getExportDir());
    }
  }

  private File getExportDir() {
    File destinationDir = new File(this.outputDir, context.getDestination());
    File yearDir = new File(destinationDir, Integer.toString(context.getExportYear()));
    File monthDir = new File(yearDir, zeroPad(Integer.toString(context.getExportMonth()), 2));
    File dayDir = new File(monthDir, zeroPad(Integer.toString(context.getExportDay()), 2));
    File siteDir = new File(dayDir, testSiteCode);
    return siteDir;
  }

  private String zeroPad(String value, int size) {
    StringBuilder sb = new StringBuilder(value);
    while(sb.length() < size) {
      sb.insert(0, '0');
    }
    return sb.toString();
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

    public void verify(File exportDir) throws IOException {
      File entryFile = new File(exportDir, name);
      Assert.assertTrue(entryFile.exists());

      FileInputStream fis = new FileInputStream(entryFile);
      byte[] entryData = new byte[testData.length];
      fis.read(entryData);
      Assert.assertArrayEquals(testData, entryData);
    }

  }
}
