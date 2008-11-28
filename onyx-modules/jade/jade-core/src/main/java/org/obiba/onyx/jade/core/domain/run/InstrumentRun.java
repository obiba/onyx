/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.run;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.contraindication.IContraindicatable;
import org.obiba.onyx.core.domain.contraindication.Contraindication.Type;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;

@Entity
public class InstrumentRun extends AbstractEntity implements IContraindicatable {

  private static final long serialVersionUID = -2756620040202577411L;

  @ManyToOne
  @JoinColumn(name = "participant_interview_id")
  private ParticipantInterview participantInterview;

  @OneToMany(mappedBy = "instrumentRun")
  private List<InstrumentRunValue> instrumentRunValues;

  @ManyToOne
  @JoinColumn(name = "instrument_id")
  private Instrument instrument;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  private InstrumentRunStatus status;

  @Temporal(TemporalType.TIMESTAMP)
  private Date timeStart;

  @Temporal(TemporalType.TIMESTAMP)
  private Date timeEnd;

  @Enumerated(EnumType.STRING)
  private InstrumentRunRefusalReason refusalReason;

  @Column(length = 2000)
  private String refusalReasonComment;

  private String contraindicationCode;

  @Column(length = 2000)
  private String otherContraindication;

  public InstrumentRun() {
    super();
  }

  public ParticipantInterview getParticipantInterview() {
    return participantInterview;
  }

  public void setParticipantInterview(ParticipantInterview participantInterview) {
    this.participantInterview = participantInterview;
  }

  public List<InstrumentRunValue> getInstrumentRunValues() {
    return instrumentRunValues != null ? instrumentRunValues : (instrumentRunValues = new ArrayList<InstrumentRunValue>());
  }

  public void addInstrumentRunValue(InstrumentRunValue value) {
    if(value != null) {
      getInstrumentRunValues().add(value);
      value.setInstrumentRun(this);
    }
  }

  public InstrumentRunValue getInstrumentRunValue(InstrumentParameter instrumentParameter) {
    for(InstrumentRunValue runValue : getInstrumentRunValues()) {
      if(runValue.getInstrumentParameter().getId().equals(instrumentParameter.getId())) return runValue;
    }
    return null;
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  public InstrumentRunStatus getStatus() {
    return status;
  }

  public void setStatus(InstrumentRunStatus status) {
    this.status = status;
  }

  public Date getTimeStart() {
    return timeStart;
  }

  public void setTimeStart(Date timeStart) {
    this.timeStart = timeStart;
  }

  public Date getTimeEnd() {
    return timeEnd;
  }

  public void setTimeEnd(Date timeEnd) {
    this.timeEnd = timeEnd;
  }

  public InstrumentRunRefusalReason getRefusalReason() {
    return refusalReason;
  }

  public boolean isRefused() {
    return refusalReason != null;
  }

  public void setRefusalReason(InstrumentRunRefusalReason refusalReason) {
    this.refusalReason = refusalReason;
  }

  public String getRefusalReasonComment() {
    return refusalReasonComment;
  }

  public void setRefusalReasonComment(String refusalReasonComment) {
    this.refusalReasonComment = refusalReasonComment;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Contraindication getContraindication() {
    for(Contraindication ci : instrument.getContraindications()) {
      if(ci.getCode().equals(contraindicationCode)) return ci;
    }
    return null;
  }

  public void setContraindication(Contraindication contraindication) {
    if(contraindication != null) {
      this.contraindicationCode = contraindication.getCode();
    } else {
      this.contraindicationCode = null;
    }
  }

  public String getOtherContraindication() {
    return otherContraindication;
  }

  public void setOtherContraindication(String reason) {
    otherContraindication = reason;
  }

  @Transient
  public List<Contraindication> getContraindications(Contraindication.Type type) {
    List<Contraindication> ciList = new ArrayList<Contraindication>(5);
    for(Contraindication ci : this.instrument.getContraindications()) {
      if(ci.getType() == type) ciList.add(ci);
    }
    return ciList.size() > 0 ? ciList : null;
  }

  @Transient
  public boolean isContraindicated() {
    return this.contraindicationCode != null;
  }

  public boolean hasContraindications(Type type) {
    return getContraindications(type) != null;
  }
}
