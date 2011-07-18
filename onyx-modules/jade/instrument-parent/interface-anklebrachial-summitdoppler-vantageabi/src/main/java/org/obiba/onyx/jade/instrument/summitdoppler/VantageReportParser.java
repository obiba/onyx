/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.summitdoppler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class VantageReportParser {

  protected Logger log = LoggerFactory.getLogger(VantageReportParser.class);

  private String name;

  private Date timestamp;

  private Integer patientHeight;

  private Integer cuffHeight;

  public VantageReportParser() {
    super();
  }

  public void parse(File file) throws IOException {

    BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

    String line = buff.readLine();
    byte[] ba = hexStringToByteArray(line);

    // patient name
    log.info(decodeToString(ba, "0", 32));
    // timestamp
    log.info(decodeToString(ba, "20", 12));

    // pressure index

    byte[] pressures = decodeToBytes(ba, "30", 20);
    for(int i = 0; i < pressures.length; i++) {
      log.info(i + " => " + pressures[i]);
    }

    // left waveform
    decodeToBytes(ba, "60", 400);
    // left clock
    log.info(decodeToString(ba, "01f0", 12));
    // left scale
    log.info(Byte.toString(decodeToByte(ba, "01fc")));

    // left waveform
    decodeToBytes(ba, "0200", 400);
    // right clock
    log.info(decodeToString(ba, "0390", 12));
    // right scale
    log.info(Byte.toString(decodeToByte(ba, "039c")));

  }

  private byte[] decodeToBytes(byte[] ba, String from, int length) {
    return extract(ba, Integer.parseInt(from, 16), length);
  }

  private byte decodeToByte(byte[] ba, String from) {
    return ba[Integer.parseInt(from, 16)];
  }

  private String decodeToString(byte[] ba, String from, int length) {
    return new String(extract(ba, Integer.parseInt(from, 16), length));
  }

  private byte[] extract(byte[] ba, int from, int length) {
    byte[] result = new byte[length];
    for(int i = 0; i < length; i++) {
      result[i] = ba[i + from];
    }
    return result;
  }

  private byte[] hexStringToByteArray(String s) {
    if((s.length() % 2) != 0) throw new IllegalArgumentException("Input string must contain an even number of characters");

    int len = s.length();
    byte[] data = new byte[len / 2];
    for(int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

}
