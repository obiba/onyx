/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.vsm.bptru.bpm;

import org.obiba.vsm.bptru.bpm.BpmCommands.BpmCommand;
import org.obiba.vsm.bptru.bpm.BpmMessageHandler.DefaultBpmMessageHandler;

public class BpmMessageLoop implements Runnable {

  private final BpmInstrument instrument;

  private boolean stop = false;

  public BpmMessageLoop(BpmInstrument instrument) {
    this.instrument = instrument;
  }

  public BpmInstrument getInstrument() {
    return instrument;
  }

  protected BpmMessageHandler getCurrentHandler() {
    return new DefaultBpmMessageHandler(getInstrument());
  }

  public void run() {
    instrument.connect();
    try {
      BpmCommand cmd = getCurrentHandler().onStart();
      if(cmd != null) {
        cmd.send();
      }
      while(!stop) {
        loop();
      }
    } finally {
      instrument.close();
    }
  }

  public void exit() {
    this.stop = true;
  }

  public void loop() {
    BpmMessage msg = instrument.read();
    if(msg != null) {
      BpmCommand cmd = getCurrentHandler().onBpmMessage(msg);
      if(cmd != null) {
        cmd.send();
      }
    } else {
      BpmCommand cmd = getCurrentHandler().onNoMessage();
      if(cmd != null) {
        cmd.send();
      }
    }
  }
}
