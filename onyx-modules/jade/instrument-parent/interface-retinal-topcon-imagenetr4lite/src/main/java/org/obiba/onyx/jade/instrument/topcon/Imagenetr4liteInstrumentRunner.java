package org.obiba.onyx.jade.instrument.topcon;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.FileCopyUtils;

public class Imagenetr4liteInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(Imagenetr4liteInstrumentRunner.class);

  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private JdbcTemplate jdbc;

  private Set<String> outVendorNames;

  private String personUUID;

  private String patientUUID;

  @Override
  public void initialize() {
    outVendorNames = instrumentExecutionService.getExpectedOutputParameterVendorNames();
    initializeParticipantData();
  }

  @Override
  public void run() {
    log.info("Launching IMAGEnet R4 Lite");
    externalAppHelper.launch();

    log.info("Sending data to server");
    instrumentExecutionService.addOutputParameterValues(retrieveData());
  }

  @Override
  public void shutdown() {
    log.info("shutdown");
    cleanData();
  }

  /**
   * Delete inserted participant data
   */
  private void cleanData() {
    log.info("Cleaning Data");
    jdbc.update("DELETE FROM dbo.Patients WHERE PatientUid = ?", new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, patientUUID);
      }
    });
    jdbc.update("DELETE FROM dbo.Persons WHERE PersonUid = ?", new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, personUUID);
      }
    });
  }

  /**
   * Retrieve participant data from the database and write them in the patient scan database
   * 
   * @throws Exception
   */
  private void initializeParticipantData() {
    log.info("initializing participant Data");
    personUUID = UUID.randomUUID().toString();
    jdbc.update("insert into dbo.Persons (PersonUid, SurName, ForeName) values(?,?,?)", new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, personUUID);
        ps.setString(2, instrumentExecutionService.getParticipantLastName());
        ps.setString(3, instrumentExecutionService.getParticipantFirstName());
      }
    });

    final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    patientUUID = UUID.randomUUID().toString();
    jdbc.update("insert into dbo.Patients (PatientUid, PatientIdentifier, PersonUid) values(?,?,?)", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, patientUUID);
        ps.setString(2, "ONYX-" + formatter.format(new Date()));
        ps.setString(3, personUUID);
      }
    });
  }

  public Map<String, Data> retrieveData() {
    Map<String, Data> data = new HashMap<String, Data>();
    SqlRowSet mediasRowSet = jdbc.queryForRowSet("SELECT FileName, FileExt, StoragePathUid FROM dbo.Media WHERE PatientUid = ?", new Object[] { patientUUID });
    while(mediasRowSet.next()) {
      String location = jdbc.queryForObject("SELECT Location FROM dbo.StoragePaths WHERE StoragePathUid = ?", new Object[] { mediasRowSet.getString("StoragePathUid") }, String.class);
      String fileName = mediasRowSet.getString("FileName").trim();
      String extension = mediasRowSet.getString("FileExt").trim();
      byte[] pict = pathToByteArray(location, fileName, extension);
      data.put("EYEPICT_VENDOR", new Data(DataType.DATA, pict));
    }
    return data;
  }

  private byte[] pathToByteArray(String location, String fileName, String extension) {
    try {
      return FileCopyUtils.copyToByteArray(new File(location, fileName + extension));
    } catch(IOException e) {
      throw new RuntimeException(e);
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
