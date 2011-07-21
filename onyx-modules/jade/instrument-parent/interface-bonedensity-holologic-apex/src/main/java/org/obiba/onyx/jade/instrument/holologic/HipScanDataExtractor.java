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
import org.obiba.onyx.util.data.DataBuilder;
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
      data.put(getResultPrefix() + "_TROCH_AREA", DataBuilder.buildDecimal(rs.getDouble("TROCH_AREA")));
      data.put(getResultPrefix() + "_TROCH_BMC", DataBuilder.buildDecimal(rs.getDouble("TROCH_BMC")));
      data.put(getResultPrefix() + "_TROCH_BMD", DataBuilder.buildDecimal(rs.getDouble("TROCH_BMD")));
      data.put(getResultPrefix() + "_INTER_AREA", DataBuilder.buildDecimal(rs.getDouble("INTER_AREA")));
      data.put(getResultPrefix() + "_INTER_BMC", DataBuilder.buildDecimal(rs.getDouble("INTER_BMC")));
      data.put(getResultPrefix() + "_INTER_BMD", DataBuilder.buildDecimal(rs.getDouble("INTER_BMD")));
      data.put(getResultPrefix() + "_NECK_AREA", DataBuilder.buildDecimal(rs.getDouble("NECK_AREA")));
      data.put(getResultPrefix() + "_NECK_BMC", DataBuilder.buildDecimal(rs.getDouble("NECK_BMC")));
      data.put(getResultPrefix() + "_NECK_BMD", DataBuilder.buildDecimal(rs.getDouble("NECK_BMD")));
      data.put(getResultPrefix() + "_WARDS_AREA", DataBuilder.buildDecimal(rs.getDouble("WARDS_AREA")));
      data.put(getResultPrefix() + "_WARDS_BMC", DataBuilder.buildDecimal(rs.getDouble("WARDS_BMC")));
      data.put(getResultPrefix() + "_WARDS_BMD", DataBuilder.buildDecimal(rs.getDouble("WARDS_BMD")));
      data.put(getResultPrefix() + "_HTOT_AREA", DataBuilder.buildDecimal(rs.getDouble("HTOT_AREA")));
      data.put(getResultPrefix() + "_HTOT_BMC", DataBuilder.buildDecimal(rs.getDouble("HTOT_BMC")));
      data.put(getResultPrefix() + "_HTOT_BMD", DataBuilder.buildDecimal(rs.getDouble("HTOT_BMD")));
      data.put(getResultPrefix() + "_HSTD_TOT", DataBuilder.buildDecimal(rs.getDouble("HSTD_TOT")));
      data.put(getResultPrefix() + "_ROI_TYPE", DataBuilder.buildDecimal(rs.getDouble("ROI_TYPE")));
      data.put(getResultPrefix() + "_ROI_WIDTH", DataBuilder.buildDecimal(rs.getDouble("ROI_WIDTH")));
      data.put(getResultPrefix() + "_ROI_HEIGHT", DataBuilder.buildDecimal(rs.getDouble("ROI_HEIGHT")));
      data.put(getResultPrefix() + "_AXIS_LENGTH", DataBuilder.buildDecimal(rs.getDouble("AXIS_LENGTH")));
    }
  }

  private final class HipHSAResultSetExtractor extends APEXResultSetExtractor {

    public HipHSAResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void extractDataImpl(ResultSet rs) throws SQLException, DataAccessException {
      data.put(getResultPrefix() + "_NN_BMD", DataBuilder.buildDecimal(rs.getDouble("NN_BMD")));
      data.put(getResultPrefix() + "_NN_CSA", DataBuilder.buildDecimal(rs.getDouble("NN_CSA")));
      data.put(getResultPrefix() + "_NN_CSMI", DataBuilder.buildDecimal(rs.getDouble("NN_CSMI")));
      data.put(getResultPrefix() + "_NN_WIDTH", DataBuilder.buildDecimal(rs.getDouble("NN_WIDTH")));
      data.put(getResultPrefix() + "_NN_ED", DataBuilder.buildDecimal(rs.getDouble("NN_ED")));
      data.put(getResultPrefix() + "_NN_ACT", DataBuilder.buildDecimal(rs.getDouble("NN_ACT")));
      data.put(getResultPrefix() + "_NN_PCD", DataBuilder.buildDecimal(rs.getDouble("NN_PCD")));
      data.put(getResultPrefix() + "_NN_CMP", DataBuilder.buildDecimal(rs.getDouble("NN_CMP")));
      data.put(getResultPrefix() + "_NN_SECT_MOD", DataBuilder.buildDecimal(rs.getDouble("NN_SECT_MOD")));
      data.put(getResultPrefix() + "_NN_BR", DataBuilder.buildDecimal(rs.getDouble("NN_BR")));
      data.put(getResultPrefix() + "_IT_BMD", DataBuilder.buildDecimal(rs.getDouble("IT_BMD")));
      data.put(getResultPrefix() + "_IT_CSA", DataBuilder.buildDecimal(rs.getDouble("IT_CSA")));
      data.put(getResultPrefix() + "_IT_CSMI", DataBuilder.buildDecimal(rs.getDouble("IT_CSMI")));
      data.put(getResultPrefix() + "_IT_WIDTH", DataBuilder.buildDecimal(rs.getDouble("IT_WIDTH")));
      data.put(getResultPrefix() + "_IT_ED", DataBuilder.buildDecimal(rs.getDouble("IT_ED")));
      data.put(getResultPrefix() + "_IT_ACT", DataBuilder.buildDecimal(rs.getDouble("IT_ACT")));
      data.put(getResultPrefix() + "_IT_PCD", DataBuilder.buildDecimal(rs.getDouble("IT_PCD")));
      data.put(getResultPrefix() + "_IT_CMP", DataBuilder.buildDecimal(rs.getDouble("IT_CMP")));
      data.put(getResultPrefix() + "_IT_SECT_MOD", DataBuilder.buildDecimal(rs.getDouble("IT_SECT_MOD")));
      data.put(getResultPrefix() + "_IT_BR", DataBuilder.buildDecimal(rs.getDouble("IT_BR")));
      data.put(getResultPrefix() + "_FS_BMD", DataBuilder.buildDecimal(rs.getDouble("FS_BMD")));
      data.put(getResultPrefix() + "_FS_CSA", DataBuilder.buildDecimal(rs.getDouble("FS_CSA")));
      data.put(getResultPrefix() + "_FS_CSMI", DataBuilder.buildDecimal(rs.getDouble("FS_CSMI")));
      data.put(getResultPrefix() + "_FS_WIDTH", DataBuilder.buildDecimal(rs.getDouble("FS_WIDTH")));
      data.put(getResultPrefix() + "_FS_ED", DataBuilder.buildDecimal(rs.getDouble("FS_ED")));
      data.put(getResultPrefix() + "_FS_ACT", DataBuilder.buildDecimal(rs.getDouble("FS_ACT")));
      data.put(getResultPrefix() + "_FS_PCD", DataBuilder.buildDecimal(rs.getDouble("FS_PCD")));
      data.put(getResultPrefix() + "_FS_CMP", DataBuilder.buildDecimal(rs.getDouble("FS_CMP")));
      data.put(getResultPrefix() + "_FS_SECT_MOD", DataBuilder.buildDecimal(rs.getDouble("FS_SECT_MOD")));
      data.put(getResultPrefix() + "_FS_BR", DataBuilder.buildDecimal(rs.getDouble("FS_BR")));
      data.put(getResultPrefix() + "_SHAFT_NECK_ANGLE", DataBuilder.buildDecimal(rs.getDouble("SHAFT_NECK_ANGLE")));
    }
  }

  public enum Side {
    LEFT, RIGHT
  }

}
