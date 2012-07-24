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

import java.util.HashMap;

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Add the QUESTIONNAIRE_EDITOR role if it doesn't exist.
 */
public class Upgrade_1_9_0 extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(Upgrade_1_9_0.class);

  private static final String UPDATE_USER_NAME = "UPDATE :table AS t, user AS u SET t.user_name=u.login WHERE t.user_id=u.id;";

  private static final String[] TABLES = { "action", "questionnaire_participant", "instrument_run", "measure", "experimental_condition" };

  private NamedParameterJdbcTemplate jdbcTemplate;

  public void execute(Version currentVersion) {
    log.info("Updating user_name columns.");
    for(String table : TABLES) {
      String sql = UPDATE_USER_NAME.replace(":table", table);
      log.debug("{}", sql);
      jdbcTemplate.update(sql, new HashMap<String, String>());
    }
  }

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

}