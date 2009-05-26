package org.obiba.onyx.jade.instrument.tanita;

import gnu.io.SerialPortEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import javax.swing.JOptionPane;

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

  private BufferedReader bufferedReader;

  String outputData[];

  public Bc418InstrumentRunner() throws Exception {
    super();
    log = LoggerFactory.getLogger(Bc418InstrumentRunner.class);
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
        if(bufferedReader.ready()) {
          // Parse and sets the data in the GUI.
          String wResponse = bufferedReader.readLine();
          if(wResponse != null) {
            wResponse = wResponse.trim();
          }
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

  private String sendReceive(String pCommand) {
    return sendReceive(pCommand.getBytes());
  }

  /**
   * Send receive.
   */
  private String sendReceive(byte[] pCommand) {

    String wResponse = null;

    try {
      if(pCommand != null) {

        outputStream = serialPort.getOutputStream();

        // Send parameter
        outputStream.write(pCommand);
        outputStream.flush();

        List<Byte> pCommandList = new ArrayList<Byte>();
        for(int i = 0; i < pCommand.length; i++) {
          pCommandList.add(pCommand[i]);
        }

        log.info("Sending data:{}", (pCommandList));
      }
    } catch(IOException e) {
      throw new RuntimeException("Error when sending data to device", e);
    }

    // Read response
    try {
      wResponse = bufferedReader.readLine();
      if(wResponse != null) {
        wResponse = wResponse.trim();
      }
      log.info("Receiving response:{}", wResponse);
    } catch(IOException e) {

      // In case of communication failure, wait for 5 secs and try again...
      try {
        Thread.sleep(5000);
        wResponse = bufferedReader.readLine();
        log.info("Receiving response:{}", wResponse);
      } catch(Exception ex) {
        JOptionPane.showMessageDialog(appWindow, tanitaResourceBundle.getString("Err.Communication_error_BC418"));
        throw new RuntimeException("Communication error with the Tanita BC418, exiting runner...");
      }

    }

    return wResponse;
  }

  protected boolean checkIfPortIsAvailable() throws TooManyListenersException {
    return true;
  }

  /**
   * Retrieve participant data from the database and transfer them to the bioimpedance device
   * @throws IOException
   * @throws Exception
   */
  private void initParticipantData() {

    Map<String, Data> inputData = instrumentExecutionService.getInputParametersValue("INPUT_CLOTHES_WEIGHT", "INPUT_PARTICIPANT_GENDER", "INPUT_BODY_TYPE", "INPUT_PARTICIPANT_HEIGHT", "INPUT_PARTICIPANT_AGE");

    try {
      String wUnitsOfMeasure = "U0\r\n";

      String clothesWeight = null;
      try {
        clothesWeight = "D000" + inputData.get("INPUT_CLOTHES_WEIGHT").getValueAsString() + "\r\n";
        log.info("wClothesWeight" + clothesWeight);
      } catch(NullPointerException ex) {
        throw new RuntimeException("Error: Expected parameter INPUT_CLOTHES_WEIGHT is null \n" + ex);
      }

      String wGender = null;
      try {
        if(inputData.get("INPUT_PARTICIPANT_GENDER").getValueAsString().equals("MALE")) {
          wGender = "D11\r\n";
        } else {
          if(inputData.get("INPUT_PARTICIPANT_GENDER").getValueAsString().equals("FEMALE")) {
            wGender = "D12\r\n";
          }
        }
      } catch(NullPointerException ex) {
        throw new RuntimeException("Error: Expected parameter INPUT_PARTICIPANT_GENDER is null \n" + ex);
      }

      String wBodyType = null;
      try {
        if(inputData.get("INPUT_BODY_TYPE").getValueAsString().equals("STANDARD")) {
          wBodyType = "D20\r\n";
        } else {
          if(inputData.get("INPUT_BODY_TYPE").getValueAsString().equals("ATHLETIC")) {
            wBodyType = "D22\r\n";
          }
        }
      } catch(NullPointerException ex) {
        throw new RuntimeException("Error: Expected parameter INPUT_BODY_TYPE is null \n" + ex);
      }

      String wHeight = null;
      try {
        Float inputHeight = Float.parseFloat(inputData.get("INPUT_PARTICIPANT_HEIGHT").getValueAsString());
        if(inputHeight.intValue() < 100) {
          wHeight = "D3000" + inputData.get("Height") + "\r\n";
        } else {
          if(inputHeight.intValue() >= 100) {
            wHeight = "D300" + inputHeight.intValue() + "\r\n";
          }
        }
      } catch(NullPointerException ex) {
        throw new RuntimeException("Error: Expected parameter INPUT_PARTICIPANT_HEIGHT is null \n" + ex);
      }

      // Send commands and receives response

      // Send the units of measurement
      String wResponse = null;
      wResponse = sendReceive(wUnitsOfMeasure);

      if(!wResponse.equals("U0")) {
        throw new RuntimeException("Error when setting units of measurement");
      }

      // Send tare weight
      wResponse = sendReceive(clothesWeight);
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
      String inputAgeStr = inputData.get("INPUT_PARTICIPANT_AGE").getValueAsString();
      int inputAge = Integer.parseInt(inputAgeStr);
      log.info("Input age is {}", inputAgeStr);
      if(inputAge < 7 || inputAge > 99) {
        log.error("The input age is outside the range supported by the Tanita BC418.  Input age has to be in the following interval: [7,99]");
      }

      // Age is sent to Tanita has to be two bytes long, so we have to add zero at the beginning if age between 7 and 9.
      wResponse = sendReceive("D4" + (inputAge < 10 ? "0" + inputAgeStr : inputAgeStr) + "\r\n");

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

  /**
   * Reset the Tanita settings before a measurement.
   */
  private void resetTanita() {
    log.info("Resetting Tanita");
    sendReceive(new byte[] { (byte) 0x1F, (byte) 0x0D, (byte) 0x0A });
  }

  /**
   * Sets the Bioimpedance data in the GUI components.
   * 
   * @param pOutputData The parsed output data from the Tanita.
   * @throws ParseException
   */
  protected void setTanitaData(String[] outputData) {

    this.outputData = outputData;

    String wBodyTypeCode = outputData[2];
    if(wBodyTypeCode.endsWith("0")) {
      bodyTypeTxt.setText("STANDARD");
    } else if(wBodyTypeCode.endsWith("2")) {
      bodyTypeTxt.setText("ATHLETIC");
    }

    String wGender = outputData[3];
    if(wGender.equals("1")) {
      genderTxt.setText("MALE");
    } else if(wGender.equals("2")) {
      genderTxt.setText("FEMALE");
    }

    heightTxt.setText(outputData[4]);
    weightTxt.setText(outputData[5]);
    fatPctTxt.setText(outputData[6]);
    fatMassTxt.setText(outputData[7]);
    ffmTxt.setText(outputData[8]);
    tbwTxt.setText(outputData[9]);
    ageTxt.setText(outputData[10]);
    bmiTxt.setText(outputData[11]);
    bmrTxt.setText(outputData[12]);
    impedanceTxt.setText(outputData[13]);

  }

  protected void sendOutputToServer() {
    log.info("Sending output of Tanita BC-418 to server...");

    Map<String, Data> output = new HashMap<String, Data>();

    output.put("BodyType", new Data(DataType.TEXT, bodyTypeTxt.getText()));
    output.put("Weight", getDecimalValue(weightTxt.getText()));
    output.put("Impedance", getIntegerValue(impedanceTxt.getText()));
    output.put("BMI", getDecimalValue(bmiTxt.getText()));
    output.put("BMR", getIntegerValue(bmrTxt.getText()));
    output.put("FatFreeMass", getDecimalValue(ffmTxt.getText()));
    output.put("FatMass", getDecimalValue(fatMassTxt.getText()));
    output.put("TotalBodyWater", getDecimalValue(tbwTxt.getText()));
    output.put("FatPercentage", getDecimalValue(fatPctTxt.getText()));
    output.put("Gender", new Data(DataType.TEXT, genderTxt.getText()));
    output.put("Height", getIntegerValue(heightTxt.getText()));
    output.put("Age", getIntegerValue(ageTxt.getText()));

    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm");
    Date timestamp = null;
    try {
      timestamp = format.parse(outputData[0].replace("\"", "") + " " + outputData[1].replace("\"", ""));
    } catch(ParseException e) {
      throw new RuntimeException("The timestamp extracted for BC418 output could not be parsed", e);
    }
    output.put("Timestamp", new Data(DataType.DATE, timestamp));

    output.put("RightLegImpedance", getIntegerValue(outputData[14]));
    output.put("LeftLegImpedance", getIntegerValue(outputData[15]));
    output.put("RightArmImpedance", getIntegerValue(outputData[16]));
    output.put("LeftArmImpedance", getIntegerValue(outputData[17]));

    output.put("RightLegFatPercentage", getDecimalValue(outputData[18]));
    output.put("RightLegFatMass", getDecimalValue(outputData[19]));
    output.put("RightLegFatFreeMass", getDecimalValue(outputData[20]));
    output.put("RightLegPredictedMuscleMass", getDecimalValue(outputData[21]));

    output.put("LeftLegFatPercentage", getDecimalValue(outputData[22]));
    output.put("LeftLegFatMass", getDecimalValue(outputData[23]));
    output.put("LeftLegFatFreeMass", getDecimalValue(outputData[24]));
    output.put("LeftLegPredictedMuscleMass", getDecimalValue(outputData[25]));

    output.put("RightArmFatPercentage", getDecimalValue(outputData[26]));
    output.put("RightArmFatMass", getDecimalValue(outputData[27]));
    output.put("RightArmFatFreeMass", getDecimalValue(outputData[28]));
    output.put("RightArmPredictedMuscleMass", getDecimalValue(outputData[29]));

    output.put("LeftArmFatPercentage", getDecimalValue(outputData[30]));
    output.put("LeftArmFatMass", getDecimalValue(outputData[31]));
    output.put("LeftArmFatFreeMass", getDecimalValue(outputData[32]));
    output.put("LeftArmPredictedMuscleMass", getDecimalValue(outputData[33]));

    output.put("TrunkFatPercentage", getDecimalValue(outputData[34]));
    output.put("TrunkFatMass", getDecimalValue(outputData[35]));
    output.put("TrunkFatFreeMass", getDecimalValue(outputData[36]));
    output.put("TrunkPredictedMuscleMass", getDecimalValue(outputData[37]));

    instrumentExecutionService.addOutputParameterValues(output);
    log.info("Sending output of Tanita BC-418 to server done...");
    exitUI();
  }

  public void initialize() {
    super.initialize();

    if(!shutdown) {

      try {
        bufferedReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
      } catch(IOException e) {
        throw new RuntimeException("Cannot open serial port on BC-418", e);
      }

      resetTanita();
      initParticipantData();
    }
  }

  public void run() {
    if(!shutdown) {
      try {
        serialPort.addEventListener(this);
      } catch(TooManyListenersException e) {
        throw new RuntimeException(e);
      }
      serialPort.notifyOnDataAvailable(true);
    }

    super.run();
  }

}
