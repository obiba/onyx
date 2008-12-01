/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

/**
 *
 */
public abstract class AbstractJadeStageState extends AbstractStageState {

  protected InstrumentRunService instrumentRunService;

  protected InstrumentService instrumentService;

  @Override
  public Data getData(String key) {
    if(isCompleted() == false) {
      return null;
    }
    // Ignore the key. Simply return true when this stage has a value for the current instrument run false otherwise.
    // Eventually, this method should lookup a specific parameter within the run and return its value or a boolean
    // indicating its presence.
    Participant participant = activeInterviewService.getParticipant();
    InstrumentType instrumentType = instrumentService.getInstrumentType(getStage().getName());
    InstrumentRun instrumentRun = instrumentRunService.getLastCompletedInstrumentRun(participant, instrumentType);

    if(instrumentRun != null && instrumentRun.getInstrumentRunValues().size() > 0) {
      return DataBuilder.buildBoolean(true);
    } else {
      return DataBuilder.buildBoolean(false);
    }
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }
}
