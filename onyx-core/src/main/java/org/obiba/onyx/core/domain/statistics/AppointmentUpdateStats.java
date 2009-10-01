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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;

/**
 * Table persisting the appointment list update data
 */
@Entity
public class AppointmentUpdateStats extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date date;

  @Column(nullable = false)
  private Integer addedParticipants;

  @Column(nullable = false)
  private Integer updatedParticipants;

  @Column(nullable = false)
  private Integer ignoredParticipants;

  @Column(nullable = false)
  private Integer unreadableParticipants;

  @Column(nullable = false)
  private String fileName;

  public AppointmentUpdateStats() {
    this.addedParticipants = 0;
    this.updatedParticipants = 0;
    this.ignoredParticipants = 0;
    this.unreadableParticipants = 0;
  }

  public AppointmentUpdateStats(Date date, int addedParticipants, int updatedParticipants, int ignoredParticipants, int unreadableParticipants) {
    super();
    this.date = date;
    this.addedParticipants = addedParticipants;
    this.updatedParticipants = updatedParticipants;
    this.ignoredParticipants = ignoredParticipants;
    this.unreadableParticipants = unreadableParticipants;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Integer getAddedParticipants() {
    return addedParticipants;
  }

  public void setAddedParticipants(Integer addedParticipants) {
    this.addedParticipants = addedParticipants;
  }

  public Integer getUpdatedParticipants() {
    return updatedParticipants;
  }

  public void setUpdatedParticipants(Integer updatedParticipants) {
    this.updatedParticipants = updatedParticipants;
  }

  public Integer getIgnoredParticipants() {
    return ignoredParticipants;
  }

  public void setIgnoredParticipants(Integer ignoredParticipants) {
    this.ignoredParticipants = ignoredParticipants;
  }

  public Integer getUnreadableParticipants() {
    return unreadableParticipants;
  }

  public void setUnreadableParticipants(Integer unreadableParticipants) {
    this.unreadableParticipants = unreadableParticipants;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Integer getTotalParticipants() {
    return (getUpdatedParticipants() + getIgnoredParticipants() + getAddedParticipants() + getUnreadableParticipants());
  }

}
