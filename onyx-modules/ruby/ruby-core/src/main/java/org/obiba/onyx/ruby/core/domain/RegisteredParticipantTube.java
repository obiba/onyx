/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.obiba.core.domain.AbstractEntity;

/**
 * The registered participant tube persistence
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "barcode", "participant_tube_registration_id" }) })
public class RegisteredParticipantTube extends AbstractEntity {

  private static final long serialVersionUID = -9113927705255991817L;

  @Column(nullable = false)
  private String barcode;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date registrationTime;

  @CollectionOfElements
  @Cascade(CascadeType.ALL)
  private Set<String> remarks;

  @Column(length = 2000)
  private String comment;

  @ManyToOne(optional = false)
  @JoinColumn(name = "participant_tube_registration_id")
  private ParticipantTubeRegistration participantTubeRegistration;

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public String getBarcode() {
    return barcode;
  }

  public void setRegistrationTime(Date registrationTime) {
    this.registrationTime = registrationTime;
  }

  public Date getRegistrationTime() {
    return registrationTime;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getComment() {
    return comment;
  }

  public void setParticipantTubeRegistration(ParticipantTubeRegistration participantTubeRegistration) {
    this.participantTubeRegistration = participantTubeRegistration;
  }

  public ParticipantTubeRegistration getParticipantTubeRegistration() {
    return participantTubeRegistration;
  }

  public Set<String> getRemarks() {
    return (remarks != null) ? remarks : new HashSet<String>();
  }

  public void setRemarks(Set<String> remarks) {
    this.remarks = remarks;
  }

  public void addRemark(String remark) {
    getRemarks().add(remark);
  }

  @Transient
  public Map<String, Boolean> getRemarkSelections() {
    Map<String, Boolean> remarkSelections = new HashMap<String, Boolean>();
    for(String remark : getRemarks()) {
      remarkSelections.put(remark, Boolean.TRUE);
    }
    return remarkSelections;
  }
}
