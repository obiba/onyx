/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.gehealthcare;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.obiba.onyx.jade.client.JnlpClient;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specified instrument runner for the ECG
 * @author acarey
 */

public class CardiosoftInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(JnlpClient.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private String cardioPath;

  private String initPath;

  private String databasePath;

  private String exportPath;

  private String settingsFileName;

  private String btrRecordFileName;

  private String btrDatabaseFileName;

  private String executableForParticipantInfo;

  private String xmlFileName;

  private String pdfFileName;

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

  public String getBtrRecordFileName() {
    return btrRecordFileName;
  }

  public void setBtrRecordFileName(String btrRecordFileName) {
    this.btrRecordFileName = btrRecordFileName;
  }

  public String getBtrDatabaseFileName() {
    return btrDatabaseFileName;
  }

  public void setBtrDatabaseFileName(String btrDatabaseFileName) {
    this.btrDatabaseFileName = btrDatabaseFileName;
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

  public String getPdfFileName() {
    return pdfFileName;
  }

  public void setPdfFileName(String pdfFileName) {
    this.pdfFileName = pdfFileName;
  }

  /**
   * Replace the instrument configuration file if needed Delete the result database and files
   * @throws Exception
   */
  protected void deleteDeviceData() {

    // Overwrite the CardioSoft configuration file
    File backupSettingsFile = new File(getInitPath(), getSettingsFileName());
    File currentSettingsFile = new File(getCardioPath(), getSettingsFileName());
    try {
      if(backupSettingsFile.exists()) {
        FileUtil.copyFile(backupSettingsFile, currentSettingsFile);
      } else {
        new File(getInitPath()).mkdir();
        FileUtil.copyFile(currentSettingsFile, backupSettingsFile);
      }
    } catch(Exception ex) {
      throw new RuntimeException("Error initializing ECG cardio.ini file", ex);
    }

    // Initialize the CardioSoft database
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (name.endsWith(".BTR"));
      }
    };

    try {
      File[] backupDatabaseFiles = new File(getInitPath()).listFiles(filter);
      if(backupDatabaseFiles.length > 0) {
        for(int i = 0; i < backupDatabaseFiles.length; i++) {
          FileUtil.copyFile(backupDatabaseFiles[i], new File(getDatabasePath(), backupDatabaseFiles[i].getName()));
        }
      } else {
        File[] databaseFiles = new File(getDatabasePath()).listFiles(filter);
        for(int i = 0; i < databaseFiles.length; i++) {
          FileUtil.copyFile(databaseFiles[i], new File(getInitPath(), databaseFiles[i].getName()));
        }
      }
    } catch(Exception couldNotInitDbs) {
      throw new RuntimeException("Error initializing ECG database files", couldNotInitDbs);
    }

    File reportFile = new File(getExportPath(), getXmlFileName());
    if(!reportFile.delete()) {
      log.warn("Could not delete Cardiosoft XML output file!");
    }

    reportFile = new File(getExportPath(), getPdfFileName());
    if(!reportFile.delete()) {
      log.warn("Could not delete Cardiosoft PDF output file!");
    }

  }

  /**
   * Place the results and xml and pdf files into a map object to send them to the server for persistence
   * @param resultParser
   * @throws Exception
   */
  public void sendDataToServer(CardiosoftInstrumentResultParser resultParser) {
    Map<String, Data> ouputToSend = new HashMap<String, Data>();

    try {
      for(PropertyDescriptor pd : Introspector.getBeanInfo(CardiosoftInstrumentResultParser.class).getPropertyDescriptors()) {
        if(!pd.getName().equalsIgnoreCase("doc") && !pd.getName().equalsIgnoreCase("xpath") && !pd.getName().equalsIgnoreCase("xmldocument") && !pd.getName().equalsIgnoreCase("class")) {
          Object value = pd.getReadMethod().invoke(resultParser);
          if(value == null) continue;
          if(value instanceof Long) {
            ouputToSend.put(pd.getName(), DataBuilder.buildInteger((Long) value));
          } else {
            ouputToSend.put(pd.getName(), DataBuilder.buildText(value.toString()));
          }
        }
      }

      // Save the xml and pdf files
      File xmlFile = new File(getExportPath(), getXmlFileName());
      ouputToSend.put("xmlFile", DataBuilder.buildBinary(xmlFile));

      File pdfFile = new File(getExportPath(), getPdfFileName());
      ouputToSend.put("pdfFile", DataBuilder.buildBinary(pdfFile));

      instrumentExecutionService.addOutputParameterValues(ouputToSend);

    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void initParticipantData() {
    File participantDataFile = new File(getDatabasePath() + getBtrRecordFileName());
    try {
      Map<String, Data> inputData = instrumentExecutionService.getInputParametersValue("ID", "LastName", "FirstName", "Gender", "Height", "Weight", "EthnicGroup", "BirthYear", "BirthMonth", "BirthDay", "Pacemaker");

      FileOutputStream participantDataOuputStream = new FileOutputStream(participantDataFile);
      BtrInputGenerator inputGenerator = new BtrInputGenerator();
      participantDataOuputStream.write((inputGenerator.generateByteBuffer(inputData)).array());
      participantDataOuputStream.close();

    } catch(Exception ex) {
      throw new RuntimeException("Error writing ecg participant data file: ", ex);
    }

    List<String> command = new ArrayList<String>();
    command.add("cmd");
    command.add("/c");
    command.add(getExecutableForParticipantInfo() + " " + getBtrRecordFileName() + " " + getBtrDatabaseFileName());

    ProcessBuilder builder = new ProcessBuilder(command);
    builder.directory(new File(getDatabasePath()));

    Process wProcess = null;
    try {
      wProcess = builder.start();
    } catch(IOException wCouldNotCreateProcess) {
      JOptionPane.showMessageDialog(null, wCouldNotCreateProcess.getStackTrace());
      JOptionPane.showMessageDialog(null, wCouldNotCreateProcess.getMessage());
      log.error("CardioSoftIntrumentRunner: Could not create external process for: " + command, wCouldNotCreateProcess.getStackTrace());
      System.exit(1);
    }

    try {
      wProcess.waitFor();
    } catch(InterruptedException wThreadInterrupted) {
      wThreadInterrupted.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Implements parent method initialize from InstrumentRunner Delete results from previous measurement
   */
  public void initialize() {
    deleteDeviceData();
    initParticipantData();
  }

  /**
   * Implements parent method run from InstrumentRunner Launch the external application, retrieve and send the data
   */
  public void run() {
    externalAppHelper.launch();
    FileInputStream resultInputStream = null;

    // Get data from external app
    try {
      resultInputStream = new FileInputStream(new File(getExportPath(), getXmlFileName()));
      CardiosoftInstrumentResultParser resultParser = new CardiosoftInstrumentResultParser(resultInputStream);
      sendDataToServer(resultParser);
    } catch(FileNotFoundException fnfEx) {
      log.error("Cardiosoft output data file not found");
    } finally {
      try {
        resultInputStream.close();
      } catch(IOException e) {
        log.warn("Could not close the inputStream", e);
      }
    }

  }

  /**
   * Implements parent method shutdown from InstrumentRunner Delete results from current measurement
   */
  public void shutdown() {
    deleteDeviceData();

  }

}
