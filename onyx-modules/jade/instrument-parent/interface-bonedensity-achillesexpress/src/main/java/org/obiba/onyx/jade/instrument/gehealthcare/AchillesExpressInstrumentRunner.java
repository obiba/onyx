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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
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

        ps.setString(6, (String) instrumentExecutionService.getInputParameterValue("INPUT_FOOT_SCANNED").getValue());
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

    return (Map<String, Data>) achillesExpressDb.query("select assessment, fxrisk, total, tscore, zscore, agematched, percentnormal, sidescanned, stiffnessindex, patients.chart_num, results.SOS, results.BUA, achillesbitmap, achillesbitmap2, appversion, roi_x, roi_y, roi_s, patients.Chart_Num, patients.FName, patients.LName, patients.Sex, patients.DOB from results, patients where results.chart_num = patients.chart_num and patients.chart_num = ?", new PreparedStatementSetter() {

      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, participantID);
      }

    },

    new ResultSetExtractor() {

      public Object extractData(ResultSet rs) throws SQLException {

        rs.next();

        Map<String, Data> boneDensityData = new HashMap<String, Data>();
        boneDensityData.put("OUTPUT_PARTICIPANT_BARCODE", DataBuilder.buildText(rs.getString("Chart_Num")));
        boneDensityData.put("OUTPUT_PARTICIPANT_FIRST_NAME", DataBuilder.buildText(rs.getString("FName")));
        boneDensityData.put("OUTPUT_PARTICIPANT_LAST_NAME", DataBuilder.buildText(rs.getString("LName")));
        boneDensityData.put("OUTPUT_PARTICIPANT_DATE_BIRTH", DataBuilder.buildDate(rs.getDate("DOB")));

        String gender = rs.getString("Sex").equals("M") ? "MALE" : "FEMALE";
        boneDensityData.put("OUTPUT_PARTICIPANT_GENDER", DataBuilder.buildText(gender));

        boneDensityData.put("RES_ASSESSMENT", DataBuilder.buildDecimal(rs.getDouble("assessment")));
        boneDensityData.put("RES_FRACTURE_RISK", DataBuilder.buildDecimal(rs.getDouble("fxrisk")));
        boneDensityData.put("RES_STIFFNESS_INDEX_RES", DataBuilder.buildDecimal(rs.getDouble("total")));
        boneDensityData.put("RES_T-SCORE", DataBuilder.buildDecimal(rs.getDouble("tscore")));
        boneDensityData.put("RES_Z-SCORE", DataBuilder.buildDecimal(rs.getDouble("zscore")));
        boneDensityData.put("RES_PERCENT_AGE_MATCHED", DataBuilder.buildDecimal(rs.getDouble("agematched")));
        boneDensityData.put("RES_PERCENT_YOUNG_ADULT", DataBuilder.buildDecimal(rs.getDouble("percentnormal")));
        boneDensityData.put("OUTPUT_FOOT_SCANNED", DataBuilder.buildText(rs.getString("sidescanned")));
        boneDensityData.put("RES_STIFFNESS_INDEX", DataBuilder.buildDecimal(rs.getDouble("stiffnessindex")));
        boneDensityData.put("RES_SPEED_ULTRASOUND", DataBuilder.buildDecimal(rs.getDouble("SOS")));
        boneDensityData.put("RES_BROADBAND_ULTRASOUND_ATT", DataBuilder.buildDecimal(rs.getDouble("BUA")));
        boneDensityData.put("RES_SOFTWARE_VERSION", DataBuilder.buildText(rs.getString("appversion")));
        boneDensityData.put("RES_REGION_INTERSECTION_X_COOR", DataBuilder.buildInteger(rs.getLong("roi_x")));
        boneDensityData.put("RES_REGION_INTERSECTION_Y_COOR", DataBuilder.buildInteger(rs.getLong("roi_y")));
        boneDensityData.put("RES_REGION_INTERSECTION_Z_COOR", DataBuilder.buildInteger(rs.getLong("roi_s")));
        boneDensityData.put("RES_STIFFNESS_INDEX_GRAPH", DataBuilder.buildBinary(rs.getBinaryStream("achillesbitmap")));
        boneDensityData.put("RES_ULTRASOUND_GRAPHIC", DataBuilder.buildBinary(rs.getBinaryStream("achillesbitmap2")));

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
