/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.etl.participant.AbstractParticipantExcelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticipantExcelReader extends AbstractParticipantExcelReader {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ParticipantExcelReader.class);

  protected int getStartAtLine() {
    return 3;
  }

  protected Participant processParticipant(HSSFRow row, HSSFFormulaEvaluator evaluator) {
    Participant participant = new Participant();

    participant.setSiteNo(getTextValue(row, evaluator, row.getCell(2)));
    participant.setFirstName(getTextValue(row, evaluator, row.getCell(5)));
    participant.setLastName(getTextValue(row, evaluator, row.getCell(4)));
    Long id = getNumericValue(row, evaluator, row.getCell(3)).longValue();
    participant.setEnrollmentId(id == null ? null : id.toString());
    participant.setBirthDate(getDateValue(row, evaluator, row.getCell(7)));
    String gender = getTextValue(row, evaluator, row.getCell(6));
    if(gender.equals("M")) {
      participant.setGender(Gender.MALE);
    } else if(gender.equals("F")) {
      participant.setGender(Gender.FEMALE);
    }

    return participant;
  }

  protected Appointment processAppointment(HSSFRow row, HSSFFormulaEvaluator evaluator) {
    Appointment appointment = new Appointment();

    Long id = getNumericValue(row, evaluator, row.getCell(3)).longValue();
    appointment.setAppointmentCode(id == null ? null : id.toString());
    appointment.setDate(getDateValue(row, evaluator, row.getCell(0)));

    return appointment;
  }
}
