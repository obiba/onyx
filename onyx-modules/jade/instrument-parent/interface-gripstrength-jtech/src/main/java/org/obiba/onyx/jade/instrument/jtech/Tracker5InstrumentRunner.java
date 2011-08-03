/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.jtech;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.paradox.ParadoxDb;
import org.obiba.paradox.ParadoxRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class Tracker5InstrumentRunner implements InstrumentRunner, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(Tracker5InstrumentRunner.class);

  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private File databaseBackupFolder;

  private String trackerDatabaseName;

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

  public String getTrackerDatabaseName() {
    return trackerDatabaseName;
  }

  public void setTrackerDatabaseName(String trackerDatabaseName) {
    this.trackerDatabaseName = trackerDatabaseName;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
  }

  @Override
  public void initialize() {
    try {
      createDatabaseBackupFolder();
      backupTrackerDatabase();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run() {
    log.info("Launching Tracker 5 software");
    externalAppHelper.launch();
    extractTrials();
  }

  @Override
  public void shutdown() {
    try {
      restoreTackerDatabase();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void extractTrials() {
    log.info("Extracting data");
    Map<String, Data> exam = extractExam();

    ParadoxDb dataDb = getGripTestDataDB();

    for(ParadoxRecord record : dataDb) {

      Long examMax = record.getValue("Maximum");
      Long avg = record.getValue("Average");
      Long cv = record.getValue("CV");

      for(int i = 1; i <= 4; i++) {
        String side = record.getValue("Side");
        String rungPosition = record.getValue("Position");
        Long rep = record.getValue("Rep" + i);
        Integer exclude = record.getValue("Rep" + i + "Exclude");
        if(rep != null && (exclude == null || exclude == 0)) {
          LinkedHashMap<String, Data> map = new LinkedHashMap<String, Data>(exam);
          map.put("Side", DataBuilder.buildText(side));
          // Convert it to an int
          map.put("Position", DataBuilder.build(DataType.INTEGER, rungPosition));
          map.put("Rep", DataBuilder.buildDecimal(Tracker5Util.asKg(rep.intValue())));

          // These don't change for each rep... but onyx doesn't support repeated and non-repeated values
          map.put("Max", DataBuilder.buildDecimal(Tracker5Util.asKg(examMax.intValue())));
          map.put("Avg", DataBuilder.buildDecimal(Tracker5Util.asKg(avg.intValue())));
          map.put("CV", DataBuilder.buildInteger(cv));
          sendToOnyx(map);
        }
      }
    }
  }

  private Map<String, Data> extractExam() {

    ParadoxDb gripTestDb = getGripTestDB();

    if(gripTestDb.getHeader().getNumRecords() != 1) {
      throw new RuntimeException("there should be only one test");
    }

    ParadoxRecord record = gripTestDb.iterator().next();
    LinkedHashMap<String, Data> map = new LinkedHashMap<String, Data>();
    String[] fields = { "Rung", "MaxReps", "Sequence", "RestTime", "Rate", "Threshold", "NormType", "Comparison" };
    DataType[] types = { DataType.INTEGER, DataType.INTEGER, DataType.TEXT, DataType.INTEGER, DataType.INTEGER, DataType.DECIMAL, DataType.INTEGER, DataType.INTEGER };
    Set<String> expectedValues = this.instrumentExecutionService.getExpectedOutputParameterVendorNames();
    for(int i = 0; i < fields.length; i++) {
      String field = fields[i];
      DataType type = types[i];
      if(expectedValues.contains(field)) {
        Serializable value = record.getValue(field);
        if(field.equals("Threshold")) {
          value = Tracker5Util.asKg(((Number) value).intValue());
        }
        Data data = value != null ? DataBuilder.build(type, value.toString()) : null;
        log.info("{} : {}", field, data);
        map.put(field, data);
      }
    }
    return map;
  }

  private void sendToOnyx(Map<String, Data> values) {
    log.info("Sending data to Onyx");
    this.instrumentExecutionService.addOutputParameterValues(values);
  }

  private ParadoxDb getGripTestDB() {
    return getTracker5DB("ZGripTest.DB");
  }

  private ParadoxDb getGripTestDataDB() {
    return getTracker5DB("ZGripTestData.DB");
  }

  private ParadoxDb getTracker5DB(String name) {
    File gripTestDb = new File(getTrackerDatabaseFolder(), name);
    if(gripTestDb.exists() == false) {
      throw new RuntimeException(name + " file cannot be found.");
    }
    if(gripTestDb.canRead() == false) {
      throw new RuntimeException(name + " file cannot be read.");
    }
    try {
      return new ParadoxDb(gripTestDb);
    } catch(IOException e) {
      throw new RuntimeException("Error reading DB " + name, e);
    }
  }

  private void restoreTackerDatabase() throws IOException {
    // Copy backed-up database files back into the tracker database folder
    FileUtil.delete(getTrackerDatabaseFolder());
    FileUtil.copyDirectory(this.databaseBackupFolder, this.getTrackerDatabaseFolder());
    FileUtil.delete(this.databaseBackupFolder);
  }

  private void createDatabaseBackupFolder() throws IOException {
    File tempDirectory = File.createTempFile("tmp", "");
    if(tempDirectory.delete() == false) {
      log.error("Cannot create temp directory: could delete temp file.");
      throw new IOException("cannot create temp direcotry");
    }
    if(tempDirectory.mkdir() == false) {
      log.error("Cannot create temp directory: could not mkdir.");
      throw new IOException("cannot create temp direcotry");
    }
    this.databaseBackupFolder = tempDirectory;
  }

  private void backupTrackerDatabase() throws IOException {
    // Copy database files out of the database folder
    try {
      FileUtil.copyDirectory(getTrackerDatabaseFolder(), databaseBackupFolder);
    } catch(IOException e) {
      log.error("Error backing up database: {}", e.getMessage());
      try {
        FileUtil.delete(databaseBackupFolder);
      } catch(IOException again) {
        // ignore
      }
      throw e;
    }
  }

  private File getTrackerDatabaseFolder() {
    return new File("c:\\Program Files\\Tracker 5\\Data", trackerDatabaseName);
  }

}
