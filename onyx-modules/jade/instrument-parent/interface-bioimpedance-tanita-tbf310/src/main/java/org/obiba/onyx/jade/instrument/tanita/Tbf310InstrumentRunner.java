package org.obiba.onyx.jade.instrument.tanita;


import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.LoggerFactory;

public class Tbf310InstrumentRunner extends TanitaInstrument {

  public Tbf310InstrumentRunner() throws Exception {

    super();
    log = LoggerFactory.getLogger(Tbf310InstrumentRunner.class);
  }

  /**
   * Establish the connection with the device connected to the serial port.
   */
  public void setupSerialPort() {

    try {

      // If port already open, close it.
      if(serialPort != null) {
        serialPort.close();
        serialPort = null;
      }

      // Initialize serial port attributes.
      log.info("Fetching communication port {}", getTanitaCommPort());
      CommPortIdentifier wPortId = CommPortIdentifier.getPortIdentifier(getTanitaCommPort());

      log.info("Opening communication port {}", getTanitaCommPort());
      serialPort = (SerialPort) wPortId.open(portOwnerName, 2000);

      // Make sure the port is "Clear To Send"
      serialPort.setSerialPortParams(baudeRate, dataLength, stopBit, parity);
      bufferedReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

      if(serialPort.isCTS()) {

        // Set serial port parameters.
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
        serialPort.notifyOnCTS(true);
        serialPort.notifyOnCarrierDetect(true);
        serialPort.notifyOnRingIndicator(true);
        serialPort.notifyOnDSR(true);

        portIsAvailable = true;

      } else {
        portIsAvailable = false;
      }

    } catch(Exception wCouldNotAccessSerialPort) {
      portIsAvailable = false;
      log.warn("Could not access the specified serial port.");
    }
  }

  /**
   * Handles any serial port events from the Tanita.
   * 
   * @param pTanitaOutput The serial port event.
   */
  public void serialEvent(SerialPortEvent pEvent) {

    switch(pEvent.getEventType()) {

    // Clear to send
    case SerialPortEvent.CTS:

      // If serial is not CTS, it means that the cable was disconnected.
      // Attempt to reestablish the connection.
      if(shutdown == false) {
        // Only try to reestablish if we're not shutting down the
        // application.
        reestablishConnection();
      }

      break;

    // Data is available at the serial port, so read it...
    case SerialPortEvent.DATA_AVAILABLE:

      try {
        if(bufferedReader.ready()) {

          // Parse and sets the data in the GUI.
          String wResponse = bufferedReader.readLine().trim();
          setTanitaData(parseTanitaData(wResponse));

          // Enable save button, so data can be saved.
          saveDataBtn.setEnabled(true);
        }
      } catch(IOException wErrorReadingDataOnSerialPort) {
        JOptionPane.showMessageDialog(appWindow, tanitaResourceBundle.getString("Err.Result_communication"), tanitaResourceBundle.getString("Title.Communication_error"), JOptionPane.ERROR_MESSAGE);
      }
      break;
    }
  }

  public void run() {

    if(!externalAppHelper.isSotfwareAlreadyStarted("tbf310InstrumentRunner")) {

      log.info("Starting TBF-310 GUI");
      buildGUI();

      // Obtain the lock outside the UI thread. This will block until the UI releases the lock, at which point it should
      // be safe to exit the main thread.
      synchronized(uiLock) {
        try {
          uiLock.wait();
        } catch(InterruptedException e) {
          throw new RuntimeException(e);
        }
      }

      log.info("Lock obtained. Exiting software.");

    } else {
      JOptionPane.showMessageDialog(null, tanitaResourceBundle.getString("Err.Application_lock"), tanitaResourceBundle.getString("Title.Cannot_start_application"), JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Sets the Bioimpedance data in the GUI components.
   * 
   * @param pOutputData The parsed output data from the Tanita.
   */
  protected void setTanitaData(String[] pOutputData) {

    String wBodyTypeCode = pOutputData[0];
    if(wBodyTypeCode.endsWith("0")) {
      bodyTypeTxt.setText("STANDARD");
    } else if(wBodyTypeCode.endsWith("2")) {
      bodyTypeTxt.setText("ATHLETIC");
    }

    String wGender = pOutputData[1];
    if(wGender.equals("1")) {
      genderTxt.setText("MALE");
    } else if(wGender.equals("2")) {
      genderTxt.setText("FEMALE");
    }

    heightTxt.setText(pOutputData[2]);
    weightTxt.setText(pOutputData[3]);
    impedanceTxt.setText(pOutputData[4]);
    fatPctTxt.setText(pOutputData[5]);
    fatMassTxt.setText(pOutputData[6]);
    ffmTxt.setText(pOutputData[7]);
    tbwTxt.setText(pOutputData[8]);
    ageTxt.setText(pOutputData[9]);
    bmiTxt.setText(pOutputData[10]);
    bmrTxt.setText(pOutputData[11]);

  }

  public void sendOutputToServer() {
    log.info("Sending output of tanita to server...");

    Map<String, Data> output = new HashMap<String, Data>();

    output.put("BodyType", new Data(DataType.TEXT, bodyTypeTxt.getText()));
    output.put("Weight", getDecimalValue(weightTxt));
    output.put("Impedance", getIntegerValue(impedanceTxt));
    output.put("BMI", getDecimalValue(bmiTxt));
    output.put("BMR", getIntegerValue(bmrTxt));
    output.put("FatFreeMass", getDecimalValue(ffmTxt));
    output.put("FatMass", getDecimalValue(fatMassTxt));
    output.put("TotalBodyWater", getDecimalValue(tbwTxt));
    output.put("FatPercentage", getDecimalValue(fatPctTxt));
    output.put("Gender", new Data(DataType.TEXT, genderTxt.getText()));
    output.put("Height", getIntegerValue(heightTxt));
    output.put("Age", getIntegerValue(ageTxt));

    instrumentExecutionService.addOutputParameterValues(output);
    log.info("Sending output of tanita to server done...");
    exitUI();
  }
}
