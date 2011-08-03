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

import org.obiba.onyx.core.domain.user.Role;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.collect.ImmutableMap;

/**
 * Add the QUESTIONNAIRE_EDITOR role if it doesn't exist.
 */
public class Upgrade_1_8_0 extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(Upgrade_1_8_0.class);

  private static final String ROLE_EXISTS = "SELECT count(*) FROM role WHERE name = :role";

  private static final String INSERT_ROLE = "INSERT INTO role(name) VALUES(:role)";

  private NamedParameterJdbcTemplate jdbcTemplate;

  public void execute(Version currentVersion) {
    log.info("Creating role {} if it doesn't exist.", Role.QUESTIONNAIRE_EDITOR.getName());
    int roles = jdbcTemplate.queryForInt(ROLE_EXISTS, ImmutableMap.of("role", Role.QUESTIONNAIRE_EDITOR.getName()));
    if(roles < 1) {
      log.info("Inserting {} role", Role.QUESTIONNAIRE_EDITOR.getName());
      jdbcTemplate.update(INSERT_ROLE, ImmutableMap.of("role", Role.QUESTIONNAIRE_EDITOR.getName()));
    } else {
      log.info("Role {} exists.", Role.QUESTIONNAIRE_EDITOR.getName());
    }
  }

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

}