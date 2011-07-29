/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.paradox;

import java.util.Arrays;
import java.util.Calendar;

public enum ParadoxFieldType {

  Alpha(0x01) {

    @Override
    public Object parse(byte[] bytes) {
      StringBuilder sb = new StringBuilder();
      for(byte b : bytes) {
        if(b == 0) break;
        sb.append((char) b);
      }
      return sb.toString();
    }

  },
  Date(0x02) {
    @Override
    public Object parse(byte[] bytes) {
      Long daysSinceEpoch = asNumber(bytes);
      if(daysSinceEpoch == null) return null;
      Calendar c = Calendar.getInstance();
      c.clear();
      // When using Jan-1-0001, dates are always off by one. I don't know why.
      c.set(1, 0, 2);
      if(daysSinceEpoch > Integer.MAX_VALUE) {
        c.add(Calendar.DATE, Integer.MAX_VALUE);
        daysSinceEpoch -= Integer.MAX_VALUE;
      }
      c.add(Calendar.DATE, daysSinceEpoch.intValue());
      return c.getTime();
    }

  },
  ShortInteger(0x03) {
    @Override
    public Object parse(byte[] bytes) {
      return asNumber(bytes);
    }
  },
  LongInteger(0x04) {
    @Override
    public Object parse(byte[] bytes) {
      return asNumber(bytes);
    }
  },
  Currency(0x05), Number(0x06), Logical(0x09) {
    @Override
    public Object parse(byte[] bytes) {
      Long number = asNumber(bytes);
      return number != null ? number.intValue() : null;
    }
  },
  MemoBlob(0x0C), BLOB(0x0D), FormatedMemoBlob(0x0E), OLE(0x0F), GraphicBlob(0x10), Time(0x14), Timestamp(0x15), AutoInc(0x16), BCD(0x17), Bytes(0x18);

  private int value;

  private ParadoxFieldType(int value) {
    this.value = value;
  }

  public Object parse(byte[] bytes) {
    return null;
  }

  private static byte[] fixSign(byte[] bytes) {
    byte[] fixed = Arrays.copyOf(bytes, bytes.length);
    if((fixed[0] & 0x80) == 0x80) {
      fixed[0] &= 0x7F;
    }
    return fixed;
  }

  private static Long asNumber(byte[] bytes) {
    byte[] fixed = fixSign(bytes);
    long value = 0;
    int leftShift = (bytes.length - 1) * 8;
    int mask = 0x000000FF;
    for(int i = 0; i < bytes.length; i++) {
      value |= fixed[i] << leftShift & (mask << leftShift);
      leftShift -= 8;
    }
    return (bytes[0] & 0x80) == 0x80 ? new Long(value) : (value == 0 ? null : new Long(-value));
  }

  static ParadoxFieldType forType(int value) {
    for(ParadoxFieldType type : ParadoxFieldType.values()) {
      if(type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("unknown field type " + value);
  }
}
