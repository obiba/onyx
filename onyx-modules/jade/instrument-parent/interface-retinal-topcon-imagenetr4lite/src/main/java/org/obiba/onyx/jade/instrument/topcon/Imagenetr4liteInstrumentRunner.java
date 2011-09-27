package org.obiba.onyx.jade.instrument.topcon;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class Imagenetr4liteInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(Imagenetr4liteInstrumentRunner.class);

  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private JdbcTemplate jdbc;

  private Set<String> outVendorNames;

  private final String personUUID = "11111111-2222-3333-4444-555555555555";

  private final String patientUUID = personUUID;

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

    final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    jdbc.update("insert into dbo.Patients (PatientUid, PatientIdentifier, PersonUid) values(?,?,?)", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, patientUUID);
        ps.setString(2, "ONYX-" + formatter.format(new Date()));
        ps.setString(3, personUUID);
      }
    });
  }

  public Map<String, Data> retrieveData() {
    log.info("Retrieving Data");

    Map<String, Data> data = new HashMap<String, Data>();

    Map<String, Class<? extends EyeExtractor>> availableExtractors = new HashMap<String, Class<? extends EyeExtractor>>();

    availableExtractors.put(LeftEyeExtractor.name, LeftEyeExtractor.class);
    availableExtractors.put(RightEyeExtractor.name, RightEyeExtractor.class);

    for(String vendorName : outVendorNames) {
      if(availableExtractors.keySet().contains(vendorName)) {
        EyeExtractor instance = instantiate(availableExtractors, vendorName);
        instance.extractData(jdbc, data, patientUUID);
      }
    }
    return data;
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

  private EyeExtractor instantiate(Map<String, Class<? extends EyeExtractor>> extractors, String vendorName) {
    try {
      return BeanUtils.instantiate(extractors.get(vendorName));
    } catch(Exception e) {
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
