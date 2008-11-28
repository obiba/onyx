/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * This helper class allows settings to be persisted locally by the instrument bootstrap mechanism. With this we can
 * store a list of parameters for an instrument interface running on a specific machine (ex: communication port), so
 * they can retrieved the next time the interface is launched.
 * 
 * Practically, this allows a user to setup an instrument interface the first time it is launched, without having to
 * worry about setting it up on each run, because the local configuration is kept on disk, and can be retrieved
 * automatically.
 */
public class LocalSettingsHelper {

  // The name of the file in which the settings will be saved.
  private String settingsFileName;

  // A comment which is included at the top of the file, describing its content.
  private String settingsFileComment;

  private static String SETTINGS_DIR = System.getProperty("user.home") + File.separatorChar + ".jade";

  /**
   * Saves the settings to disk.
   * 
   * @param settings A property object containing the settings to be saved.
   * @throws CouldNotSaveSettingsException
   */
  public void saveSettings(Properties settings) throws CouldNotSaveSettingsException {

    File settingsDir = new File(SETTINGS_DIR);
    if(!settingsDir.exists()) {
      (new File(SETTINGS_DIR)).mkdir();
    }

    try {
      OutputStream os = new FileOutputStream(new File(SETTINGS_DIR, settingsFileName));
      settings.store(os, settingsFileComment);
      os.close();
    } catch(IOException couldNotPersistSettingsToLocalFile) {
      throw new CouldNotSaveSettingsException(couldNotPersistSettingsToLocalFile);
    }

  }

  /**
   * Retrieves the setting stored on disk.
   * 
   * @return A property containing the settings retrieved from disk.
   * @throws CouldNotRetrieveSettingsException
   */
  public Properties retrieveSettings() throws CouldNotRetrieveSettingsException {

    InputStream is;
    Properties settings = new Properties();
    try {
      is = new FileInputStream(new File(SETTINGS_DIR, settingsFileName));

      try {
        settings.load(is);
      } catch(IOException cannotReadSettingsFile) {
        throw new CouldNotRetrieveSettingsException(cannotReadSettingsFile);
      }
    } catch(FileNotFoundException settingsFileDontExist) {
      throw new CouldNotRetrieveSettingsException(settingsFileDontExist);
    }

    try {
      if(is != null) {
        is.close();
      }
    } catch(Exception e) {
      // Ignore
    }

    return settings;

  }

  public class CouldNotRetrieveSettingsException extends Exception {

    private static final long serialVersionUID = 1L;

    public CouldNotRetrieveSettingsException(Throwable exception) {
      super(exception);
    }

  }

  public class CouldNotSaveSettingsException extends Exception {

    private static final long serialVersionUID = 1L;

    public CouldNotSaveSettingsException(Throwable exception) {
      super(exception);
    }

  }

  public String getSettingsFileName() {
    return settingsFileName;
  }

  public void setSettingsFileName(String settingsFileName) {
    this.settingsFileName = settingsFileName;
  }

  public String getSettingsFileComment() {
    return settingsFileComment;
  }

  public void setSettingsFileComment(String settingsFileComment) {
    this.settingsFileComment = settingsFileComment;
  }

}
