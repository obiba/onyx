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
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * If the "action_definition_code" column of the "action" table contains any null fields, set them to "action." plus the
 * content of the "type" field. The column "action_definition_code" is a new -- since it is non null, we are populating
 * it with values.
 */
public class DataUpgrade_1_5_4 extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(DataUpgrade_1_5_4.class);

  private static final String IDS_WITH_NULL_ACTION_DEFINITION_CODE = "SELECT id FROM action WHERE action_definition_code IS null";

  private static final String UPDATE_ACTION_DEFINITION_CODE = "UPDATE action SET action_definition_code = CONCAT('action.', action.type) WHERE id = :id;";

  private NamedParameterJdbcTemplate jdbcTemplate;

  @SuppressWarnings("unchecked")
  public void execute(Version currentVersion) {
    int numberOfUpdates = 0;
    Map<String, Long> map = new HashMap<String, Long>();
    List<Map<String, Long>> actionIdsWithNullActionDefinitionCode = jdbcTemplate.queryForList(IDS_WITH_NULL_ACTION_DEFINITION_CODE, map);
    numberOfUpdates += runSqlOnId(UPDATE_ACTION_DEFINITION_CODE, actionIdsWithNullActionDefinitionCode);
    log.info("[{}] records updated.", numberOfUpdates);
  }

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  private int runSqlOnId(String sqlCommand, List<Map<String, Long>> idMaps) {
    int numberOfUpdates = 0;
    for(Map<String, Long> idMap : idMaps) {
      numberOfUpdates += jdbcTemplate.update(sqlCommand, idMap);
    }
    return numberOfUpdates;
  }

  @SuppressWarnings("unused")
  private void printListOfMaps(List<Map<String, Long>> listOfMaps) {
    int index = 0;
    for(Map<String, Long> m : listOfMaps) {
      for(Map.Entry<String, Long> entry : m.entrySet()) {
        System.out.println(index++ + ". [" + entry.getKey() + " : " + entry.getValue() + "]");
      }
    }
  }
}
