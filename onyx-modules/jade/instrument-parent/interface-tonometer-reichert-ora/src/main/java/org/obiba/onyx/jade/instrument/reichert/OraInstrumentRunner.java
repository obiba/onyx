/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.reichert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

public class OraInstrumentRunner implements InstrumentRunner {
  private static final Logger log = LoggerFactory.getLogger(OraInstrumentRunner.class);

  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private JdbcTemplate jdbc;

  private final int id = 1234;

  @Override
  public void initialize() {
    if(externalAppHelper.isSotfwareAlreadyStarted()) {
      JOptionPane.showMessageDialog(null, externalAppHelper.getExecutable() + " already lock for execution.  Please make sure that another instance is not running.", "Cannot start application!", JOptionPane.ERROR_MESSAGE);
      throw new RuntimeException("already lock for execution");
    }
    initializeParticipantData();
  }

  @Override
  public void run() {
    log.info("Launching Ora");
    externalAppHelper.launchExternalSoftware();

    log.info("Sending data to server");
    processData();
  }

  @Override
  public void shutdown() {
    log.info("shutdown");
    cleanData();
  }

  /**
   * Retrieve participant data from the database and write them in the patient scan database
   * 
   * @throws Exception
   */
  private void initializeParticipantData() {
    log.info("initializing participant Data");

    jdbc.update("insert into Patients ( Name, BirthDate, Sex, GroupID, ID, RaceID ) values( ?, ?, ?, ?, ?, ? )", new PreparedStatementSetter() {

      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, instrumentExecutionService.getParticipantLastName() + ", " + instrumentExecutionService.getParticipantFirstName());
        ps.setDate(2, new java.sql.Date(instrumentExecutionService.getParticipantBirthDate().getTime()));
        ps.setBoolean(3, instrumentExecutionService.getParticipantGender().startsWith("M"));
        ps.setInt(4, 2);
        ps.setInt(5, id);
        ps.setInt(6, 1);
      }
    });
  }

  public void processData() {
    log.info("Processing Data");

    instrumentExecutionService.addOutputParameterValues(extractData("L"));
    instrumentExecutionService.addOutputParameterValues(extractData("R"));
  }

  private class Putter {

    private ResultSet rs;

    private Map<String, Data> data;

    public Putter(ResultSet rs) {
      this.rs = rs;
      this.data = new HashMap<String, Data>();
    }

    private void putInt(String key) throws SQLException {
      data.put(key, DataBuilder.buildInteger(rs.getInt(key)));
    }

    private void putDecimal(String key) throws SQLException {
      data.put(key, DataBuilder.buildDecimal(rs.getDouble(key)));
    }

    private void putDate(String key) throws SQLException {
      data.put(key, DataBuilder.buildDate(rs.getDate(key)));
    }

    private void putString(String key) throws SQLException {
      data.put(key, DataBuilder.buildText(rs.getString(key)));
    }

    private void putBoolean(String key) throws SQLException {
      data.put(key, DataBuilder.buildBoolean(rs.getString(key)));
    }

    public Map<String, Data> getData() {
      return data;
    }

  }

  private Map<String, Data> extractData(final String eyeSide) {
    return jdbc.query("select * from Measures where PatientId = ? and Eye = ? order by MeasureDate desc", new PreparedStatementSetter() {

      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setInt(1, extractPatientId());
        ps.setString(2, eyeSide);
      }

    }, new ResultSetExtractor<Map<String, Data>>() {

      @Override
      public Map<String, Data> extractData(ResultSet rs) throws SQLException, DataAccessException {

        // how to avoid while() ? last() on Access DB can not be called
        log.info("Retrieve measures");
        if(rs.next()) {
          Putter data = new Putter(rs);
          data.putInt("MeasureID");
          data.putInt("MeasureNumber");
          data.putDate("MeasureDate");
          data.putDate("SessionDate");
          data.putString("Eye");
          data.putString("ORASerialNumber");
          data.putString("ORASoftware");
          data.putString("PCSoftware");
          data.putDecimal("IOPG");
          data.putDecimal("IOPCC");
          data.putDecimal("CRF");
          data.putDecimal("CCTAvg");
          data.putDecimal("CCTLowest");
          data.putDecimal("CCTSD");
          data.putDecimal("CH");
          data.putDecimal("TearFilmValue");
          data.putString("Pressure");
          data.putString("Applanation");
          data.putDecimal("TimeIn");
          data.putDecimal("TimeOut");
          data.putString("Meds");
          data.putString("Conditions");
          data.putString("Notes1");
          data.putString("Notes2");
          data.putString("Notes3");
          data.putDecimal("m_G2");
          data.putDecimal("b_G2");
          data.putDecimal("m_G3");
          data.putDecimal("b_G3");
          data.putDecimal("iop_cc_coef");
          data.putDecimal("crf_coef");
          data.putDecimal("m_ABC");
          data.putDecimal("b_ABC");
          data.putDecimal("b_PP");
          data.putBoolean("BestWeighted");
          data.putDecimal("QualityIndex");
          data.putString("Indexes");
          return data.getData();
        }
        return Collections.emptyMap();
      }
    });
  }

  /**
   * Clean data
   */
  private void cleanData() {
    log.info("Cleaning Data");

    int patientDBId = extractPatientId();
    jdbc.update("DELETE FROM Measures WHERE PatientID = ?", patientDBId);
    jdbc.update("DELETE FROM Patients WHERE PatientID = ?", patientDBId);
  }

  private int extractPatientId() {
    return jdbc.queryForInt("select PatientID from Patients where ID = " + id);
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

  public JdbcTemplate getJdbc() {
    return jdbc;
  }

  public void setJdbc(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }
}
