/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.holologic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.obiba.onyx.util.data.Data;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Hip (left or right) data are to extracted from Hip and HipHSA tables.
 */
public class HipScanDataExtractor extends APEXScanDataExtractor {

  private Side side;

  /**
   * @param patScanDb
   * @param participantKey
   */
  protected HipScanDataExtractor(JdbcTemplate patScanDb, String participantKey, Side side) {
    super(patScanDb, participantKey);
    this.side = side;
  }

  @Override
  public String getName() {
    switch(side) {
    case LEFT:
      return "L_HIP";
    default:
      return "R_HIP";
    }
  }

  @Override
  protected Map<String, Data> extractDataImpl(Map<String, Data> data) {
    extractScanData("Hip", data, new HipResultSetExtractor(data));
    extractScanData("HipHSA", data, new HipHSAResultSetExtractor(data));
    return data;
  }

  @Override
  protected long getScanType() {
    switch(side) {
    case LEFT:
      return 2l;
    default:
      return 3l;
    }
  }

  private final class HipResultSetExtractor extends APEXResultSetExtractor {

    public HipResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void extractDataImpl(ResultSet rs) throws SQLException, DataAccessException {
      putDouble(rs, "TROCH_AREA");
      putDouble(rs, "TROCH_BMC");
      putDouble(rs, "TROCH_BMD");
      putDouble(rs, "INTER_AREA");
      putDouble(rs, "INTER_BMC");
      putDouble(rs, "INTER_BMD");
      putDouble(rs, "NECK_AREA");
      putDouble(rs, "NECK_BMC");
      putDouble(rs, "NECK_BMD");
      putDouble(rs, "WARDS_AREA");
      putDouble(rs, "WARDS_BMC");
      putDouble(rs, "WARDS_BMD");
      putDouble(rs, "HTOT_AREA");
      putDouble(rs, "HTOT_BMC");
      putDouble(rs, "HTOT_BMD");
      putDouble(rs, "HSTD_TOT");
      putDouble(rs, "ROI_TYPE");
      putDouble(rs, "ROI_WIDTH");
      putDouble(rs, "ROI_HEIGHT");
      putDouble(rs, "AXIS_LENGTH");
    }
  }

  private final class HipHSAResultSetExtractor extends APEXResultSetExtractor {

    public HipHSAResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void extractDataImpl(ResultSet rs) throws SQLException, DataAccessException {
      putDouble(rs, "NN_BMD");
      putDouble(rs, "NN_CSA");
      putDouble(rs, "NN_CSMI");
      putDouble(rs, "NN_WIDTH");
      putDouble(rs, "NN_ED");
      putDouble(rs, "NN_ACT");
      putDouble(rs, "NN_PCD");
      putDouble(rs, "NN_CMP");
      putDouble(rs, "NN_SECT_MOD");
      putDouble(rs, "NN_BR");
      putDouble(rs, "IT_BMD");
      putDouble(rs, "IT_CSA");
      putDouble(rs, "IT_CSMI");
      putDouble(rs, "IT_WIDTH");
      putDouble(rs, "IT_ED");
      putDouble(rs, "IT_ACT");
      putDouble(rs, "IT_PCD");
      putDouble(rs, "IT_CMP");
      putDouble(rs, "IT_SECT_MOD");
      putDouble(rs, "IT_BR");
      putDouble(rs, "FS_BMD");
      putDouble(rs, "FS_CSA");
      putDouble(rs, "FS_CSMI");
      putDouble(rs, "FS_WIDTH");
      putDouble(rs, "FS_ED");
      putDouble(rs, "FS_ACT");
      putDouble(rs, "FS_PCD");
      putDouble(rs, "FS_CMP");
      putDouble(rs, "FS_SECT_MOD");
      putDouble(rs, "FS_BR");
      putDouble(rs, "SHAFT_NECK_ANGLE");
    }
  }

  public enum Side {
    LEFT, RIGHT
  }

}
