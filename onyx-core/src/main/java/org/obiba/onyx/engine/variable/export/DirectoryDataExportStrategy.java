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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.obiba.onyx.core.service.ApplicationConfigurationService;

/**
 * 
 */
public class DirectoryDataExportStrategy implements IOnyxDataExportStrategy {

  private File outputRootDirectory;

  private ApplicationConfigurationService applicationConfigurationService;

  private File currentOutputDirectory;

  private OutputStream currentOutputStream;

  public void setOutputRootDirectory(File outputDir) {
    this.outputRootDirectory = outputDir;
  }

  public void setApplicationConfigurationService(ApplicationConfigurationService appConfiguration) {
    this.applicationConfigurationService = appConfiguration;
  }

  public void prepare(OnyxDataExportContext context) {
    String siteCode = applicationConfigurationService.getApplicationConfiguration().getSiteNo();

    // Build ${outputRootDirectory}/destination/year/month/day/siteCode

    File destinationDir = new File(this.outputRootDirectory, context.getDestination());
    File yearDir = new File(destinationDir, Integer.toString(context.getExportYear()));
    File monthDir = new File(yearDir, zeroPad(Integer.toString(context.getExportMonth()), 2));
    File dayDir = new File(monthDir, zeroPad(Integer.toString(context.getExportDay()), 2));
    File siteDir = new File(dayDir, siteCode);

    if(siteDir.exists() == false) {
      if(siteDir.mkdirs() == false) {
        throw new RuntimeException("Cannot create output directory: " + siteDir.getAbsolutePath());
      }
    }
    currentOutputDirectory = siteDir;
  }

  public OutputStream newEntry(String filename) {
    if(currentOutputStream != null) {
      try {
        currentOutputStream.close();
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
    File newFile = new File(this.currentOutputDirectory, filename);
    try {
      newFile.createNewFile();
      return currentOutputStream = new FileOutputStream(newFile);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void terminate(OnyxDataExportContext context) {
    if(currentOutputStream != null) {
      try {
        currentOutputStream.close();
      } catch(IOException e) {
        throw new RuntimeException(e);
      } finally {
        currentOutputStream = null;
      }
    }
  }

  private String zeroPad(String value, int size) {
    StringBuilder sb = new StringBuilder(value);
    while(sb.length() < size) {
      sb.insert(0, '0');
    }
    return sb.toString();
  }

}
