package org.obiba.onyx.jade.instrument.tanita;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple Swing application that connects to a bioimpedance and weight device (Tanita Body Composition
 * Analyzer). It allows the data to retrieve automatically through the serial port of the device.
 * 
 * @author cag-mboulanger
 * 
 */
public class Tbf310InstrumentRunner implements InstrumentRunner, SerialPortEventListener {

  private static final Logger log = LoggerFactory.getLogger(Tbf310InstrumentRunner.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  // Injected by spring.
  protected ExternalAppLauncherHelper externalAppHelper;

  // Injected by spring.
  protected String tbf310CommPort;

  // Interface components
  private JFrame appWindow;

  private JTextField bodyTypeTxt;

  private JTextField genderTxt;

  private JTextField heightTxt;

  private JTextField weightTxt;

  private JTextField impedanceTxt;

  private JTextField fatPctTxt;

  private JTextField fatMassTxt;

  private JTextField ffmTxt;

  private JTextField tbwTxt;

  private JTextField ageTxt;

  private JTextField bmiTxt;

  private JTextField bmrTxt;

  private JButton saveDataBtn;

  /** Lock used to block the main thread as long as the UI has not finished its job */
  private final Object uiLock = new Object();

  class ResultTextField extends JTextField {

    private static final long serialVersionUID = 1L;

    public ResultTextField() {
      super();
      this.setEditable(false);
      this.setSelectionColor(Color.WHITE);
      this.setBackground(Color.WHITE);
      this.setHorizontalAlignment(JTextField.RIGHT);
      this.setPreferredSize(new Dimension(30, 0));
    }

  }

  // Serial port attributes
  private StringBuffer tanitaData = new StringBuffer();

  private InputStream inputStream;

  private SerialPort serialPort = null;

  private boolean portIsAvailable = false;

  private String portOwnerName;

  private boolean shutdown = false;

  public Tbf310InstrumentRunner() throws Exception {

    // Initialize interface components.
    bodyTypeTxt = new ResultTextField();
    bodyTypeTxt.setHorizontalAlignment(JTextField.LEFT);
    bodyTypeTxt.setPreferredSize(new Dimension(100, 0));
    genderTxt = new ResultTextField();
    genderTxt.setHorizontalAlignment(JTextField.LEFT);
    genderTxt.setPreferredSize(new Dimension(100, 0));
    heightTxt = new ResultTextField();
    weightTxt = new ResultTextField();
    impedanceTxt = new ResultTextField();
    fatPctTxt = new ResultTextField();
    fatMassTxt = new ResultTextField();
    ffmTxt = new ResultTextField();
    tbwTxt = new ResultTextField();
    ageTxt = new ResultTextField();
    bmiTxt = new ResultTextField();
    bmrTxt = new ResultTextField();
    saveDataBtn = new JButton("Sauvegarder");
    saveDataBtn.setMnemonic('S');
    saveDataBtn.setEnabled(false);
    saveDataBtn.setToolTipText("Sauvegarder les mesures et retourner à l'interface CaG");

    // Initialize serial port.
    portOwnerName = "TANITA Body Composition Analyzer";
    // Test string
    /*
     * setTanitaData( parseTanitaData( "0,2,185,110.6,431,28.4,31.4,79.2,58.0,27,32.3,9771" ) ); saveDataBtn.setEnabled(
     * true );
     */

  }

  public InstrumentExecutionService getInstrumentExecutionService() {
    return instrumentExecutionService;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public ExternalAppLauncherHelper getExternalAppHelper() {
    return externalAppHelper;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public String getTbf310CommPort() {
    return tbf310CommPort;
  }

  public void setTbf310CommPort(String tbf310CommPort) {
    this.tbf310CommPort = tbf310CommPort;
  }

  /**
   * Establish the connection with the device connected to the serial port.
   */
  private void setupSerialPort() {

    try {
      // List all ports found to the console. Used for debugging...
      Enumeration en = CommPortIdentifier.getPortIdentifiers();
      while(en != null && en.hasMoreElements()) {
        CommPortIdentifier type = (CommPortIdentifier) en.nextElement();
        System.out.println("type.name=" + type.getName() + " type.portType=" + type.getPortType());
      }

      // If port already open, close it.
      if(serialPort != null) {
        serialPort.close();
        serialPort = null;
      }

      // Initialize serial port attributes.
      log.info("Fetching communication port {}", getTbf310CommPort());
      CommPortIdentifier wPortId = CommPortIdentifier.getPortIdentifier(getTbf310CommPort());

      log.info("Opening communication port {}", getTbf310CommPort());
      serialPort = (SerialPort) wPortId.open(portOwnerName, 2000);

      // Make sure the port is "Clear To Send"
      if(serialPort.isCTS()) {

        // Set serial port parameters.
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
        serialPort.notifyOnCTS(true);
        serialPort.notifyOnCarrierDetect(true);
        serialPort.notifyOnRingIndicator(true);
        serialPort.notifyOnDSR(true);
        serialPort.setSerialPortParams(2400, SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);

        inputStream = serialPort.getInputStream();

        portIsAvailable = true;

      } else {
        portIsAvailable = false;
      }

    } catch(Exception wCouldNotAccessSerialPort) {
      portIsAvailable = false;
      wCouldNotAccessSerialPort.printStackTrace();
    }

  }

  /**
   * Reestablish a lost connection.
   */
  private void reestablishConnection() {

    // Loop until connection is reestablished.
    int wConfirmation;
    while(serialPort == null || !serialPort.isCTS()) {

      wConfirmation = JOptionPane.showConfirmDialog(appWindow, "La communication n'a pu être établie\n" + "avec l'appareil de bioimpédance!\n\n" + "Vérifiez si les cables sont bien\n" + "branchés et appuyez ensuite sur OK.", "Problème de communication", JOptionPane.OK_CANCEL_OPTION);

      if(wConfirmation == JOptionPane.OK_OPTION) {

        // Try to reestablish connection.
        setupSerialPort();

      } else {
        exitUI();
      }

    }

  }

  /**
   * Parses the Bioimpedance device output string (Tanita).
   * 
   * @param pTanitaOutput Output string.
   * @return Parsed output string.
   */
  private String[] parseTanitaData(String pTanitaOutput) {
    return pTanitaOutput.split(",");
  }

  /**
   * Sets the Bioimpedance data in the GUI components.
   * 
   * @param pOutputData The parsed output data from the Tanita.
   */
  private void setTanitaData(String[] pOutputData) {

    String wBodyTypeCode = pOutputData[0];
    if(wBodyTypeCode.endsWith("0")) {
      bodyTypeTxt.setText("Standard");
    } else if(wBodyTypeCode.endsWith("2")) {
      bodyTypeTxt.setText("Athlétique");
    }

    String wGender = pOutputData[1];
    if(wGender.equals("1")) {
      genderTxt.setText("Homme");
    } else if(wGender.equals("2")) {
      genderTxt.setText("Femme");
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
        // Only try to reestablish if we're not shutting down the application.
        reestablishConnection();
      }

      break;

    // Data is available at the serial port, so read it...
    case SerialPortEvent.DATA_AVAILABLE:

      try {

        // Read the serial port until there is nothing left to read.
        StringBuffer readBuffer = new StringBuffer();

        while(inputStream.available() > 0) {
          readBuffer.append((char) inputStream.read());
        }

        // Append the data we just read to the output string.
        tanitaData.append(readBuffer.toString());

        // If data ends with a carriage return, this means
        // that we have reach the end of the output string (Tanita output).
        if(tanitaData.toString().endsWith("\r\n")) {

          // Parse and sets the data in the GUI.
          setTanitaData(parseTanitaData(tanitaData.toString()));

          // Enable save button, so data can be saved.
          saveDataBtn.setEnabled(true);

        }

      } catch(IOException wErrorReadingDataOnSerialPort) {
        tanitaData = new StringBuffer();
        JOptionPane.showMessageDialog(appWindow, "Une erreur s'est produite lors de la communication\n" + "des résultats par l'appareil de bioimpédance." + "Veuillez reprendre les mesures SVP.", "Erreur de communication", JOptionPane.ERROR_MESSAGE);
      }

      break;

    }
  }

  /**
   * Builds the GUI which will display the bioimpedance results.
   */
  private void buildGUI() {

    appWindow = new JFrame("TANITA Body Composition Analyzer");

    appWindow.setAlwaysOnTop(true);
    appWindow.setUndecorated(true);
    appWindow.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
    appWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    appWindow.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        confirmOnExit();
      }
    });

    appWindow.getContentPane().add(buildMainPanel(), BorderLayout.CENTER);

    appWindow.pack();
    appWindow.setSize(460, 260);

    // Display the GUI in the middle of the screen.
    Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    appWindow.setLocation(SCREEN_SIZE.width / 2 - 420 / 2, SCREEN_SIZE.height / 2 - 260 / 2);

    appWindow.setBackground(Color.white);
    appWindow.setVisible(true);

    // If serial port is not available display error message
    if(!portIsAvailable) {
      reestablishConnection();
    }

  }

  /**
   * Signals that the UI has finished its job.
   */
  private void exitUI() {
    appWindow.setVisible(false);
    synchronized(uiLock) {
      uiLock.notify();
    }
  }

  /**
   * Puts together the GUI main panel component.
   * 
   * @return
   */
  private JPanel buildMainPanel() {

    JPanel wMainPanel = new JPanel();
    wMainPanel.setBackground(new Color(206, 231, 255));
    wMainPanel.setLayout(new BoxLayout(wMainPanel, BoxLayout.Y_AXIS));

    // Add the results sub panel.
    JPanel wResultPanel = new JPanel();
    GridLayout wResultPanelLayout = new GridLayout(0, 2);
    wResultPanel.setBackground(new Color(206, 231, 255));
    wResultPanelLayout.setHgap(60);
    wResultPanel.setLayout(wResultPanelLayout);
    wResultPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    wMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    addDataField(bodyTypeTxt, wResultPanel, "Type Corporel :", "");
    addDataField(genderTxt, wResultPanel, "Sexe :", "");
    addDataField(heightTxt, wResultPanel, "Taille :", " cm");
    addDataField(weightTxt, wResultPanel, "Poids :", " kg");
    addDataField(impedanceTxt, wResultPanel, "Résistance :", " \u03A9");
    addDataField(fatPctTxt, wResultPanel, "% graisse :", " %");
    addDataField(fatMassTxt, wResultPanel, "Masse grasse :", " kg");
    addDataField(ffmTxt, wResultPanel, "Masse maigre :", " kg");
    addDataField(tbwTxt, wResultPanel, "Masse hydrique :", " kg");
    addDataField(ageTxt, wResultPanel, "Âge :", " ans");
    addDataField(bmiTxt, wResultPanel, "IMC :", "");
    addDataField(bmrTxt, wResultPanel, "MB :", " kJ");

    // Add the action buttons sub panel.
    JPanel wButtonPanel = new JPanel();
    wButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    wButtonPanel.setLayout(new BoxLayout(wButtonPanel, BoxLayout.X_AXIS));
    wButtonPanel.setBackground(new Color(206, 231, 255));
    JButton wCancelBtn = new JButton("Annuler");
    wCancelBtn.setMnemonic('A');
    wCancelBtn.setToolTipText("Annuler la prise de mesure");
    wButtonPanel.add(Box.createHorizontalGlue());
    wButtonPanel.add(saveDataBtn);
    wButtonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    wButtonPanel.add(wCancelBtn);

    // Save button listener.
    saveDataBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendOutputToServer();
      }
    });

    // Cancel button listener.
    wCancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        confirmOnExit();
      }
    });

    wMainPanel.add(wResultPanel);
    wMainPanel.add(wButtonPanel);

    return wMainPanel;
  }

  /**
   * Adds a data field to the GUI.
   * 
   * @param pField JTextField object.
   * @param pTargetPanel Target panel to which the data field will be added.
   * @param pLabel A label which will be displayed to the left of the field.
   * @param pUnits Units which will be displayed to the right of the field.
   */
  private void addDataField(JTextField pField, JPanel pTargetPanel, String pLabel, String pUnits) {

    // Create field sub panel.
    JPanel wFieldPanel = new JPanel();
    wFieldPanel.setBackground(new Color(206, 231, 255));
    wFieldPanel.setLayout(new BoxLayout(wFieldPanel, BoxLayout.X_AXIS));
    wFieldPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    // Add field label.
    JLabel wFieldLabel = new JLabel(pLabel);
    wFieldLabel.setPreferredSize(new Dimension(100, 60));
    wFieldPanel.add(wFieldLabel);

    // Add text field.
    wFieldPanel.add(pField);

    // Add units label.
    JLabel wFieldUnit = new JLabel(pUnits);
    wFieldUnit.setPreferredSize(new Dimension(30, 60));
    wFieldPanel.add(wFieldUnit);

    // Add sub panel to main panel.
    pTargetPanel.add(wFieldPanel);

  }

  /**
   * Displays a confirmation window when the application is closed by the user without saving.
   */
  private void confirmOnExit() {

    // Ask for confirmation only if data has been fetch from the device.
    if(saveDataBtn.isEnabled()) {

      int wConfirmation = JOptionPane.showConfirmDialog(appWindow, "Voulez-vous vraiment fermer cette fenêtre?\n" + "(les données affichées seront perdues)", "Confirmation", JOptionPane.YES_NO_OPTION);

      // If confirmed, application is closed.
      if(wConfirmation == JOptionPane.YES_OPTION) {
        exitUI();
      }

    } else {
      exitUI();
    }

  }

  private void sendOutputToServer() {

    Map<String, Data> wOutput = new HashMap<String, Data>();
    wOutput.put("weight", getDecimalValue(weightTxt));
    wOutput.put("impedance", getIntegerValue(impedanceTxt));
    wOutput.put("bmi", getDecimalValue(bmiTxt));
    wOutput.put("bmr", getIntegerValue(bmrTxt));
    wOutput.put("fatFreeMass", getDecimalValue(ffmTxt));
    wOutput.put("fatMass", getDecimalValue(fatMassTxt));
    wOutput.put("totalBodyWater", getDecimalValue(tbwTxt));
    wOutput.put("fatPct", getDecimalValue(fatPctTxt));
    wOutput.put("gender", new Data(DataType.TEXT, genderTxt.getText().equals("Homme") ? "MALE" : "FEMALE )"));
    wOutput.put("height", getIntegerValue(heightTxt));
    wOutput.put("age", getIntegerValue(ageTxt));
    instrumentExecutionService.addOutputParameterValues(wOutput);
    exitUI();
  }

  private Data getIntegerValue(JTextField f) {
    return new Data(DataType.INTEGER, new Integer(f.getText().trim()));
  }

  private Data getDecimalValue(JTextField f) {
    return new Data(DataType.DECIMAL, new Double(f.getText().trim()));
  }

  public void initialize() {
    log.info("Initializing Tanita Runner");
    setupSerialPort();
  }

  public void run() {

    if(!externalAppHelper.isSotfwareAlreadyStarted("tbf310InstrumentRunner")) {
      buildGUI();

      // Obtain the lock outside the UI thread. This will block until the UI releases the lock, at which point it should
      // be safe to exit the main thread.
      synchronized(uiLock) {
        try {
          uiLock.wait();
        } catch(InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      log.info("Lock obtained. Exiting software.");
    } else {
      JOptionPane.showMessageDialog(null, "Tanita TBF-310 already lock for execution.  Please make sure that another instance is not running.", "Cannot start application!", JOptionPane.ERROR_MESSAGE);
    }

  }

  public void shutdown() {
    log.info("Shuting down runner");
    shutdown = true;
    if(serialPort != null) {
      try {
        log.info("Closing serial port");
        serialPort.close();
      } catch(Exception e) {
        // ignore
      }
    }
  }

}
