/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.runtime.upgrade;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.obiba.onyx.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 
 */
public class DataUpgrade_1_4_0 extends AbstractUpgradeStep {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(DataUpgrade_1_4_0.class);

  private static final String CREATE_TEMP_TABLE = "CREATE TEMPORARY TABLE participant_tube_registration_tmp LIKE participant_tube_registration";

  private static final String INSERT_PTR_INTO_TEMP_TABLE = "INSERT INTO participant_tube_registration_tmp SELECT * FROM participant_tube_registration";

  private static final String INSERT_PTR_WITH_TUBE_SET_NAME = "INSERT INTO participant_tube_registration (start_time, end_time, contraindication_code, other_contraindication, interview_id, tube_set_name) SELECT start_time, end_time, contraindication_code, other_contraindication, interview_id, :tubeSetName AS tube_set_name FROM participant_tube_registration_tmp";

  private static final String UPDATE_REGISTERED_PARTICIPANT_TUBES = "UPDATE registered_participant_tube INNER JOIN participant_tube_registration SET participant_tube_registration_id = (SELECT id FROM participant_tube_registration p WHERE p.interview_id = participant_tube_registration.interview_id AND p.tube_set_name = :tubeSetName) WHERE registered_participant_tube.participant_tube_registration_id = participant_tube_registration.id AND participant_tube_registration.tube_set_name IS NULL AND registered_participant_tube.barcode LIKE :tubeBarcodePattern";

  private static final String DELETE_PTR_WITH_NO_TUBES = "DELETE FROM participant_tube_registration WHERE id NOT IN (SELECT participant_tube_registration_id FROM registered_participant_tube)";

  //
  // Instance Variables
  //

  private NamedParameterJdbcTemplate jdbcTemplate;

  private Properties dataUpgradeProperties;

  //
  // AbstractUpgradeStep Methods
  //

  public void execute(Version currentVersion) {
    log.info("Checking if data upgrade configured");
    if(isDataUpgradeConfigured()) {
      // Create a temporary table to hold existing participant_tube_registration rows.
      log.info("Creating temp table");
      jdbcTemplate.getJdbcOperations().execute(CREATE_TEMP_TABLE);

      // Copy participant_tube_registration rows into participant_tube_registration_tmp.
      log.info("Copying participant_tube_registration rows");
      jdbcTemplate.getJdbcOperations().execute(INSERT_PTR_INTO_TEMP_TABLE);

      // For each Ruby stage, insert into participant_tube_registration copies of existing rows (copied from
      // participant_registration_tube_tmp), setting tube_set_name to the Ruby stage name.
      log.info("Inserting participant_tube_registration rows with tube_set_name");
      insertParticipantTubeRegistrationRowsForEachRubyStage();

      // Link registered_participant_tube rows to correct participant_tube_registration.
      log.info("Linking tubes to appropriate participant_tube_registration");
      linkTubesToCorrectParticipantTubeRegistration();

      // Delete any participant_tube_registration rows with no registered_participant_tube children.
      jdbcTemplate.getJdbcOperations().execute(DELETE_PTR_WITH_NO_TUBES);
    } else {
      log.info("Data upgrade skipped (not configured)");
    }
  }

  //
  // Methods
  //

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  public void setDataUpgradeResource(Resource resource) {
    if(resource.exists()) {
      try {
        dataUpgradeProperties = new Properties();
        dataUpgradeProperties.load(resource.getInputStream());
      } catch(IOException ex) {
        log.error("Data upgrade properties found but could not be loaded", ex);
      }
    } else {
      log.info("Data upgrade properties not found");
    }
  }

  /**
   * Indicates whether data upgrade has been configured by the cohort.
   * 
   * @return <code>true</code> if data upgrade configured
   */
  private boolean isDataUpgradeConfigured() {
    log.info("dataUpgradeProperties is null = " + (dataUpgradeProperties == null));
    log.info("dataUpgradeProperties is empty = " + (dataUpgradeProperties.isEmpty()));
    return (dataUpgradeProperties != null && !dataUpgradeProperties.isEmpty());
  }

  private void insertParticipantTubeRegistrationRowsForEachRubyStage() {
    Enumeration<Object> values = dataUpgradeProperties.elements();

    Set<String> tubeSets = new HashSet<String>();

    while(values.hasMoreElements()) {
      String tubeSetName = (String) values.nextElement();

      if(!tubeSets.contains(tubeSetName)) {
        tubeSets.add(tubeSetName);

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("tubeSetName", tubeSetName);
        jdbcTemplate.update(INSERT_PTR_WITH_TUBE_SET_NAME, paramMap);
      }
    }
  }

  private void linkTubesToCorrectParticipantTubeRegistration() {
    Iterator<Map.Entry<Object, Object>> entryIter = dataUpgradeProperties.entrySet().iterator();

    while(entryIter.hasNext()) {
      Map.Entry<Object, Object> entry = entryIter.next();
      String tubeBarcodePattern = (String) entry.getKey();
      String tubeSetName = (String) entry.getValue();

      Map<String, String> paramMap = new HashMap<String, String>();
      paramMap.put("tubeBarcodePattern", tubeBarcodePattern);
      paramMap.put("tubeSetName", tubeSetName);

      jdbcTemplate.update(UPDATE_REGISTERED_PARTICIPANT_TUBES, paramMap);
    }
  }
}
