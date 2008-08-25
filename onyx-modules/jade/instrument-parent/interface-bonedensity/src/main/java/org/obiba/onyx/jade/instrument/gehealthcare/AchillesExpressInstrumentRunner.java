package org.obiba.onyx.jade.instrument.gehealthcare;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

public class AchillesExpressInstrumentRunner implements InstrumentRunner, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JnlpClient.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private JdbcTemplate achillesExpressDb;

  Participant participant;

  Map<String, Data> operatorInputData;

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
    participant = instrumentExecutionService.getParticipant();
    operatorInputData = instrumentExecutionService.getInputParametersValue("foot_scanned", "serialnumber");
  }

  public void setAchillesExpressConfig() {

    achillesExpressDb.update("update Configuration set CompressPrompt = ?, BackupPrompt = ?, SerialNumber = ?, COMPort = ?, TargetDevice = ? ", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setInt(1, 2);
        ps.setInt(2, 2);

        // TODO find a way to set the serial number in the db (set text in a longbinary)
        ps.setString(3, (String) operatorInputData.get("serialnumber").getValue());

        ps.setString(4, "7");
        ps.setString(5, "Express");
      }
    });

  }

  public void setParticipantData() {

    achillesExpressDb.update("insert into Patients ( Chart_Num, FName, LName, DOB, Sex, Foot ) values( ?, ?, ?, ?, ?, ? )", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, participant.getBarcode());
        ps.setString(2, participant.getFirstName());
        ps.setString(3, participant.getLastName());
        ps.setDate(4, new java.sql.Date(participant.getBirthDate().getTime()));

        Gender gender = participant.getGender();
        if(gender.equals(Gender.MALE)) {
          ps.setString(5, "M");
        } else {
          ps.setString(5, "F");
        }

        ps.setString(6, (String) operatorInputData.get("foot_scanned").getValue());
      }
    });

  }

  protected void deleteLocalData() {
    achillesExpressDb.update("delete from Results");
    achillesExpressDb.update("delete from Patients");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Data> retrieveDeviceData() {

    return (Map<String, Data>) achillesExpressDb.query("select assessment, fxrisk, total, tscore, zscore, agematched, percentnormal, sidescanned, stiffnessindex, patients.chart_num, results.SOS, results.BUA, achillesbitmap, achillesbitmap2, appversion, roi_x, roi_y, roi_s from results, patients where results.chart_num = patients.chart_num and patients.chart_num = ?", new PreparedStatementSetter() {

      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, participant.getBarcode());
      }

    },

    new ResultSetExtractor() {

      public Object extractData(ResultSet rs) throws SQLException {

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
        boneDensityData.put("Region of Intersection Z coordinate", new Data(DataType.INTEGER, rs.getLong("roi_z")));

        return boneDensityData;
      }

    });

  }

  public void sendDataToServer(Map<String, Data> data) {
    instrumentExecutionService.addOutputParameterValues(data);
  }

  public void initialize() {
    log.info("*** Initializing Achilles Express Runner ***");
    try {
      log.info("Cleaning up local database");
      deleteLocalData();
      log.info("Setting Achilles Express configuration");
      setAchillesExpressConfig();
      log.info("Sending participant data");
      setParticipantData();
      
      throw new RuntimeException(new Exception("test"));
    } catch(Exception ex) {
      log.error("*** EXCEPTION INITIALIZE STEP: ", ex);
    }
  }

  public void run() {
    log.info("*** Running Achilles Express Runner ***");
    log.info("Launching external software");
    externalAppHelper.launch();
    log.info("Retrieving measurements and sending data to server");
    sendDataToServer(retrieveDeviceData());

  }

  public void shutdown() {
    log.info("*** Shutting down Achilles Express Runner ***");
    try {
      log.info("Cleaning up local database");
      deleteLocalData();
    } catch(Exception ex) {
      log.error("*** EXCEPTION SHUTDOWN STEP: ", ex);
    }
  }

}