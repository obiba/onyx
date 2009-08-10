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

import javax.sql.DataSource;

import org.obiba.onyx.runtime.upgrade.DatabaseChecker;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

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

  private static final String SKIP_MEASUREMENT_COLUMN = "skip_measurement";

  private static final String SKIP_COMMENT_COLUMN = "skip_comment";

  private static final String ADD_SKIP_MEASUREMENT_COLUMN = "ALTER TABLE instrument_run ADD COLUMN skip_measurement bit(1) DEFAULT NULL;";

  private static final String ADD_SKIP_COMMENT_COLUMN = "ALTER TABLE instrument_run ADD COLUMN skip_comment varchar(2000) DEFAULT NULL;";

  private JdbcTemplate jdbcTemplate;

  public void execute(Version currentVersion) {
    DatabaseChecker databaseChecker = new DatabaseChecker(jdbcTemplate);

    if(databaseChecker.isTableExists(CONCLUSION_TABLE)) {
      jdbcTemplate.execute(BACKUP_CONCLUSION_TABLE);
      log.info("Backed up contents of [conclusion] table to [TMP_CONCLUSION_BACKUP] table.");
      jdbcTemplate.execute(DROP_CONCLUSION_TABLE);
      log.info("Removed [conclusion] table. The [conclusion] table is no longer required, since the Mica module has been removed.");
    } else {
      log.info("Skipping the removal of the [conclusion] table. The [conclusion] table does not exist.");
    }

    if(databaseChecker.isTableExists(INSTRUMENT_RUN_TABLE)) {
      if(!databaseChecker.hasColumn(INSTRUMENT_RUN_TABLE, SKIP_MEASUREMENT_COLUMN)) jdbcTemplate.execute(ADD_SKIP_MEASUREMENT_COLUMN);
      if(!databaseChecker.hasColumn(INSTRUMENT_RUN_TABLE, SKIP_COMMENT_COLUMN)) jdbcTemplate.execute(ADD_SKIP_COMMENT_COLUMN);
    } else {
      log.info("skip_measurement and skip_comment columns not added (table instrument_run does not exist)");
    }
  }

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }
}