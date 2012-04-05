/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.gemac800;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.obiba.onyx.jade.client.JnlpClient;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Specified instrument runner for the ECG
 * @author acarey
 */

public class CardiosoftInstrumentRunner implements InstrumentRunner, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JnlpClient.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private String cardioPath;

  private String initPath;

  private String databasePath;

  private String exportPath;

  private String settingsFileName;

  private String winSettingsFileName;

  private String executableForParticipantInfo;

  private String xmlFileName;

  private ResourceBundle ecgResourceBundle;

  private Locale locale;

  public void afterPropertiesSet() throws Exception {
    setEcgResourceBundle(ResourceBundle.getBundle("ecg-instrument", getLocale()));
  }

  public InstrumentExecutionService getInstrumentExecutionService() {
    return instrumentExecutionService;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public ExternalAppLauncherHelper getExternalAppHelper() {
    return externalAppHelper;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public String getCardioPath() {
    return cardioPath;
  }

  public void setCardioPath(String cardioPath) {
    this.cardioPath = cardioPath;
  }

  public String getInitPath() {
    return initPath;
  }

  public void setInitPath(String initPath) {
    this.initPath = initPath;
  }

  public String getDatabasePath() {
    return databasePath;
  }

  public void setDatabasePath(String databasePath) {
    this.databasePath = databasePath;
  }

  public String getExportPath() {
    return exportPath;
  }

  public void setExportPath(String exportPath) {
    this.exportPath = exportPath;
  }

  public String getSettingsFileName() {
    return settingsFileName;
  }

  public void setSettingsFileName(String settingsFileName) {
    this.settingsFileName = settingsFileName;
  }

  public String getWinSettingsFileName() {
    return winSettingsFileName;
  }

  public void setWinSettingsFileName(String winSettingsFileName) {
    this.winSettingsFileName = winSettingsFileName;
  }

  public String getExecutableForParticipantInfo() {
    return executableForParticipantInfo;
  }

  public void setExecutableForParticipantInfo(String executableForParticipantInfo) {
    this.executableForParticipantInfo = executableForParticipantInfo;
  }

  public String getXmlFileName() {
    return xmlFileName;
  }

  public void setXmlFileName(String xmlFileName) {
    this.xmlFileName = xmlFileName;
  }

  /**
   * Replace the instrument configuration file if needed Delete the result database and files
   * @throws Exception
   */
  protected void deleteDeviceData() {

    // Overwrite the CardioSoft configuration file
    overwriteIniFile(getSettingsFileName());
    overwriteIniFile(getWinSettingsFileName());

    // Initialize the CardioSoft database
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (name.endsWith(".BTR"));
      }
    };

    try {
      File[] backupDatabaseFiles = new File(getCardioPath(), getInitPath()).listFiles(filter);
      if(backupDatabaseFiles.length > 0) {
        for(int i = 0; i < backupDatabaseFiles.length; i++) {
          FileUtil.copyFile(backupDatabaseFiles[i], new File(getCardioPath() + "/" + getDatabasePath(), backupDatabaseFiles[i].getName()));
        }
      } else {
        File[] databaseFiles = new File(getCardioPath(), getDatabasePath()).listFiles(filter);
        if(databaseFiles != null) {
          for(int i = 0; i < databaseFiles.length; i++) {
            FileUtil.copyFile(databaseFiles[i], new File(getCardioPath() + "/" + getInitPath(), databaseFiles[i].getName()));
          }
        }
      }

    } catch(Exception couldNotInitDbs) {
      throw new RuntimeException("Error initializing ECG database files", couldNotInitDbs);
    }

    File reportFile = new File(getExportPath(), getXmlFileName());
    if(reportFile.exists() && !reportFile.delete()) {
      log.warn("Could not delete Cardiosoft XML output file!");
    }

  }

  private void overwriteIniFile(String settingsFileName) {
    File backupDir = new File(getCardioPath(), getInitPath());
    File backupSettingsFile = new File(backupDir, settingsFileName);
    File currentSettingsFile = new File(getCardioPath(), settingsFileName);
    try {
      if(backupSettingsFile.exists()) {
        log.info("Restoring backup from {} to {}.", backupSettingsFile.getAbsolutePath(), currentSettingsFile.getAbsolutePath());
        FileUtil.copyFile(backupSettingsFile, currentSettingsFile);
      } else {
        log.info("Backup file {} does not exist.", backupSettingsFile.getAbsolutePath());
        if(backupDir.exists() == false) {
          if(backupDir.mkdir() == false) {
            log.error("Unable to create backup directory {}", backupDir.getAbsolutePath());
            throw new RuntimeException("unable to create backup directory " + backupDir.getAbsolutePath());
          }
        }
        log.info("Copying {} to {}", currentSettingsFile.getAbsolutePath(), backupSettingsFile.getAbsolutePath());
        FileUtil.copyFile(currentSettingsFile, backupSettingsFile);
      }
    } catch(Exception ex) {
      throw new RuntimeException("Error initializing ECG " + currentSettingsFile.getName() + " file", ex);
    }
  }

  /**
   * Place the results and xml file into a map object to send them to the server for persistence
   * @param resultParser
   * @throws Exception
   */
  public void sendDataToServer(CardiosoftInstrumentResultParser resultParser) {
    Map<String, Data> outputToSend = new HashMap<String, Data>();

    try {
      for(PropertyDescriptor pd : Introspector.getBeanInfo(CardiosoftInstrumentResultParser.class).getPropertyDescriptors()) {
        if(instrumentExecutionService.hasOutputParameter(pd.getName())) {
          Object value = pd.getReadMethod().invoke(resultParser);
          if(value != null) {
            if(value instanceof Long) {

              // We need to subtract one to the birthday month since the month of January is represented by "0" in
              // java.util.Calendar (January is represented by "1" in Cardiosoft).
              if(pd.getName().equals("participantBirthMonth")) {
                outputToSend.put(pd.getName(), DataBuilder.buildInteger(((Long) value) - 1));
              } else {
                outputToSend.put(pd.getName(), DataBuilder.buildInteger((Long) value));
              }

            } else if(value instanceof Double) {
              outputToSend.put(pd.getName(), DataBuilder.buildDecimal((Double) value));
            } else {
              outputToSend.put(pd.getName(), DataBuilder.buildText(value.toString()));
            }
          } else { // send null values as well (ONYX-585)
            log.info("Output parameter " + pd.getName() + " was null; will send null to server");

            if(pd.getPropertyType().isAssignableFrom(Long.class)) {
              log.info("Output parameter " + pd.getName() + " is of type INTEGER");
              outputToSend.put(pd.getName(), new Data(DataType.INTEGER, null));
            } else if(pd.getPropertyType().isAssignableFrom(Double.class)) {
              log.info("Output parameter " + pd.getName() + " is of type DECIMAL");
              outputToSend.put(pd.getName(), new Data(DataType.DECIMAL, null));
            } else {
              log.info("Output parameter " + pd.getName() + " is of type TEXT");
              outputToSend.put(pd.getName(), new Data(DataType.TEXT, null));
            }
          }
        }
      }

      // Save the xml and pdf files
      File xmlFile = new File(getExportPath(), getXmlFileName());
      outputToSend.put("xmlFile", DataBuilder.buildBinary(xmlFile));

      instrumentExecutionService.addOutputParameterValues(outputToSend);

    } catch(Exception e) {
      log.debug("Sending data to server failed.", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Create an information dialog that tells the user to wait while the data is being processed.
   */
  private void showProcessingDialog() {

    JPanel messagePanel = new JPanel();
    messagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

    JLabel message = new JLabel(ecgResourceBundle.getString("Message.ProcessingEcgMeasurement"));
    message.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
    messagePanel.add(message);

    JLabel subMessage = new JLabel(ecgResourceBundle.getString("Message.ProcessingEcgMeasurementInstructions"));
    subMessage.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
    subMessage.setForeground(Color.RED);
    messagePanel.add(subMessage);

    JFrame window = new JFrame();
    window.add(messagePanel);
    window.pack();

    // Make sure dialog stays on top of all other application windows.
    window.setAlwaysOnTop(true);
    window.setLocationByPlatform(true);

    // Center dialog horizontally at the bottom of the screen.
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    window.setLocation((screenSize.width - window.getWidth()) / 2, screenSize.height - window.getHeight() - 70);

    window.setEnabled(false);
    window.setVisible(true);

  }

  /**
   * Implements parent method initialize from InstrumentRunner Delete results from previous measurement
   */
  public void initialize() {
    showProcessingDialog();
    deleteDeviceData();
  }

  /**
   * Implements parent method run from InstrumentRunner Launch the external application, retrieve and send the data
   */
  public void run() {
    externalAppHelper.launch();
    FileInputStream resultInputStream = null;
    File cardioSoftXmlOutput = new File(getExportPath(), getXmlFileName());

    // Get data from external app
    log.info("Expected XML file path: {}", cardioSoftXmlOutput.getAbsolutePath());
    if(cardioSoftXmlOutput.exists()) {

      try {
        resultInputStream = new FileInputStream(cardioSoftXmlOutput);
      } catch(FileNotFoundException ex) {
        throw new RuntimeException("Cardiosoft output data file not found", ex);
      }

      CardiosoftInstrumentResultParser resultParser = new CardiosoftInstrumentResultParser(resultInputStream);
      sendDataToServer(resultParser);

      try {
        resultInputStream.close();
      } catch(Exception e) {
        log.warn("Could not close the inputStream", e);
      }

    } else {
      log.error("Cardiosoft output data file not found.  This usually happens if the application is closed before completing the ECG measurement.");
    }

  }

  /**
   * Implements parent method shutdown from InstrumentRunner Delete results from current measurement
   */
  public void shutdown() {
    deleteDeviceData();

  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public void setEcgResourceBundle(ResourceBundle ecgResourceBundle) {
    this.ecgResourceBundle = ecgResourceBundle;
  }

}
