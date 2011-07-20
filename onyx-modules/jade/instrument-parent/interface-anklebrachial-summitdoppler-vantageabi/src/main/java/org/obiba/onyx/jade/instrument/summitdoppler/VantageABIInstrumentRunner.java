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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.LocalSettingsHelper;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class VantageABIInstrumentRunner implements InstrumentRunner, InitializingBean {

  protected Logger log = LoggerFactory.getLogger(VantageABIInstrumentRunner.class);

  protected ResourceBundle resourceBundle;

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  protected LocalSettingsHelper settingsHelper;

  private Locale locale;

  // Interface components
  protected JFrame appWindow;

  protected JButton saveDataBtn;

  // Interface components dimension
  protected int appWindowWidth;

  protected int appWindowHeight;

  /**
   * Lock used to block the main thread as long as the UI has not finished its job
   */
  protected final Object uiLock = new Object();

  protected boolean shutdown = false;

  public VantageABIInstrumentRunner() throws Exception {
    super();

    // Initialize interface components.
    saveDataBtn = new JButton();
    saveDataBtn.setMnemonic('S');
    // saveDataBtn.setEnabled(false);

    // Initialize interface components size
    appWindowWidth = 525;
    appWindowHeight = 300;
  }

  @Override
  public void initialize() {
    // TODO Auto-generated method stub

  }

  @Override
  public void run() {
    if(!shutdown) {

      log.info("Starting Tanita GUI");
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
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    log.info("Setting anklebrachial-locale to {}", getLocale().getDisplayLanguage());

    resourceBundle = ResourceBundle.getBundle("anklebrachial-instrument", getLocale());
    appWindow = new JFrame(resourceBundle.getString("Title.VantageABI"));
    saveDataBtn.setToolTipText(resourceBundle.getString("ToolTip.Save_and_return"));
    saveDataBtn.setText(resourceBundle.getString("Save"));
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public void setSettingsHelper(LocalSettingsHelper settingsHelper) {
    this.settingsHelper = settingsHelper;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  protected void buildGUI() {

    appWindow.setAlwaysOnTop(true);
    appWindow.setUndecorated(true);
    appWindow.setResizable(false);
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
   * Puts together the GUI main panel component.
   * 
   * @return
   */
  protected JPanel buildMainPanel() {

    JPanel wMainPanel = new JPanel();
    wMainPanel.setBackground(new Color(206, 231, 255));
    wMainPanel.setLayout(new BoxLayout(wMainPanel, BoxLayout.Y_AXIS));
    wMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    // TODO ABI file selection
    wMainPanel.add(buildActionButtonSubPanel());

    return wMainPanel;
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
    JButton wCancelBtn = new JButton(resourceBundle.getString("Cancel"));
    wCancelBtn.setMnemonic('A');
    wCancelBtn.setToolTipText(resourceBundle.getString("ToolTip.Cancel_measurement"));
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

  public void sendOutputToServer() {
    log.info("Sending output of Vantage ABI to server...");

    Map<String, Data> output = new HashMap<String, Data>();

    VantageReportParser parser = new VantageReportParser();
    // TODO parser.parse();

    // output.put("Name", new Data(DataType.TEXT, bodyTypeTxt.getText()));

    instrumentExecutionService.addOutputParameterValues(output);

    log.info("Sending output of Vantage ABI to server done...");
    if(instrumentExecutionService.getExpectedMeasureCount() <= instrumentExecutionService.getCurrentMeasureCount()) {
      exitUI();
    }
  }

  /**
   * Displays a confirmation window when the application is closed by the user without saving.
   */
  protected void confirmOnExit() {

    // Ask for confirmation only if data has been fetch from the device.
    if(saveDataBtn.isEnabled()) {

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

}
