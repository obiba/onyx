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
 *
 */
public class LateralScanDataExtractor extends APEXScanDataExtractor {

  private Energy energy;

  protected LateralScanDataExtractor(JdbcTemplate patScanDb, File scanDataDir, String participantKey, Energy energy) {
    super(patScanDb, scanDataDir, participantKey);
    this.energy = energy;
  }

  @Override
  public String getName() {
    switch(energy) {
    case SINGLE:
      return "SEL";
    default:
      return "DEL";
    }
  }

  @Override
  protected long getScanType() {
    switch(energy) {
    case SINGLE:
      return 36l;
    default:
      return 37l;
    }
  }

  @Override
  protected void extractDataImpl(Map<String, Data> data) {
    extractScanData("Lateral", data, new LateralResultSetExtractor(data));
  }

  private final class LateralResultSetExtractor extends ResultSetDataExtractor {

    public LateralResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void putData() throws SQLException, DataAccessException {
      for(int i = 1; i <= 5; i++) {
        putDouble("L" + i + "_AREA");
        putDouble("L" + i + "_BMC");
        putDouble("L" + i + "_BMD");
        putDouble("L" + i + "_VBMD");
        putDouble("L" + i + "_WIDTH");
      }
      putDouble("LTOT_AREA");
      putDouble("LTOT_BMC");
      putDouble("LTOT_BMD");
      putDouble("LTOT_VBMD");

      for(int i = 1; i <= 5; i++) {
        putDouble("L" + i + "_MID_AREA");
        putDouble("L" + i + "_MID_BMC");
        putDouble("L" + i + "_MID_BMD");
        putDouble("L" + i + "_MID_VBMD");
      }
      putDouble("MIDTOT_AREA");
      putDouble("MIDTOT_BMC");
      putDouble("MIDTOT_BMD");
      putDouble("MIDTOT_VBMD");

      for(int i = 1; i <= 5; i++) {
        putDouble("L" + i + "_P&A_AREA");
        putDouble("L" + i + "_P&A_BMC");
      }
      putDouble("TOTAL_P&A_AREA");
      putDouble("TOTAL_P&A_BMC");

      putString("PHYSICIAN_COMMENT");
    }

    @Override
    protected String getVariableName(String name) {
      return super.getVariableName(name.replace("&", ""));
    }
  }

  public enum Energy {
    SINGLE, DUAL
  }

}
