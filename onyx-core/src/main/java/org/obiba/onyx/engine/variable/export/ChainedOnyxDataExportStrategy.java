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

import java.io.OutputStream;

import org.springframework.beans.factory.InitializingBean;

/**
 * 
 */
public class ChainedOnyxDataExportStrategy implements IOnyxDataExportStrategy, InitializingBean {

  private DigestingOnyxDataExportStrategy firstDigestStrategy;

  private ZipExportStrategy zipStrategy;

  private DigestingOnyxDataExportStrategy secondDigestStrategy;

  private DirectoryDataExportStrategy directoryStrategy;

  public void setFirstDigestStrategy(DigestingOnyxDataExportStrategy firstDigestStrategy) {
    this.firstDigestStrategy = firstDigestStrategy;
  }

  public void setZipStrategy(ZipExportStrategy zipStrategy) {
    this.zipStrategy = zipStrategy;
  }

  public void setSecondDigestStrategy(DigestingOnyxDataExportStrategy secondDigestStrategy) {
    this.secondDigestStrategy = secondDigestStrategy;
  }

  public void setDirectoryStrategy(DirectoryDataExportStrategy directoryStrategy) {
    this.directoryStrategy = directoryStrategy;
  }

  public OutputStream newEntry(String name) {
    return firstDigestStrategy.newEntry(name);
  }

  public void prepare(OnyxDataExportContext context) {
    firstDigestStrategy.prepare(context);
  }

  public void terminate(OnyxDataExportContext context) {
    firstDigestStrategy.terminate(context);
  }

  public void afterPropertiesSet() throws Exception {
    firstDigestStrategy.setDelegate(zipStrategy);
    zipStrategy.setDelegate(secondDigestStrategy);
    secondDigestStrategy.setDelegate(directoryStrategy);
  }

}
