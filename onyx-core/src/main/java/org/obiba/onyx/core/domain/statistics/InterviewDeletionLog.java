/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.statistics;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class InterviewDeletionLog extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  String participantBarcode;

  //TODO write code to create this field during upgrade process, see AbstractUpgradeStep
  @Column
  String enrollmentId;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  Date date;

  @Column(nullable = false)
  String user;

  @Temporal(TemporalType.TIMESTAMP)
  Date exportDate;

  @Column(nullable = false)
  String status;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Date getExportDate() {
    return exportDate;
  }

  public void setExportDate(Date exportDate) {
    this.exportDate = exportDate;
  }

  public String getParticipantBarcode() {
    return participantBarcode;
  }

  public void setParticipantBarcode(String participantBarcode) {
    this.participantBarcode = participantBarcode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getEnrollmentId() {
    return enrollmentId;
  }

  public void setEnrollmentId(String enrollmentId) {
    this.enrollmentId = enrollmentId;
  }

}
