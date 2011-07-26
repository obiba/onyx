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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 */
public abstract class APEXScanDataExtractor {

  private JdbcTemplate patScanDb;

  private File scanDataDir;

  private String participantKey;

  private String scanID;

  private String pFileName;

  private String rFileName;

  protected APEXScanDataExtractor(JdbcTemplate patScanDb, File scanDataDir, String participantKey) {
    super();
    this.patScanDb = patScanDb;
    this.scanDataDir = scanDataDir;
    this.participantKey = participantKey;
  }

  public Map<String, Data> extractData() {
    Map<String, Data> data = extractScanAnalysisData();
    if(scanID != null) {
      extractDataImpl(data);
    }

    return data;
  }

  protected String getPFileName() {
    return pFileName;
  }

  protected String getRFileName() {
    return rFileName;
  }

  public abstract String getName();

  protected abstract long getScanType();

  protected abstract void extractDataImpl(Map<String, Data> data);

  protected JdbcTemplate getPatScanDb() {
    return patScanDb;
  }

  protected String getParticipantKey() {
    return participantKey;
  }

  private Map<String, Data> extractScanAnalysisData() {
    return patScanDb.query("select SCANID, PFILE_NAME from ScanAnalysis where PARTICIPANT_KEY = ? and SCAN_TYPE = ?", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, getParticipantKey());
        ps.setLong(2, getScanType());
      }
    }, new ScanAnalysisResultSetExtractor());
  }

  protected String getResultPrefix() {
    return getName();
  }

  protected String getScanID() {
    return scanID;
  }

  private final class ScanAnalysisResultSetExtractor implements ResultSetExtractor<Map<String, Data>> {
    @Override
    public Map<String, Data> extractData(ResultSet rs) throws SQLException, DataAccessException {
      Map<String, Data> data = new HashMap<String, Data>();

      // assume there is only one scan of the given type for the participant
      if(rs.next()) {
        scanID = rs.getString("SCANID");
        pFileName = rs.getString("PFILE_NAME");
        rFileName = rs.getString("PFILE_NAME").replace(".P", ".R");

        data.put(getResultPrefix() + "_SCANID", DataBuilder.buildText(scanID));

        data.put(getResultPrefix() + "_PFILE_NAME", DataBuilder.buildText(pFileName));
        data.put(getResultPrefix() + "_RFILE_NAME", DataBuilder.buildText(rFileName));

        File rFile = new File(scanDataDir, rFileName);
        if(rFile.exists()) {
          data.put(getResultPrefix() + "_RFILE", DataBuilder.buildBinary(rFile));
        }
      }

      return data;
    }

  }

  protected Map<String, Data> extractScanData(String table, Map<String, Data> data, ResultSetExtractor<Map<String, Data>> rsExtractor) {
    return getPatScanDb().query("select * from " + table + " where PARTICIPANT_KEY = ? and SCANID = ?", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, getParticipantKey());
        ps.setString(2, getScanID());
      }
    }, rsExtractor);
  }

  protected abstract class ResultSetDataExtractor implements ResultSetExtractor<Map<String, Data>> {

    protected Map<String, Data> data;

    protected ResultSet rs;

    public ResultSetDataExtractor(Map<String, Data> data) {
      super();
      this.data = data;
    }

    @Override
    public Map<String, Data> extractData(ResultSet rs) throws SQLException, DataAccessException {
      this.rs = rs;
      if(rs.next()) {
        putData();
      }
      return data;
    }

    protected void putString(String name) throws SQLException {
      put(name, DataBuilder.buildText(rs.getString(name)));
    }

    protected void putNString(String name) throws SQLException {
      put(name, DataBuilder.buildText(rs.getNString(name)));
    }

    protected void putInt(String name) throws SQLException {
      put(name, DataBuilder.buildInteger(rs.getInt(name)));
    }

    protected void putLong(String name) throws SQLException {
      put(name, DataBuilder.buildInteger(rs.getLong(name)));
    }

    protected void putDouble(String name) throws SQLException {
      put(name, DataBuilder.buildDecimal(rs.getDouble(name)));
    }

    protected void put(String name, Data value) {
      String varName = getVariableName(name);
      if(data.keySet().contains(varName)) {
        throw new IllegalArgumentException("Instrument variable name already defined: " + varName);
      }
      data.put(varName, value);
    }

    protected String getVariableName(String name) {
      return getResultPrefix() + "_" + name;
    }

    protected abstract void putData() throws SQLException, DataAccessException;
  }

}
