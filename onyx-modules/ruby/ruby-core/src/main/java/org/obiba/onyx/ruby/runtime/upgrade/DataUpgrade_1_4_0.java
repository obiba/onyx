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

import javax.sql.DataSource;

import org.obiba.onyx.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.Version;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 
 */
public class DataUpgrade_1_4_0 extends AbstractUpgradeStep {
  //
  // Constants
  //

  private static final String CREATE_TEMP_TABLE = "CREATE TEMPORARY TABLE participant_tube_registration_tmp LIKE participant_tube_registration";

  private static final String INSERT_PTR_INTO_TEMP_TABLE = "INSERT INTO participant_tube_registration_tmp SELECT * FROM participant_tube_registration";

  //
  // Instance Variables
  //

  private NamedParameterJdbcTemplate jdbcTemplate;

  //
  // AbstractUpgradeStep Methods
  //

  public void execute(Version currentVersion) {
    jdbcTemplate.getJdbcOperations().execute(CREATE_TEMP_TABLE);
    jdbcTemplate.getJdbcOperations().execute(INSERT_PTR_INTO_TEMP_TABLE);
  }

  //
  // Methods
  //

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }
}
