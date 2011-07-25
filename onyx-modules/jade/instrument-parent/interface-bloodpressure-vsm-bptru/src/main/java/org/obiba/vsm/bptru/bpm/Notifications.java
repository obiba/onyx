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

public class Notifications {

  public enum Type {
    RESET((byte) 0x08) {
      @Override
      @SuppressWarnings("unchecked")
      public Reset getNotification(BpmMessage msg) {
        return new Reset(msg);
      }
    };

    private byte id;

    private Type(byte id) {
      this.id = id;
    }

    public byte id() {
      return id;
    }

    public abstract <T extends Notification> T getNotification(BpmMessage msg);
  }

  public static Type forMessage(BpmMessage msg) {
    for(Type type : Type.values()) {
      if(type.id() == msg.data0()) {
        return type;
      }
    }
    return null;
  }

  public static class Notification {

    private final BpmMessage message;

    protected Notification(BpmMessage message) {
      this.message = message;
    }

    public BpmMessage message() {
      return message;
    }
  }

  public static class Reset extends Notification {

    private Reset(BpmMessage message) {
      super(message);
    }

    public boolean isPowerUp() {
      return (message().data1() & 0x01) == 0;
    }

  }

}
