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

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.tool.dcmrcv.ApexTag;
import org.dcm4che2.tool.dcmrcv.DicomServer;
import org.dcm4che2.tool.dcmrcv.DicomServer.StoredDicomFile;
import org.obiba.onyx.jade.instrument.holologic.APEXInstrumentRunner.Side;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

public abstract class APEXScanDataExtractor {

  /**
   * Static ivar needed for computing T- and Z-scores. Map distinct BMD variable name(s) (eg., HTOT_BMD) for a given
   * PatScanDb table (eg., Hip) and the corresponding bonerange code in the RefScanDb ReferenceCurve table (eg., 123.).
   * Additional BMD variables and codes should be added here for other tables (ie., Spine).
   */
  protected static final Map<String, String> ranges = new HashMap<String, String>();
  static {
    // forearm
    ranges.put("RU13TOT_BMD", "1..");
    ranges.put("RUMIDTOT_BMD", ".2.");
    ranges.put("RUUDTOT_BMD", "..3");
    ranges.put("RUTOT_BMD", "123");
    ranges.put("R_13_BMD", "R..");
    ranges.put("R_MID_BMD", ".R.");
    ranges.put("R_UD_BMD", "..R");
    ranges.put("RTOT_BMD", "RRR");
    ranges.put("U_13_BMD", "U..");
    ranges.put("U_MID_BMD", ".U.");
    ranges.put("U_UD_BMD", "..U");
    ranges.put("UTOT_BMD", "UUU");

    // whole body
    ranges.put("WBTOT_BMD", "NULL");

    // hip
    ranges.put("NECK_BMD", "1...");
    ranges.put("TROCH_BMD", ".2..");
    ranges.put("INTER_BMD", "..3.");
    ranges.put("WARDS_BMD", "...4");
    ranges.put("HTOT_BMD", "123.");
  }

  private static final Logger log = LoggerFactory.getLogger(APEXScanDataExtractor.class);

  private JdbcTemplate patScanDb;

  private JdbcTemplate refCurveDb;

  private String scanID;

  private String scanDate;

  private String scanMode;

  /*
   * private String pFileName;
   * 
   * private String rFileName;
   * 
   * private List<String> fileNames;
   */
  private Map<String, String> participantData;

  private DicomServer server;

  private ApexReceiver apexReceiver;

  //
  // Abstract get methods.
  //

  public abstract String getName();

  public abstract String getDicomBodyPartName();

  public abstract Side getSide();

  protected abstract long getScanType();

  public abstract String getRefType();

  public abstract String getRefSource();

  /**
   * Constructor.
   */
  protected APEXScanDataExtractor(JdbcTemplate patScanDb, JdbcTemplate refCurveDb, Map<String, String> participantData, DicomServer server, ApexReceiver apexReceiver) {
    super();
    this.patScanDb = patScanDb;
    this.refCurveDb = refCurveDb;
    this.participantData = participantData;
    this.server = server;
    this.apexReceiver = apexReceiver;
  }

  /**
   * Called by APEXInstrumentRunner extractScanData(). Get scan information from Apex PatScan db, get the data values,
   * compute T- and Z-scores for BMD values.
   */
  public Map<String, Data> extractData() {

    /** get the dicom file(s) for a given type of scan */
    Map<String, Data> data = extractScanAnalysisData();

    log.info("start data with " + Integer.toString(data.size()) + " entries");

    /** get the variables associated with an analysis of the scan */

    if(!data.isEmpty()) {
      log.info("getting data from concrete impl of extractDataImp");
      extractDataImpl(data);
      log.info("getting TZscores... ");
      computeTZScore(data);
    }

    log.info("returning data with " + Integer.toString(data.size()) + " entries");
    return data;
  }

  /**
   * Called by extractData(). Query the Apex PatScan db for the scan ID, raw data file name, scan mode and scan type
   * based on the patient key and scan type. Scan type is provided by child classes (eg., AP Spine = 1).
   */
  private Map<String, Data> extractScanAnalysisData() {
    log.info("extractscananalysisdata: " + getParticipantKey() + ", " + Long.toString(getScanType()));
    return patScanDb.query("select SCANID, SCAN_MODE, SCAN_DATE from ScanAnalysis where PATIENT_KEY = ? and SCAN_TYPE = ?", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, getParticipantKey());
        ps.setString(2, Long.toString(getScanType()));
      }
    }, new ScanAnalysisResultSetExtractor());
  }

  /**
   * Called by extractData().
   * 
   * @param data
   */
  protected abstract void extractDataImpl(Map<String, Data> data);

  /**
   * Called by extractData(). Computes T- and Z-score and adds to data collection.
   * 
   * @param data
   */
  protected void computeTZScore(Map<String, Data> data) throws DataAccessException, IllegalArgumentException {

    if(data == null || data.isEmpty()) return;

    String prefix = getResultPrefix() + "_";

    Map<String, Double> bmdData = new HashMap<String, Double>();
    for(Map.Entry<String, Data> entry : data.entrySet()) {
      String key = entry.getKey();
      if(key.endsWith("_BMD")) {
        log.info("key pre: " + key + ", new key: " + key.replace(prefix, ""));
        key = key.replace(prefix, "");
        if(ranges.containsKey(key)) {
          bmdData.put(key, (Double) entry.getValue().getValue());
          log.info("ranges contains key: " + key);
        }
      }
    }

    log.info(prefix + " data contains: " + Integer.toString(data.size()) + " possible entries to get bmd values from");
    log.info(prefix + " bmddata contains: " + Integer.toString(bmdData.size()) + " entries to get tz");

    for(Map.Entry<String, Double> entry : bmdData.entrySet()) {
      String bmdBoneRangeKey = entry.getKey();
      Double bmdValue = entry.getValue();

      log.info("working on range key:" + bmdBoneRangeKey + " with value: " + bmdValue.toString());

      // T- and Z-scores are interpolated from X, Y reference curve data.
      // A curve depends on the type of scan, gender, ethnicity, and
      // the coded anatomic region that bmd was measured in.
      // Determine the unique curve ID along with the age at which
      // peak bmd occurs. Implementation assumes ethnicity is always Caucasian
      // and gender is always female in accordance with WHO and
      // Osteoporosis Canada guidelines.
      //
      String sql = "SELECT UNIQUE_ID, AGE_YOUNG FROM ReferenceCurve";
      sql += " WHERE REFTYPE = '" + getRefType() + "'";
      sql += " AND IF_CURRENT = 1 AND SEX = 'F' AND ETHNIC IS NULL AND METHOD IS NULL";
      sql += " AND SOURCE LIKE '%" + getRefSource() + "%'";
      sql += " AND BONERANGE ";
      sql += (ranges.get(bmdBoneRangeKey).equals("NULL") ? ("IS NULL") : ("= '" + ranges.get(bmdBoneRangeKey) + "'"));

      log.info("first query: " + sql);
      Map<String, Object> mapResult;
      try {
        mapResult = refCurveDb.queryForMap(sql);
      } catch(DataAccessException e) {
        throw e;
      }
      String curveId = mapResult.get("UNIQUE_ID").toString();
      Double ageYoung = new Double(mapResult.get("AGE_YOUNG").toString());

      // Determine the age values (X axis variable) of the curve
      //
      List<Double> ageTable = new ArrayList<Double>();
      sql = "SELECT X_VALUE FROM Points WHERE UNIQUE_ID = " + curveId;

      log.info("second query: " + sql);

      List<Map<String, Object>> listResult;
      try {
        listResult = refCurveDb.queryForList(sql);
      } catch(DataAccessException e) {
        throw e;
      }
      for(Map<String, Object> row : listResult) {
        ageTable.add(new Double(row.get("X_VALUE").toString()));
      }

      // Determine the discrete age values that bracket the
      // participant's age (at the time of the scan).
      //
      Double age = null;
      try {
        age = computeYearsDifference(getScanDate(), getParticipantDOB());
      } catch(ParseException e) {
      }

      log.info("computed age from scandate and dob: " + age.toString() );

      ageBracket bracket = new ageBracket();
      bracket.compute(age, ageTable);

      // Determine the bmd, skewness factor and standard deviation
      // at the bracketing and peak bmd age values.
      //
      List<Double> bmdValues = new ArrayList<Double>();

      sql = "SELECT Y_VALUE, L_VALUE, STD FROM Points WHERE UNIQUE_ID = " + curveId;
      sql += " AND X_VALUE = ";

      Double[] x_value_array = { bracket.ageMin, bracket.ageMax, ageYoung };
      for(int i = 0; i < x_value_array.length; i++) {
        mapResult.clear();
        log.info("third query iter " + ((Integer) i).toString() + " : " + sql + x_value_array[i].toString());

        try {
          mapResult = refCurveDb.queryForMap(sql + x_value_array[i].toString());
        } catch(DataAccessException e) {
          throw e;
        }

        bmdValues.add(new Double(mapResult.get("Y_VALUE").toString()));
        bmdValues.add(new Double(mapResult.get("L_VALUE").toString()));
        bmdValues.add(new Double(mapResult.get("STD").toString()));
      }
      
      DecimalFormat df = new DecimalFormat("#.0");
      Double X_value = bmdValue;
      Double M_value = bmdValues.get(6);
      Double L_value = bmdValues.get(7);
      Double sigma = bmdValues.get(8);

      Double T_score = M_value * (Math.pow(X_value / M_value, L_value) - 1.) / (L_value * sigma);
      T_score = Double.valueOf(df.format(T_score));
      if(Math.abs(T_score) == 0.) T_score = 0.;

      String varName = getResultPrefix() + "_" + bmdBoneRangeKey.replace("_BMD", "_T");
      if(data.keySet().contains(varName)) {
        throw new IllegalArgumentException("Instrument variable name already defined: " + varName);
      }
      data.put(varName, DataBuilder.buildDecimal(T_score));
      log.info( varName + " = " + T_score.toString() );

      Double Z_score = null;

      if(bracket.ageSpan != 0.) {
        Double u = (age - bracket.ageMin) / bracket.ageSpan;

        List<Double> interpValues = new ArrayList<Double>();
        for(int i = 0; i < bmdValues.size() / 3; i++)
          interpValues.add((1. - u) * bmdValues.get(i) + u * bmdValues.get(i + 3));

        M_value = interpValues.get(0);
        L_value = interpValues.get(1);
        sigma = interpValues.get(2);

        Z_score = M_value * (Math.pow(X_value / M_value, L_value) - 1.) / (L_value * sigma);
        Z_score = Double.valueOf(df.format(Z_score));
        if(Math.abs(Z_score) == 0.) Z_score = 0.;
      }

      varName = getResultPrefix() + "_" + bmdBoneRangeKey.replace("_BMD", "_Z");
      if(data.keySet().contains(varName)) {
        throw new IllegalArgumentException("Instrument variable name already defined: " + varName);
      }
      data.put(varName, DataBuilder.buildDecimal(Z_score));

      if(Z_score != null) {
        log.info( varName + " = " + Z_score.toString() );
      }
      else {
        log.info( varName + " = null" );
      }  

      log.info("finished current key: " + bmdBoneRangeKey);
    }
  }

  /**
   * Called by extractScanAnalysisData(). Implementation of ResultSetExtractor. Process the query that recovers raw DEXA
   * scan P & R data file names and dicom files from Apex receiver. P and R data files are embedded in dicom files.
   * Note: the Enterprise Data Management install option must be activated in Apex with a license key for the dicom
   * export of embedded P and R data.
   */
  private final class ScanAnalysisResultSetExtractor implements ResultSetExtractor<Map<String, Data>> {
    @Override
    public Map<String, Data> extractData(ResultSet rs) throws SQLException, DataAccessException {
      Map<String, Data> data = new HashMap<String, Data>();

      log.info("starting result set processing");

      while(rs.next()) {
        scanID = rs.getString("SCANID");
        scanMode = rs.getString("SCAN_MODE");
        log.info("Visiting scan: " + scanID);
        scanDate = rs.getString("SCAN_DATE");
      }

      if(scanID != null && scanMode != null) {

        List<StoredDicomFile> selectList = new ArrayList<StoredDicomFile>();
        List<StoredDicomFile> listDicomFiles = server.listSortedDicomFiles();

        // there must be at least one dicom file with a body part examined key
        // body part name depends on the data extractor class
        // LSPINE = spine, expects 3 files
        // null = whole body, expects 2 files
        // HIP = hip, expects 1 to 2 files
        // ARM = forearm, expects 1 to 2 files
        //
        String bodyPartName = getDicomBodyPartName();

        for(StoredDicomFile sdf : listDicomFiles) {
          try {
            DicomObject dicomObject = sdf.getDicomObject();
            boolean dcmBodyPartKey = dicomObject.contains(Tag.BodyPartExamined);
            String dcmBodyPart = dicomObject.getString(Tag.BodyPartExamined);
            String dcmSide = dicomObject.getString(Tag.Laterality);

            if(bodyPartName == "LSPINE") {
              if(dcmBodyPartKey) {
                if(bodyPartName.equals(dcmBodyPart)) selectList.add(sdf);
              } else {
                selectList.add(sdf);
              }
            } else if(bodyPartName == "ARM") {
              if(dcmBodyPartKey) {
                if(bodyPartName.equals(dcmBodyPart) && dcmSide != null) selectList.add(sdf);
              }
            } else if(bodyPartName == "HIP") {
              if(dcmBodyPartKey) {
                if(bodyPartName.equals(dcmBodyPart) && dcmSide != null) selectList.add(sdf);
              }
            } else {
              if(dcmBodyPartKey && dcmSide == null) {
                selectList.add(sdf);
              }
            }
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }

        if(!selectList.isEmpty()) {

          // Forearm
          if("ARM".equals(getDicomBodyPartName()) && selectList.size() <= 2) {
            data.put(getResultPrefix() + "_SCANID", DataBuilder.buildText(scanID));
            data.put(getResultPrefix() + "_SCAN_MODE", DataBuilder.buildText(scanMode));

            log.info("processing forearm dicom side: " + getSide().toString());
            processFilesExtractionForeArm(getSide(), selectList, data);
          }
          // Lateral Spine
          else if("LSPINE".equals(getDicomBodyPartName()) && selectList.size() == 3) {
            data.put(getResultPrefix() + "_SCANID", DataBuilder.buildText(scanID));
            data.put(getResultPrefix() + "_SCAN_MODE", DataBuilder.buildText(scanMode));

            log.info("processing spine dicom");
            processFilesExtractionSpine(listDicomFiles, data);
          }
          // Hip
          else if("HIP".equals(getDicomBodyPartName()) && selectList.size() <= 2) {
            data.put(getResultPrefix() + "_SCANID", DataBuilder.buildText(scanID));
            data.put(getResultPrefix() + "_SCAN_MODE", DataBuilder.buildText(scanMode));

            log.info("processing hip dicom side: " + getSide().toString());
            processFilesExtractionHip(getSide(), selectList, data);
          }
          // Whole Body
          else if(null == getDicomBodyPartName() && selectList.size() == 2) {
            data.put(getResultPrefix() + "_SCANID", DataBuilder.buildText(scanID));
            data.put(getResultPrefix() + "_SCAN_MODE", DataBuilder.buildText(scanMode));

            log.info("processing whole body dicom");
            processFilesExtractionWB(selectList, data);
          }
        }
      }

      log.info("finished processing files");
      return data;
    }
  }

  /**
   * Called by ScanAnalysisResultSetExtractor extractData(). Adds Hip scan dicom files to data collection.
   * 
   * @param side
   * @param files
   * @param data
   */
  private void processFilesExtractionHip(Side side, List<StoredDicomFile> files, Map<String, Data> data) {
    try {
      for(int i = 0; i < files.size(); i++) {
        StoredDicomFile storedDicomFile = files.get(i);
        if(side != null && (side == Side.LEFT ? "L" : "R").equals(storedDicomFile.getDicomObject().getString(Tag.Laterality))) {
          putDicom(data, getResultPrefix() + "_DICOM", storedDicomFile);
        }
      }
    } catch(IOException e) {
    }
  }

  /**
   * Called by ScanAnalysisResultSetExtractor extractData(). Adds Whole Body scan dicom files to data collection.
   * 
   * @param files
   * @param data
   */
  private void processFilesExtractionWB(List<StoredDicomFile> files, Map<String, Data> data) {
    for(int i = 0; i < files.size(); i++) {
      StoredDicomFile storedDicomFile = files.get(i);
      putDicom(data, getResultPrefix() + "_DICOM" + "_" + (i + 1), storedDicomFile);
    }
  }

  /**
   * Called by ScanAnalysisResultSetExtractor extractData(). Adds Forearm scan dicom files to data collection.
   * 
   * @param side
   * @param files
   * @param data
   */
  private void processFilesExtractionForeArm(Side side, List<StoredDicomFile> files, Map<String, Data> data) {
    try {
      for(int i = 0; i < files.size(); i++) {
        StoredDicomFile storedDicomFile = files.get(i);
        if(side != null && (side == Side.LEFT ? "L" : "R").equals(storedDicomFile.getDicomObject().getString(Tag.Laterality))) {
          putDicom(data, getResultPrefix() + "_DICOM", storedDicomFile);
        }
      }
    } catch(IOException e) {
    }
  }

  /**
   * Called by ScanAnalysisResultSetExtractor extractData(). Adds Spine scan dicom files to data collection.
   * 
   * @param files
   * @param data
   */
  private void processFilesExtractionSpine(List<StoredDicomFile> files, Map<String, Data> data) {
    try {
      for(int i = 0; i < files.size(); i++) {
        StoredDicomFile storedDicomFile = files.get(i);
        String bodyPartExam = storedDicomFile.getDicomObject().getString(Tag.BodyPartExamined);
        String modality = storedDicomFile.getDicomObject().getString(Tag.Modality);
        if("LSPINE".equals(bodyPartExam)) {
          putDicom(data, getResultPrefix() + "_DICOM_MEASURE", storedDicomFile);
        } else if("PR".equals(modality)) {
          putDicom(data, getResultPrefix() + "_DICOM_PR", storedDicomFile);
        } else {
          putDicom(data, getResultPrefix() + "_DICOM_OT", storedDicomFile);
        }
      }
    } catch(IOException e) {
    }
  }

  /**
   * Called by processFilesExtraction* methods. Add a dicom file exported from Apex via DICOM send transfer to the data
   * collection.
   * 
   * @param data
   * @param name
   * @param storedDicomFile
   */
  public void putDicom(Map<String, Data> data, String name, StoredDicomFile storedDicomFile) {
    boolean completeDicom = isCompleteDicom(storedDicomFile);
    apexReceiver.missingRawInDicomFile(completeDicom);
    Data binary = DataBuilder.buildBinary(storedDicomFile.getFile());
    data.put(name, binary);
  }

  /**
   * Called by putDicom(). Return true if dicom contains raw P & R data, false otherwise.
   * 
   * @return
   */
  private boolean isCompleteDicom(StoredDicomFile storedDicomFile) {
    for(ApexTag tag : ApexTag.values())
      try {
        DicomObject dicomObject = storedDicomFile.getDicomObject();
        if(dicomObject.contains(tag.getValue())) {
          if(dicomObject.containsValue(tag.getValue()) == false) {
            log.info("Missing P and/or R data in DICOM file: " + tag.name());
            return false;
          }
        }
      } catch(IOException e) {
      }
    return true;
  }

  /**
   * Called by extractDataImpl(). Implementation is specific to child classes which define Apex PatScan db table names
   * corresponding to the type of scan. Adds all analysis variables to data collection.
   * 
   * @param table
   * @param data
   * @param rsExtractor
   * @return
   */
  protected Map<String, Data> extractScanData(String table, Map<String, Data> data, ResultSetExtractor<Map<String, Data>> rsExtractor) {
    return getPatScanDb().query("select * from " + table + " where PATIENT_KEY = ? and SCANID = ?", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, getParticipantKey());
        ps.setString(2, getScanID());
      }
    }, rsExtractor);
  }

  /**
   * Used during extractDataImpl(). Implementation of ResultSetExtractor. Processes the query that recovers all scan
   * analysis variables from Apex PatScan db.
   */
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

  /**
   * Called by computeTZScore().
   * 
   * @param s1
   * @param s2
   * @return
   * @throws ParseException
   */
  public static Double computeYearsDifference(String s1, String s2) throws ParseException {
    SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
    Date d1 = df.parse(s1);
    Date d2 = df.parse(s2);

    Calendar c1 = Calendar.getInstance();
    c1.setTime(d1);
    Calendar c2 = Calendar.getInstance();
    c2.setTime(d2);

    Double diff = (c1.getTimeInMillis() - c2.getTimeInMillis()) / (1000. * 60. * 60. * 24. * 365.25);
    if(diff < 0.) diff *= -1.;

    return diff;
  }

  /**
   * Helper class for computeTZScore().
   */
  public final class ageBracket {
    public Double ageMin;

    public Double ageMax;

    public Double ageSpan;

    public ageBracket() {
      ageMin = Double.MIN_VALUE;
      ageMax = Double.MAX_VALUE;
      ageSpan = 0.;
    }

    public void compute(Double age, List<Double> ageTable) {
      ageMin = Double.MIN_VALUE;
      ageMax = Double.MAX_VALUE;
      for(int i = 0; i < ageTable.size() - 1; i++) {
        double min = ageTable.get(i);
        double max = ageTable.get(i + 1);
        if(age >= min && age <= max) {
          ageMin = min;
          ageMax = age == min ? min : max;
        }
        else if( age > max )
        {
          ageMin = max;
          ageMax = max;
        }
      }
      if( ageMin == Double.MIN_VALUE ) ageMin = age;
      if( ageMax == Double.MAX_VALUE ) ageMax = age;
      ageSpan = ageMax - ageMin;
    }
  }

  //
  // Set/Get methods
  //
  /*
   * Methods to get P and R data files are not needed.
   * Hologic embeds P and R data files in the DICOM data exported from APEX.
   *
   * protected String getPFileName() { return pFileName; }
   * 
   * protected String getRFileName() { return rFileName; }
   */
  /*
   * public List<String> getFileNames() { return fileNames != null ? fileNames : (fileNames = new ArrayList<String>());
   * }
   */

  protected JdbcTemplate getPatScanDb() {
    return patScanDb;
  }

  protected String getParticipantKey() {
    return participantData.get("participantKey");
  }

  protected String getParticipantDOB() {
    return participantData.get("participantDOB");
  }

  protected String getParticipantGender() {
    return participantData.get("participantGender");
  }

  protected String getResultPrefix() {
    return getName();
  }

  protected String getScanID() {
    return scanID;
  }

  protected String getScanDate() {
    return scanDate;
  }
}
