package org.obiba.onyx.jade.instrument.gehealthcare;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.util.io.Streams;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.client.JnlpClient;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class AchillesExpressInstrumentRunner implements InstrumentRunner, InitializingBean {

  protected Connection deviceDatabaseConn;

  protected String deviceDbUrl = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);DBQ=C:/Program Files/Lunar/OsteoReport/WinOsteo.mdb;DriverId=25;FIL=MSAccess";

  private static final Logger log = LoggerFactory.getLogger(JnlpClient.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  Participant participant;

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

  public void afterPropertiesSet() throws Exception {
    deviceDatabaseConn = getMsAccessConnection(deviceDbUrl);
    participant = instrumentExecutionService.getParticipant();
  }

  public static Connection getDatabaseConnection(String pDriver, String pUrlForConnection) throws Exception {
    try {
      Class.forName(pDriver);
      Connection wDBConnection = DriverManager.getConnection(pUrlForConnection);
      return wDBConnection;
    } catch(ClassNotFoundException wInvalidDriverName) {
      throw new RuntimeException("Invalid driver name: " + pDriver, wInvalidDriverName);
    } catch(SQLException wCouldNotEstablishConnection) {
      throw new RuntimeException("Could not establish connection with database: " + pUrlForConnection, wCouldNotEstablishConnection);
    }
  }

  public static Connection getMsAccessConnection(String pUrlForConnection) throws Exception {
    final String MS_ACCESS_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";
    Connection wMsAccessConn = getDatabaseConnection(MS_ACCESS_DRIVER, pUrlForConnection);
    wMsAccessConn.setAutoCommit(false);

    return wMsAccessConn;
  }

  public void initParticipantData() throws Exception {

    Map<String, Data> operatorInputData = instrumentExecutionService.getInputParametersValue("foot_scanned", "serialnumber");

    PreparedStatement wUpdateOsteoReportConfig = deviceDatabaseConn.prepareStatement("update Configuration " + " set CompressPrompt = ?, BackupPrompt = ?, SerialNumber = ?, " + " COMPort = ?, TargetDevice = ? ");
    wUpdateOsteoReportConfig.setInt(1, 2);
    wUpdateOsteoReportConfig.setInt(2, 2);

    // TODO find a way to set the serial number in the db (set text in a longbinary)
    wUpdateOsteoReportConfig.setString(3, (String) operatorInputData.get("serialnumber").getValue());

    wUpdateOsteoReportConfig.setString(4, "7");
    wUpdateOsteoReportConfig.setString(5, "Express");

    wUpdateOsteoReportConfig.executeUpdate();
    wUpdateOsteoReportConfig.close();

    PreparedStatement wInsertOsteoReportData = deviceDatabaseConn.prepareStatement("insert into Patients " + "( Chart_Num, FName, LName, " + " DOB, Sex, Foot ) " + "values( ?, ?, ?, ?, ?, ? )");

    wInsertOsteoReportData.setString(1, participant.getBarcode());
    wInsertOsteoReportData.setString(2, participant.getFirstName());
    wInsertOsteoReportData.setString(3, participant.getLastName());
    wInsertOsteoReportData.setDate(4, new java.sql.Date(participant.getBirthDate().getTime()));

    Gender gender = participant.getGender();
    if(gender.equals(Gender.MALE)) {
      wInsertOsteoReportData.setString(5, "M");
    } else {
      wInsertOsteoReportData.setString(5, "F");
    }

    wInsertOsteoReportData.setString(6, (String) operatorInputData.get("foot_scanned").getValue());

    wInsertOsteoReportData.executeUpdate();
    wInsertOsteoReportData.close();

    deviceDatabaseConn.commit();
  }

  public void deleteData(Connection pDatabaseConnection, String pQuery) throws Exception {

    try {
      PreparedStatement wDeleteStatement = pDatabaseConnection.prepareStatement(pQuery);
      wDeleteStatement.executeUpdate();

    } catch(Exception wSqlEx) {
      wSqlEx.printStackTrace();
      throw wSqlEx;
    }

    // pDatabaseConnection.commit();
  }

  protected void deleteDeviceData() throws Exception {
    String wDeleteOsteoReportDataSql2 = "delete from Results";
    String wDeleteOsteoReportDataSql1 = "delete from Patients";
    deleteData(deviceDatabaseConn, wDeleteOsteoReportDataSql1);
    deleteData(deviceDatabaseConn, wDeleteOsteoReportDataSql2);

    deviceDatabaseConn.commit();
  }

  public ResultSet retrieveData(Connection pDatabaseConnection, String pQuery) {
    return retrieveData(pDatabaseConnection, pQuery, ResultSet.TYPE_FORWARD_ONLY);
  }

  public ResultSet retrieveData(Connection pDatabaseConnection, String pQuery, int pResultSetType) {

    ResultSet wResult = null;

    try {
      Statement wStatement = pDatabaseConnection.createStatement(pResultSetType, ResultSet.CONCUR_READ_ONLY);
      wResult = wStatement.executeQuery(pQuery);

    } catch(Exception wSqlEx) {
      wSqlEx.printStackTrace();
    }

    return wResult;

  }

  public ResultSet retrieveData(Connection pDatabaseConnection, String pQuery, Object[] pParams) {

    ResultSet wResult = null;

    try {
      PreparedStatement wStatement = pDatabaseConnection.prepareStatement(pQuery);
      int x = 1;
      for(Object pOneParam : pParams) {
        wStatement.setObject(x++, pOneParam);
      }
      wResult = wStatement.executeQuery(pQuery);

    } catch(Exception wSqlEx) {
      wSqlEx.printStackTrace();
    }

    return wResult;

  }

  private ResultSet retrieveDeviceData(Connection pOsteoreportDbConnection) {

    final String OSTEOREPORT_QUERY = "select assessment, fxrisk, total, tscore, zscore, agematched, percentnormal, sidescanned, stiffnessindex, patients.chart_num, results.SOS, results.BUA, achillesbitmap, achillesbitmap2, appversion, roi_x, roi_y, roi_s from results, patients where results.chart_num = patients.chart_num and patients.chart_num = '" + participant.getBarcode() + "'";

    ResultSet wOsteoreport = retrieveData(pOsteoreportDbConnection, OSTEOREPORT_QUERY);

    return wOsteoreport;
  }

  public void sendDataToServer(ResultSet data) throws Exception {

    Map<String, Data> ouputToSend = new HashMap<String, Data>();

    ouputToSend.put("Assessment", new Data(DataType.DECIMAL, data.getDouble("assessment")));
    ouputToSend.put("Fracture Risk", new Data(DataType.DECIMAL, data.getDouble("fxrisk")));
    ouputToSend.put("Stiffness Index Result", new Data(DataType.DECIMAL, data.getDouble("total")));
    ouputToSend.put("T-Score", new Data(DataType.DECIMAL, data.getDouble("tscore")));
    ouputToSend.put("Z-Score", new Data(DataType.DECIMAL, data.getDouble("zscore")));
    ouputToSend.put("% Age Matched", new Data(DataType.DECIMAL, data.getDouble("agematched")));
    ouputToSend.put("% Young Adult", new Data(DataType.DECIMAL, data.getDouble("percentnormal")));
    ouputToSend.put("Foot Scanned", new Data(DataType.TEXT, data.getString("sidescanned")));
    ouputToSend.put("Stiffness Index", new Data(DataType.DECIMAL, data.getDouble("stiffnessindex")));
    ouputToSend.put("Speed of Ultrasound", new Data(DataType.DECIMAL, data.getDouble("SOS")));
    ouputToSend.put("Broadband Ultrasound Attenuation", new Data(DataType.DECIMAL, data.getDouble("BUA")));
    ouputToSend.put("Achilles Software Version", new Data(DataType.TEXT, data.getString("appversion")));
    ouputToSend.put("Region of Intersection X coordinate", new Data(DataType.INTEGER, data.getLong("roi_x")));
    ouputToSend.put("Region of Intersection Y coordinate", new Data(DataType.INTEGER, data.getLong("roi_y")));
    ouputToSend.put("Region of Intersection Z coordinate", new Data(DataType.INTEGER, data.getLong("roi_z")));
    String achillebitmapData = Streams.readString(data.getBinaryStream("achillesbitmap"), "UTF-8");
    ouputToSend.put("Stiffness Index graph", new Data(DataType.DATA, achillebitmapData.getBytes()));
    String achillebitmapData2 = Streams.readString(data.getBinaryStream("achillesbitmap"), "UTF-8");
    ouputToSend.put("Ultrasound Graphic", new Data(DataType.DATA, achillebitmapData2.getBytes()));

    instrumentExecutionService.addOutputParameterValues(ouputToSend);

  }

  public void initialize() {
    log.info("*** Initializing Achilles Express Runner ***");
    try {
      deleteDeviceData();
      initParticipantData();
    } catch(Exception ex) {
      log.info("*** EXCEPTION INITIALIZE STEP: ", ex);
    }
  }

  public void run() {
    log.info("*** Running Achilles Express Runner ***");
    externalAppHelper.launch();
    ResultSet results = retrieveDeviceData(deviceDatabaseConn);
    try {
      sendDataToServer(results);
    } catch(Exception e) {
      log.error("Could not send Achilles Express data to server", e);
    }
  }

  public void shutdown() {
    log.info("*** Shutdown Achilles Express Runner ***");
    // Get data from external app
    try {
      // TODO set patient id
      deleteDeviceData();
    } catch(Exception ex) {
      log.info("*** EXCEPTION SHUTDOWN STEP: ", ex);
    }
  }

}