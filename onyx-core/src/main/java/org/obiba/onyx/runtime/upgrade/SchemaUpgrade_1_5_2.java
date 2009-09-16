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

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.upgrade.support.DatabaseMetadataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Upgrade step for ONYX-777 (version 1.5.0).
 * 
 * Adds the action_definition_code column to the action table (if present).
 */
public class SchemaUpgrade_1_5_2 extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(SchemaUpgrade_1_5_2.class);

  private static final String ACTION_TABLE = "action";

  private static final String ACTIONE_DEFINITION_CODE_COLUMN = "action_definition_code";

  private static final String ADD_ACTIONE_DEFINITION_CODE_COLUMN = "ALTER TABLE action ADD COLUMN action_definition_code VARCHAR(255) NOT NULL;";

  private static final String ALTER_TYPE_TO_NOT_NULL = "ALTER TABLE action CHANGE type type VARCHAR(255) NOT NULL;";

  private static final String ALTER_ACTION_DEFINITION_CODE_TO_NOT_NULL = "ALTER TABLE action CHANGE action_definition_code action_definition_code VARCHAR(255) NOT NULL;";

  private JdbcTemplate jdbcTemplate;

  public void execute(Version currentVersion) {
    DatabaseMetadataUtil dbMetadataUtil = new DatabaseMetadataUtil(jdbcTemplate.getDataSource());
    if(dbMetadataUtil.isTableExists(ACTION_TABLE)) {
      if(!dbMetadataUtil.hasColumn(ACTION_TABLE, ACTIONE_DEFINITION_CODE_COLUMN)) {
        jdbcTemplate.execute(ADD_ACTIONE_DEFINITION_CODE_COLUMN);
      } else {
        log.info("action_definition_code column not added (action table has this column already)");
      }
      jdbcTemplate.execute(ALTER_TYPE_TO_NOT_NULL);
      jdbcTemplate.execute(ALTER_ACTION_DEFINITION_CODE_TO_NOT_NULL);
    } else {
      log.info("action_definition_code column not added (action table does not exist)");
    }
  }

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

}
