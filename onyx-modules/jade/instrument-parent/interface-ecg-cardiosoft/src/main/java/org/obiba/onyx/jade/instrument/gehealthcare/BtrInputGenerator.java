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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

  /**
   * Reads a record from a text file and outputs the same record ready to be loaded in the BTRIEVE patient table.
   * <p>
   * The input files is expected to have one attribute per line in the following order:
   * <ul>
   * <li>Primary key: integer</li>
   * <li>First name: string</li>
   * <li>Last name: string</li>
   * <li>Patient ID: string</li>
   * <li>Birth year (yyyy): short</li>
   * <li>Birth month: short</li>
   * <li>Birth day: short</li>
   * <li>Gender: one of {@link Gender} values</li>
   * <li>Weight (dg): short</li>
   * <li>Height (cm): short</li>
   * <li>Ethnicity: one of {@link Ethnicity} values</li>
   * <li>Pacemaker: byte</li>
   * </ul>
   * <p>
   * Notes:
   * <ul>
   * <li>Primary key must be unique when loading the record.</li>
   * <li>Weight is given in decigrams (dg).</li>
   * <li>Pacemaker values are 1 (has one), 0 (does not have one).</li>
   * </ul>
   * <p>
   * The output record is stored in btr-record.dat (overwritten if it exists). The record can be loaded in the
   * PATIENT.BTR database by using butil.exe:
   * 
   * <pre>
   * butil.exe -LOAD btr-record.dat PATIENT.BTR
   * </pre>
   */
  public static void main(String[] args) throws Exception {

    BufferedReader br = new BufferedReader(new FileReader(args[0]));
    int autoinc = Integer.valueOf(br.readLine());
    String firstName = br.readLine();
    String lastName = br.readLine();
    String patientID = br.readLine();
    short birthYear = Short.valueOf(br.readLine());
    short birthMonth = Short.valueOf(br.readLine());
    short birthDay = Short.valueOf(br.readLine());
    Gender gender = Gender.OTHER;
    try {
      gender = Gender.valueOf(br.readLine().toUpperCase());
    } catch(Exception e) {
    }

    short weight = Short.valueOf(br.readLine());
    short height = Short.valueOf(br.readLine());

    Ethnicity ethnicity = Ethnicity.NONE;
    try {
      ethnicity = Ethnicity.valueOf(br.readLine().toUpperCase());
    } catch(Exception e) {
    }

    byte pacemaker = Byte.valueOf(br.readLine());

    // Create a buffer that will hold the record, its header, its separator, and the eof
    ByteBuffer bb = ByteBuffer.allocate(RECORD_SIZE + recordHeader.length + recordEnd.length + fileEnd.length);
    bb.order(ByteOrder.LITTLE_ENDIAN);

    bb.put(recordHeader);
    putString(bb, lastName); // 0-30
    putString(bb, firstName); // 31-61

    bb.putShort(birthYear).putShort(birthMonth).putShort(birthDay); // 62-63,64-65,66-67

    putString(bb, patientID); // 68-98
    bb.putInt(autoinc); // 99-102
    bb.putShort(weight); // 103-104
    WeightUnits.KG.put(bb); // 105-106
    bb.putShort(height); // 107-108
    HeightUnits.CM.put(bb); // 109-110
    gender.put(bb); // 111
    ethnicity.put(bb); // 112-113
    bb.put(pacemaker); // 114

    // The rest is unknown. Either it is unused or its use for internal purposes. Filling it with zeroes is ok.
    fillWithZeroes(bb);
    bb.put(recordEnd).put(fileEnd);

    FileOutputStream fos = new FileOutputStream("btr-record.dat");
    fos.write(bb.array());
    fos.close();
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
