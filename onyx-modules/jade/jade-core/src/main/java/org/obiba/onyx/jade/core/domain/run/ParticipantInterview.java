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
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Participant;

@Entity
public class ParticipantInterview extends AbstractEntity {

  private static final long serialVersionUID = -6980920538268971339L;

  @OneToOne
  @JoinColumn(name = "participant_id")
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
