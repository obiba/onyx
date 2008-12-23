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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;

/**
 * The registered participant tube persistence
 */
@Entity
public class RegisteredParticipantTube extends AbstractEntity {

  private static final long serialVersionUID = -9113927705255991817L;

  private String barcode;

  @Temporal(TemporalType.TIMESTAMP)
  private Date registrationTime;

  // private String remarkCode;
  @OneToMany(mappedBy = "registeredParticipantTube")
  private List<RemarkCode> remarkCode;

  @Column(length = 2000)
  private String comment;

  @ManyToOne
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

  /*
   * public void setRemarkCode(String remarkCode) { this.remarkCode = remarkCode; }
   * 
   * public String getRemarkCode() { return remarkCode; }
   */

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

  public List<RemarkCode> getRemarkCode() {
    return (remarkCode != null) ? remarkCode : new ArrayList<RemarkCode>();
  }

  public void setRemarkCode(List<RemarkCode> remarkCode) {
    this.remarkCode = remarkCode;
  }

  public List<String> getRemarkCodeString() {
    List<String> remarkStrList = new ArrayList<String>();

    for(RemarkCode remark : getRemarkCode()) {
      remarkStrList.add(remark.getCode());
    }
    return remarkStrList;
  }

  public void addRemarkCode(RemarkCode remarkCode) {
    getRemarkCode().add(remarkCode);
    remarkCode.setRegisteredParticipantTube(this);
  }
}
