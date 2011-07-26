/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.vsm.bptru.bpm.bpm200;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import libhidapi.HidapiLibrary;
import libhidapi.HidapiLibrary.hid_device;

import org.bridj.Pointer;
import org.obiba.vsm.bptru.bpm.BpmCommands;
import org.obiba.vsm.bptru.bpm.BpmInstrument;
import org.obiba.vsm.bptru.bpm.BpmMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bpm200 implements BpmInstrument {

  private static final Logger log = LoggerFactory.getLogger("vsm.bptru.bpm");

  private final Thread commThread;

  private final BlockingDeque<BpmMessage> messageQueue = new LinkedBlockingDeque<BpmMessage>();

  private final BlockingDeque<BpmMessage> writeQueue = new LinkedBlockingDeque<BpmMessage>();

  private boolean connected = false;

  private boolean loop = true;

  public Bpm200() {
    commThread = new Thread(new BpmCommLoop());
  }

  @Override
  public String getManufacturerString() {
    return null;
  }

  @Override
  public String getProductString() {
    return null;
  }

  @Override
  public String getSerialNumber() {
    return null;
  }

  public BpmCommands commands() {
    return new Bpm200Commands(this);
  }

  public void send(BpmMessage msg) {
    writeQueue.add(msg);
  }

  public BpmMessage read() {
    try {
      return messageQueue.poll(100, TimeUnit.MILLISECONDS);
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void putBack(BpmMessage message) {
    messageQueue.addFirst(message);
  }

  public List<BpmMessage> readAll() {
    List<BpmMessage> messages = new LinkedList<BpmMessage>();
    messageQueue.drainTo(messages);
    return Collections.unmodifiableList(messages);
  }

  public void connect() {
    commThread.start();
  }

  public void disconnect() {
    if(isConnected()) {
      connected = false;
    }
  }

  public boolean isConnected() {
    return connected;
  }

  public void close() {
    this.loop = false;
  }

  private final class BpmCommLoop implements Runnable {

    private ByteBuffer writeBuffer = ByteBuffer.allocate(9);

    public void run() {
      while(loop) {
        Pointer<hid_device> device = connect();
        if(device != null) {
          connected = true;
          try {
            comm(device);
          } catch(RuntimeException e) {
            log.error("Error communicating with device", e);
          } finally {
            HidapiLibrary.hid_close(device);
          }
        }
      }
    }

    private void comm(Pointer<hid_device> device) {
      log.debug("starting comm loop");
      Pointer<Byte> data = Pointer.allocateBytes(1024);
      HidapiLibrary.hid_set_nonblocking(device, 1);
      while(loop && connected) {
        int result = HidapiLibrary.hid_read(device, data, 1024);
        if(result > 0) {
          byte bytes[] = data.getBytes(result);
          messageQueue.addAll(parse(result, bytes));
        }

        BpmMessage msg = writeQueue.poll();
        if(msg != null) {
          encode(msg, writeBuffer);
          try {
            HidapiLibrary.hid_write(device, Pointer.pointerToBytes(writeBuffer), 9);
          } finally {
            writeBuffer.rewind();
          }
        }
      }
    }

    private Pointer<hid_device> connect() {
      log.debug("connecting");
      Pointer<hid_device> device = null;
      while(device == null && loop == true) {
        device = HidapiLibrary.hid_open((short) 0x10b7, (short) 0x1234, null);
        if(device == null) {
          log.debug("no device found");
          try {
            Thread.sleep(1000);
          } catch(InterruptedException e) {
            throw new RuntimeException(e);
          }
        } else {
          log.debug("device found");
        }
      }
      return device;
    }

  }

  private static final byte STX = 0x02;

  private static final byte ETX = 0x03;

  private void encode(BpmMessage msg, ByteBuffer bb) {
    bb.put((byte) 0); // required by HID lib
    bb.put(STX);
    bb.put(msg.bytes());
    bb.put(ETX);
  }

  private List<BpmMessage> parse(int count, byte[] bytes) {
    ArrayList<BpmMessage> msgs = new ArrayList<BpmMessage>();
    for(int i = 0; i < count; i++) {
      if(bytes[i] == STX) {
        BpmMessage msg = BpmMessage.Builder.newMessage()//
        .messageId(bytes[++i])//
        .data0(bytes[++i])//
        .data1(bytes[++i])//
        .data2(bytes[++i])//
        .data3(bytes[++i])//
        .crc(bytes[++i]).build();
        if(bytes[++i] != ETX) throw new IllegalArgumentException("malformed BPM message");
        msgs.add(msg);
      }
    }

    return msgs;
  }

}
