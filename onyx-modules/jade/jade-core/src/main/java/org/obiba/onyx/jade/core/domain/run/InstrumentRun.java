package org.obiba.onyx.jade.core.domain.run;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;

@Entity
public class InstrumentRun extends AbstractEntity {

  private static final long serialVersionUID = -2756620040202577411L;

  @ManyToOne
  @JoinColumn(name = "participant_interview_id")
  private ParticipantInterview participantInterview;

  @ManyToOne
  @JoinColumn(name = "instrument_id")
  private Instrument instrument;

  @Enumerated(EnumType.STRING)
  private InstrumentRunStatus status;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date timeStart;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date timeComplete;
  
  @Enumerated(EnumType.STRING)
  private InstrumentRunRefusalReason refusalReason;
  
  @Column(length = 2000)
  private String refusalReasonComment;
  
  public InstrumentRun() {
    super();
  }

  public ParticipantInterview getParticipantInterview() {
    return participantInterview;
  }

  public void setParticipantInterview(ParticipantInterview participantInterview) {
    this.participantInterview = participantInterview;
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

  public Date getTimeComplete() {
    return timeComplete;
  }

  public void setTimeComplete(Date timeComplete) {
    this.timeComplete = timeComplete;
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

}
