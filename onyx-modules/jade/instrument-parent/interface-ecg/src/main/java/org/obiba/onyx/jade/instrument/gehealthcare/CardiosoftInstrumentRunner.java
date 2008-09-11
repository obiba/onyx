package org.obiba.onyx.jade.instrument.gehealthcare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.obiba.onyx.jade.client.JnlpClient;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
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
   * Replace the instrument configuration file if needed
   * Delete the result database and files
   * @throws Exception
   */
  protected void deleteDeviceData() {
    // Overwrite the CardioSoft configuration file
    File backupSettingsFile = new File(getInitPath() + getSettingsFileName());
    File currentSettingsFile = new File(getCardioPath() + getSettingsFileName());
    try {
      if(backupSettingsFile.exists() && !(backupSettingsFile.lastModified() == currentSettingsFile.lastModified())) {
        FileUtil.copyFile(backupSettingsFile, currentSettingsFile);
        FileUtil.copyFile(currentSettingsFile, backupSettingsFile); // to set same lastModified property
      }      
    } catch(IOException ioEx) {
      throw new RuntimeException("Error in deleteDeviceData IOException ", ioEx);
    }
    

    // Delete the Pervasive database files
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (name.endsWith(".BTR"));
      }
    };
    File[] databaseFiles = new File(getDatabasePath()).listFiles(filter);
    for(int i = 0; i < databaseFiles.length; i++) {
      databaseFiles[i].delete();
    }

    // Delete generated reports
    File reportFile = new File(getExportPath() + getXmlFileName());
    reportFile.delete();
    reportFile = new File(getExportPath() + getPdfFileName());
    reportFile.delete();
  }

  /**
   * Place the results and xml and pdf files into a map object to send them to the server for persistence
   * @param resultParser
   * @throws Exception
   */
  public void SendDataToServer(CardiosoftInstrumentResultParser resultParser) {
    Map<String, Data> ouputToSend = new HashMap<String, Data>();

    Class resultParserClass = CardiosoftInstrumentResultParser.class;
    Method method;
    try {
      Field[] resultParserFields = resultParserClass.getDeclaredFields();
      for(Field field : resultParserFields) {
        if(field.getName().equalsIgnoreCase("doc") || field.getName().equalsIgnoreCase("xpath") || field.getName().equalsIgnoreCase("xmldocument"))
          continue;
        method = resultParserClass.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
        Object value = method.invoke(resultParser);
        if (value == null) continue;
        if(value instanceof Long) ouputToSend.put(field.getName(), DataBuilder.buildInteger((Long)value));
        else
          ouputToSend.put(field.getName(), DataBuilder.buildText(value.toString()));
      }
      
      // Save the xml and pdf files
      File xmlFile = new File(getExportPath() + getXmlFileName());
      ouputToSend.put("xmlFile", DataBuilder.buildBinary(xmlFile));

      File pdfFile = new File(getExportPath() + getPdfFileName());
      ouputToSend.put("pdfFile", DataBuilder.buildBinary(pdfFile));

      instrumentExecutionService.addOutputParameterValues(ouputToSend);

    } catch(Exception e) {
      log.error("Failed result: " + e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Implements parent method initialize from InstrumentRunner
   * Delete results from previous measurement
   */
  public void initialize() {
    deleteDeviceData();
  }

  /**
   * Implements parent method run from InstrumentRunner
   * Launch the external application, retrieve and send the data  
   */
  public void run() {
    externalAppHelper.launch();
    FileInputStream resultInputStream;
    
    // Get data from external app
    try {
      resultInputStream = new FileInputStream(getExportPath() + getXmlFileName());
    } catch(FileNotFoundException fnfEx) {
      JOptionPane.showMessageDialog(null, "Error: Cardiosoft output data file not found", "Could not complete process", JOptionPane.ERROR_MESSAGE);
      throw new RuntimeException("Error: Cardiosoft output data file not found: ", fnfEx);
    }
    
    CardiosoftInstrumentResultParser resultParser = new CardiosoftInstrumentResultParser(resultInputStream);
    SendDataToServer(resultParser);
    
  }

  /**
   * Implements parent method shutdown from InstrumentRunner
   * Delete results from current measurement
   */
  public void shutdown() {
    deleteDeviceData();
    
  }
}
