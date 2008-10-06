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
import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

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
import org.obiba.onyx.jade.instrument.LocalSettingsHelper;
import org.obiba.onyx.jade.instrument.LocalSettingsHelper.CouldNotRetrieveSettingsException;
import org.obiba.onyx.jade.instrument.LocalSettingsHelper.CouldNotSaveSettingsException;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * This is a simple Swing application that connects to a bioimpedance and weight device (Tanita Body Composition
 * Analyzer). It allows the data to retrieve automatically through the serial port of the device.
 * 
 * @author cag-mboulanger
 * 
 */

@SuppressWarnings( { "unused", "unused" })
public class TanitaInstrument implements InstrumentRunner, InitializingBean, SerialPortEventListener {

  protected Logger log;

  protected ResourceBundle tanitaResourceBundle;

  @SuppressWarnings("unused")
  private String tanitaCommPort;

  protected BufferedReader bufferedReader;

  protected OutputStream outputStream;

  private Locale locale;

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  protected LocalSettingsHelper settingsHelper;

  // Interface components
  protected JFrame appWindow;

  protected JTextField bodyTypeTxt;

  protected JTextField genderTxt;

  protected JTextField heightTxt;

  protected JTextField weightTxt;

  protected JTextField impedanceTxt;

  protected JTextField fatPctTxt;

  protected JTextField fatMassTxt;

  protected JTextField ffmTxt;

  protected JTextField tbwTxt;

  protected JTextField ageTxt;

  protected JTextField bmiTxt;

  protected JTextField bmrTxt;

  protected JButton saveDataBtn;

  // Interface components dimension
  protected int appWindowWidth;

  protected int appWindowHeight;

  protected int gridCol;

  /**
   * Lock used to block the main thread as long as the UI has not finished its job
   */
  protected final Object uiLock = new Object();

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

  // Serial port configuration
  protected SerialPort serialPort = null;

  protected int baudeRate;

  protected int dataLength;

  protected int parity;

  protected int stopBit;

  protected boolean portIsAvailable = false;

  protected String portOwnerName;

  private ArrayList<String> availablePortNames;

  protected boolean shutdown = false;

  protected Properties tanitaLocalSettings;

  protected TanitaInstrument() throws Exception {

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

    saveDataBtn = new JButton();
    saveDataBtn.setMnemonic('S');
    saveDataBtn.setEnabled(false);

    // Initialize interface components size
    appWindowWidth = 460;
    appWindowHeight = 260;
    gridCol = 2;

    // Initialize serial port.
    portOwnerName = "TANITA Body Composition Analyzer";
  }

  public void afterPropertiesSet() throws Exception {
    // Attempt to retrieve settings persisted locally (if exist).
    try {
      tanitaLocalSettings = settingsHelper.retrieveSettings();
    } catch(CouldNotRetrieveSettingsException e) {
    }

    log.info("Setting bioimpedance-locale to {}", getLocale().getDisplayLanguage());

    tanitaResourceBundle = ResourceBundle.getBundle("bioimpedance-instrument", getLocale());
    saveDataBtn.setToolTipText(tanitaResourceBundle.getString("ToolTip.Save_and_return"));
    saveDataBtn.setText(tanitaResourceBundle.getString("Save"));
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

  public LocalSettingsHelper getSettingsHelper() {
    return settingsHelper;
  }

  public void setSettingsHelper(LocalSettingsHelper settingsHelper) {
    this.settingsHelper = settingsHelper;
  }

  public String getTanitaCommPort() {
    return tanitaLocalSettings.getProperty("commPort");
  }

  public void setTanitaCommPort(String tanitaCommPort) {
    if(tanitaLocalSettings == null) {
      tanitaLocalSettings = new Properties();
    }
    tanitaLocalSettings.setProperty("commPort", tanitaCommPort);
  }

  public int getBaudeRate() {
    return baudeRate;
  }

  public void setBaudeRate(int baudeRate) {
    this.baudeRate = baudeRate;
  }

  public int getDataLength() {
    return dataLength;
  }

  public void setDataLength(int dataLength) {
    this.dataLength = dataLength;
  }

  public int getParity() {
    return parity;
  }

  public void setParity(int parity) {
    this.parity = parity;
  }

  public int getStopBit() {
    return stopBit;
  }

  public void setStopBit(int stopBit) {
    this.stopBit = stopBit;
  }

  public JTextField getBodyTypeTxt() {
    return bodyTypeTxt;
  }

  public void setBodyTypeTxt(JTextField bodyTypeTxt) {
    this.bodyTypeTxt = bodyTypeTxt;
  }

  public JTextField getGenderTxt() {
    return genderTxt;
  }

  public void setGenderTxt(JTextField genderTxt) {
    this.genderTxt = genderTxt;
  }

  public JTextField getHeightTxt() {
    return heightTxt;
  }

  public void setHeightTxt(JTextField heightTxt) {
    this.heightTxt = heightTxt;
  }

  public JTextField getWeightTxt() {
    return weightTxt;
  }

  public void setWeightTxt(JTextField weightTxt) {
    this.weightTxt = weightTxt;
  }

  public JTextField getImpedanceTxt() {
    return impedanceTxt;
  }

  public void setImpedanceTxt(JTextField impedanceTxt) {
    this.impedanceTxt = impedanceTxt;
  }

  public JTextField getFatPctTxt() {
    return fatPctTxt;
  }

  public void setFatPctTxt(JTextField fatPctTxt) {
    this.fatPctTxt = fatPctTxt;
  }

  public JTextField getFatMassTxt() {
    return fatMassTxt;
  }

  public void setFatMassTxt(JTextField fatMassTxt) {
    this.fatMassTxt = fatMassTxt;
  }

  public JTextField getFfmTxt() {
    return ffmTxt;
  }

  public void setFfmTxt(JTextField ffmTxt) {
    this.ffmTxt = ffmTxt;
  }

  public JTextField getTbwTxt() {
    return tbwTxt;
  }

  public void setTbwTxt(JTextField tbwTxt) {
    this.tbwTxt = tbwTxt;
  }

  public JTextField getAgeTxt() {
    return ageTxt;
  }

  public void setAgeTxt(JTextField ageTxt) {
    this.ageTxt = ageTxt;
  }

  public JTextField getBmiTxt() {
    return bmiTxt;
  }

  public void setBmiTxt(JTextField bmiTxt) {
    this.bmiTxt = bmiTxt;
  }

  public JTextField getBmrTxt() {
    return bmrTxt;
  }

  public void setBmrTxt(JTextField bmrTxt) {
    this.bmrTxt = bmrTxt;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  protected Data getIntegerValue(JTextField f) {
    return new Data(DataType.INTEGER, new Long(f.getText().trim()));
  }

  protected Data getDecimalValue(JTextField f) {
    return new Data(DataType.DECIMAL, new Double(f.getText().trim()));
  }

  @SuppressWarnings("unchecked")
  protected void refreshSerialPortList() {

    log.info("Refreshing serial port list...");
    Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
    availablePortNames = new ArrayList<String>();

    // Build a list of all serial ports found.
    while(portEnum != null && portEnum.hasMoreElements()) {

      CommPortIdentifier port = portEnum.nextElement();
      if(port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
        log.info("Port name={}, Port type={}", port.getName(), port.getPortType());
        availablePortNames.add(port.getName());
      }
    }
  }

  /**
   * Parses the Bioimpedance device output string (Tanita).
   * 
   * @param pTanitaOutput Output string.
   * @return Parsed output string.
   */
  protected String[] parseTanitaData(String pTanitaOutput) {
    return pTanitaOutput.split(",");
  }

  /**
   * Sets the Bioimpedance data in the GUI components.
   * 
   * @param pOutputData The parsed output data from the Tanita.
   */
  protected void setTanitaData(String[] pOutputData) {
  }

  protected void sendOutputToServer() {
  }

  /**
   * Builds the GUI which will display the bioimpedance results.
   */
  protected void buildGUI() {

    appWindow = new JFrame(tanitaResourceBundle.getString("Title.Tanita"));

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
    appWindow.setSize(appWindowWidth, appWindowHeight);

    // Display the GUI in the middle of the screen.
    Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    appWindow.setLocation(SCREEN_SIZE.width / 2 - appWindowWidth / 2, SCREEN_SIZE.height / 2 - appWindowHeight / 2);

    appWindow.setBackground(Color.white);
    appWindow.setVisible(true);
  }

  /**
   * Signals that the UI has finished its job.
   */
  protected void exitUI() {
    appWindow.setVisible(false);
    synchronized(uiLock) {
      uiLock.notify();
    }
    setTanitaCommPort(null);
  }

  /**
   * Build results sub panel
   */
  protected JPanel buildResultSubPanel() {
    // Add the results sub panel.
    JPanel wResultPanel = new JPanel();

    // Number of elements per line
    GridLayout wResultPanelLayout = new GridLayout(0, gridCol);
    wResultPanel.setBackground(new Color(206, 231, 255));
    wResultPanelLayout.setHgap(60);
    wResultPanel.setLayout(wResultPanelLayout);
    wResultPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    addDataField(bodyTypeTxt, wResultPanel, tanitaResourceBundle.getString("Body_type"), null);
    addDataField(genderTxt, wResultPanel, tanitaResourceBundle.getString("Gender"), null);
    addDataField(heightTxt, wResultPanel, tanitaResourceBundle.getString("Height"), tanitaResourceBundle.getString("cm"));
    addDataField(weightTxt, wResultPanel, tanitaResourceBundle.getString("Weight"), tanitaResourceBundle.getString("kg"));
    addDataField(impedanceTxt, wResultPanel, tanitaResourceBundle.getString("Impedance"), tanitaResourceBundle.getString("Ohm"));
    addDataField(fatPctTxt, wResultPanel, tanitaResourceBundle.getString("Fat_percentage"), tanitaResourceBundle.getString("%"));
    addDataField(fatMassTxt, wResultPanel, tanitaResourceBundle.getString("Fat_mass"), tanitaResourceBundle.getString("kg"));
    addDataField(ffmTxt, wResultPanel, tanitaResourceBundle.getString("Fat_free_mass"), tanitaResourceBundle.getString("kg"));
    addDataField(tbwTxt, wResultPanel, tanitaResourceBundle.getString("Total_body_water"), tanitaResourceBundle.getString("kg"));
    addDataField(ageTxt, wResultPanel, tanitaResourceBundle.getString("Age"), tanitaResourceBundle.getString("years"));
    addDataField(bmiTxt, wResultPanel, tanitaResourceBundle.getString("BMI"), null);
    addDataField(bmrTxt, wResultPanel, tanitaResourceBundle.getString("BMR"), tanitaResourceBundle.getString("kJ"));

    return (wResultPanel);
  }

  /**
   * Build action buttons sub panel
   */
  protected JPanel buildActionButtonSubPanel() {

    // Add the action buttons sub panel.
    JPanel wButtonPanel = new JPanel();
    wButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    wButtonPanel.setLayout(new BoxLayout(wButtonPanel, BoxLayout.X_AXIS));
    wButtonPanel.setBackground(new Color(206, 231, 255));
    JButton wCancelBtn = new JButton(tanitaResourceBundle.getString("Cancel"));
    wCancelBtn.setMnemonic('A');
    wCancelBtn.setToolTipText(tanitaResourceBundle.getString("ToolTip.Cancel_measurement"));
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

    return (wButtonPanel);
  }

  /**
   * Puts together the GUI main panel component.
   * 
   * @return
   */
  protected JPanel buildMainPanel() {

    JPanel wMainPanel = new JPanel();
    wMainPanel.setBackground(new Color(206, 231, 255));
    wMainPanel.setLayout(new BoxLayout(wMainPanel, BoxLayout.Y_AXIS));
    wMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    wMainPanel.add(buildResultSubPanel());
    wMainPanel.add(buildActionButtonSubPanel());

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
  protected void addDataField(JTextField pField, JPanel pTargetPanel, String pLabel, String pUnits) {

    // Create field sub panel.
    JPanel wFieldPanel = new JPanel();
    wFieldPanel.setBackground(new Color(206, 231, 255));
    wFieldPanel.setLayout(new BoxLayout(wFieldPanel, BoxLayout.X_AXIS));
    wFieldPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    // Add field label.
    JLabel wFieldLabel = new JLabel(pLabel + ":");
    wFieldLabel.setPreferredSize(new Dimension(100, 60));
    wFieldPanel.add(wFieldLabel);

    // Add text field.
    wFieldPanel.add(pField);

    // Add units label.
    if(pUnits != null) {
      JLabel wFieldUnit = new JLabel(" " + pUnits);
      wFieldUnit.setPreferredSize(new Dimension(55, 60));
      wFieldPanel.add(wFieldUnit);
    }

    // Add sub panel to main panel.
    pTargetPanel.add(wFieldPanel);

  }

  /**
   * Displays a confirmation window when the application is closed by the user without saving.
   */
  protected void confirmOnExit() {

    // Ask for confirmation only if data has been fetch from the device.
    if(saveDataBtn.isEnabled()) {

      int wConfirmation = JOptionPane.showConfirmDialog(appWindow, tanitaResourceBundle.getString("Confirmation.Close_window"), tanitaResourceBundle.getString("Title.Confirmation"), JOptionPane.YES_NO_OPTION);

      // If confirmed, application is closed.
      if(wConfirmation == JOptionPane.YES_OPTION) {
        exitUI();
      }

    } else {
      exitUI();
    }
  }

  /**
   * Reestablish a lost connection.
   */
  protected void reestablishConnection() {

    String[] options = { tanitaResourceBundle.getString("OK"), tanitaResourceBundle.getString("Cancel"), tanitaResourceBundle.getString("Settings") };

    // Loop until connection is reestablished.
    int selectedOption;
    while(serialPort == null || !serialPort.isCTS()) {

      selectedOption = JOptionPane.showOptionDialog(appWindow, tanitaResourceBundle.getString("Err.No_communication"), tanitaResourceBundle.getString("Title.Communication_problem"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, tanitaResourceBundle.getString("OK"));

      // OK option selected.
      if(selectedOption == 0) {

        // Try to reestablish connection.
        setupSerialPort();

        // Cancel option selected.
      } else if(selectedOption == 1) {

        // Temporary fix, need to find another solution...
        exitUI();

        // Configuration option selected.
      } else if(selectedOption == 2) {

        // List all serial port in a drop down list, so a new one can be
        // selected.
        refreshSerialPortList();
        String selectedPort = (String) JOptionPane.showInputDialog(appWindow, tanitaResourceBundle.getString("Instruction.Choose_port"), tanitaResourceBundle.getString("Title.Settings"), JOptionPane.QUESTION_MESSAGE, null, availablePortNames.toArray(), getTanitaCommPort());

        if(selectedPort != null) {
          setTanitaCommPort(selectedPort);

          try {
            settingsHelper.saveSettings(tanitaLocalSettings);
          } catch(CouldNotSaveSettingsException e) {
            log.error("Local settings could not be persisted.", e);
          }

          setupSerialPort();
        } else {
          exitUI();
        }
      }
    }
  }

  protected void setupSerialPort() {
  }

  public void serialEvent(SerialPortEvent pEvent) {
  }

  public void initialize() {
    log.info("Refresh serial port list");
    refreshSerialPortList();
   log.info("Setup serial port");
    setupSerialPort();
    // If serial port is not available display error message
    if(!portIsAvailable) {
      reestablishConnection();
    }
  }

  public void run() {
  }

  public void shutdown() {
    shutdown = true;
    if(serialPort != null) {
      try {
        log.info("Closing serial port");
        serialPort.close();
      } catch(Exception e) {
        // ignore
        log.info("Error Closing serial port");
      }
    }
  }
}
