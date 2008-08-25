package org.obiba.onyx.jade.instrument.gehealthcare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.wicket.util.io.Streams;
import org.obiba.core.util.FileUtil;
import org.obiba.onyx.jade.client.JnlpClient;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  protected void deleteDeviceData() throws Exception {
    // Overwrite the CardioSoft configuration file
    File backupSettingsFile = new File(getInitPath() + getSettingsFileName());
    File currentSettingsFile = new File(getCardioPath() + getSettingsFileName());
    if(backupSettingsFile.exists() && !(backupSettingsFile.lastModified() == currentSettingsFile.lastModified())) {
      FileUtil.copyFile(backupSettingsFile, currentSettingsFile);
      FileUtil.copyFile(currentSettingsFile, backupSettingsFile);// to set same lastModified property
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

  public void SendDataToServer(CardiosoftInstrumentResultParser resultParser) throws Exception {
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
        if(value instanceof Long) ouputToSend.put(field.getName(), new Data(DataType.INTEGER, (Serializable) value));
        else
          ouputToSend.put(field.getName(), new Data(DataType.TEXT, (Serializable) value));
      }
      
      // Save the xml and pdf files
      File xmlFile = new File(getExportPath() + getXmlFileName());
      String fileContent = Streams.readString(new FileInputStream(xmlFile), "UTF-8");
      byte[] xmlInput = fileContent.getBytes("UTF-8");
      ouputToSend.put("xmlFile", new Data(DataType.DATA, (Serializable) xmlInput));

      File pdfFile = new File(getExportPath() + getPdfFileName());
      fileContent = Streams.readString(new FileInputStream(pdfFile), "UTF-8");
      byte[] pdfInput = fileContent.getBytes("UTF-8");
      ouputToSend.put("pdfFile", new Data(DataType.DATA, (Serializable) pdfInput));

      instrumentExecutionService.addOutputParameterValues(ouputToSend);
      
    } catch(FileNotFoundException fnfEx) {
      log.error("*** Error: Cardiosoft output data file not found: ", fnfEx);
      JOptionPane.showMessageDialog(null, "Error: Cardiosoft output data file not found", "Could not complete process", JOptionPane.ERROR_MESSAGE);
    } catch(Exception e) {
      log.warn("Failed result: " + e.getMessage(), e);
    }
  }

  public void initialize() {
    log.info("*** Initializing Cardiosoft Runner ***");
    try {
      deleteDeviceData(); // Delete ancient data in instrument specific database
    } catch(Exception ex) {
      log.error("*** EXCEPTION INITIALIZE STEP: ", ex);
    }
  }

  public void run() {
    log.info("*** Running Cardiosoft Runner ***");
    externalAppHelper.launch();
    
    // Get data from external app
    try {
      FileInputStream resultInputStream = new FileInputStream(getExportPath() + getXmlFileName());
      CardiosoftInstrumentResultParser resultParser = new CardiosoftInstrumentResultParser(resultInputStream);
      SendDataToServer(resultParser);
    } catch(FileNotFoundException fnfEx) {
      log.error("*** Error: Cardiosoft output data file not found: ", fnfEx);
      JOptionPane.showMessageDialog(null, "Error: Cardiosoft output data file not found", "Could not complete process", JOptionPane.ERROR_MESSAGE);
    } catch(Exception ex) {
      log.error("*** EXCEPTION SHUTDOWN STEP: ", ex);
    }
  }

  public void shutdown() {
    log.info("*** Shutdown Cardiosoft Runner ***");
    try {
      deleteDeviceData(); // Delete current data in instrument specific database and files for privacy
    } catch(Exception ex) {
      log.error("*** EXCEPTION INITIALIZE STEP: ", ex);
    }
  }

}
