/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.atcor.dao;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public interface SphygmoCorDao {

  public void addPatient(String systemId, String studyId, String patientId, int patientNo, String familyName, String firstName, Date birthDate, String gender);

  public void deletePatientById(String patientId);

  public void deletePatientByNumber(int patientNo);

  public void deleteAllPatients();

  public void deleteAllOutput();

  @SuppressWarnings("unchecked")
  public List<Map> getOutput(int patientNo);
}
