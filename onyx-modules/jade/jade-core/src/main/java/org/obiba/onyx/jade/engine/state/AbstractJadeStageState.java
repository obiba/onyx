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

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;

/**
 * 
 */
public abstract class AbstractJadeStageState extends AbstractStageState {

  protected InstrumentRunService instrumentRunService;

  protected InstrumentService instrumentService;

  protected ActiveInstrumentRunService activeInstrumentRunService;

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public void setActiveInstrumentRunService(ActiveInstrumentRunService activeInstrumentRunService) {
    this.activeInstrumentRunService = activeInstrumentRunService;
  }

  /**
   * Delete the instrument run associated with this stage. Currently each stage is associated with exactly one
   * instrument.
   */
  protected void cancelInstrumentRuns() {
    instrumentRunService.deleteInstrumentRun(activeInterviewService.getParticipant(), getStage().getName());
  }
}
