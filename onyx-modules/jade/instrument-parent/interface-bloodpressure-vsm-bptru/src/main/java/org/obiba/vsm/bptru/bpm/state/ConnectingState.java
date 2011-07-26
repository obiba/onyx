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
import org.obiba.vsm.bptru.bpm.Notifications;

public class ConnectingState implements State {

  private final StateMachine stateMachine;

  private final BpmMessageHandler handler;

  long startHandshake;

  boolean handshaked = false;

  public ConnectingState(StateMachine machine, BpmInstrument instrument) {
    this.stateMachine = machine;
    this.handler = new DefaultBpmMessageHandler(instrument) {

      public BpmCommand onStart() {
        startHandshake = System.currentTimeMillis();
        return getInstrument().commands().handshake();
      };

      protected BpmCommand onAck(Acks.Type type, Acks.Ack ack) {
        switch(type) {
        case HANDSHAKE:
          handshaked = true;
          stateMachine.getSession().setFirmware(((Acks.Handshake) ack).firmwareVersionString());
          return getInstrument().commands().clear();
        case CLEAR:
          return getInstrument().commands().cycle();
        case CYCLE:
          Acks.Cycle cycle = (Acks.Cycle) ack;
          stateMachine.getSession().setCycle(cycle.cycleTime());
          if(cycle.cycleTime() != 1) {
            return getInstrument().commands().cycle();
          } else {
            stateMachine.transition(States.READY);
          }
          break;
        }
        return null;
      };

      protected BpmCommand onNotification(Notifications.Type type, Notifications.Notification notification) {
        switch(type) {
        case RESET:
          return getInstrument().commands().handshake();
        }
        return null;
      };

      public BpmCommand onNoMessage() {
        if(handshaked == false && System.currentTimeMillis() - startHandshake > 3000) {
          if(getInstrument().isConnected()) {
            startHandshake = System.currentTimeMillis();
            getInstrument().disconnect();
            return getInstrument().commands().handshake();
          }
        }
        return null;
      };
    };
  }

  public States getName() {
    return States.CONNECTING;
  }

  public void start() {
  }

  public void stop() {
  }

  public BpmMessageHandler getMessageHandler() {
    return handler;
  }

}
