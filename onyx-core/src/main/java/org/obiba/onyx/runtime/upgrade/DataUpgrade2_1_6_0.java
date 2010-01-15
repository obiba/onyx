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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DataUpgrade2_1_6_0 extends AbstractUpgradeStep {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(DataUpgrade2_1_6_0.class);

  private static final String SELECT_BARCODES_OF_EXPORTED_PARTICIPANTS = "SELECT barcode FROM participant WHERE exported = true";

  private static final String INSERT_EXPORT_LOG = "INSERT INTO export_log (type, identifier, destination, user, capture_start_date, capture_end_date, export_date) VALUES ('Participant', :identifier, 'Unknown', 'Unknown', :capture_start_date, :capture_end_date, :export_date)";

  private static final String UPDATE_CANCELED_TO_CANCELLED = "UPDATE instrument_run SET status='CANCELLED' WHERE STATUS='CANCELED'";

  //
  // Instance Variables
  //

  private NamedParameterJdbcTemplate jdbcTemplate;

  //
  // AbstractUpgradeStep Methods
  //

  public void execute(Version currentVersion) {
    createExportLogs();
    changeCanceledToCancelled();
  }

  //
  // Methods
  //

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  @SuppressWarnings("unchecked")
  private void createExportLogs() {
    List<String> results = (List<String>) jdbcTemplate.queryForList(SELECT_BARCODES_OF_EXPORTED_PARTICIPANTS, (Map<String, Object>) null, String.class);
    log.info("Creating export logs for {} exported participants", results.size());

    for(String barcode : results) {
      Map<String, Object> paramMap = new HashMap<String, Object>();
      Date exportDate = new Date();
      paramMap.put("identifier", barcode);
      paramMap.put("capture_start_date", exportDate);
      paramMap.put("capture_end_date", exportDate);
      paramMap.put("export_date", exportDate);

      jdbcTemplate.update(INSERT_EXPORT_LOG, paramMap);
    }
  }

  private void changeCanceledToCancelled() {
    Map<String, Object> paramMap = new HashMap<String, Object>();
    jdbcTemplate.update(UPDATE_CANCELED_TO_CANCELLED, paramMap);
  }
}
