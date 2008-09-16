package org.obiba.onyx.jade.instrument.atcor.dao.impl;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.onyx.jade.instrument.atcor.dao.SphygmoCorDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

public class JdbcSphygmoCorDao extends NamedParameterJdbcDaoSupport implements SphygmoCorDao {

  //
  // Constants
  //

  private static final String INSERT_PATIENT_SQL = "insert into PATIENT (SYSTEM_ID, STUDY_ID, PATIENT_ID, PATIENT_NO, FAM_NAME, FIRST_NAME, DOB, SEX) " + "values (:systemId, :studyId, :patientId, :patientNo, :familyName, :firstName, :birthDate, :gender)";

  private static final String DELETE_PATIENT_BY_ID_SQL = "delete from PATIENT where PATIENT_ID = :patientId";

  private static final String DELETE_PATIENT_BY_NUMBER_SQL = "delete from PATIENT where PATIENT_NO = :patientNo";

  private static final String DELETE_ALL_PATIENTS_SQL = "delete from PATIENT";

  private static final String DELETE_ALL_OUTPUT_SQL = "delete from M_PWA";

  private static final String GET_OUTPUT_SQL = "select * from M_PWA where SYSTEM_ID = :systemId and STUDY_ID = :studyId and PATIENT_NO = :patientNo";

  //
  // Methods
  //

  public void addPatient(String systemId, String studyId, String patientId, int patientNo, String familyName, String firstName, Date birthDate, String gender) {
    Map paramMap = new HashMap();

    paramMap.put("systemId", systemId);
    paramMap.put("studyId", studyId);
    paramMap.put("patientId", patientId);
    paramMap.put("patientNo", patientNo);
    paramMap.put("familyName", familyName);
    paramMap.put("firstName", firstName);
    paramMap.put("birthDate", birthDate);
    paramMap.put("gender", gender);

    getNamedParameterJdbcTemplate().update(INSERT_PATIENT_SQL, paramMap);
  }

  public void deletePatientById(String patientId) {
    Map paramMap = new HashMap();

    paramMap.put("patientId", patientId);

    getNamedParameterJdbcTemplate().update(DELETE_PATIENT_BY_ID_SQL, paramMap);
  }

  public void deletePatientByNumber(int patientNo) {
    Map paramMap = new HashMap();

    paramMap.put("patientNo", patientNo);

    getNamedParameterJdbcTemplate().update(DELETE_PATIENT_BY_NUMBER_SQL, paramMap);
  }

  public void deleteAllPatients() {
    Map paramMap = new HashMap();

    getNamedParameterJdbcTemplate().update(DELETE_ALL_PATIENTS_SQL, paramMap);
  }

  public void deleteAllOutput() {
    Map paramMap = new HashMap();

    getNamedParameterJdbcTemplate().update(DELETE_ALL_OUTPUT_SQL, paramMap);
  }

  public List getOutput(String systemId, String studyId, int patientNo) {
    Map paramMap = new HashMap();

    paramMap.put("systemId", systemId);
    paramMap.put("studyId", studyId);
    paramMap.put("patientNo", patientNo);

    List matches = getNamedParameterJdbcTemplate().queryForList(GET_OUTPUT_SQL, paramMap);

    return matches.size() > 0 ? matches : null;
  }

  public static void main(String[] args) throws Exception {

    ApplicationContext appContext = new FileSystemXmlApplicationContext("C:\\eclipse-SDK-3.3.2-win32\\eclipse-workspaces\\onyx\\trunk\\onyx-modules\\jade\\instrument-parent\\interface-arterialstiffness\\src\\main\\resources\\META-INF\\spring\\instrument-context.xml");

    SphygmoCorDao sphygmoCorDao = (SphygmoCorDao) appContext.getBean("sphygmoCorDao");

    // sphygmoCorDao.deleteAllOutput();
    // sphygmoCorDao.deleteAllPatients();

    // sphygmoCorDao.addPatient("01400", "DATA", "4AAA", 1, "Spathis", "Dennis", new java.sql.Date(new
    // java.util.Date().getTime()), "MALE".toString());
    // sphygmoCorDao.addPatient("01400", "DATA", "4AAA", 1, "Spathis", "Stella", new java.sql.Date(new
    // java.util.Date().getTime()), "FEMALE".toString());

    List output = sphygmoCorDao.getOutput("01400", "DATA", 1);

    if(output != null) {
      System.out.println("Got output!");
      System.out.println("Rows: " + output.size());

      System.out.println("Row 0...");
      Map firstRow = (Map) output.get(0);
      System.out.println("P_QC_PH: " + firstRow.get("P_QC_PH"));
    }
  }
}