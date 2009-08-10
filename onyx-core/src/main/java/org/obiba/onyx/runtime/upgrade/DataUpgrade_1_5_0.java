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
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 */
public class DataUpgrade_1_5_0 extends AbstractUpgradeStep {
  //
  // Constants
  //

  private static final String UPDATE_ACTION_DEFINITION_CODE = "UPDATE action SET action_definition_code = CONCAT('action.', action.type);";

  //
  // Instance Variables
  //

  private JdbcTemplate jdbcTemplate;

  //
  // AbstractUpgradeStep Methods
  //

  public void execute(Version currentVersion) {
    jdbcTemplate.execute(UPDATE_ACTION_DEFINITION_CODE);
  }

  //
  // Methods
  //

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }
}
