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

public class Data {

  public enum Type {
    INFL_CUFF_PRESSURE((byte) 0x49) {
      @Override
      @SuppressWarnings("unchecked")
      public CuffPressure getDatum(BpmMessage msg) {
        return new CuffPressure(msg);
      }
    },
    DEFL_CUFF_PRESSURE((byte) 0x44) {
      @Override
      @SuppressWarnings("unchecked")
      public CuffPressure getDatum(BpmMessage msg) {
        return new CuffPressure(msg);
      }
    },
    BP_RESULT((byte) 0x42) {
      @Override
      @SuppressWarnings("unchecked")
      public BloodPressure getDatum(BpmMessage msg) {
        return new BloodPressure(msg);
      }
    },
    BP_AVG((byte) 0x41) {
      @Override
      @SuppressWarnings("unchecked")
      public AvgPressure getDatum(BpmMessage msg) {
        return new AvgPressure(msg);
      }
    },
    REVIEW((byte) 0x52) {
      @Override
      @SuppressWarnings("unchecked")
      public Review getDatum(BpmMessage msg) {
        return new Review(msg);
      }
    };

    private byte id;

    private Type(byte id) {
      this.id = id;
    }

    public byte id() {
      return id;
    }

    public abstract <T extends Datum> T getDatum(BpmMessage msg);
  }

  public static Type forMessage(BpmMessage msg) {
    for(Type type : Type.values()) {
      if(type.id() == msg.messageId()) {
        return type;
      }
    }
    return null;
  }

  public static class Datum {

    private final BpmMessage message;

    protected Datum(BpmMessage message) {
      this.message = message;
    }

    public BpmMessage message() {
      return message;
    }
  }

  public static class HasBloodPressure extends Datum {
    private HasBloodPressure(BpmMessage msg) {
      super(msg);
    }

    public int sbp() {
      return message().data1() & 0x000000FF;
    }

    public int dbp() {
      return message().data2() & 0x000000FF;
    }

    public int pulse() {
      return message().data3() & 0x000000FF;
    }
  }

  public static class CuffPressure extends Datum {
    private CuffPressure(BpmMessage msg) {
      super(msg);
    }

    public int pressure() {
      int ls = message().data0() & 0x000000FF;
      int ms = (message().data1() << 8) & 0x0000FF00;
      return ls + ms;
    }
  }

  public static class BloodPressure extends HasBloodPressure {
    private BloodPressure(BpmMessage msg) {
      super(msg);
    }

    public boolean hasError() {
      return code().hasError();
    }

    public NIBPCode.Error sbpError() {
      return code().sbpError(message().data1());
    }

    public NIBPCode.Error dbpError() {
      return code().dbpError(message().data2());
    }

    public NIBPCode.Error pulseError() {
      return code().pulseError(message().data3());
    }

    public NIBPCode code() {
      return new NIBPCode(message().data0());
    }

  }

  public static class AvgPressure extends HasBloodPressure {
    private AvgPressure(BpmMessage msg) {
      super(msg);
    }

    public int count() {
      return message().data0();
    }

  }

  public static class Review extends HasBloodPressure {
    private Review(BpmMessage msg) {
      super(msg);
    }

    public int reading() {
      return message().data0();
    }

    public boolean isAvg() {
      return reading() == 0;
    }
  }

  public static class NIBPCode {

    public enum Error {
      INDETERMINATE, UNDER_RANGE, OVER_RANGE, ARITHMETIC_ERROR
    }

    final byte code;

    public NIBPCode(byte code) {
      this.code = code;
    }

    public boolean hasError() {
      return hasSystemError() || hasSbpError() || hasDbpError() || hasPulseError();
    }

    public boolean hasSystemError() {
      return (code & 1 << 7) == 1;
    }

    public boolean hasSbpError() {
      return (code & 1 << 6) == 1;
    }

    public Error sbpError(byte code) {
      switch(code) {
      case 0x14:
        return Error.INDETERMINATE;
      case 0x17:
        return Error.UNDER_RANGE;
      case 0x19:
        return Error.OVER_RANGE;
      case 0x21:
        return Error.ARITHMETIC_ERROR;
      }
      throw new IllegalArgumentException("unknown error code " + code);
    }

    public boolean hasDbpError() {
      return (code & 1 << 5) == 1;
    }

    public Error dbpError(byte code) {
      switch(code) {
      case 0x15:
        return Error.INDETERMINATE;
      case 0x18:
        return Error.UNDER_RANGE;
      case 0x20:
        return Error.OVER_RANGE;
      case 0x21:
        return Error.ARITHMETIC_ERROR;
      }
      throw new IllegalArgumentException("unknown error code " + code);
    }

    public boolean hasPulseError() {
      return (code & 1 << 4) == 1;
    }

    public Error pulseError(byte code) {
      switch(code) {
      case 0x16:
        return Error.INDETERMINATE;
      case 0x21:
        return Error.ARITHMETIC_ERROR;
      }
      throw new IllegalArgumentException("unknown error code " + code);
    }

    public byte systemErrorCode() {
      return (byte) (code & (byte) 0x0F);
    }
  }

}
