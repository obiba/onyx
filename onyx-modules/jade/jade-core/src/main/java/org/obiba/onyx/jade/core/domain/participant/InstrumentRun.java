package org.obiba.onyx.jade.core.domain.participant;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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

}
