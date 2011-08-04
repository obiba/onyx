/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.ricelake;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TooManyListenersException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.LocalSettingsHelper;
import org.obiba.onyx.jade.instrument.LocalSettingsHelper.CouldNotRetrieveSettingsException;
import org.obiba.onyx.jade.instrument.LocalSettingsHelper.CouldNotSaveSettingsException;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class RiceLakeWeightInstrumentRunner implements InstrumentRunner, InitializingBean {

  private Logger log = LoggerFactory.getLogger(RiceLakeWeightInstrumentRunner.class);

  private ResourceBundle resourceBundle;

  // Injected by spring.
  private InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  protected LocalSettingsHelper settingsHelper;

  private Locale locale;

  // Interface components
  private JFrame appWindow;

  private JButton saveButton;

  private MeasureCountLabel measureCountLabel;

  protected JTextField weightTxt;

  // Interface components dimension
  private int appWindowWidth;

  private int appWindowHeight;

  // Serial port configuration
  private ArrayList<String> availablePortNames;

  private Properties localSettings;

  /**
   * Lock used to block the main thread as long as the UI has not finished its job
   */
  private final Object uiLock = new Object();

  private boolean shutdown = false;

  private RiceLakeWeightComm rlComm;

  public RiceLakeWeightInstrumentRunner() throws Exception {
    super();

    // Initialize interface components.
    saveButton = new JButton();
    saveButton.setMnemonic('S');

    weightTxt = new ResultTextField();

    // Initialize interface components size
    appWindowWidth = 300;
    appWindowHeight = 175;

    localSettings = new Properties();
  }

  @Override
  public void initialize() {
    if(!externalAppHelper.isSotfwareAlreadyStarted("tanitaInstrumentRunner")) {
      log.info("Refresh serial port list");
      refreshSerialPortList();
      log.info("Setup serial port");
      setupSerialPort();
      // If serial port is not available display error message
      if(rlComm == null) {// || !rlComm.isReady()) {
        reestablishConnection();
      }
    } else {
      JOptionPane.showMessageDialog(null, resourceBundle.getString("Err.Application_lock"), resourceBundle.getString("Title.Cannot_start_application"), JOptionPane.ERROR_MESSAGE);
      shutdown = true;
    }
  }

  @Override
  public void run() {
    if(!shutdown) {

      log.info("Starting Rice Lake GUI");
      buildGUI();

      // Obtain the lock outside the UI thread. This will block until the UI releases the lock, at which point it
      // should
      // be safe to exit the main thread.
      synchronized(uiLock) {
        try {
          uiLock.wait();
        } catch(InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      log.info("Lock obtained. Exiting software.");

    }
  }

  @Override
  public void shutdown() {
    shutdown = true;
    if(rlComm != null) {
      try {
        log.info("Closing serial port");
        rlComm.close();
      } catch(Exception e) {
        // ignore
        log.info("Error Closing serial port");
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // Attempt to retrieve settings persisted locally (if exist).
    try {
      localSettings = settingsHelper.retrieveSettings();
    } catch(CouldNotRetrieveSettingsException e) {
    }

    log.info("Setting ricelakeweight-locale to {}", getLocale().getDisplayLanguage());

    resourceBundle = ResourceBundle.getBundle("ricelakeweight-instrument", getLocale());

    // Turn off metal's use of bold fonts
    UIManager.put("swing.boldMetal", Boolean.FALSE);

    appWindow = new JFrame(resourceBundle.getString("Title.RiceLakeWeight"));

    saveButton.setToolTipText(resourceBundle.getString("ToolTip.Save_and_return"));
    saveButton.setText(resourceBundle.getString("Save"));
    saveButton.setEnabled(false);
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String getComPort() {
    return localSettings.getProperty("comPort");
  }

  public void setComPort(String tanitaCommPort) {
    localSettings.setProperty("comPort", tanitaCommPort);
  }

  public int getBaudeRate() {
    try {
      return Integer.parseInt(localSettings.getProperty("baudeRate"));
    } catch(NumberFormatException e) {
      return 9600;
    }
  }

  public void setBaudeRate(int baudeRate) {
    localSettings.setProperty("baudeRate", Integer.toString(baudeRate));
  }

  public void setSettingsHelper(LocalSettingsHelper settingsHelper) {
    this.settingsHelper = settingsHelper;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  protected void buildGUI() {

    appWindow.setAlwaysOnTop(true);
    appWindow.setResizable(false);
    appWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    appWindow.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        confirmOnExit();
      }
    });

    appWindow.add(buildMainPanel(), BorderLayout.CENTER);

    appWindow.pack();
    appWindow.setSize(appWindowWidth, appWindowHeight);

    // Display the GUI in the middle of the screen.
    Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    appWindow.setLocation(SCREEN_SIZE.width / 2 - appWindowWidth / 2, SCREEN_SIZE.height / 2 - appWindowHeight / 2);

    clearData();

    appWindow.setVisible(true);
  }

  /**
   * Puts together the GUI main panel component.
   * 
   * @return
   */
  protected JPanel buildMainPanel() {

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.add(buildMeasureCountSubPanel());
    panel.add(buildResultsSubPanel());
    panel.add(buildActionButtonSubPanel());

    return panel;
  }

  protected JPanel buildMeasureCountSubPanel() {
    JPanel panel = new JPanel();

    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(measureCountLabel = new MeasureCountLabel());
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    return (panel);
  }

  protected JPanel buildResultsSubPanel() {
    final JPanel panel = new JPanel();

    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(weightTxt);
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    return panel;
  }

  /**
   * Build action buttons sub panel
   */
  protected JPanel buildActionButtonSubPanel() {
    JPanel panel = new JPanel();

    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    JButton cancelButton = new JButton(resourceBundle.getString("Cancel"));
    cancelButton.setMnemonic('A');
    cancelButton.setToolTipText(resourceBundle.getString("ToolTip.Cancel_measurement"));
    panel.add(Box.createHorizontalGlue());
    panel.add(saveButton);
    panel.add(Box.createRigidArea(new Dimension(10, 0)));
    panel.add(cancelButton);

    // Save button listener.
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendOutputToServer();
      }
    });

    // Cancel button listener.
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        confirmOnExit();
      }
    });

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    return (panel);
  }

  class ABIFileLabel extends JLabel {

    private static final long serialVersionUID = 1L;

    public ABIFileLabel() {
      super();
    }

    @Override
    public String getText() {
      return resourceBundle.getString("No_file_selected");
    }

  }

  class MeasureCountLabel extends JLabel {

    private static final long serialVersionUID = 1L;

    public MeasureCountLabel() {
      super();
    }

    @Override
    public String getText() {
      return resourceBundle.getString("MeasureCount.Measures") + ": " + instrumentExecutionService.getCurrentMeasureCount() + " " + resourceBundle.getString("MeasureCount.saved") + ", " + instrumentExecutionService.getExpectedMeasureCount() + " " + resourceBundle.getString("MeasureCount.expected") + ".";
    }

  }

  public void sendOutputToServer() {
    log.info("Sending output of Rice Lake Weight to server...");

    Map<String, Data> output = new HashMap<String, Data>();

    // TODO
    output.put("Weight", DataBuilder.buildDecimal(weightTxt.getText()));
    instrumentExecutionService.addOutputParameterValues(output);

    saveButton.setEnabled(false);

    appWindow.repaint();

    log.info("Sending output of Rice Lake Weight to server done...");
    clearData();

    if(instrumentExecutionService.getExpectedMeasureCount() <= instrumentExecutionService.getCurrentMeasureCount()) {
      exitUI();
    }
  }

  /**
   * Displays a confirmation window when the application is closed by the user without saving.
   */
  protected void confirmOnExit() {

    // Ask for confirmation only if data has been fetch from the device.
    if(saveButton.isEnabled()) {

      int wConfirmation = JOptionPane.showConfirmDialog(appWindow, resourceBundle.getString("Confirmation.Close_window"), resourceBundle.getString("Title.Confirmation"), JOptionPane.YES_NO_OPTION);

      // If confirmed, application is closed.
      if(wConfirmation == JOptionPane.YES_OPTION) {
        exitUI();
      }

    } else {
      exitUI();
    }
  }

  /**
   * Signals that the UI has finished its job.
   */
  protected void exitUI() {
    appWindow.setVisible(false);
    synchronized(uiLock) {
      uiLock.notify();
    }
    shutdown = true;
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
   * Reestablish a lost connection.
   */
  protected void reestablishConnection() {

    String[] options = { resourceBundle.getString("OK"), resourceBundle.getString("Cancel"), resourceBundle.getString("Settings") };

    // Loop until connection is reestablished.
    int selectedOption;
    while(rlComm == null || !rlComm.isReady()) {

      selectedOption = JOptionPane.showOptionDialog(appWindow, resourceBundle.getString("Err.No_communication"), resourceBundle.getString("Title.Communication_problem"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, resourceBundle.getString("OK"));

      // OK option selected.
      if(selectedOption == 0) {

        // Try to reestablish connection.
        setupSerialPort();

        // Cancel option selected.
      } else if(selectedOption == 1) {

        exitUI();
        break;

        // Configuration option selected.
      } else if(selectedOption == 2) {

        // List all serial port in a drop down list, so a new one can be
        // selected.
        refreshSerialPortList();
        String selectedPort = (String) JOptionPane.showInputDialog(appWindow, resourceBundle.getString("Instruction.Choose_port"), resourceBundle.getString("Title.Settings"), JOptionPane.QUESTION_MESSAGE, null, availablePortNames.toArray(), getComPort());

        if(selectedPort != null) {
          setComPort(selectedPort);

          try {
            settingsHelper.saveSettings(localSettings);
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

  /**
   * Establish the connection with the device connected to the serial port.
   */
  public void setupSerialPort() {

    try {

      // If port already open, close it.
      if(rlComm != null) {
        rlComm.close();
        rlComm = null;
      }

      // Initialize serial port attributes.
      log.info("Fetching communication port {}", getComPort());
      CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(getComPort());

      log.info("Opening communication port {}", getComPort());
      SerialPort serialPort = (SerialPort) portId.open("OBiBa Onyx Rice Lake Weight Reader", 2000);

      // Make sure the port is "Clear To Send"
      rlComm = new RiceLakeWeightComm(serialPort, getBaudeRate());

    } catch(Exception e) {
      rlComm = null;
      log.warn("Could not access the specified serial port.");
    }
  }

  private void clearData() {
    weightTxt.setText("0.0");

    saveButton.setEnabled(false);

    measureCountLabel.repaint();
  }

  class RiceLakeWeightComm implements SerialPortEventListener {

    private final SerialPort serialPort;

    private final BufferedReader bufferedReader;

    public RiceLakeWeightComm(SerialPort serialPort, int baudRate) throws UnsupportedCommOperationException, TooManyListenersException, IOException {
      this.serialPort = serialPort;

      serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
      serialPort.addEventListener(this);
      serialPort.notifyOnDataAvailable(true);
      serialPort.notifyOnCTS(true);
      serialPort.notifyOnDSR(true);

      this.bufferedReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
    }

    public void close() {
      this.serialPort.close();
    }

    public boolean isReady() {
      return serialPort.isCTS();
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
      switch(event.getEventType()) {

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
            String response = bufferedReader.readLine().trim();
            log.info("data={}", response);
            parseResponse(response);

            // Enable save button, so data can be saved.
            saveButton.setEnabled(true);
          }
        } catch(IOException wErrorReadingDataOnSerialPort) {
          JOptionPane.showMessageDialog(appWindow, resourceBundle.getString("Err.Result_communication"), resourceBundle.getString("Title.Communication_error"), JOptionPane.ERROR_MESSAGE);
        }
        break;
      }
    }

    private void parseResponse(String response) {
      String[] values = response.split(" ");
      Double weight = Double.parseDouble(values[0]);
      if(values[1].equals("lb")) {
        weight = ((double) Math.round(weight * 0.45359237 * 10)) / 10;
      }
      weightTxt.setText(weight.toString());
    }
  }

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

}
