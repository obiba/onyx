package org.obiba.onyx.jade.instrument.topcon;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class Imagenetr4liteInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(Imagenetr4liteInstrumentRunner.class);

  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private JdbcTemplate jdbc;

  private final String personUUID = "11111111-2222-3333-4444-555555555555";

  private final String patientUUID = personUUID;

  @Override
  public void initialize() {
    initializeParticipantData();
  }

  @Override
  public void run() {
    log.info("Launching IMAGEnet R4 Lite");
    externalAppHelper.launch();

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
    jdbc.update("insert into dbo.Persons (PersonUid, SurName, ForeName) values(?,?,?)", new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, personUUID);
        ps.setString(2, instrumentExecutionService.getParticipantLastName());
        ps.setString(3, instrumentExecutionService.getParticipantFirstName());
      }
    });

    jdbc.update("insert into dbo.Patients (PatientUid, PatientIdentifier, PersonUid) values(?,?,?)", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, patientUUID);
        ps.setString(2, instrumentExecutionService.getParticipantID());
        ps.setString(3, personUUID);
      }
    });
  }

  public void processData() {
    log.info("Processing Data");

    instrumentExecutionService.addOutputParameterValues((new LeftEyeExtractor()).extractData(jdbc, patientUUID));
    instrumentExecutionService.addOutputParameterValues((new RightEyeExtractor()).extractData(jdbc, patientUUID));
  }

  /**
   * Clean data
   */
  private void cleanData() {
    log.info("Cleaning Data");

    deletePictureFiles();

    jdbc.update("DELETE FROM dbo.Exams WHERE PatientUid = ?", patientUUID);
    jdbc.update("DELETE FROM dbo.Media WHERE PatientUid = ?", patientUUID);
    jdbc.update("DELETE FROM dbo.Patients WHERE PatientUid = ?", patientUUID);
    jdbc.update("DELETE FROM dbo.Persons WHERE PersonUid = ?", personUUID);
  }

  private void deletePictureFiles() {
    SqlRowSet mediaRowSet = jdbc.queryForRowSet("SELECT FileName, FileExt, StoragePathUid FROM dbo.Media WHERE PatientUid = ?", new Object[] { patientUUID });
    while(mediaRowSet.next()) {
      String storagePathUid = mediaRowSet.getString("StoragePathUid");
      String fileName = mediaRowSet.getString("FileName").trim();
      String extension = mediaRowSet.getString("FileExt").trim();
      String location = EyeExtractorQueryUtil.getLocation(jdbc, storagePathUid);
      log.info("Deleting: " + location + "/" + fileName + extension);
      new File(location, fileName + extension).delete();
    }
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
