package org.obiba.onyx.jade.instrument.topcon;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.*;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
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
    if(externalAppHelper.isSotfwareAlreadyStarted()) {
      JOptionPane.showMessageDialog(null, externalAppHelper.getExecutable() +
          " already lock for execution.  Please make sure that another instance is not running.",
          "Cannot start application!", JOptionPane.ERROR_MESSAGE);
      throw new RuntimeException("already lock for execution");
    }
    cleanData();
    initializeParticipantData();
  }

  @Override
  public void run() {
    log.info("Launching IMAGEnet R4 Lite");
    externalAppHelper.launchExternalSoftware();

    log.info("Sending data to server");
    processData();
  }

  @Override
  public void shutdown() {
    log.info("Shutdown");
    cleanData();
  }

  private void initializeParticipantData() {
    final String participantId = instrumentExecutionService.getParticipantID();
    log.info("Initializing Participant {} Data", participantId);
    jdbc.update("insert into dbo.Persons (PersonUid, SurName, ForeName) values(?,?,?)", new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, personUUID);
        ps.setString(2, instrumentExecutionService.getParticipantLastName());
        ps.setString(3, instrumentExecutionService.getParticipantFirstName());
      }
    });

    jdbc.update("insert into dbo.Patients (PatientUid, PatientIdentifier, PersonUid) values(?,?,?)",
        new PreparedStatementSetter() {
          public void setValues(PreparedStatement ps) throws SQLException {
            ps.setString(1, patientUUID);
            ps.setString(2, participantId);
            ps.setString(3, personUUID);
          }
        });
  }

  public void processData() {
    log.info("Processing Data");
    Map<String, Data> leftData = new LeftEyeExtractor().extractData(jdbc, patientUUID);
    if(leftData.get(EyeExtractor.EYE_PICT_VENDOR) != null) {
      log.info("Left Data found");
      instrumentExecutionService.addOutputParameterValues(leftData);
    }

    Map<String, Data> rightData = new RightEyeExtractor().extractData(jdbc, patientUUID);
    if(rightData.get(EyeExtractor.EYE_PICT_VENDOR) != null) {
      log.info("Right Data found");
      instrumentExecutionService.addOutputParameterValues(rightData);
    }
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
    SqlRowSet mediaRowSet = jdbc
        .queryForRowSet("SELECT FileName, FileExt, StoragePathUid FROM dbo.Media WHERE PatientUid = ?",
            new Object[] { patientUUID });
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
