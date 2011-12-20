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

import org.dcm4che2.tool.dcmrcv.DicomServer;
import org.obiba.onyx.jade.instrument.holologic.APEXInstrumentRunner.Side;
import org.obiba.onyx.util.data.Data;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 */
public class WholeBodyScanDataExtractor extends APEXScanDataExtractor {

  protected WholeBodyScanDataExtractor(JdbcTemplate patScanDb, File scanDataDir, String participantKey, DicomServer server) {
    super(patScanDb, scanDataDir, participantKey, server);
  }

  @Override
  public String getName() {
    return "WB";
  }

  @Override
  public String getDicomBodyPartName() {
    return null;
  }

  @Override
  protected long getScanType() {
    return 5l;
  }

  @Override
  protected void extractDataImpl(Map<String, Data> data) {
    extractScanData("Wbody", data, new WbodyResultSetExtractor(data));
    extractScanData("WbodyComposition", data, new WbodyCompositionResultSetExtractor(data));
    extractScanData("SubRegionBone", data, new SubRegionBoneResultSetExtractor(data));
    extractScanData("SubRegionComposition", data, new SubRegionCompositionResultSetExtractor(data));
    extractScanData("ObesityIndices", data, new ObesityIndicesResultSetExtractor(data));
    extractScanData("AndroidGynoidComposition", data, new AndroidGynoidCompositionResultSetExtractor(data));
  }

  private final class WbodyResultSetExtractor extends ResultSetDataExtractor {

    public WbodyResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void putData() throws SQLException, DataAccessException {
      putDouble("WBTOT_AREA");
      putDouble("WBTOT_BMC");
      putDouble("WBTOT_BMD");
      putDouble("SUBTOT_AREA");
      putDouble("SUBTOT_BMC");
      putDouble("SUBTOT_BMD");
      putDouble("HEAD_AREA");
      putDouble("HEAD_BMC");
      putDouble("HEAD_BMD");
      putDouble("LARM_AREA");
      putDouble("LARM_BMC");
      putDouble("LARM_BMD");
      putDouble("RARM_AREA");
      putDouble("RARM_BMC");
      putDouble("RARM_BMD");
      putDouble("LRIB_AREA");
      putDouble("LRIB_BMC");
      putDouble("LRIB_BMD");
      putDouble("RRIB_AREA");
      putDouble("RRIB_BMC");
      putDouble("RRIB_BMD");
      putDouble("T_S_AREA");
      putDouble("T_S_BMC");
      putDouble("T_S_BMD");
      putDouble("L_S_AREA");
      putDouble("L_S_BMC");
      putDouble("L_S_BMD");
      putDouble("PELV_AREA");
      putDouble("PELV_BMC");
      putDouble("PELV_BMD");
      putDouble("LLEG_AREA");
      putDouble("LLEG_BMC");
      putDouble("LLEG_BMD");
      putDouble("RLEG_AREA");
      putDouble("RLEG_BMC");
      putDouble("RLEG_BMD");
      putString("PHYSICIAN_COMMENT");
    }

    @Override
    protected String getVariableName(String name) {
      if(name.equals("PHYSICIAN_COMMENT")) {
        return super.getVariableName("WB_" + name);
      }
      return super.getVariableName(name);
    }
  }

  private final class WbodyCompositionResultSetExtractor extends ResultSetDataExtractor {

    public WbodyCompositionResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void putData() throws SQLException, DataAccessException {
      putDouble("FAT_STD");
      putDouble("LEAN_STD");
      putDouble("BRAIN_FAT");
      putDouble("WATER_LBM");
      putDouble("HEAD_FAT");
      putDouble("HEAD_LEAN");
      putDouble("HEAD_MASS");
      putDouble("HEAD_PFAT");
      putDouble("LARM_FAT");
      putDouble("LARM_LEAN");
      putDouble("LARM_MASS");
      putDouble("LARM_PFAT");
      putDouble("RARM_FAT");
      putDouble("RARM_LEAN");
      putDouble("RARM_MASS");
      putDouble("RARM_PFAT");
      putDouble("TRUNK_FAT");
      putDouble("TRUNK_LEAN");
      putDouble("TRUNK_MASS");
      putDouble("TRUNK_PFAT");
      putDouble("L_LEG_FAT");
      putDouble("L_LEG_LEAN");
      putDouble("L_LEG_MASS");
      putDouble("L_LEG_PFAT");
      putDouble("R_LEG_FAT");
      putDouble("R_LEG_LEAN");
      putDouble("R_LEG_MASS");
      putDouble("R_LEG_PFAT");
      putDouble("SUBTOT_FAT");
      putDouble("SUBTOT_LEAN");
      putDouble("SUBTOT_MASS");
      putDouble("SUBTOT_PFAT");
      putDouble("WBTOT_FAT");
      putDouble("WBTOT_LEAN");
      putDouble("WBTOT_MASS");
      putDouble("WBTOT_PFAT");
      putString("PHYSICIAN_COMMENT");
    }

    @Override
    protected String getVariableName(String name) {
      if(name.equals("PHYSICIAN_COMMENT") || name.equals("FAT_STD") || name.equals("LEAN_STD") || name.equals("BRAIN_FAT") || name.equals("WATER_LBM")) {
        return super.getVariableName("WBC_" + name);
      }
      return super.getVariableName(name);
    }
  }

  private final class SubRegionBoneResultSetExtractor extends ResultSetDataExtractor {

    public SubRegionBoneResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void putData() throws SQLException, DataAccessException {
      putDouble("NET_AVG_AREA");
      putDouble("NET_AVG_BMC");
      putDouble("NET_AVG_BMD");
      putDouble("GLOBAL_AREA");
      putDouble("GLOBAL_BMC");
      putDouble("GLOBAL_BMD");

      putLong("NO_REGIONS");

      for(int i = 1; i <= 14; i++) {
        putString("REG" + i + "_NAME");
        putDouble("REG" + i + "_AREA");
        putDouble("REG" + i + "_BMC");
        putDouble("REG" + i + "_BMD");
      }

      putString("PHYSICIAN_COMMENT");
    }

    @Override
    protected String getVariableName(String name) {
      if(name.equals("PHYSICIAN_COMMENT") || name.equals("NO_REGIONS") || name.startsWith("REG")) {
        return super.getVariableName("SRB_" + name);
      }
      return super.getVariableName(name);
    }
  }

  private final class SubRegionCompositionResultSetExtractor extends ResultSetDataExtractor {

    public SubRegionCompositionResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void putData() throws SQLException, DataAccessException {
      putDouble("NET_AVG_FAT");
      putDouble("NET_AVG_LEAN");
      putDouble("NET_AVG_MASS");
      putDouble("NET_AVG_PFAT");
      putDouble("GLOBAL_FAT");
      putDouble("GLOBAL_LEAN");
      putDouble("GLOBAL_MASS");
      putDouble("GLOBAL_PFAT");

      putLong("NO_REGIONS");

      for(int i = 1; i <= 14; i++) {
        putString("REG" + i + "_NAME");
        putDouble("REG" + i + "_FAT");
        putDouble("REG" + i + "_LEAN");
        putDouble("REG" + i + "_MASS");
        putDouble("REG" + i + "_PFAT");
      }

      putInt("TISSUE_ANALYSIS_METHOD");

      putString("PHYSICIAN_COMMENT");
    }

    @Override
    protected String getVariableName(String name) {
      if(name.equals("PHYSICIAN_COMMENT") || name.equals("NO_REGIONS") || name.startsWith("REG")) {
        return super.getVariableName("SRC_" + name);
      }
      return super.getVariableName(name);
    }
  }

  private final class ObesityIndicesResultSetExtractor extends ResultSetDataExtractor {

    public ObesityIndicesResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void putData() throws SQLException, DataAccessException {
      putDouble("FAT_STD");
      putDouble("LEAN_STD");
      putDouble("BRAIN_FAT");
      putDouble("WATER_LBM");
      putDouble("TOTAL_PERCENT_FAT");
      putDouble("BODY_MASS_INDEX");
      putDouble("ANDROID_GYNOID_RATIO");
      putDouble("ANDROID_PERCENT_FAT");
      putDouble("GYNOID_PERCENT_FAT");
      putDouble("FAT_MASS_RATIO");
      putDouble("TRUNK_LIMB_FAT_MASS_RATIO");
      putDouble("FAT_MASS_HEIGHT_SQUARED");
      putDouble("TOTAL_FAT_MASS");
      putDouble("LEAN_MASS_HEIGHT_SQUARED");
      putDouble("APPENDAGE_LEAN_MASS_HEIGHT_2");
      putDouble("TOTAL_LEAN_MASS");
      putString("PHYSICIAN_COMMENT");
    }

    @Override
    protected String getVariableName(String name) {
      if(name.equals("PHYSICIAN_COMMENT") || name.equals("FAT_STD") || name.equals("LEAN_STD") || name.equals("BRAIN_FAT") || name.equals("WATER_LBM")) {
        return super.getVariableName("OI_" + name);
      }
      return super.getVariableName(name);
    }

  }

  private final class AndroidGynoidCompositionResultSetExtractor extends ResultSetDataExtractor {

    public AndroidGynoidCompositionResultSetExtractor(Map<String, Data> data) {
      super(data);
    }

    @Override
    protected void putData() throws SQLException, DataAccessException {
      putDouble("ANDROID_FAT");
      putDouble("ANDROID_LEAN");
      putDouble("GYNOID_FAT");
      putDouble("GYNOID_LEAN");
      putString("PHYSICIAN_COMMENT");
    }

    @Override
    protected String getVariableName(String name) {
      if(name.equals("PHYSICIAN_COMMENT")) {
        return super.getVariableName("AGC_" + name);
      }
      return super.getVariableName(name);
    }
  }

  @Override
  public Side getSide() {
    return null;
  }

}
