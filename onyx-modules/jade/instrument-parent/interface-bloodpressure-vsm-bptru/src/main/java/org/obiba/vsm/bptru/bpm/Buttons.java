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

import org.obiba.vsm.bptru.bpm.Data.NIBPCode;

public class Buttons {

  public enum Type {
    STOPPED((byte) 0x01) {
      @Override
      @SuppressWarnings("unchecked")
      public Stop getButton(BpmMessage msg) {
        return Stop.instance;
      }
    },
    REVIEW((byte) 0x02) {
      @Override
      @SuppressWarnings("unchecked")
      public Review getButton(BpmMessage msg) {
        return new Review(msg);
      }
    },
    CYCLED((byte) 0x03) {
      @Override
      @SuppressWarnings("unchecked")
      public Cycle getButton(BpmMessage msg) {
        return new Cycle(msg);
      }
    },
    STARTED((byte) 0x04) {
      @Override
      @SuppressWarnings("unchecked")
      public Start getButton(BpmMessage msg) {
        return new Start(msg);
      }
    },
    CLEARED((byte) 0x05) {
      @Override
      @SuppressWarnings("unchecked")
      public Clear getButton(BpmMessage msg) {
        return Clear.instance;
      }
    };

    private byte id;

    private Type(byte id) {
      this.id = id;
    }

    public byte id() {
      return id;
    }

    public abstract <T extends Button> T getButton(BpmMessage msg);
  }

  public static Type forMessage(BpmMessage msg) {
    for(Type type : Type.values()) {
      if(type.id() == msg.data0()) {
        return type;
      }
    }
    return null;
  }

  public static class Button {

    private final BpmMessage message;

    protected Button(BpmMessage message) {
      this.message = message;
    }

    public BpmMessage message() {
      return message;
    }
  }

  public static class Cycle extends Button {
    private Cycle(BpmMessage msg) {
      super(msg);
    }

    public int cycleTime() {
      return message().data1();
    }
  }

  public static class Start extends Cycle {
    private Start(BpmMessage msg) {
      super(msg);
    }

    public int readingNumber() {
      return message().data2();
    }
  }

  public static class Review extends Start {
    private Review(BpmMessage msg) {
      super(msg);
    }

    public NIBPCode resultCode() {
      return new NIBPCode(message().data3());
    }
  }

  public static class Stop extends Button {
    static final Stop instance = new Stop();

    private Stop() {
      super(null);
    }
  }

  public static class Clear extends Button {
    static final Clear instance = new Clear();

    private Clear() {
      super(null);
    }
  }

}
