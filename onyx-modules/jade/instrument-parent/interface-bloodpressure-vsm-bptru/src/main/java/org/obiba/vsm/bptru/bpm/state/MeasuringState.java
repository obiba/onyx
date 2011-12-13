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

import java.util.Date;

import org.obiba.vsm.bptru.bpm.Acks;
import org.obiba.vsm.bptru.bpm.BpmCommands.BpmCommand;
import org.obiba.vsm.bptru.bpm.BpmInstrument;
import org.obiba.vsm.bptru.bpm.BpmMessageHandler;
import org.obiba.vsm.bptru.bpm.BpmMessageHandler.DefaultBpmMessageHandler;
import org.obiba.vsm.bptru.bpm.Buttons;
import org.obiba.vsm.bptru.bpm.Data;
import org.obiba.vsm.bptru.bpm.Notifications;

public class MeasuringState implements State {

  private final StateMachine stateMachine;

  private final BpmMessageHandler handler;

  private Date startTime;

  private boolean complete = false;

  public MeasuringState(StateMachine machine, BpmInstrument instrument) {
    this.stateMachine = machine;
    this.handler = new DefaultBpmMessageHandler(instrument) {

      protected BpmCommand onAck(Acks.Type type, Acks.Ack ack) {
        switch(type) {
        case CLEAR:
          stateMachine.transition(States.READY);
          break;
        case STOP:
          endMeasures();
          break;
        }
        return null;
      };

      protected BpmCommand onButton(Buttons.Type type, Buttons.Button button) {
        switch(type) {
        case STOPPED:
          endMeasures();
          break;
        }
        return null;
      };

      protected BpmCommand onData(Data.Type type, Data.Datum datum) {
        switch(type) {
        case INFL_CUFF_PRESSURE:
          if(startTime == null) startReading();
        case DEFL_CUFF_PRESSURE:
          Data.CuffPressure pressure = (Data.CuffPressure) datum;
          stateMachine.getSession().setCuffPressure(pressure.pressure());
          break;
        case BP_RESULT:
          Data.BloodPressure bp = (Data.BloodPressure) datum;
          endReading(bp);
          if(stateMachine.getSession().getCycle() == 0) {
            endMeasures();
          }
          break;
        case BP_AVG:
          stateMachine.getSession().addAverage((Data.AvgPressure) datum);
          complete = true;
          break;
        case REVIEW:
          Data.Review review = (Data.Review) datum;
          if(complete) {
            endMeasures();
          }
          break;
        }
        return null;
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
    return States.MEASURING;
  }

  public void start() {
  }

  public void add() {
  }

  public void stop() {
    stateMachine.getInstrument().commands().stop().send();
  }

  public BpmMessageHandler getMessageHandler() {
    return handler;
  }

  private void startReading() {
    startTime = new Date();
  }

  private void endReading(Data.BloodPressure bp) {
    stateMachine.getSession().incrementReading();
    stateMachine.getSession().addResult(startTime, new Date(), bp);
    startTime = null;
  }

  private void endMeasures() {
    stateMachine.getSession().setCuffPressure(0);
    stateMachine.getInstrument().commands().clear().send();
  }

}
