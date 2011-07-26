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

import org.obiba.vsm.bptru.bpm.Acks;
import org.obiba.vsm.bptru.bpm.BpmCommands.BpmCommand;
import org.obiba.vsm.bptru.bpm.BpmInstrument;
import org.obiba.vsm.bptru.bpm.BpmMessageHandler;
import org.obiba.vsm.bptru.bpm.BpmMessageHandler.DefaultBpmMessageHandler;
import org.obiba.vsm.bptru.bpm.Buttons;
import org.obiba.vsm.bptru.bpm.Data;
import org.obiba.vsm.bptru.bpm.Notifications;

public class ReadyState implements State {

  private final StateMachine stateMachine;

  private final BpmMessageHandler handler;

  public ReadyState(StateMachine machine, BpmInstrument instrument) {
    this.stateMachine = machine;
    this.handler = new DefaultBpmMessageHandler(instrument) {

      protected BpmCommand onAck(Acks.Type type, Acks.Ack ack) {
        switch(type) {
        case CLEAR:
          stateMachine.getSession().clearResults();
          getInstrument().commands().start().send();
          break;
        case START:
          Acks.Start start = (Acks.Start) ack;
          stateMachine.getSession().setCycle(start.cycleTime());
          stateMachine.getSession().setReading(start.readingNumber());
          startMeasure();
          break;
        }
        return null;
      };

      protected BpmCommand onButton(Buttons.Type type, Buttons.Button button) {
        switch(type) {
        case CLEARED:
          stateMachine.getSession().clearResults();
          break;
        case STARTED:
          Buttons.Start start = (Buttons.Start) button;
          stateMachine.getSession().setCycle(start.cycleTime());
          startMeasure();
          break;
        }
        return null;
      };

      protected BpmCommand onData(Data.Type type, Data.Datum datum) {
        // We shouldn't be getting data. Some Ack probably got lost. Stop the measurement.
        return getInstrument().commands().stop();
      };

      protected BpmCommand onNotification(Notifications.Type type, Notifications.Notification notification) {
        switch(type) {
        case RESET:
          stateMachine.transition(States.CONNECTING);
        }
        return null;
      };

    };
  }

  public States getName() {
    return States.READY;
  }

  public void start() {
    // Clear existing measures before starting. Start is send in the CLEAR ack.
    stateMachine.getInstrument().commands().clear().send();
  }

  public void stop() {

  }

  public BpmMessageHandler getMessageHandler() {
    return handler;
  }

  private void startMeasure() {
    stateMachine.transition(States.MEASURING);
  }

}
