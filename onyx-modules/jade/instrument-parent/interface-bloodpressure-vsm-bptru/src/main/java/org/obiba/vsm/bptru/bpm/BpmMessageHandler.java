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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface BpmMessageHandler {

  public BpmCommand onStart();

  public BpmCommand onBpmMessage(BpmMessage message);

  public BpmCommand onNoMessage();

  public class DefaultBpmMessageHandler implements BpmMessageHandler {

    private static final Logger log = LoggerFactory.getLogger("vsm.bptru.bpm");

    private final BpmInstrument instrument;

    public DefaultBpmMessageHandler(BpmInstrument instrument) {
      this.instrument = instrument;
    }

    public BpmCommand onBpmMessage(BpmMessage msg) {
      log.debug("received: {}", msg);
      BpmCommand cmd = null;
      switch(msg.getType()) {
      case ACK:
        Acks.Type ack = Acks.forMessage(msg);
        if(ack == null) return null;
        log.debug("=>Ack: {}", ack);
        cmd = onAck(ack, ack.getAck(msg));
        break;
      case NACK:
        cmd = onNack(msg);
        break;
      case BUTTON:
        Buttons.Type button = Buttons.forMessage(msg);
        if(button == null) return null;
        log.debug("=>Button: {}", button);
        cmd = onButton(button, button.getButton(msg));
        break;
      case DATA:
        Data.Type data = Data.forMessage(msg);
        if(data == null) return null;
        log.debug("=>Data: {}", data);
        cmd = onData(data, data.getDatum(msg));
        break;
      case NOTIFICATION:
        Notifications.Type notification = Notifications.forMessage(msg);
        if(notification == null) return null;
        log.debug("=>Notification: {}", notification);
        cmd = onNotification(notification, notification.getNotification(msg));
        break;
      }
      return cmd;
    }

    public BpmCommand onStart() {
      return null;
    }

    public BpmCommand onNoMessage() {
      return null;
    }

    protected BpmInstrument getInstrument() {
      return this.instrument;
    }

    protected BpmCommand onAck(Acks.Type type, Acks.Ack ack) {
      return null;
    }

    protected BpmCommand onNack(BpmMessage message) {
      return null;
    }

    protected BpmCommand onButton(Buttons.Type type, Buttons.Button button) {
      return null;
    }

    protected BpmCommand onData(Data.Type type, Data.Datum datum) {
      return null;
    }

    protected BpmCommand onNotification(Notifications.Type type, Notifications.Notification notification) {
      return null;
    }
  }

}
