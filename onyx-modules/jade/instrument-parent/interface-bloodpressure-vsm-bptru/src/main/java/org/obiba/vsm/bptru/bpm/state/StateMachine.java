/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.vsm.bptru.bpm.state;

import org.obiba.vsm.bptru.bpm.BpmInstrument;
import org.obiba.vsm.bptru.bpm.BpmMessageHandler;
import org.obiba.vsm.bptru.bpm.BpmMessageLoop;
import org.obiba.vsm.bptru.bpm.state.State.States;

public class StateMachine extends BpmMessageLoop {

  private final BpmSession session;

  private State state;

  public StateMachine(BpmSession session, BpmInstrument instrument) {
    super(instrument);
    this.session = session;
    state = new ConnectingState(this, instrument);
    session.setState(state.getName());
  }

  public BpmSession getSession() {
    return session;
  }

  @Override
  protected BpmMessageHandler getCurrentHandler() {
    return this.state.getMessageHandler();
  }

  public void start() {
    state.start();
  }

  public void stop() {
    state.stop();
  }

  public void add() {
    state.add();
  }

  public void transition(States states) {
    switch(states) {
    case CONNECTING:
      state = new ConnectingState(this, getInstrument());
      break;
    case READY:
      state = new ReadyState(this, getInstrument());
      break;
    case MEASURING:
      state = new MeasuringState(this, getInstrument());
      break;
    }
    getSession().setState(states);
  }

}
