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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.obiba.onyx.jade.instrument.tremetrics.ra300.Ra300Exception.Cause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ra300 {

  public enum State {
    CONNECTED, DISCONNECTED
  }

  private final static Logger log = LoggerFactory.getLogger(Ra300.class);

  private final static byte[] READ_CURRENT_TEST_COMMAND = new byte[] { 0x05, '4', 0x0d };

  private Ra300Comm comm;

  private State state = State.DISCONNECTED;

  public Ra300() {

  }

  public Ra300Test readCurrentTest() throws IOException {
    comm.send(READ_CURRENT_TEST_COMMAND);
    ByteBuffer bb = ByteBuffer.allocate(1024);
    bb.put(comm.receive());
    while(isMessageEnd(bb) == false) {
      log.debug("Waiting for msg end");
      bb.put(comm.receive());
    }
    Ra300Test test = new Ra300Test(bb.array());
    log.debug("current test: {}", test);
    return test;
  }

  public State getState() {
    return state;
  }

  private boolean isMessageEnd(ByteBuffer bb) {
    if(bb.position() < 6) return false;
    int position = bb.position();
    return bb.get(position - 1) == (byte) 0x0d && bb.get(position - 4) == (byte) 0x17 && bb.get(position - 5) == (byte) 'p' && bb.get(position - 6) == (byte) '~';
  }

  public void connect(String comPort, int baudRate) {

    disconnect();

    SerialPort ra300 = openSerialPort(comPort);

    try {
      this.comm = openRa300(baudRate, ra300);
      readCurrentTest();
      this.state = State.CONNECTED;
    } catch(RuntimeException e) {
      try {
        disconnect();
      } catch(Exception e2) {
        // Ignore
      }
      throw e;
    } catch(IOException e) {
      try {
        disconnect();
      } catch(Exception e2) {
        // Ignore
      }
      throw new Ra300Exception(Cause.COMMUNICATION_ERROR, e);
    }

  }

  private Ra300Comm openRa300(int baudRate, SerialPort ra300) {
    try {
      return new Ra300Comm(ra300, baudRate);
    } catch(IOException e) {
      log.warn("Unable to open streams on port {}", ra300.getName());
      throw new Ra300Exception(Cause.CONNECTION_ERROR, e);
    } catch(UnsupportedCommOperationException e) {
      log.warn("Cannot set RA300 settings on port {}", ra300.getName());
      throw new Ra300Exception(Cause.CONNECTION_ERROR, e);
    } catch(TooManyListenersException e) {
      log.warn("Too many listeners on serial port {}", ra300.getName());
      throw new Ra300Exception(Cause.CONNECTION_ERROR, e);
    }
  }

  private SerialPort openSerialPort(String comPort) {
    try {
      log.debug("Fetching communication port {}", comPort);
      CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(comPort);
      log.debug("Opening communication port {}", portId.getName());
      return (SerialPort) portId.open("OBiBa Onyx Audiogram Reader", 2000);
    } catch(NoSuchPortException e) {
      log.warn("Port {} does not exist.", comPort);
      throw new Ra300Exception(Cause.INVALID_PORT, e);
    } catch(PortInUseException e) {
      log.warn("Port {} is already in use by {}.", comPort, e.currentOwner);
      throw new Ra300Exception(Cause.INVALID_PORT, e);
    }
  }

  void disconnect() {
    try {
      if(this.comm != null) {
        log.debug("Disconnecting.");
        this.comm.close();
      }
    } finally {
      this.comm = null;
      state = State.DISCONNECTED;
    }
  }

  private class Ra300Comm implements SerialPortEventListener {

    private final SerialPort ra300;

    private final InputStream is;

    private final OutputStream os;

    private final BlockingQueue<byte[]> readQueue = new LinkedBlockingQueue<byte[]>();

    Ra300Comm(SerialPort ra300, int baudRate) throws IOException, UnsupportedCommOperationException, TooManyListenersException {
      ra300.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
      ra300.notifyOnDataAvailable(true);
      ra300.notifyOnCTS(true);
      ra300.notifyOnDSR(true);
      ra300.addEventListener(this);

      this.ra300 = ra300;
      this.is = ra300.getInputStream();
      this.os = ra300.getOutputStream();
    }

    public void close() {
      this.ra300.close();
    }

    public void send(byte[] command) throws IOException {
      if(ra300.isCTS()) {
        log.debug("Sending {}", command);
        os.write(command);
      }
    }

    public byte[] receive() {
      try {
        byte[] data = readQueue.poll(5, TimeUnit.SECONDS);
        if(data == null) throw new Ra300Exception(Cause.RECEIVE_TIMEOUT);
        log.debug("Receiving {}", data);
        return data;
      } catch(InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
      switch(event.getEventType()) {
      // Clear to send
      case SerialPortEvent.CTS:
        log.debug("CTS");
        break;
      case SerialPortEvent.DATA_AVAILABLE:
        log.debug("DATA_AVAILABLE");
        byte[] data = read();
        log.debug("read {}", data);
        readQueue.add(data);
        break;
      // Data set ready
      case SerialPortEvent.DSR:
        log.debug("DSR");
        break;
      }
    }

    private byte[] read() {
      try {
        byte[] buffer = new byte[1024];
        int bytes = is.read(buffer);
        return Arrays.copyOf(buffer, bytes);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Set<CommPortIdentifier> listAvailablePorts() {
    HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();

    @SuppressWarnings("unchecked")
    Enumeration<CommPortIdentifier> thePorts = CommPortIdentifier.getPortIdentifiers();

    while(thePorts.hasMoreElements()) {
      CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
      switch(com.getPortType()) {
      case CommPortIdentifier.PORT_SERIAL:
        try {
          CommPort port = com.open("CommUtil", 50);
          port.close();
          h.add(com);
        } catch(PortInUseException e) {
          // Ignore
        } catch(Exception e) {
          // Ignore
        }
      }
    }
    return h;

  }

  public static void main(String[] args) throws IOException {
    Ra300 ra = new Ra300();
    ra.connect("COM1", 19200);
    System.out.println(ra.readCurrentTest());
    new BufferedReader(new InputStreamReader(System.in)).readLine();
    ra.disconnect();
  }
}
