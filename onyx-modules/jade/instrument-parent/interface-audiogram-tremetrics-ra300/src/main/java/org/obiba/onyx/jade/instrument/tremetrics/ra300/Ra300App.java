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

import gnu.io.CommPortIdentifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.obiba.onyx.jade.instrument.tremetrics.ra300.Ra300Test.Frequency;
import org.obiba.onyx.jade.instrument.tremetrics.ra300.Ra300Test.HTL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Ra300App {

  private final static Logger log = LoggerFactory.getLogger(Ra300App.class);

  private final CountDownLatch exitLatch = new CountDownLatch(1);

  private final Ra300 ra300;

  private JFrame frame;

  private JLabel stateLabel;

  private JLabel[] leftResults;

  private JLabel[] rightResults;

  private JComboBox ports;

  private JSpinner baudRate;

  private JButton connectionButton;

  private JButton readTestButton;

  private JButton saveButton;

  private Ra300Test currentTest;

  private SaveListener saveListener;

  public interface SaveListener {
    public void onSave(Ra300Test test);
  }

  /**
   * Launch the application.
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, ExecutionException {
    Ra300App app = Ra300App.build(new Ra300(), new SaveListener() {

      @Override
      public void onSave(Ra300Test test) {
      }
    }).get();
    app.waitForExit();
  }

  /**
   * Create the application.
   */
  public Ra300App(Ra300 ra300, SaveListener saveListener) {
    this.ra300 = ra300;
    this.saveListener = saveListener;
    initialize();
    updateState();
  }

  private void updateState() {
    stateLabel.setText(ra300.getState().toString());
    switch(ra300.getState()) {
    case CONNECTED:
      onConnected();
      break;
    case DISCONNECTED:
      onDisconnected();
      break;
    }
  }

  private void onDisconnected() {
    connectionButton.setText("Connect");
    ports.setEnabled(true);
    baudRate.setEnabled(true);
    readTestButton.setEnabled(false);
    saveButton.setEnabled(false);

    ((DefaultComboBoxModel) ports.getModel()).removeAllElements();
    for(CommPortIdentifier id : ra300.listAvailablePorts()) {
      ((DefaultComboBoxModel) ports.getModel()).addElement(id.getName());
    }
  }

  private void onConnected() {
    connectionButton.setText("Disconnect");
    ports.setEnabled(false);
    baudRate.setEnabled(false);
    readTestButton.setEnabled(true);
    saveButton.setEnabled(true);
  }

  private void updateTestValues() {
    try {
      currentTest = ra300.readCurrentTest();
    } catch(IOException e) {
      log.warn("Error reading test", e);
      return;
    }
    updateTestValues(leftResults, currentTest.getHTLLeft());
    updateTestValues(rightResults, currentTest.getHTLRight());
  }

  private void updateTestValues(JLabel[] results, HTL htl) {
    for(Frequency freq : Frequency.values()) {
      JLabel freqLabel = freqLabel(results, freq);
      freqLabel.setForeground(Color.BLACK);

      if(htl.wasTested(freq) && htl.wasDeleted(freq) == false) {
        freqLabel.setText(htl.value(freq));
        if(htl.hasError(freq)) {
          freqLabel.setForeground(Color.RED);
        }
      } else {
        freqLabel.setText("--");
      }
    }
  }

  private JLabel freqLabel(JLabel[] leftRight, Frequency freq) {
    return leftRight[freq.ordinal()];
  }

  public static Future<Ra300App> build(final Ra300 ra300, final SaveListener saveListener) {
    FutureTask<Ra300App> future = new FutureTask<Ra300App>(new Callable<Ra300App>() {
      @Override
      public Ra300App call() throws Exception {
        Ra300App window = new Ra300App(ra300, saveListener);
        window.frame.setVisible(true);
        return window;
      }
    });
    EventQueue.invokeLater(future);
    return future;
  }

  public void waitForExit() {
    // Block on the lock (held by the Event queue thread)
    try {
      exitLatch.await();
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Initialize the contents of the frame.
   * @wbp.parser.entryPoint
   */
  private void initialize() {
    frame = new JFrame();
    frame.setTitle("RA300 Audiometer");
    frame.setBounds(100, 100, 771, 327);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel panel = new JPanel();
    panel.setBorder(new TitledBorder(null, "RA300 Connection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    frame.getContentPane().add(panel, BorderLayout.NORTH);
    panel.setLayout(new GridLayout(0, 4, 10, 10));

    JLabel lblNewLabel = new JLabel("COM Port");
    panel.add(lblNewLabel);

    ports = new JComboBox();
    ports.setModel(new DefaultComboBoxModel());
    panel.add(ports);

    JLabel lblNewLabel_1 = new JLabel("Baud Rate");
    panel.add(lblNewLabel_1);

    baudRate = new JSpinner();
    baudRate.setModel(new SpinnerListModel(new String[] { "9600", "19200" }));
    baudRate.getModel().setValue("19200");
    panel.add(baudRate);

    JLabel lblNewLabel_2 = new JLabel("State");
    panel.add(lblNewLabel_2);

    stateLabel = new JLabel("--");
    panel.add(stateLabel);

    connectionButton = new JButton("Connect");
    connectionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        switch(ra300.getState()) {
        case CONNECTED:
          ra300.disconnect();
          break;
        case DISCONNECTED:
          try {
            ra300.connect((String) ports.getModel().getSelectedItem(), Integer.parseInt(baudRate.getModel().getValue().toString()));
          } catch(Ra300Exception e) {
            JOptionPane.showMessageDialog(frame, "Error connecting to the instrument.\nChange port settings and try again.\nError :" + e.getExceptionCause().toString(), "Cannot Connect", JOptionPane.ERROR_MESSAGE);
          }
          break;
        }
        updateState();
      }
    });
    panel.add(connectionButton);

    JPanel panel_2 = new JPanel();
    frame.getContentPane().add(panel_2, BorderLayout.SOUTH);

    readTestButton = new JButton("Update");
    readTestButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        updateTestValues();
      }
    });
    panel_2.add(readTestButton);

    saveButton = new JButton("Save");
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveListener.onSave(currentTest);
        JOptionPane.showMessageDialog(frame, "Test result sucessfully saved. You may click 'Close' to exit.", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
      }
    });
    panel_2.add(saveButton);

    JButton btnNewButton_1 = new JButton("Close");
    btnNewButton_1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        frame.setVisible(false);
        frame.dispose();
      }
    });
    panel_2.add(btnNewButton_1);

    JPanel panel_3 = new JPanel();
    panel_3.setBorder(new TitledBorder(null, "Current Test Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    frame.getContentPane().add(panel_3, BorderLayout.CENTER);
    panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

    JPanel panel_1 = new JPanel();
    panel_3.add(panel_1);
    panel_1.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Left", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
    panel_1.setLayout(new GridLayout(0, 4, 0, 0));

    JLabel lblNewLabel_3 = new JLabel("1 KHz Test");
    panel_1.add(lblNewLabel_3);

    JLabel l1ktResult = new JLabel("--");
    panel_1.add(l1ktResult);

    JLabel lblNewLabel_5 = new JLabel("500 Hz");
    panel_1.add(lblNewLabel_5);

    JLabel l500Result = new JLabel("--");
    panel_1.add(l500Result);

    JLabel lblNewLabel_7 = new JLabel("1 KHz");
    panel_1.add(lblNewLabel_7);

    JLabel l1kResult = new JLabel("--");
    panel_1.add(l1kResult);

    JLabel lblNewLabel_4 = new JLabel("2 KHz");
    panel_1.add(lblNewLabel_4);

    JLabel l2kResult = new JLabel("--");
    panel_1.add(l2kResult);

    JLabel lblNewLabel_9 = new JLabel("3 KHz");
    panel_1.add(lblNewLabel_9);

    JLabel l3kResult = new JLabel("--");
    panel_1.add(l3kResult);

    JLabel lblNewLabel_11 = new JLabel("4 KHz");
    panel_1.add(lblNewLabel_11);

    JLabel l4kResult = new JLabel("--");
    panel_1.add(l4kResult);

    JLabel lblNewLabel_13 = new JLabel("6 KHz");
    panel_1.add(lblNewLabel_13);

    JLabel l6kResult = new JLabel("--");
    panel_1.add(l6kResult);

    JLabel lblNewLabel_15 = new JLabel("8 KHz");
    panel_1.add(lblNewLabel_15);

    JLabel l8kResult = new JLabel("--");
    panel_1.add(l8kResult);

    leftResults = new JLabel[] { l1ktResult, l500Result, l1kResult, l2kResult, l3kResult, l4kResult, l6kResult, l8kResult };

    JPanel panel_4 = new JPanel();
    panel_4.setBorder(new TitledBorder(null, "Right", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panel_3.add(panel_4);
    panel_4.setLayout(new GridLayout(0, 4, 0, 0));

    JLabel label = new JLabel("1 KHz Test");
    panel_4.add(label);

    JLabel label_1 = new JLabel("--");
    panel_4.add(label_1);

    JLabel label_2 = new JLabel("500 Hz");
    panel_4.add(label_2);

    JLabel label_3 = new JLabel("--");
    panel_4.add(label_3);

    JLabel label_4 = new JLabel("1 KHz");
    panel_4.add(label_4);

    JLabel label_5 = new JLabel("--");
    panel_4.add(label_5);

    JLabel label_6 = new JLabel("2 KHz");
    panel_4.add(label_6);

    JLabel label_7 = new JLabel("--");
    panel_4.add(label_7);

    JLabel label_8 = new JLabel("3 KHz");
    panel_4.add(label_8);

    JLabel label_9 = new JLabel("--");
    panel_4.add(label_9);

    JLabel label_10 = new JLabel("4 KHz");
    panel_4.add(label_10);

    JLabel label_11 = new JLabel("--");
    panel_4.add(label_11);

    JLabel label_12 = new JLabel("6 KHz");
    panel_4.add(label_12);

    JLabel label_13 = new JLabel("--");
    panel_4.add(label_13);

    JLabel label_14 = new JLabel("8 KHz");
    panel_4.add(label_14);

    JLabel label_15 = new JLabel("--");
    panel_4.add(label_15);

    rightResults = new JLabel[] { label_1, label_3, label_5, label_7, label_9, label_11, label_13, label_15 };

    frame.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosed(WindowEvent e) {
        try {
          ra300.disconnect();
        } finally {
          exitLatch.countDown();
        }
      }

    });
  }
}
