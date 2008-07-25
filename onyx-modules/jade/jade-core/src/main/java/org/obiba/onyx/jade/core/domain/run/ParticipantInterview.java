package org.obiba.onyx.jade.core.domain.run;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Participant;

@Entity
public class ParticipantInterview extends AbstractEntity {

  private static final long serialVersionUID = -6980920538268971339L;

  private Participant participant;
  
  @OneToMany(mappedBy = "participantInterview")
  private List<InstrumentRun> instrumentRuns;
  
  public ParticipantInterview() {
  }
  
  public ParticipantInterview(Participant participant) {
    this.participant = participant;
  }

  public Participant getParticipant() {
    return participant;
  }

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

  public List<InstrumentRun> getInstrumentRuns() {
    return instrumentRuns != null ? instrumentRuns : (instrumentRuns = new ArrayList<InstrumentRun>());
  }

  public void addInstrumentRun(InstrumentRun instrumentRun) {
    if (instrumentRun != null) {
      getInstrumentRuns().add(instrumentRun);
      instrumentRun.setParticipantInterview(this);
    }
  }
}
