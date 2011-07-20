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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.instrument.summitdoppler.VantageReportParser.ExamData;
import org.obiba.onyx.jade.instrument.summitdoppler.VantageReportParser.SideData;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class VantageABIInstrumentRunner implements InstrumentRunner, InitializingBean {

  private Logger log = LoggerFactory.getLogger(VantageABIInstrumentRunner.class);

  private ResourceBundle resourceBundle;

  // Injected by spring.
  private InstrumentExecutionService instrumentExecutionService;

  private Locale locale;

  private File abiFile;

  // Interface components
  private JFrame appWindow;

  private JButton saveButton;

  private MeasureCountLabel measureCountLabel;

  private JFileChooser fileChooser;

  // Interface components dimension
  private int appWindowWidth;

  private int appWindowHeight;

  /**
   * Lock used to block the main thread as long as the UI has not finished its job
   */
  private final Object uiLock = new Object();

  private boolean shutdown = false;

  public VantageABIInstrumentRunner() throws Exception {
    super();

    // Initialize interface components.
    saveButton = new JButton();
    saveButton.setMnemonic('S');
    // saveDataBtn.setEnabled(false);

    fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileFilter() {

      @Override
      public String getDescription() {
        return "Vantage ABI (.ABI)";
      }

      @Override
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".ABI");
      }
    });

    // Initialize interface components size
    appWindowWidth = 300;
    appWindowHeight = 175;
  }

  @Override
  public void initialize() {
    // TODO Auto-generated method stub

  }

  @Override
  public void run() {
    if(!shutdown) {

      log.info("Starting Vantage ABI GUI");
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

    // Turn off metal's use of bold fonts
    UIManager.put("swing.boldMetal", Boolean.FALSE);

    appWindow = new JFrame(resourceBundle.getString("Title.VantageABI"));

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

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  protected void buildGUI() {

    appWindow.setAlwaysOnTop(true);
    // appWindow.setUndecorated(true);
    appWindow.setResizable(false);
    // appWindow.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
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

    // appWindow.setBackground(Color.white);
    appWindow.setVisible(true);
  }

  /**
   * Puts together the GUI main panel component.
   * 
   * @return
   */
  protected JPanel buildMainPanel() {

    JPanel panel = new JPanel();
    // panel.setBackground(new Color(206, 231, 255));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.add(buildMeasureCountSubPanel());
    panel.add(buildFileSelectionSubPanel());
    panel.add(buildActionButtonSubPanel());

    return panel;
  }

  protected JPanel buildMeasureCountSubPanel() {
    // Add the results sub panel.
    JPanel panel = new JPanel();

    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    // panel.setBackground(new Color(206, 231, 255));

    panel.add(measureCountLabel = new MeasureCountLabel());

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    return (panel);
  }

  protected JPanel buildFileSelectionSubPanel() {
    final JPanel panel = new JPanel();

    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    // panel.setBackground(new Color(206, 231, 255));
    panel.add(Box.createVerticalGlue());

    JButton openButton = new JButton(resourceBundle.getString("Select_file"));
    openButton.setMnemonic('O');
    openButton.setToolTipText(resourceBundle.getString("ToolTip.Select_file"));
    panel.add(openButton);
    panel.add(Box.createRigidArea(new Dimension(10, 10)));

    JLabel fileLabel = new ABIFileLabel();
    panel.add(fileLabel);

    // Open button listener.
    openButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int returnVal = fileChooser.showOpenDialog(appWindow);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
          abiFile = fileChooser.getSelectedFile();
          panel.repaint();
          saveButton.setEnabled(true);
        }
      }
    });

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    return panel;
  }

  /**
   * Build action buttons sub panel
   */
  protected JPanel buildActionButtonSubPanel() {

    // Add the action buttons sub panel.
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    // panel.setBackground(new Color(206, 231, 255));
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
      return abiFile != null ? abiFile.getName() : resourceBundle.getString("No_file_selected");
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
    log.info("Sending output of Vantage ABI to server...");

    Map<String, Data> output = new HashMap<String, Data>();

    VantageReportParser parser = new VantageReportParser();
    try {
      parser.parse(abiFile);

      for(ExamData exam : parser.getExamDatas()) {

        output.put("Name", new Data(DataType.TEXT, exam.getName()));
        output.put("Timestamp", new Data(DataType.DATE, exam.getTimestamp()));

        SideData side = exam.getLeft();
        output.put("LeftBrachial", new Data(DataType.INTEGER, side.getBrachial()));
        output.put("LeftAnkle", new Data(DataType.INTEGER, side.getAnkle()));
        output.put("LeftIndex", new Data(DataType.DECIMAL, side.getIndex()));
        output.put("LeftWaveform", new Data(DataType.DATA, side.getWaveForm()));
        output.put("LeftClock", new Data(DataType.DATE, side.getClock()));
        output.put("LeftScale", new Data(DataType.TEXT, side.getScale()));

        side = exam.getRight();
        output.put("RightBrachial", new Data(DataType.INTEGER, side.getBrachial()));
        output.put("RightAnkle", new Data(DataType.INTEGER, side.getAnkle()));
        output.put("RightIndex", new Data(DataType.DECIMAL, side.getIndex()));
        output.put("RightWaveform", new Data(DataType.DATA, side.getWaveForm()));
        output.put("RightClock", new Data(DataType.DATE, side.getClock()));
        output.put("RightScale", new Data(DataType.TEXT, side.getScale()));

        instrumentExecutionService.addOutputParameterValues(output);

        measureCountLabel.repaint();

        if(instrumentExecutionService.getExpectedMeasureCount() == instrumentExecutionService.getCurrentMeasureCount()) {
          break;
        }
      }

      saveButton.setEnabled(false);
      abiFile = null;
      appWindow.repaint();

      log.info("Sending output of Vantage ABI to server done...");
      if(instrumentExecutionService.getExpectedMeasureCount() <= instrumentExecutionService.getCurrentMeasureCount()) {
        exitUI();
      }

    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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

}
