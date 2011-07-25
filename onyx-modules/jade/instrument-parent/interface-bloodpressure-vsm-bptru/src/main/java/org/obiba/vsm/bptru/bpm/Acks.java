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

public class Acks {

  public enum Type {
    HANDSHAKE((byte) 0x00) {
      @Override
      @SuppressWarnings("unchecked")
      public Handshake getAck(BpmMessage msg) {
        return new Handshake(msg);
      }
    },
    STOP((byte) 0x01) {
      @Override
      @SuppressWarnings("unchecked")
      public Stop getAck(BpmMessage msg) {
        return new Stop(msg);
      }
    },
    REVIEW((byte) 0x02) {
      @Override
      @SuppressWarnings("unchecked")
      public Review getAck(BpmMessage msg) {
        return new Review(msg);
      }
    },
    CYCLE((byte) 0x03) {
      @Override
      @SuppressWarnings("unchecked")
      public Cycle getAck(BpmMessage msg) {
        return new Cycle(msg);
      }
    },
    START((byte) 0x04) {
      @Override
      @SuppressWarnings("unchecked")
      public Start getAck(BpmMessage msg) {
        return new Start(msg);
      }
    },
    CLEAR((byte) 0x05) {
      @Override
      @SuppressWarnings("unchecked")
      public Clear getAck(BpmMessage msg) {
        return new Clear(msg);
      }
    };

    private byte id;

    private Type(byte id) {
      this.id = id;
    }

    public byte id() {
      return id;
    }

    public abstract <T extends Ack> T getAck(BpmMessage msg);
  }

  public static Type forMessage(BpmMessage msg) {
    for(Type type : Type.values()) {
      if(type.id() == msg.data0()) {
        return type;
      }
    }
    return null;
  }

  public static class Ack {

    private final BpmMessage message;

    protected Ack(BpmMessage message) {
      this.message = message;
    }

    public BpmMessage message() {
      return message;
    }
  }

  public static class Handshake extends Ack {

    private Handshake(BpmMessage message) {
      super(message);
    }

    public int firmwareVersion() {
      return message().data1() * 100 + message().data2() * 10 + message().data3();
    }

    public String firmwareVersionString() {
      return new StringBuilder().append(message().data1()).append('.').append(message().data2() * 10 + message().data3()).toString();
    }
  }

  public static class Stop extends Ack {

    private Stop(BpmMessage message) {
      super(message);
    }
  }

  public static class Review extends Start {

    private Review(BpmMessage message) {
      super(message);
    }

    public byte resultCode() {
      return message().data3();
    }
  }

  public static class Cycle extends Ack {

    private Cycle(BpmMessage message) {
      super(message);
    }

    public int cycleTime() {
      return message().data1();
    }
  }

  public static class Start extends Cycle {

    private Start(BpmMessage message) {
      super(message);
    }

    public int readingNumber() {
      return message().data2();
    }
  }

  public static class Clear extends Ack {

    private Clear(BpmMessage message) {
      super(message);
    }
  }
}
