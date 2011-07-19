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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class VantageReportParser {

  private final int SEGMENT_LENGTH = 1024;

  private final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("MMddyyHHmmss");

  protected Logger log = LoggerFactory.getLogger(VantageReportParser.class);

  private List<ExamData> examDatas;

  public VantageReportParser() {
    super();
  }

  public int getExamCount() {
    return examDatas == null ? 0 : examDatas.size();
  }

  public ExamData getExamData(int index) {
    return examDatas.get(index);
  }

  public Iterable<ExamData> getExamDatas() {
    return examDatas;
  }

  public void parse(File file) throws IOException {

    BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

    String line = buff.readLine();
    int[] ba = hexStringToByteArray(line);
    int examCount = ba.length / SEGMENT_LENGTH;

    examDatas = new ArrayList<VantageReportParser.ExamData>();

    for(int exam = 0; exam < examCount; exam++) {

      ExamData examData = new ExamData();
      examDatas.add(examData);

      // patient name
      examData.setName(decodeToString(ba, exam, "0", 32));
      // timestamp
      examData.setTimestamp(decodeToString(ba, exam, "20", 12));

      // pressure index
      int[] pressures = decodeToInts(ba, exam, "30", 20);
      // for(int i = 0; i < pressures.length; i++) {
      // log.info(i + " => " + pressures[i]);
      // }

      // left
      SideData side = new SideData();
      examData.setLeft(side);
      // left pressures
      side.setBrachial(pressures[8]);
      side.setAnkle(pressures[10]);
      side.setIndex(((double) pressures[14]) / 100);
      // left waveform
      side.setWaveForm(decodeToBytes(ba, exam, "60", 400));
      // left clock
      side.setClock(decodeToString(ba, exam, "01f0", 12));
      // left scale
      side.setScale(decodeToByte(ba, exam, "01fc"));

      // right
      side = new SideData();
      examData.setRight(side);
      // right pressures
      side.setBrachial(pressures[2]);
      side.setAnkle(pressures[4]);
      side.setIndex(((double) pressures[12]) / 100);
      // right waveform
      side.setWaveForm(decodeToBytes(ba, exam, "0200", 400));
      // right clock
      side.setClock(decodeToString(ba, exam, "0390", 12));
      // right scale
      side.setScale(decodeToByte(ba, exam, "039c"));

      log.info(examData.toString());
    }
  }

  private int[] decodeToInts(int[] ba, int exam, String from, int length) {
    return extract(ba, exam, Integer.parseInt(from, 16), length);
  }

  private byte[] decodeToBytes(int[] ba, int exam, String from, int length) {
    int[] values = extract(ba, exam, Integer.parseInt(from, 16), length);
    byte[] result = new byte[values.length];

    for(int i = 0; i < values.length; i++) {
      result[i] = (byte) values[i];
    }

    return result;
  }

  private byte decodeToByte(int[] ba, int exam, String from) {
    return (byte) ba[Integer.parseInt(from, 16) + exam * SEGMENT_LENGTH];
  }

  private String decodeToString(int[] ba, int exam, String from, int length) {
    int[] sba = extract(ba, exam, Integer.parseInt(from, 16), length);
    int size = 0;
    // find the end of the string in the byte chunk
    for(int i = 0; i < sba.length; i++) {
      if(sba[i] != 0) {
        size++;
      } else {
        break;
      }
    }

    return new String(sba, 0, size);
  }

  private int[] extract(int[] ba, int exam, int from, int length) {
    int[] result = new int[length];
    for(int i = 0; i < length; i++) {
      result[i] = ba[i + exam * SEGMENT_LENGTH + from];
    }
    return result;
  }

  private int[] hexStringToByteArray(String s) {
    if((s.length() % 2) != 0) throw new IllegalArgumentException("Input string must contain an even number of characters");

    int len = s.length();
    int[] data = new int[len / 2];
    for(int i = 0; i < len; i += 2) {
      data[i / 2] = ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  private Date parseTimestamp(String timestamp) {
    if(timestamp == null || timestamp.equals("000000000000")) return null;

    try {
      return TIMESTAMP_FORMAT.parse(timestamp);
    } catch(ParseException e) {
      log.error("Unable to parse timestamp: " + timestamp, e);
      return null;
    }
  }

  public class ExamData {
    private String name;

    private Date timestamp;

    private SideData left;

    private SideData right;

    public ExamData() {
      super();
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Date getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
    }

    public void setTimestamp(String timestamp) {
      this.timestamp = parseTimestamp(timestamp);
    }

    public SideData getLeft() {
      return left;
    }

    public void setLeft(SideData left) {
      this.left = left;
    }

    public SideData getRight() {
      return right;
    }

    public void setRight(SideData right) {
      this.right = right;
    }

    public String toString() {
      return "{name=" + name + ", timestamp=" + timestamp + ", left=" + left + ", right=" + right + "}";
    }
  }

  public class SideData {

    private Integer brachial;

    private Integer ankle;

    private Double index;

    private byte[] waveForm;

    private Date clock;

    private String scale;

    public SideData() {
      super();
    }

    public void setBrachial(byte b) {
      this.brachial = new Integer(b);
    }

    public Integer getBrachial() {
      return brachial;
    }

    public void setBrachial(Integer brachial) {
      this.brachial = brachial;
    }

    public Integer getAnkle() {
      return ankle;
    }

    public void setAnkle(Integer ankle) {
      this.ankle = ankle;
    }

    public Double getIndex() {
      return index;
    }

    public void setIndex(Double index) {
      this.index = index;
    }

    public byte[] getWaveForm() {
      return waveForm;
    }

    public void setWaveForm(byte[] waveForm) {
      this.waveForm = waveForm;
    }

    public Date getClock() {
      return clock;
    }

    public void setClock(Date clock) {
      this.clock = clock;
    }

    public void setClock(String timestamp) {
      this.clock = parseTimestamp(timestamp);
    }

    public String getScale() {
      return scale;
    }

    public void setScale(byte scale) {
      switch(scale) {
      case 1:
        this.scale = "x8";
        break;
      case 2:
        this.scale = "x4";
        break;
      case 3:
        this.scale = "x2";
        break;
      case 4:
        this.scale = "x1";
        break;
      }
    }

    public String toString() {
      return "{brachial=" + brachial + ", ankle=" + ankle + ", index=" + index + ", clock=" + clock + ", scale=" + scale + ", waveForm=" + toString(waveForm) + "}";
    }

    private String toString(byte[] ba) {
      HexBinaryAdapter adapter = new HexBinaryAdapter();
      return adapter.marshal(ba);
    }

  }

}
