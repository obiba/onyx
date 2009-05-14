package org.obiba.onyx.jade.instrument.tanita;

import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.LoggerFactory;

public class Tbf310InstrumentRunner extends TanitaInstrument {

  public Tbf310InstrumentRunner() throws Exception {
    super();
    log = LoggerFactory.getLogger(Tbf310InstrumentRunner.class);
  }

  protected boolean checkIfPortIsAvailable() throws TooManyListenersException {

    if(serialPort.isCTS()) {

      // Set serial port parameters.
      serialPort.addEventListener(this);
      serialPort.notifyOnDataAvailable(true);
      serialPort.notifyOnCTS(true);
      serialPort.notifyOnCarrierDetect(true);
      serialPort.notifyOnRingIndicator(true);
      serialPort.notifyOnDSR(true);

      return true;

    } else {
      return false;
    }

  }

  /**
   * Sets the Bioimpedance data in the GUI components.
   * 
   * @param pOutputData The parsed output data from the Tanita.
   */
  public void setTanitaData(String[] pOutputData) {

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

    instrumentExecutionService.addOutputParameterValues(output);
    clearTanitaData();
    log.info("Sending output of Tanita TBF-310 to server done...");
    if(instrumentExecutionService.getExpectedMeasureCount() <= instrumentExecutionService.getCurrentMeasureCount()) {
      exitUI();
    }
  }
}
