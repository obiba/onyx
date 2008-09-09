package org.obiba.onyx.jade.instrument.gehealthcare;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

public class AchillesExpressInstrumentRunner implements InstrumentRunner, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(AchillesExpressInstrumentRunner.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private JdbcTemplate achillesExpressDb;

  private String participantID;
  private String participantFirstName;
  private String participantLastName;
  private Date participantBirthDate;
  private String participantGender;

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

  public JdbcTemplate getAchillesExpressDb() {
    return achillesExpressDb;
  }

  public void setAchillesExpressDb(JdbcTemplate achillesExpressDb) {
    this.achillesExpressDb = achillesExpressDb;
  }

  public void afterPropertiesSet() throws Exception {
    participantFirstName = instrumentExecutionService.getParticipantFirstName();
    participantLastName = instrumentExecutionService.getParticipantLastName();
    participantID = instrumentExecutionService.getParticipantID();
    participantBirthDate = instrumentExecutionService.getParticipantBirthDate();
    participantGender = instrumentExecutionService.getParticipantGender();
  }

  public void setAchillesExpressConfig() {

    achillesExpressDb.update("update Configuration set CompressPrompt = ?, BackupPrompt = ?, TargetDevice = ? ", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setInt(1, 2);
        ps.setInt(2, 2);
        ps.setString(3, "Express");
      }
    });

  }

  public void setParticipantData() {

    achillesExpressDb.update("insert into Patients ( Chart_Num, FName, LName, DOB, Sex, Foot ) values( ?, ?, ?, ?, ?, ? )", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, participantID);
        ps.setString(2, participantFirstName);
        ps.setString(3, participantLastName);
        ps.setDate(4, new java.sql.Date(participantBirthDate.getTime()));

        if(participantGender.equals("MALE")) {
          ps.setString(5, "M");
        } else {
          ps.setString(5, "F");
        }

        ps.setString(6, (String) instrumentExecutionService.getInputParameterValue("foot_scanned").getValue());
      }
    });

  }

  protected void deleteLocalData() {
    achillesExpressDb.update("delete from Results");
    achillesExpressDb.update("delete from Patients");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Data> retrieveDeviceData() {

    log.info("retrieveDeviceData");

    return (Map<String, Data>) achillesExpressDb.query("select assessment, fxrisk, total, tscore, zscore, agematched, percentnormal, sidescanned, stiffnessindex, patients.chart_num, results.SOS, results.BUA, achillesbitmap, achillesbitmap2, appversion, roi_x, roi_y, roi_s from results, patients where results.chart_num = patients.chart_num and patients.chart_num = ?", new PreparedStatementSetter() {

      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, participantID);
      }

    },

    new ResultSetExtractor() {

      public Object extractData(ResultSet rs) throws SQLException {

        rs.next();

        Map<String, Data> boneDensityData = new HashMap<String, Data>();
        boneDensityData.put("Assessment", new Data(DataType.DECIMAL, rs.getDouble("assessment")));
        boneDensityData.put("Fracture Risk", new Data(DataType.DECIMAL, rs.getDouble("fxrisk")));
        boneDensityData.put("Stiffness Index Result", new Data(DataType.DECIMAL, rs.getDouble("total")));
        boneDensityData.put("T-Score", new Data(DataType.DECIMAL, rs.getDouble("tscore")));
        boneDensityData.put("Z-Score", new Data(DataType.DECIMAL, rs.getDouble("zscore")));
        boneDensityData.put("% Age Matched", new Data(DataType.DECIMAL, rs.getDouble("agematched")));
        boneDensityData.put("% Young Adult", new Data(DataType.DECIMAL, rs.getDouble("percentnormal")));
        boneDensityData.put("Foot Scanned", new Data(DataType.TEXT, rs.getString("sidescanned")));
        boneDensityData.put("Stiffness Index", new Data(DataType.DECIMAL, rs.getDouble("stiffnessindex")));
        boneDensityData.put("Speed of Ultrasound", new Data(DataType.DECIMAL, rs.getDouble("SOS")));
        boneDensityData.put("Broadband Ultrasound Attenuation", new Data(DataType.DECIMAL, rs.getDouble("BUA")));
        boneDensityData.put("Achilles Software Version", new Data(DataType.TEXT, rs.getString("appversion")));
        boneDensityData.put("Region of Intersection X coordinate", new Data(DataType.INTEGER, rs.getLong("roi_x")));
        boneDensityData.put("Region of Intersection Y coordinate", new Data(DataType.INTEGER, rs.getLong("roi_y")));
        boneDensityData.put("Region of Intersection Z coordinate", new Data(DataType.INTEGER, rs.getLong("roi_s")));

        try {
          String achillebitmapData = FileUtil.readString(rs.getBinaryStream("achillesbitmap"), "UTF-8");
          boneDensityData.put("Stiffness Index graph", new Data(DataType.DATA, achillebitmapData.getBytes()));
        } catch(IOException couldNotReadGraph) {
          throw new RuntimeException("Could not retrieve Stiffness Index Graph from Achilles Express", couldNotReadGraph);
        }

        try {
          String achillebitmapData2 = FileUtil.readString(rs.getBinaryStream("achillesbitmap"), "UTF-8");
          boneDensityData.put("Ultrasound Graphic", new Data(DataType.DATA, achillebitmapData2.getBytes()));
        } catch(IOException couldNotReadGraph) {
          throw new RuntimeException("Could not retrieve Ultrasound Graphic from Achilles Express", couldNotReadGraph);
        }

        return boneDensityData;
      }

    });

  }

  public void sendDataToServer(Map<String, Data> data) {
    instrumentExecutionService.addOutputParameterValues(data);
  }

  public void initialize() {
    log.info("Cleaning up local database");
    deleteLocalData();
    log.info("Setting Achilles Express configuration");
    setAchillesExpressConfig();
    log.info("Sending participant data");
    setParticipantData();
  }

  public void run() {
    log.info("Launching Achilles Express software");
    externalAppHelper.launch();
    log.info("Retrieving measurements");
    Map<String, Data> data = retrieveDeviceData();
    log.info("Sending data to server");
    sendDataToServer(data);
  }

  public void shutdown() {
    log.info("Cleaning up local database");
    deleteLocalData();
  }

}