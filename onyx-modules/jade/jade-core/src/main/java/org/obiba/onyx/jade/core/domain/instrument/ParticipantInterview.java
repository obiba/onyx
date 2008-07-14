package org.obiba.onyx.jade.core.domain.instrument;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class ParticipantInterview extends AbstractEntity {

  private static final long serialVersionUID = -6980920538268971339L;

  @OneToMany(mappedBy = "participantInterview")
  private List<InstrumentRun> instrumentRuns;
  
  // TODO participant
  
  public ParticipantInterview() {
    super();
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
