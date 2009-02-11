/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.gehealthcare;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.obiba.onyx.util.data.Data;

/**
 * 
 */
public class BtrInputGenerator {

  /** The size of a record's data */
  private static final int RECORD_SIZE = 145;

  /** The bytes that represents the record's header */
  private static byte[] recordHeader = getStringBytes(RECORD_SIZE + ",");

  /** The bytes that represents the record's end (separator when multiple records in the same file) */
  private static byte[] recordEnd = { (byte) 0x0D, (byte) 0x0A };

  /** The bytes that represents the EOF */
  private static byte[] fileEnd = { (byte) 0x1A };

  /** Useful constant */
  private static byte ZERO = 0;

  private enum WeightUnits {
    // The KG unit is actually DG, but the interface will convert the value to KG, so using KG here is more meaningful
    KG, LBS;
    public void put(ByteBuffer bb) {
      bb.putShort((short) ordinal());
    }
  }

  private enum HeightUnits {
    CM, IN;
    public void put(ByteBuffer bb) {
      bb.putShort((short) ordinal());
    }
  }

  private enum Gender {
    OTHER, MALE, FEMALE;
    public void put(ByteBuffer bb) {
      bb.put((byte) ordinal());
    }
  }

  private enum Ethnicity {
    NONE, CAUCASIAN, BLACK, ASIAN, ORIENTAL, HISPANIC, NATIVE, INUIT, POLYNESIAN, PACIFIC_ISLAND, MOGOLIAN, IDIAN;

    public void put(ByteBuffer bb) {
      bb.putShort((short) ordinal());
    }
  }

  public BtrInputGenerator() {
    // constructor
  }

  public ByteBuffer generateByteBuffer(Map<String, Data> inputData) {

    // Create a buffer that will hold the record, its header, its separator, and the eof
    ByteBuffer bb = ByteBuffer.allocate(RECORD_SIZE + recordHeader.length + recordEnd.length + fileEnd.length);
    bb.order(ByteOrder.LITTLE_ENDIAN);

    bb.put(recordHeader);
    putString(bb, inputData.get("INPUT_PARTICIPANT_LAST_NAME").getValueAsString()); // 0-30
    putString(bb, inputData.get("INPUT_PARTICIPANT_FIRST_NAME").getValueAsString()); // 31-61

    bb.putShort(Short.valueOf(inputData.get("INPUT_PARTICIPANT_BIRTH_YEAR").getValueAsString())).putShort(Short.valueOf(inputData.get("INPUT_PARTICIPANT_BIRTH_MONTH").getValueAsString())).putShort(Short.valueOf(inputData.get("INPUT_PARTICIPANT_BIRTH_DAY").getValueAsString())); // 62-63,64-65,66-67

    putString(bb, inputData.get("INPUT_PARTICIPANT_BARCODE").getValueAsString()); // 68-98
    bb.putInt(Integer.valueOf(inputData.get("INPUT_PARTICIPANT_BARCODE").getValueAsString())); // 99-102

    Data weightData = inputData.get("INPUT_PARTICIPANT_WEIGHT");
    if(weightData.getValue() != null) {
      bb.putShort(Short.valueOf(weightData.getValueAsString()));
    } else {
      bb.putShort(new Short("0"));
    }
    WeightUnits.KG.put(bb); // 105-106

    Data heightData = inputData.get("INPUT_PARTICIPANT_HEIGHT");
    if(heightData.getValue() != null) {
      bb.putShort(Short.valueOf(heightData.getValueAsString()));
    } else {
      bb.putShort(new Short("0"));
    }
    HeightUnits.CM.put(bb); // 109-110

    Gender gender = Gender.valueOf(inputData.get("INPUT_PARTICIPANT_GENDER").getValueAsString());
    gender.put(bb); // 111
    Ethnicity ethnicity = Ethnicity.valueOf(inputData.get("INPUT_PARTICIPANT_ETHNIC_GROUP").getValueAsString());
    ethnicity.put(bb); // 112-113
    bb.put(Byte.valueOf(inputData.get("INPUT_PARTICIPANT_PACEMAKER").getValueAsString()));

    // The rest is unknown. Either it is unused or its use for internal purposes. Filling it with zeroes is ok.
    fillWithZeroes(bb);
    bb.put(recordEnd).put(fileEnd);
    return bb;
  }

  private static void fillWithZeroes(ByteBuffer bb) {
    // Fill the rest of the record (everything except the record end and file end)
    while(bb.remaining() > recordEnd.length + fileEnd.length) {
      bb.put(ZERO);
    }
  }

  private static void putString(ByteBuffer bb, String s) {
    byte b[] = getStringBytes(s);

    for(int i = 0; i < 31; i++) {
      if(b != null && i < b.length) {
        bb.put(b[i]);
      } else {
        bb.put(ZERO);
      }
    }
  }

  private static byte[] getStringBytes(String s) {
    if(s == null || s.length() == 0) {
      return new byte[0];
    }

    try {
      // I have no idea what encoding they use. We should test with accentuated characters.
      // ISO-8859-1 is a likely candidate.
      return s.getBytes("ISO-8859-1");
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
