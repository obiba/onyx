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

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import org.dcm4che2.tool.dcmrcv.DicomServer;
import org.obiba.onyx.jade.instrument.holologic.APEXInstrumentRunner.Side;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class ForearmScanDataExtractor extends APEXScanDataExtractor {

  private Side side;

  public ForearmScanDataExtractor(JdbcTemplate patScanDb, File scanDataDir, String pFileName, Side side, DicomServer server, ApexReceiver apexReceiver) {
    super(patScanDb, scanDataDir, pFileName, server, apexReceiver);
    this.side = side;
  }

  @Override
  public String getName() {
    switch(side) {
    case LEFT:
      return "L_FA";
    default:
      return "R_FA";
    }
  }

  @Override
  public String getDicomBodyPartName() {
    return "ARM";
  }

  @Override
  protected long getScanType() {
    switch(side) {
    case LEFT:
      return 6l;
    default:
      return 7l;
    }
  }

  @Override
  protected void extractDataImpl(Map<String, Data> data) {
    data.put(getResultPrefix() + "_SIDE", DataBuilder.buildText(side.toString()));
    extractScanData("Forearm", data, new ForearmResultSetExtractor(data));
  }

  private class ForearmResultSetExtractor extends ResultSetDataExtractor {
    public ForearmResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void putData() throws SQLException, DataAccessException {
      putDouble("R_13_AREA");
      putDouble("R_13_BMC");
      putDouble("R_13_BMD");
      putDouble("R_MID_AREA");
      putDouble("R_MID_BMC");
      putDouble("R_MID_BMD");
      putDouble("R_UD_AREA");
      putDouble("R_UD_BMC");
      putDouble("R_UD_BMD");
      putDouble("U_13_AREA");
      putDouble("U_13_BMC");
      putDouble("U_13_BMD");
      putDouble("U_MID_AREA");
      putDouble("U_MID_BMC");
      putDouble("U_MID_BMD");
      putDouble("U_UD_AREA");
      putDouble("U_UD_BMC");
      putDouble("U_UD_BMD");
      putDouble("RTOT_AREA");
      putDouble("RTOT_BMC");
      putDouble("RTOT_BMD");
      putDouble("UTOT_AREA");
      putDouble("UTOT_BMC");
      putDouble("UTOT_BMD");
      putDouble("RU13TOT_AREA");
      putDouble("RU13TOT_BMC");
      putDouble("RU13TOT_BMD");
      putDouble("RUMIDTOT_AREA");
      putDouble("RUMIDTOT_BMC");
      putDouble("RUMIDTOT_BMD");
      putDouble("RUUDTOT_AREA");
      putDouble("RUUDTOT_BMC");
      putDouble("RUUDTOT_BMD");
      putDouble("RUTOT_AREA");
      putDouble("RUTOT_BMC");
      putDouble("RUTOT_BMD");
      putDouble("ROI_TYPE");
      putDouble("ROI_WIDTH");
      putDouble("ROI_HEIGHT");
      putDouble("ARM_LENGTH");
      putString("PHYSICIAN_COMMENT");
    }
  }

  @Override
  public Side getSide() {
    return side;
  }

}
