/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.runtime.upgrade;

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
 * Upgrade step for ONYX-777 (version 1.5.0).
 * 
 * Adds the action_definition_code column to the action table (if present).
 */
public class SchemaUpgrade_1_5_0 extends AbstractUpgradeStep {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(SchemaUpgrade_1_5_0.class);

  private static final String ACTION_TABLE = "action";

  private static final String ADD_ACTIONE_DEFINITION_CODE_COLUMN = "ALTER TABLE action ADD COLUMN action_definition_code VARCHAR(255) NOT NULL;";

  //
  // Instance Variables
  //

  private JdbcTemplate jdbcTemplate;

  //
  // AbstractUpgradeStep Methods
  //

  public void execute(Version currentVersion) {
    if(isTableExists(ACTION_TABLE)) {
      if () jdbcTemplate.execute(ADD_ACTIONE_DEFINITION_CODE_COLUMN);
    } else {
      log.info("action_definition_code column not added (action table does not exist)");
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
