/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class Appointment extends AbstractEntity {

  private static final long serialVersionUID = 6009569414177913575L;

  private String appointmentCode;
  
  @Temporal(TemporalType.TIMESTAMP)
  public Date date;

  @OneToOne
  @JoinColumn(name = "participant_id")
  public Participant participant;

  public Appointment() {
  }
  
  public Appointment(Participant participant, Date date) {
    this.participant = participant;
    this.date = date;
  }

  public String getAppointmentCode() {
    return appointmentCode;
  }

  public void setAppointmentCode(String appointmentCode) {
    this.appointmentCode = appointmentCode;
  }
  
  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Participant getParticipant() {
    return participant;
  }

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

}
