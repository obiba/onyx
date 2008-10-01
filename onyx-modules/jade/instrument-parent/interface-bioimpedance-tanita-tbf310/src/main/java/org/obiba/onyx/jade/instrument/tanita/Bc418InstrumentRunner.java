package org.obiba.onyx.jade.instrument.tanita;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.LoggerFactory;

/**
 * This is a simple Swing application that connects to a bioimpedance and weight device (Tanita Body Composition
 * Analyzer). It allows the data to be inputed and retrieved automatically through the serial port of the device.
 * 
 * @author mbelhiah
 * 
 */
public class Bc418InstrumentRunner extends TanitaInstrument {

  // Interface components
  protected JTextField dateTxt;

  protected JTextField timeTxt;

  protected JTextField rLegImpedanceTxt;

  protected JTextField lLegImpedanceTxt;

  protected JTextField rArmImpedanceTxt;

  protected JTextField lArmImpedanceTxt;

  protected JTextField rLegFatPctTxt;

  protected JTextField rLegFatMassTxt;

  protected JTextField rLegFfmTxt;

  protected JTextField rLegPredictedMuscleMassTxt;

  protected JTextField lLegFatPctTxt;

  protected JTextField lLegFatMassTxt;

  protected JTextField lLegFfmTxt;

  protected JTextField lLegPredictedMuscleMassTxt;

  protected JTextField rArmFatPctTxt;

  protected JTextField rArmFatMassTxt;

  protected JTextField rArmFfmTxt;

  protected JTextField rArmPredictedMuscleMassTxt;

  protected JTextField lArmFatPctTxt;

  protected JTextField lArmFatMassTxt;

  protected JTextField lArmFfmTxt;

  protected JTextField lArmPredictedMuscleMassTxt;

  protected JTextField trunkFatPctTxt;

  protected JTextField trunkFatMassTxt;

  protected JTextField trunkFfmTxt;

  protected JTextField trunkPredictedMuscleMassTxt;

  private BufferedReader bufferedReader;

  public Bc418InstrumentRunner() throws Exception {

    super();

    log = LoggerFactory.getLogger(Bc418InstrumentRunner.class);

    // Initialize interface components.
    dateTxt = new ResultTextField();
    timeTxt = new ResultTextField();

    rLegImpedanceTxt = new ResultTextField();
    lLegImpedanceTxt = new ResultTextField();
    rArmImpedanceTxt = new ResultTextField();
    lArmImpedanceTxt = new ResultTextField();

    rLegFatPctTxt = new ResultTextField();
    rLegFatMassTxt = new ResultTextField();
    rLegFfmTxt = new ResultTextField();
    rLegPredictedMuscleMassTxt = new ResultTextField();

    lLegFatPctTxt = new ResultTextField();
    lLegFatMassTxt = new ResultTextField();
    lLegFfmTxt = new ResultTextField();
    lLegPredictedMuscleMassTxt = new ResultTextField();

    rArmFatPctTxt = new ResultTextField();
    rArmFatMassTxt = new ResultTextField();
    rArmFfmTxt = new ResultTextField();
    rArmPredictedMuscleMassTxt = new ResultTextField();

    lArmFatPctTxt = new ResultTextField();
    lArmFatMassTxt = new ResultTextField();
    lArmFfmTxt = new ResultTextField();
    lArmPredictedMuscleMassTxt = new ResultTextField();

    trunkFatPctTxt = new ResultTextField();
    trunkFatMassTxt = new ResultTextField();
    trunkFfmTxt = new ResultTextField();
    trunkPredictedMuscleMassTxt = new ResultTextField();

  }

  /**
   * Establish the connection with the device connected to the serial port.
   */
  protected void setupSerialPort() {

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

      // Set serial port parameters.
      serialPort.setSerialPortParams(baudeRate, dataLength, stopBit, parity);

      portIsAvailable = true;
    } catch(Exception wCouldNotAccessSerialPort) {
      portIsAvailable = false;
      log.warn("Could not access the specified serial port.", wCouldNotAccessSerialPort);
    }
  }

  /**
   * Handles any serial port events from the Tanita.
   * 
   * @param pTanitaOutput The serial port event.
   */
  public void serialEvent(SerialPortEvent pEvent) {

    switch(pEvent.getEventType()) {

    // Data is available at the serial port, so read it...
    case SerialPortEvent.DATA_AVAILABLE:

      try {

        // Parse and sets the data in the GUI.
        String wResponse = bufferedReader.readLine().trim();
        setTanitaData(parseTanitaData(wResponse));

        // Enable save button, so data can be saved.
        saveDataBtn.setEnabled(true);
      } catch(IOException wErrorReadingDataOnSerialPort) {
        JOptionPane.showMessageDialog(appWindow, tanitaResourceBundle.getString("Err.Result_communication"), tanitaResourceBundle.getString("Title.Communication_error"), JOptionPane.ERROR_MESSAGE);
      }
      break;
    }
  }

  /**
   * Retrieve participant data from the database and transfer them to the bioimpedance device
   * @throws IOException
   * @throws Exception
   */
  private void initParticipantData() {

    Map<String, Data> inputData = instrumentExecutionService.getInputParametersValue("ClothesWeight", "Gender", "BodyType", "Height", "Age");

    try {
      bufferedReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
    } catch(IOException e) {
      throw new RuntimeException("Cannot open serial port on BC-418", e);
    }

    try {
      String wUnitsOfMeasure = "U0\r\n";

      String wClothesWeight = "D0001.0\r\n";

      String wGender = null;
      if(inputData.get("Gender").getValueAsString().equals("MALE")) {
        wGender = "D11\r\n";
      } else {
        if(inputData.get("Gender").getValueAsString().equals("FEMALE")) {
          wGender = "D12\r\n";
        }
      }

      String wBodyType = null;
      if(inputData.get("BodyType").getValueAsString().equals("STANDARD")) {
        wBodyType = "D20\r\n";
      } else {
        if(inputData.get("BodyType").getValueAsString().equals("ATHLETIC")) {
          wBodyType = "D22\r\n";
        }
      }

      String wHeight = null;
      Float inputHeight = Float.parseFloat(inputData.get("Height").getValueAsString());

      if(inputHeight.intValue() < 100) {
        wHeight = "D3000" + inputData.get("Height") + "\r\n";
      } else {
        if(inputHeight.intValue() >= 100) {
          wHeight = "D300" + inputHeight.intValue() + "\r\n";
        }
      }

      String wAge;
      wAge = "D4" + inputData.get("Age").getValueAsString() + "\r\n";

      // Send commands and receives response

      // Send the units of measurement
      String wResponse = null;
      wResponse = sendReceive(wUnitsOfMeasure);

      if(!wResponse.equals("U0")) {
        throw new RuntimeException("Error when setting units of measurement");
      }

      // Send tare weight
      wResponse = sendReceive(wClothesWeight);
      if(!wResponse.equals("D0")) {
        throw new RuntimeException("Error when setting tare weight");
      }

      // Send gender
      wResponse = sendReceive(wGender);
      if(!wResponse.equals("D1")) {
        throw new RuntimeException("Error when setting gender");
      }

      // Send body type
      wResponse = sendReceive(wBodyType);
      if(!wResponse.equals("D2")) {
        throw new RuntimeException("Error when setting body type");
      }

      // Send height
      wResponse = sendReceive(wHeight);
      if(!wResponse.equals("D3")) {
        throw new RuntimeException("Error when setting height");
      }

      // Send age
      wResponse = sendReceive(wAge);
      if(!wResponse.equals("D4")) {
        throw new RuntimeException("Error when setting age");
      }

      // Start measurement
      wResponse = sendReceive("G1\r\n");
      if(!wResponse.equals("G1")) {
        throw new RuntimeException("Error when setting input parameters");
      }
    } catch(Exception ex) {
      throw new RuntimeException("Error initializing TANITA-BC418 input parameters: ", ex);
    }
  }

  public void initialize() {
    super.initialize();
    initParticipantData();
  }

  public void run() {
    if(!externalAppHelper.isSotfwareAlreadyStarted("tanitaInstrumentRunner")) {

      log.info("Starting Tanita BC-418 GUI");
      buildGUI();

      try {
        serialPort.addEventListener(this);
      } catch(TooManyListenersException e) {
        throw new RuntimeException(e);
      }
      serialPort.notifyOnDataAvailable(true);

      // Obtain the lock outside the UI thread. This will block until the
      // UI releases the lock, at which point it should
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
   * Send receive.
   */
  private String sendReceive(String pCommand) {

    String wResponse = null;

    try {
      if(pCommand != null) {

        outputStream = serialPort.getOutputStream();
        // Send parameter
        outputStream.write(pCommand.getBytes());
        log.info("Sending data:{}", pCommand);
      }
    } catch(IOException e) {
      throw new RuntimeException("Error when sending data to device", e);
    }

    // Read response
    try {
      wResponse = bufferedReader.readLine().trim();
      log.info("Receiving response:{}", wResponse);
    } catch(IOException e) {
      throw new RuntimeException("Error when receiving data from device", e);
    }

    return wResponse;
  }

  /**
   * Sets the Bioimpedance data in the GUI components.
   * 
   * @param pOutputData The parsed output data from the Tanita.
   * @throws ParseException
   */
  protected void setTanitaData(String[] pOutputData) {

    dateTxt.setText(pOutputData[0]);
    timeTxt.setText(pOutputData[1]);

    String wBodyTypeCode = pOutputData[2];
    if(wBodyTypeCode.endsWith("0")) {
      bodyTypeTxt.setText("STANDARD");
    } else if(wBodyTypeCode.endsWith("2")) {
      bodyTypeTxt.setText("ATHLETIC");
    }

    String wGender = pOutputData[3];
    if(wGender.equals("1")) {
      genderTxt.setText("MALE");
    } else if(wGender.equals("2")) {
      genderTxt.setText("FEMALE");
    }

    heightTxt.setText(pOutputData[4]);
    weightTxt.setText(pOutputData[5]);
    fatPctTxt.setText(pOutputData[6]);
    fatMassTxt.setText(pOutputData[7]);
    ffmTxt.setText(pOutputData[8]);
    tbwTxt.setText(pOutputData[9]);
    ageTxt.setText(pOutputData[10]);
    bmiTxt.setText(pOutputData[11]);
    bmrTxt.setText(pOutputData[12]);

    impedanceTxt.setText(pOutputData[13]);
    rLegImpedanceTxt.setText(pOutputData[14]);
    lLegImpedanceTxt.setText(pOutputData[15]);
    rArmImpedanceTxt.setText(pOutputData[16]);
    lArmImpedanceTxt.setText(pOutputData[17]);

    rLegFatPctTxt.setText(pOutputData[18]);
    rLegFatMassTxt.setText(pOutputData[19]);
    rLegFfmTxt.setText(pOutputData[20]);
    rLegPredictedMuscleMassTxt.setText(pOutputData[21]);

    lLegFatPctTxt.setText(pOutputData[22]);
    lLegFatMassTxt.setText(pOutputData[23]);
    lLegFfmTxt.setText(pOutputData[24]);
    lLegPredictedMuscleMassTxt.setText(pOutputData[25]);

    rArmFatPctTxt.setText(pOutputData[26]);
    rArmFatMassTxt.setText(pOutputData[27]);
    rArmFfmTxt.setText(pOutputData[28]);
    rArmPredictedMuscleMassTxt.setText(pOutputData[29]);

    lArmFatPctTxt.setText(pOutputData[30]);
    lArmFatMassTxt.setText(pOutputData[31]);
    lArmFfmTxt.setText(pOutputData[32]);
    lArmPredictedMuscleMassTxt.setText(pOutputData[33]);

    trunkFatPctTxt.setText(pOutputData[34]);
    trunkFatMassTxt.setText(pOutputData[35]);
    trunkFfmTxt.setText(pOutputData[36]);
    trunkPredictedMuscleMassTxt.setText(pOutputData[37]);

  }

  protected void sendOutputToServer() {
    log.info("Sending output of Tanita BC-418 to server...");

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
    output.put("Height", getDecimalValue(heightTxt));
    output.put("Age", getIntegerValue(ageTxt));

    output.put("Date", new Data(DataType.TEXT, dateTxt.getText()));
    output.put("Time", new Data(DataType.TEXT, timeTxt.getText()));

    output.put("RightLegImpedance", getIntegerValue(rLegImpedanceTxt));
    output.put("LeftLegImpedance", getIntegerValue(lLegImpedanceTxt));
    output.put("RightArmImpedance", getIntegerValue(rArmImpedanceTxt));
    output.put("LeftArmImpedance", getIntegerValue(lArmImpedanceTxt));

    output.put("RightLegFatPercentage", getDecimalValue(rLegFatPctTxt));
    output.put("RightLegFatMass", getDecimalValue(rLegFatMassTxt));
    output.put("RightLegFatFreeMass", getDecimalValue(rLegFfmTxt));
    output.put("RightLegPredictedMuscleMass", getDecimalValue(rLegPredictedMuscleMassTxt));

    output.put("LeftLegFatPercentage", getDecimalValue(lLegFatPctTxt));
    output.put("LeftLegFatMass", getDecimalValue(lLegFatMassTxt));
    output.put("LeftLegFatFreeMass", getDecimalValue(lLegFfmTxt));
    output.put("LeftLegPredictedMuscleMass", getDecimalValue(lLegPredictedMuscleMassTxt));

    output.put("RightArmFatPercentage", getDecimalValue(rArmFatPctTxt));
    output.put("RightArmFatMass", getDecimalValue(rArmFatMassTxt));
    output.put("RightArmFatFreeMass", getDecimalValue(rArmFfmTxt));
    output.put("RightArmPredictedMuscleMass", getDecimalValue(rArmPredictedMuscleMassTxt));

    output.put("LeftArmFatPercentage", getDecimalValue(lArmFatPctTxt));
    output.put("LeftArmFatMass", getDecimalValue(lArmFatMassTxt));
    output.put("LeftArmFatFreeMass", getDecimalValue(lArmFfmTxt));
    output.put("LeftArmPredictedMuscleMass", getDecimalValue(lArmPredictedMuscleMassTxt));

    output.put("TrunkFatPercentage", getDecimalValue(trunkFatPctTxt));
    output.put("TrunkFatMass", getDecimalValue(trunkFatMassTxt));
    output.put("TrunkFatFreeMass", getDecimalValue(trunkFfmTxt));
    output.put("TrunkPredictedMuscleMass", getDecimalValue(trunkPredictedMuscleMassTxt));

    instrumentExecutionService.addOutputParameterValues(output);
    log.info("Sending output of Tanita BC-418 to server done...");
    exitUI();
  }
}
