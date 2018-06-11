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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * Upgrade step for ONYX-587 (version 1.4.0).
 * 
 * Adds the tube_set_name column to the participant_tube_registration table (if present).
 */
public class SchemaUpgrade_1_4_0 extends AbstractUpgradeStep {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(SchemaUpgrade_1_4_0.class);

  private static final String PARTICIPANT_TUBE_REGISTRATION_TABLE = "participant_tube_registration";

  private static final String ADD_TUBE_SET_NAME_COLUMN = "ALTER TABLE participant_tube_registration ADD COLUMN tube_set_name VARCHAR(255) DEFAULT NULL;";

  //
  // Instance Variables
  //

  private JdbcTemplate jdbcTemplate;

  //
  // AbstractUpgradeStep Methods
  //

  public void execute(Version currentVersion) {
    if(isTableExists(PARTICIPANT_TUBE_REGISTRATION_TABLE)) {
      jdbcTemplate.execute(ADD_TUBE_SET_NAME_COLUMN);
    } else {
      log.info("tube_set_name column not added (participant_tube_registration does not exist)");
    }
  }

  //
  // Methods
  //

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  /**
   * Indicates whether the specified table exists.
   * 
   * @param tableName the table name
   * @return <code>true</code> if the table exists
   */
  private boolean isTableExists(final String tableName) {
    boolean tablePresent = false;

    try {
      tablePresent = (Boolean) JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new DatabaseMetaDataCallback() {
        public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
          return dbmd.getTables(null, null, tableName, null).next();
        }
      });
    } catch(MetaDataAccessException ex) {
      throw new RuntimeException(ex);
    }

    return tablePresent;
  }
}
