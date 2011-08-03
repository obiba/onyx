/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.holologic;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.holologic.HipScanDataExtractor.Side;
import org.obiba.onyx.jade.instrument.holologic.LateralScanDataExtractor.Energy;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

public class APEXInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(APEXInstrumentRunner.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private JdbcTemplate patScanDb;

  private String patScanDbPath;

  private File scanDataDir;

  // participant data
  private String participantID;

  private List<String> participantFiles = new ArrayList<String>();

  private Set<String> outVendorNames;

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

  public JdbcTemplate getPatScanDb() {
    return patScanDb;
  }

  public void setPatScanDb(JdbcTemplate patScanDb) {
    this.patScanDb = patScanDb;
  }

  public String getPatScanDbPath() {
    return patScanDbPath;
  }

  public void setPatScanDbPath(String patScanDbPath) {
    this.patScanDbPath = patScanDbPath;
    this.scanDataDir = new File(patScanDbPath).getParentFile();
  }

  /**
   * Retrieve participant data from the database and write them in the patient scan database
   * @throws Exception
   */
  public void initParticipantData() {
    participantID = "ONYX";// "RANDOM-" + new Random().nextInt(1000000);
    final Double weight = instrumentExecutionService.getInputParameterValue("Weight").getValue();
    final Double height = instrumentExecutionService.getInputParameterValue("Height").getValue();

    patScanDb.update("insert into PATIENT ( PATIENT_KEY, IDENTIFIER1, WEIGHT, HEIGHT ) values( ?, ?, ?, ? )", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, participantID);
        ps.setString(2, participantID);
        ps.setDouble(3, weight);
        ps.setDouble(4, height);
      }
    });

    if(instrumentExecutionService.hasInputParameter("Gender")) {
      final String gender = instrumentExecutionService.getInputParameterValue("Gender").getValue();
      patScanDb.update("update PATIENT set SEX = ? where PATIENT_KEY = ?", new PreparedStatementSetter() {
        public void setValues(PreparedStatement ps) throws SQLException {
          if(gender.toUpperCase().startsWith("M")) {
            ps.setString(1, "M");
          } else {
            ps.setString(1, "F");
          }
          ps.setString(2, participantID);
        }
      });
    }

    if(instrumentExecutionService.hasInputParameter("DateOfBirth")) {
      Date dob = instrumentExecutionService.getInputParameterValue("DateOfBirth").getValue();
      final java.sql.Date sqlDob = new java.sql.Date(dob.getTime());
      patScanDb.update("update PATIENT set BIRTHDATE = ? where PATIENT_KEY = ?", new PreparedStatementSetter() {
        public void setValues(PreparedStatement ps) throws SQLException {
          ps.setDate(1, sqlDob);
          ps.setString(2, participantID);
        }
      });
    }
  }

  /**
   * Initialise or restore instrument data (database and scan files).
   * @throws Exception
   */
  protected void resetDeviceData() {
    File backupDbFile = new File(getPatScanDbPath() + ".orig");
    File currentDbFile = new File(getPatScanDbPath());
    scanDataDir = currentDbFile.getParentFile();

    try {
      if(backupDbFile.exists()) {
        FileUtil.copyFile(backupDbFile, currentDbFile);
        backupDbFile.delete();
        // delete scan files
        for(String fileName : participantFiles) {
          File file = new File(scanDataDir, fileName);
          if(file.exists()) {
            file.delete();
          }
        }
      } else {
        // init
        FileUtil.copyFile(currentDbFile, backupDbFile);
      }
    } catch(Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException("Error while reseting device data: " + ex.getMessage(), ex);
    }
  }

  private List<Map<String, Data>> retrieveDeviceData() {

    List<Map<String, Data>> dataList = new ArrayList<Map<String, Data>>();

    extractScanData(dataList, new HipScanDataExtractor(patScanDb, scanDataDir, participantID, Side.LEFT));
    extractScanData(dataList, new HipScanDataExtractor(patScanDb, scanDataDir, participantID, Side.RIGHT));
    extractScanData(dataList, new WholeBodyScanDataExtractor(patScanDb, scanDataDir, participantID));
    extractScanData(dataList, new LateralScanDataExtractor(patScanDb, scanDataDir, participantID, Energy.SINGLE));
    extractScanData(dataList, new LateralScanDataExtractor(patScanDb, scanDataDir, participantID, Energy.DUAL));
    extractScanData(dataList, new SpineScanDataExtractor(patScanDb, scanDataDir, participantID));

    return dataList;

  }

  private void extractScanData(List<Map<String, Data>> dataList, APEXScanDataExtractor extractor) {
    // filter the values to output
    Map<String, Data> extractedData = extractor.extractData();
    Map<String, Data> outputData = new HashMap<String, Data>();
    for(Entry<String, Data> entry : extractedData.entrySet()) {
      if(outVendorNames.contains(entry.getKey())) {
        outputData.put(entry.getKey(), entry.getValue());
      }
    }
    dataList.add(outputData);

    participantFiles.addAll(extractor.getFileNames());
  }

  public void sendDataToServer(Map<String, Data> data) {
    instrumentExecutionService.addOutputParameterValues(data);
  }

  /**
   * Implements parent method initialize from InstrumentRunner Delete results from previous measurement and initiate the
   * input file to be read by the external application
   */
  public void initialize() {
    log.info("Backup local database");
    resetDeviceData();

    log.info("Setting participant data");
    initParticipantData();

    outVendorNames = instrumentExecutionService.getExpectedOutputParameterVendorNames();
  }

  /**
   * Implements parent method run from InstrumentRunner Launch the external application, retrieve and send the data
   */
  public void run() {
    log.info("Launching APEX software");
    externalAppHelper.launch();

    log.info("Retrieving measurements");
    List<Map<String, Data>> dataList = retrieveDeviceData();

    log.info("Sending data to server");
    for(Map<String, Data> dataMap : dataList) {
      sendDataToServer(dataMap);
    }
  }

  /**
   * Implements parent method shutdown from InstrumentRunner Delete results from current measurement
   */
  public void shutdown() {
    log.info("Restoring local database and cleaning scan files");
    resetDeviceData();
  }

}
