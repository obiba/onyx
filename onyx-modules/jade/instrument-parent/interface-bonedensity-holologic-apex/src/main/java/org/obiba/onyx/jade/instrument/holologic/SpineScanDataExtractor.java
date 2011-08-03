/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.holologic;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import org.obiba.onyx.util.data.Data;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Hip (left or right) data are to extracted from Hip and HipHSA tables.
 */
public class SpineScanDataExtractor extends APEXScanDataExtractor {

  /**
   * @param patScanDb
   * @param participantKey
   */
  protected SpineScanDataExtractor(JdbcTemplate patScanDb, File scanDataDir, String participantKey) {
    super(patScanDb, scanDataDir, participantKey);
  }

  @Override
  public String getName() {
    return "SP";
  }

  @Override
  protected void extractDataImpl(Map<String, Data> data) {
    extractScanData("Spine", data, new SpineResultSetExtractor(data));
  }

  @Override
  protected long getScanType() {
    return 1l;
  }

  private final class SpineResultSetExtractor extends ResultSetDataExtractor {

    public SpineResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void putData() throws SQLException, DataAccessException {
      putLong("NO_REGIONS");
      putLong("STARTING_REGION");

      for(int i = 1; i <= 5; i++) {
        putBoolean("L" + i + "_INCLUDED");
        putDouble("L" + i + "_AREA");
        putDouble("L" + i + "_BMC");
        putDouble("L" + i + "_BMD");
      }

      putDouble("TOT_AREA");
      putDouble("TOT_BMC");
      putDouble("TOT_BMD");
      putDouble("STD_TOT_BMD");
      putLong("ROI_TYPE");
      putDouble("ROI_WIDTH");
      putDouble("ROI_HEIGHT");
      putString("PHYSICIAN_COMMENT");
    }
  }

}
