/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.runtime.upgrade;

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
 * Drops the conclusion table previously used by the Mica module. This upgrade step exists in the Jade module since the
 * Mica module has been removed in Onyx 1.5.
 */
public class SchemaUpgrade_1_5_0 extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(SchemaUpgrade_1_5_0.class);

  private static final String CONCLUSION_TABLE = "conclusion";

  private static final String BACKUP_CONCLUSION_TABLE = "CREATE TABLE IF NOT EXISTS TMP_CONCLUSION_BACKUP SELECT * from conclusion;";

  private static final String DROP_CONCLUSION_TABLE = "DROP TABLE IF EXISTS conclusion;";

  private static final String INSTRUMENT_RUN_TABLE = "instrument_run";

  private static final String ADD_SKIP_MEASUREMENT_COLUMN = "ALTER TABLE instrument_run ADD COLUMN skip_masurement bit(1) DEFAULT NULL;";

  private static final String ADD_SKIP_COMMENT_COLUMN = "ALTER TABLE instrument_run ADD COLUMN skip_comment varchar(2000) DEFAULT NULL;";

  private JdbcTemplate jdbcTemplate;

  public void execute(Version currentVersion) {
    if(isTableExists(CONCLUSION_TABLE)) {
      jdbcTemplate.execute(BACKUP_CONCLUSION_TABLE);
      log.info("Backed up contents of [conclusion] table to [TMP_CONCLUSION_BACKUP] table.");
      jdbcTemplate.execute(DROP_CONCLUSION_TABLE);
      log.info("Removed [conclusion] table. The [conclusion] table is no longer required, since the Mica module has been removed.");
    } else {
      log.info("Skipping the removal of the [conclusion] table. The [conclusion] table does not exist.");
    }

    if(isTableExists(INSTRUMENT_RUN_TABLE)) {
      jdbcTemplate.execute(ADD_SKIP_MEASUREMENT_COLUMN);
      jdbcTemplate.execute(ADD_SKIP_COMMENT_COLUMN);
    } else {
      log.info("skip_masurement and skip_comment columns not added (table instrument_run does not exist)");
    }
  }

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