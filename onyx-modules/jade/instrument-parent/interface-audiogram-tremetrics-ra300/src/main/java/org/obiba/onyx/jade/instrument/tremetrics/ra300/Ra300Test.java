/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.tremetrics.ra300;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;

public class Ra300Test {

  private final byte[] msg;

  Ra300Test(byte[] msg) {
    if(msg == null) throw new IllegalArgumentException("msg cannot be null");
    if(msg.length < 150) throw new RuntimeException("message too small");
    if(msg[149] != 0x0d) throw new RuntimeException("expedted 0x0d");
    if(msg[146] != 0x17) throw new RuntimeException("expedted 0x17");
    if(msg[145] != (byte) 'p') throw new RuntimeException("expedted p");
    if(msg[144] != (byte) '~') throw new RuntimeException("expedted ~");
    // TODO validate checksum (147-148)
    this.msg = msg;
  }

  public byte getFlag() {
    return msg[1];
  }

  public String getPatientId() {
    return readString(3, 17);
  }

  public int getTestType() {
    return 0;
  }

  public String getTestId() {
    return readString(19, 34);
  }

  public Date getTestDatetime() {
    String date = readString(35, 50);
    try {
      // We've seen this in the wild. Not sure why that is: 06/21/12A3:45:23
      if(date.contains("A")) {
        date = date.replace('A', '1');
      }
      return new SimpleDateFormat("MM/dd/yyHH:mm:ss").parse(date);
    } catch(ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public Date getCalibrationDate() {
    String date = readString(51, 58);
    try {
      return new SimpleDateFormat("MM/dd/yy").parse(date);
    } catch(ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public String getExaminerId() {
    return readString(59, 73);
  }

  public HTL getHTLLeft() {
    return new HTL(readString(74, 105));
  }

  public HTL getHTLRight() {
    return new HTL(readString(106, 137));
  }

  public String getExtra() {
    return readString(138, 143);
  }

  @Override
  public String toString() {
    return new StringBuilder()//
    .append(getFlag()).append(',')//
    .append(getPatientId()).append(',')//
    .append(getTestType()).append(',')//
    .append(getTestId()).append(',')//
    .append(getTestDatetime()).append(',')//
    .append(getCalibrationDate()).append(',')//
    .append(getExaminerId()).append(',')//
    .append(getHTLLeft()).append(',')//
    .append(getHTLRight()).append(',')//
    .append(getExtra())//
    .toString();
  }

  private String readString(int start, int stop) {
    StringBuilder sb = new StringBuilder();
    for(int i = start; i <= stop; i++) {
      sb.append((char) msg[i]);
    }
    return sb.toString();

  }

  public static enum HtlCode {

    NOT_TESTED("AA", Outcome.RUN_TEST),

    DELETED("DD", Outcome.NONE),

    CONTRALATERAL_RECORDED("EA", Outcome.RERUN_TEST),

    BASELINE_SHIFT("EB", Outcome.RERUN_TEST),

    ADJACENT_FREQ("EC", Outcome.RERUN_TEST),

    OUT_OF_RANGE("ED", Outcome.RERUN_TEST),

    NO_RESPONSE("EE", Outcome.RERUN_TEST),

    NO_THRESHOLD("EF", Outcome.RERUN_TEST),

    NO_RESPONSE_1K("E1", Outcome.REINSTRUCT_SUBJECT),

    NO_THREHOLD_1K("E2", Outcome.REINSTRUCT_SUBJECT),

    VERIFY_FAILED_1K("E3", Outcome.REINSTRUCT_SUBJECT),

    HANDSWITCH_ERROR("E4", Outcome.REINSTRUCT_SUBJECT),

    RESPONSE_NO_TONE("E5", Outcome.REINSTRUCT_SUBJECT),

    NO_THRESHOLD_AGAIN("E6", Outcome.REINSTRUCT_SUBJECT),

    TOO_MANY_FAILURES("E7", Outcome.REINSTRUCT_SUBJECT),

    EQUIPMENT_ERROR("E8", Outcome.CONTACT_SERVICE)

    ;

    private final String code;

    private final Outcome outcome;

    private HtlCode(String code, Outcome outcome) {
      this.code = code;
      this.outcome = outcome;
    }

    public String getCode() {
      return code;
    }

    public Outcome getOutcome() {
      return outcome;
    }

    public boolean isCode(String code) {
      return this.code.equals(code);
    }
  }

  public static enum Outcome {
    RUN_TEST, NONE, RERUN_TEST, REINSTRUCT_SUBJECT, CONTACT_SERVICE
  }

  public static enum Frequency {
    _1KT, _500, _1K, _2K, _3K, _4K, _6K, _8K
  }

  public static class HTL {

    private final String[] values;

    private HTL(String values) {
      this.values = values.trim().split(" +");
    }

    public boolean wasTested(Frequency freq) {
      return HtlCode.NOT_TESTED.isCode(value(freq)) == false;
    }

    public boolean wasDeleted(Frequency freq) {
      return HtlCode.DELETED.isCode(value(freq));
    }

    public boolean hasError(Frequency freq) {
      EnumSet<HtlCode> errors = EnumSet.range(HtlCode.CONTRALATERAL_RECORDED, HtlCode.EQUIPMENT_ERROR);
      for(HtlCode code : errors) {
        if(code.isCode(value(freq))) return true;
      }
      return false;
    }

    public String getError(Frequency freq) {
      EnumSet<HtlCode> errors = EnumSet.range(HtlCode.CONTRALATERAL_RECORDED, HtlCode.EQUIPMENT_ERROR);
      for(HtlCode code : errors) {
        if(code.isCode(value(freq))) return code.toString();
      }
      return "";
    }

    public int getLevel(Frequency freq) {
      if(hasValue(freq)) {
        return Integer.parseInt(value(freq));
      }
      return -1;
    }

    public String value(Frequency freq) {
      if(freq == null) throw new IllegalArgumentException("freq cannot be null");
      return values[freq.ordinal()];
    }

    public boolean hasValue(Frequency freq) {
      return wasTested(freq) && wasDeleted(freq) == false && hasError(freq) == false;
    }

    @Override
    public String toString() {
      return Arrays.toString(values);
    }

  }

}
