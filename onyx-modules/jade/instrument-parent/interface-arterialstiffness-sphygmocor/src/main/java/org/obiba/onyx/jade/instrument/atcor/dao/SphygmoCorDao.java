package org.obiba.onyx.jade.instrument.atcor.dao;

import java.sql.Date;
import java.util.List;

public interface SphygmoCorDao {

  public void addPatient(String systemId, String studyId, String patientId, int patientNo, String familyName, String firstName, Date birthDate, String gender);

  public void deletePatientById(String patientId);

  public void deletePatientByNumber(int patientNo);

  public void deleteAllPatients();

  public void deleteAllOutput();

  public List getOutput(int patientNo);
}
