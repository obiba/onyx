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

import java.util.Arrays;

public final class BpmMessage {

  private static final byte CMD = 0x11;

  private static final byte RESET = 0x10;

  private static final byte ACK = 0x06;

  private static final byte NACK = 0x15;

  private static final byte BUTTON = 0x55;

  private static final byte NOTIFICATION = 0x21;

  private static final byte[] DATA = new byte[] { 0x41, 0x42, 0x43, 0x44, 0x49, 0x4C, 0x50, 0x52, 0x53, 0x54 };

  public enum Type {
    CMD, ACK, NACK, BUTTON, DATA, NOTIFICATION;
  }

  byte messageId;

  byte data0;

  byte data1;

  byte data2;

  byte data3;

  byte crc;

  public static class Builder {

    private BpmMessage msg = new BpmMessage();

    public static Builder newMessage() {
      return new Builder();
    }

    public BpmMessage build() {
      if(msg.crc == 0) computeCrc();
      else
        validateCrc();
      return msg;
    }

    public Builder messageId(int value) {
      msg.messageId = (byte) value;
      return this;
    }

    public Builder data0(int value) {
      msg.data0 = (byte) value;
      return this;
    }

    public Builder data1(int value) {
      msg.data1 = (byte) value;
      return this;
    }

    public Builder data2(int value) {
      msg.data2 = (byte) value;
      return this;
    }

    public Builder data3(int value) {
      msg.data3 = (byte) value;
      return this;
    }

    public Builder crc(int value) {
      msg.crc = (byte) value;
      return this;
    }

    private void computeCrc() {
      msg.crc = CRC8.calc(msg.bytes(), 5);
    }

    private void validateCrc() {
      if(msg.crc != CRC8.calc(msg.bytes(), 5)) {
        throw new IllegalArgumentException();
      }
    }

  }

  private BpmMessage() {

  }

  public Type getType() {
    switch(messageId()) {
    case RESET:
      return Type.CMD;
    case CMD:
      return Type.CMD;
    case ACK:
      return Type.ACK;
    case NACK:
      return Type.NACK;
    case BUTTON:
      return Type.BUTTON;
    case NOTIFICATION:
      return Type.NOTIFICATION;
    default:
      if(Arrays.binarySearch(DATA, messageId()) >= 0) {
        return Type.DATA;
      }
    }
    throw new IllegalArgumentException("unknown message type: " + encode(this.messageId));
  }

  public byte messageId() {
    return messageId;
  }

  public byte data0() {
    return data0;
  }

  public byte data1() {
    return data1;
  }

  public byte data2() {
    return data2;
  }

  public byte data3() {
    return data3;
  }

  public byte[] bytes() {
    return new byte[] { messageId, data0, data1, data2, data3, crc };
  }

  @Override
  public String toString() {
    return new StringBuilder()//
    .append(encode(this.messageId)).append(" ")//
    .append(encode(this.data0)).append(" ")//
    .append(encode(this.data1)).append(" ")//
    .append(encode(this.data2)).append(" ")//
    .append(encode(this.data3)).append(" ")//
    .append(encode(this.crc)).append(" ")//
    .toString();
  }

  private String encode(byte b) {
    String h = Integer.toHexString(b).replaceFirst("ffffff", "");
    if(h.length() == 1) h = "0" + h;
    return "0x" + h;
  }

}
