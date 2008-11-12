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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.contraindication.IContraindicatable;
import org.obiba.onyx.core.domain.contraindication.Contraindication.Type;
import org.obiba.onyx.core.domain.participant.Interview;

/**
 * 
 */
@Entity
public class ParticipantTubeRegistration extends AbstractEntity implements IContraindicatable {

  private static final long serialVersionUID = 591980050842524784L;

  @OneToOne
  @JoinColumn(name = "interview_id")
  private Interview interview;

  @OneToMany(mappedBy = "participantTubeRegistration")
  private List<RegisteredParticipantTube> registeredParticipantTubes;

  @Temporal(TemporalType.TIMESTAMP)
  private Date startTime;

  @Temporal(TemporalType.TIMESTAMP)
  private Date endTime;

  private String contraindicationCode;

  @Column(length = 2000)
  private String otherContraindication;

  @Transient
  private TubeRegistrationConfiguration registrationConfig;

  public void setInterview(Interview interview) {
    this.interview = interview;
  }

  public Interview getInterview() {
    return interview;
  }

  public void addRegisteredParticipantTube(RegisteredParticipantTube registeredParticipantTube) {
    if(registeredParticipantTube != null) {
      getRegisteredParticipantTubes().add(registeredParticipantTube);
      registeredParticipantTube.setParticipantTubeRegistration(this);
    }
  }

  public void removeRegisteredParticipantTube(RegisteredParticipantTube registeredParticipantTube) {
    if(registeredParticipantTube != null) {
      getRegisteredParticipantTubes().remove(registeredParticipantTube);
      registeredParticipantTube.setParticipantTubeRegistration(null);
    }
  }

  public List<RegisteredParticipantTube> getRegisteredParticipantTubes() {
    return registeredParticipantTubes != null ? registeredParticipantTubes : (registeredParticipantTubes = new ArrayList<RegisteredParticipantTube>());
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setOtherContraindication(String otherContraindication) {
    this.otherContraindication = otherContraindication;
  }

  public String getOtherContraindication() {
    return otherContraindication;
  }

  public void setTubeRegistrationConfig(TubeRegistrationConfiguration registrationConfig) {
    this.registrationConfig = registrationConfig;
  }

  public TubeRegistrationConfiguration getTubeRegistrationConfig() {
    return registrationConfig;
  }

  public void setContraindication(Contraindication contraindication) {
    if(contraindication != null) {
      this.contraindicationCode = contraindication.getCode();
    } else {
      this.contraindicationCode = null;
    }
  }

  public Contraindication getContraindication() {
    for(Contraindication ci : registrationConfig.getAskedContraindications()) {
      if(ci.getCode().equals(contraindicationCode)) return ci;
    }
    for(Contraindication ci : registrationConfig.getObservedContraindications()) {
      if(ci.getCode().equals(contraindicationCode)) return ci;
    }
    return null;
  }

  @Transient
  public List<Contraindication> getContraindications(Type type) {
    List<Contraindication> ciList = new ArrayList<Contraindication>(5);

    if(type == Type.ASKED) {
      for(Contraindication ci : getTubeRegistrationConfig().getAskedContraindications()) {
        ciList.add(ci);
      }
    } else if(type == Type.OBSERVED) {
      for(Contraindication ci : getTubeRegistrationConfig().getObservedContraindications()) {
        ciList.add(ci);
      }
    }
    return ciList.size() > 0 ? ciList : null;
  }

  public boolean hasContraindications(Type type) {
    return getContraindications(type) != null;
  }

  @Transient
  public boolean isContraindicated() {
    return this.contraindicationCode != null;
  }
}
