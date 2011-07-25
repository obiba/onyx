/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.vsm.bptru.bpm.bpm200;

import org.obiba.vsm.bptru.bpm.BpmCommands;
import org.obiba.vsm.bptru.bpm.BpmMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bpm200Commands implements BpmCommands {

  private static final Logger log = LoggerFactory.getLogger("vsm.bptru.bpm");

  private static final BpmMessage Reset = newCommand(0x10, 0xFF);

  private static final BpmMessage Handshake = newCommand(0x11, 0x00);

  private static final BpmMessage NIBPStop = newCommand(0x11, 0x01);

  private static final BpmMessage NIBPReview = newCommand(0x11, 0x02);

  private static final BpmMessage NIBPCycle = newCommand(0x11, 0x03);

  private static final BpmMessage NIBPStart = newCommand(0x11, 0x04);

  private static final BpmMessage NIBPClear = newCommand(0x11, 0x05);

  private static final BpmMessage DisablePressures = newCommand(0x11, 0x08, 0x01);

  private static final BpmMessage EnablePressures = newCommand(0x11, 0x08);

  private static final BpmMessage RetreiveLastResult = newCommand(0x11, 0x09);

  private final Bpm200 bpm;

  public Bpm200Commands(Bpm200 bpm200) {
    this.bpm = bpm200;
  }

  @Override
  public BpmCommand disconnect() {
    return new BpmCommand() {
      @Override
      public void send() {
        bpm.disconnect();
      }
    };
  }

  @Override
  public BpmCommand connect() {
    return new BpmCommand() {
      @Override
      public void send() {
        bpm.connect();
      }
    };
  }

  public BpmCommand reset() {
    return new BpmCommandImpl(Reset);
  }

  public BpmCommand handshake() {
    return new BpmCommandImpl(Handshake);
  }

  public BpmCommand stop() {
    return new BpmCommandImpl(NIBPStop);
  }

  public BpmCommand start() {
    return new BpmCommandImpl(NIBPStart);
  }

  public BpmCommand cycle() {
    return new BpmCommandImpl(NIBPCycle);
  }

  public BpmCommand review() {
    return new BpmCommandImpl(NIBPReview);
  }

  public BpmCommand clear() {
    return new BpmCommandImpl(NIBPClear);
  }

  public BpmCommand disablePressures() {
    return new BpmCommandImpl(DisablePressures);
  }

  public BpmCommand enablePressures() {
    return new BpmCommandImpl(EnablePressures);
  }

  public BpmCommand retriveLastResult() {
    return new BpmCommandImpl(RetreiveLastResult);
  }

  private static BpmMessage newCommand(int messageId, int command) {
    return BpmMessage.Builder.newMessage().messageId(messageId).data0(command).build();
  }

  private static BpmMessage newCommand(int messageId, int command, int data1) {
    return BpmMessage.Builder.newMessage().messageId(messageId).data0(command).data1(data1).build();
  }

  private class BpmCommandImpl implements BpmCommand {

    private final BpmMessage command;

    BpmCommandImpl(BpmMessage command) {
      this.command = command;
    }

    public void send() {
      log.debug("sending: {}", command);
      bpm.send(command);
    }

  }
}
