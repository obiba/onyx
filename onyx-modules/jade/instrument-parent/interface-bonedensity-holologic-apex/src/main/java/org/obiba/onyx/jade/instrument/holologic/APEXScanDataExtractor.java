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
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.tool.dcmrcv.DicomServer;
import org.dcm4che2.tool.dcmrcv.DicomServer.StoredDicomFile;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public abstract class APEXScanDataExtractor {

  private static final Logger log = LoggerFactory.getLogger(APEXScanDataExtractor.class);

  private JdbcTemplate patScanDb;

  private File scanDataDir;

  private String participantKey;

  private String scanID;

  private String scanMode;

  private String pFileName;

  private String rFileName;

  private List<String> fileNames;

  private DicomServer server;

  protected APEXScanDataExtractor(JdbcTemplate patScanDb, File scanDataDir, String participantKey, DicomServer server) {
    super();
    this.patScanDb = patScanDb;
    this.scanDataDir = scanDataDir;
    this.participantKey = participantKey;
    this.server = server;
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

  public abstract String getDicomBodyPartName();

  public int getNbRequiredDicomFiles() {
    return 1;
  }

  protected abstract long getScanType();

  protected abstract void extractDataImpl(Map<String, Data> data);

  protected JdbcTemplate getPatScanDb() {
    return patScanDb;
  }

  protected String getParticipantKey() {
    return participantKey;
  }

  private Map<String, Data> extractScanAnalysisData() {
    return patScanDb.query("select SCANID, PFILE_NAME, SCAN_MODE, SCAN_TYPE from ScanAnalysis where PATIENT_KEY = ? and SCAN_TYPE = ?", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, getParticipantKey());
        ps.setString(2, Long.toString(getScanType()));
      }
    }, new ScanAnalysisResultSetExtractor());
  }

  protected String getResultPrefix() {
    return getName();
  }

  protected String getScanID() {
    return scanID;
  }

  public List<String> getFileNames() {
    return fileNames != null ? fileNames : (fileNames = new ArrayList<String>());
  }

  private final class ScanAnalysisResultSetExtractor implements ResultSetExtractor<Map<String, Data>> {
    @Override
    public Map<String, Data> extractData(ResultSet rs) throws SQLException, DataAccessException {
      Map<String, Data> data = new HashMap<String, Data>();

      // assume the last scan of a given type is the one we are interested in
      // + stores all scan files for future deletion
      while(rs.next()) {
        scanID = rs.getString("SCANID");
        scanMode = rs.getString("SCAN_MODE");
        log.info("Visiting scan: " + scanID);
        pFileName = rs.getString("PFILE_NAME");

        if(pFileName != null) {
          rFileName = pFileName.replace(".P", ".R");
          getFileNames().add(pFileName);
          getFileNames().add(rFileName);
        } else {
          rFileName = null;
        }
      }

      if(scanID != null && pFileName != null) {
        log.info("Retrieving P and R data from scan: " + scanID);
        data.put(getResultPrefix() + "_SCANID", DataBuilder.buildText(scanID));
        data.put(getResultPrefix() + "_SCAN_MODE", DataBuilder.buildText(scanMode));
        data.put(getResultPrefix() + "_PFILE_NAME", DataBuilder.buildText(pFileName));
        data.put(getResultPrefix() + "_RFILE_NAME", DataBuilder.buildText(rFileName));

        File rFile = new File(scanDataDir, rFileName);
        if(rFile.exists()) {
          data.put(getResultPrefix() + "_RFILE", DataBuilder.buildBinary(rFile));
        }
      }

      Collection<StoredDicomFile> selectCollection = Collections2.filter(server.listDicomFiles(), new Predicate<StoredDicomFile>() {

        @Override
        public boolean apply(StoredDicomFile o) {
          try {
            String bodyPartExam = ((StoredDicomFile) o).getDicomObject().getString(Tag.BodyPartExamined);
            log.info(bodyPartExam);
            log.info(getDicomBodyPartName());
            // if 2 null we suppose that it is a whole body scan
            boolean ret = (bodyPartExam == null && getDicomBodyPartName() == null) ? true : (getDicomBodyPartName() != null && getDicomBodyPartName().equals(bodyPartExam));
            log.info(ret + "");
            return ret;
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }
      });
      ArrayList<StoredDicomFile> selectList = new ArrayList<StoredDicomFile>(selectCollection);
      if(!selectList.isEmpty()) {
        // Whole Body
        if(getDicomBodyPartName() == null) {
          StoredDicomFile storedDicomFile = selectList.get(0);
          StoredDicomFile storedDicomFile2 = selectList.get(1);
          try {
            DicomObject d = storedDicomFile.getDicomObject();
            DicomObject d2 = storedDicomFile2.getDicomObject();

            data.put(getResultPrefix() + "_" + d.getString(Tag.Modality) + "_DICOM_1", DataBuilder.buildBinary(storedDicomFile.getFile()));
            data.put(getResultPrefix() + "_" + d2.getString(Tag.Modality) + "_DICOM_2", DataBuilder.buildBinary(storedDicomFile2.getFile()));
          } catch(IOException e) {
            throw new RuntimeException(e);
          }

        } else {
          StoredDicomFile storedDicomFile = selectList.get(0);
          try {
            DicomObject d = storedDicomFile.getDicomObject();
            data.put(getResultPrefix() + "_" + d.getString(Tag.Modality) + "_DICOM", DataBuilder.buildBinary(storedDicomFile.getFile()));
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }
      }

      return data;
    }
  }

  protected Map<String, Data> extractScanData(String table, Map<String, Data> data, ResultSetExtractor<Map<String, Data>> rsExtractor) {
    return getPatScanDb().query("select * from " + table + " where PATIENT_KEY = ? and SCANID = ?", new PreparedStatementSetter() {
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

    protected void putBoolean(String name) throws SQLException {
      put(name, DataBuilder.buildBoolean(rs.getBoolean(name)));
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
