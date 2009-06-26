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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.onyx.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Remove all but the most recent instrument run per participant per instrument. Prior to 1.4.0 all instrument runs were
 * saved, but only the most recent one was used. From 1.4.0 forward only the most recent instrument run will be saved.
 */
public class DataUpgrade_1_4_0 extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(DataUpgrade_1_4_0.class);

  private static final String SELECT_DISTINCT_INSTRUMENT_RUNS = "select distinct participant_id, instrument_id from instrument_run";

  private static final String IDS_TO_DELETE = "select id from instrument_run where participant_id = :participant_id and instrument_id = :instrument_id and time_start not in (select max(time_start) from instrument_run where participant_id = :participant_id and instrument_id = :instrument_id)";

  private static final String DELETE_FROM_INSTRUMENT_RUN_VALUE = "delete from instrument_run_value where instrument_run_id = :id";

  private static final String DELETE_MEASURE_FROM_INSTRUMENT_RUN_VALUE = "delete from instrument_run_value where measure_id = :id";

  private static final String DELETE_FROM_MEASURE = "delete from measure where instrument_run_id = :id";

  private static final String DELETE_FROM_INSTRUMENT_RUN = "delete from instrument_run where id = :id";

  private static final String MEASURE_IDS_TO_DELETE_FROM_INSTRUMENT_RUN_VALUE = "select id from measure where instrument_run_id = :id";

  private NamedParameterJdbcTemplate jdbcTemplate;

  @SuppressWarnings("unchecked")
  public void execute(Version currentVersion) {
    int numberOfUpdates = 0;

    Map<String, String> map = new HashMap<String, String>();
    map.put("", "");
    List<Map<String, Object>> distinceInstrumentRuns = jdbcTemplate.queryForList(SELECT_DISTINCT_INSTRUMENT_RUNS, map);

    List<Map<String, Long>> instrumentRunIdsToDelete = getInstrumentRunIdsToDelete(distinceInstrumentRuns);
    List<Map<String, Long>> measureIdsToDelete = getInstrumentRunValueIdsToDelete(instrumentRunIdsToDelete);

    numberOfUpdates += runSqlOnId(DELETE_FROM_INSTRUMENT_RUN_VALUE, instrumentRunIdsToDelete);
    numberOfUpdates += runSqlOnId(DELETE_MEASURE_FROM_INSTRUMENT_RUN_VALUE, measureIdsToDelete);
    numberOfUpdates += runSqlOnId(DELETE_FROM_MEASURE, instrumentRunIdsToDelete);
    numberOfUpdates += runSqlOnId(DELETE_FROM_INSTRUMENT_RUN, instrumentRunIdsToDelete);

    log.info("[{}] records updated.", numberOfUpdates);
  }

  private int runSqlOnId(String sqlCommand, List<Map<String, Long>> idMaps) {
    int numberOfUpdates = 0;
    for(Map<String, Long> idMap : idMaps) {
      numberOfUpdates += jdbcTemplate.update(sqlCommand, idMap);
    }
    return numberOfUpdates;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Long>> getInstrumentRunIdsToDelete(List<Map<String, Object>> distinceInstrumentRuns) {
    List<Map<String, Long>> result = new ArrayList<Map<String, Long>>();
    for(Map<String, Object> distinctInstrumentRunMap : distinceInstrumentRuns) {
      List<Map<String, Long>> idsToDelete = jdbcTemplate.queryForList(IDS_TO_DELETE, distinctInstrumentRunMap);
      result.addAll(idsToDelete);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Long>> getInstrumentRunValueIdsToDelete(List<Map<String, Long>> instrumentRunIdsToDelete) {
    List<Map<String, Long>> result = new ArrayList<Map<String, Long>>();
    for(Map<String, Long> instrumentRunIdToDelete : instrumentRunIdsToDelete) {
      List<Map<String, Long>> idsToDelete = jdbcTemplate.queryForList(MEASURE_IDS_TO_DELETE_FROM_INSTRUMENT_RUN_VALUE, instrumentRunIdToDelete);
      result.addAll(idsToDelete);
    }
    return result;
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

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

}