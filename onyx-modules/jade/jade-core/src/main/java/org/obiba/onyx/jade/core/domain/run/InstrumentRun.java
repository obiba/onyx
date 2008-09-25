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

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;

@Entity
public class InstrumentRun extends AbstractEntity {

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

  @ManyToOne
  @JoinColumn(name = "contra_indication_id")
  private ContraIndication contraIndication;

  private String otherContraIndication;

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

  public ContraIndication getContraIndication() {
    return contraIndication;
  }

  public void setContraIndication(ContraIndication contraIndication) {
    this.contraIndication = contraIndication;
  }

  public String getOtherContraIndication() {
    return otherContraIndication;
  }

  public void setOtherContraIndication(String otherContraIndication) {
    this.otherContraIndication = otherContraIndication;
  }

}
