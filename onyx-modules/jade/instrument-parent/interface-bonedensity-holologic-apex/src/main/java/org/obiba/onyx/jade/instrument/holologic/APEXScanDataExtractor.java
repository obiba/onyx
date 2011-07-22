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

  private String participantKey;

  private String scanID;

  private String pFileName;

  private String rFileName;

  protected APEXScanDataExtractor(JdbcTemplate patScanDb, String participantKey) {
    super();
    this.patScanDb = patScanDb;
    this.participantKey = participantKey;
  }

  public Map<String, Data> extractData() {
    Map<String, Data> data = extractScanAnalysisData();
    if(scanID != null) {
      return extractDataImpl(data);
    } else {
      return data;
    }
  }

  protected String getPFileName() {
    return pFileName;
  }

  protected String getRFileName() {
    return rFileName;
  }

  public abstract String getName();

  protected abstract long getScanType();

  protected abstract Map<String, Data> extractDataImpl(Map<String, Data> data);

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
    return "RES_" + getName();
  }

  protected String getScanID() {
    return scanID;
  }

  private final class ScanAnalysisResultSetExtractor implements ResultSetExtractor<Map<String, Data>> {
    @Override
    public Map<String, Data> extractData(ResultSet rs) throws SQLException, DataAccessException {
      Map<String, Data> data = new HashMap<String, Data>();

      // assume there is only one
      if(rs.next()) {
        scanID = rs.getString("SCANID");
        pFileName = rs.getString("PFILE_NAME");
        rFileName = rs.getString("PFILE_NAME").replace(".P", ".R");

        data.put(getResultPrefix() + "_SCANID", DataBuilder.buildText(scanID));
        data.put(getResultPrefix() + "_PFILE_NAME", DataBuilder.buildText(pFileName));
        data.put(getResultPrefix() + "_RFILE_NAME", DataBuilder.buildText(rFileName));
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

  protected abstract class APEXResultSetExtractor implements ResultSetExtractor<Map<String, Data>> {

    protected Map<String, Data> data;

    protected ResultSet rs;

    public APEXResultSetExtractor(Map<String, Data> data) {
      super();
      this.data = data;
    }

    @Override
    public Map<String, Data> extractData(ResultSet rs) throws SQLException, DataAccessException {
      if(rs.next()) {
        extractDataImpl(rs);
      }
      return data;
    }

    protected void putDouble(ResultSet rs, String name) throws SQLException {
      put(name, rs.getDouble(name));
    }

    protected void put(String name, double value) {
      put(name, DataBuilder.buildDecimal(value));
    }

    protected void put(String name, Data value) {
      data.put(getResultPrefix() + "_" + name, value);
    }

    protected abstract void extractDataImpl(ResultSet rs) throws SQLException, DataAccessException;
  }

}
