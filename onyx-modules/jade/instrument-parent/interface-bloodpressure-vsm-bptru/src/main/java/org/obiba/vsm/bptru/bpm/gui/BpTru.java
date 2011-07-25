/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.vsm.bptru.bpm.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.obiba.vsm.bptru.bpm.Data;
import org.obiba.vsm.bptru.bpm.bpm200.Bpm200;
import org.obiba.vsm.bptru.bpm.state.BpmSession;
import org.obiba.vsm.bptru.bpm.state.State.States;
import org.obiba.vsm.bptru.bpm.state.StateMachine;

public class BpTru implements BpmSession {

  private final CountDownLatch startupWork;

  private StateMachine stateMachine;

  private BpTruResultListener listener;

  private JFrame frmVsmBptru;

  private JLabel reading;

  private JLabel cuffPressure;

  private JTable table;

  private JLabel firmware;

  private JLabel state;

  private JLabel cycleTime;

  private JButton startButton;

  private JButton stopButton;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    BpTru b = new BpTru();
    b.waitForExit();
  }

  /**
   * Create the application.
   */
  public BpTru() {
    startupWork = new CountDownLatch(1);
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          initialize();
          start();
          frmVsmBptru.setVisible(true);
          startupWork.countDown();
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  private void start() {
    stateMachine = new StateMachine(this, new Bpm200());
    new Thread(stateMachine).start();
  }

  public void waitForExit() {

    try {
      startupWork.await();
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }

    final CountDownLatch counter = new CountDownLatch(1);
    this.frmVsmBptru.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        counter.countDown();
      }
    });
    try {
      counter.await();
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    frmVsmBptru = new JFrame();
    frmVsmBptru.setTitle("VSM BpTru");
    frmVsmBptru.setAlwaysOnTop(true);
    frmVsmBptru.setBounds(100, 100, 665, 305);
    frmVsmBptru.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel buttons = new JPanel();
    frmVsmBptru.getContentPane().add(buttons, BorderLayout.SOUTH);

    startButton = new JButton("Start");
    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stateMachine.start();
      }
    });
    buttons.add(startButton);

    JButton exitButton = new JButton("Exit");
    exitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stateMachine.exit();
        BpTru.this.frmVsmBptru.dispose();
      }
    });

    stopButton = new JButton("Stop");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stateMachine.stop();
      }
    });
    buttons.add(stopButton);
    buttons.add(exitButton);

    JPanel data = new JPanel();
    frmVsmBptru.getContentPane().add(data, BorderLayout.CENTER);
    data.setLayout(new BoxLayout(data, BoxLayout.Y_AXIS));

    JPanel panel_1 = new JPanel();
    data.add(panel_1);
    panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.LINE_AXIS));

    JPanel panel_2 = new JPanel();
    panel_2.setBorder(new TitledBorder(null, "Instrument", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panel_1.add(panel_2);
    panel_2.setLayout(new GridLayout(3, 2, 0, 0));

    JLabel lblState = new JLabel("State");
    panel_2.add(lblState);

    state = new JLabel("");
    panel_2.add(state);

    JLabel lblNewLabel_4 = new JLabel("Firmware");
    panel_2.add(lblNewLabel_4);

    firmware = new JLabel("N/A");
    panel_2.add(firmware);

    JPanel panel = new JPanel();
    panel_1.add(panel);
    panel.setBorder(new TitledBorder(null, "Current Measurement", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panel.setLayout(new GridLayout(3, 2, 0, 0));

    JLabel lblNewLabel_3 = new JLabel("Cycle");
    panel.add(lblNewLabel_3);

    cycleTime = new JLabel("N/A");
    panel.add(cycleTime);

    JLabel readingLabel = new JLabel("Reading");
    panel.add(readingLabel);

    reading = new JLabel();
    reading.setText("N/A");
    panel.add(reading);

    JLabel lblNewLabel_1 = new JLabel("Cuff Pressure");
    panel.add(lblNewLabel_1);

    cuffPressure = new JLabel();
    cuffPressure.setText("N/A");
    panel.add(cuffPressure);

    JPanel panel_4 = new JPanel();
    panel_4.setBorder(new TitledBorder(null, "Measures", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panel_4.setLayout(new BorderLayout());
    data.add(panel_4);

    table = new JTable(new ResultsTableModel());
    table.getColumnModel().getColumn(0).setPreferredWidth(15);
    table.getColumnModel().getColumn(0).setMaxWidth(15);
    panel_4.add(table, BorderLayout.CENTER);
    panel_4.add(table.getTableHeader(), BorderLayout.NORTH);
  }

  public void addResult(Date startTime, Date endTime, Data.BloodPressure result) {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    int reading = ((ResultsTableModel) table.getModel()).getRowCount() + 1;
    Vector<Object> row = new Vector<Object>();
    row.add(reading);
    row.add(sdf.format(startTime));
    row.add(sdf.format(endTime));
    if(result.code().hasSbpError() == false) {
      row.add(result.sbp());
    } else {
      row.add(result.sbpError().toString());
    }
    if(result.code().hasDbpError() == false) {
      row.add(result.dbp());
    } else {
      row.add(result.dbpError().toString());
    }
    if(result.code().hasPulseError() == false) {
      row.add(result.pulse());
    } else {
      row.add(result.pulseError().toString());
    }
    ((ResultsTableModel) table.getModel()).addRow(row);
    if(listener != null) {
      listener.onBpResult(startTime, endTime, result);
    }
  }

  public void addAverage(Data.AvgPressure result) {
    Vector<Object> row = new Vector<Object>();
    row.add("A");
    // no times?
    row.add("");
    row.add("");
    row.add(result.sbp());
    row.add(result.dbp());
    row.add(result.pulse());
    ((ResultsTableModel) table.getModel()).addRow(row);
    if(listener != null) {
      this.listener.onAvgResult(result);
    }
  }

  public void setCycle(int cycle) {
    this.cycleTime.setText(Integer.toString(cycle));
  };

  public void setFirmware(String firmware) {
    this.firmware.setText(firmware);
  }

  public void setState(States state) {
    this.state.setText(state.toString());
    this.startButton.setEnabled(state.canStart());
    this.stopButton.setEnabled(state.canStop());
  }

  public void setCuffPressure(int pressure) {
    if(pressure > 0) {
      this.cuffPressure.setText(Integer.toString(pressure));
    } else {
      this.cuffPressure.setText("N/A");
    }
  }

  public void setReading(int reading) {
    this.reading.setText(Integer.toString(reading));
  }

  public void clearResults() {
    while(((ResultsTableModel) table.getModel()).getRowCount() != 0) {
      ((ResultsTableModel) table.getModel()).removeRow(0);
    }
  };

  /**
   * @param bpTruResultListener
   */
  public void addResultListener(BpTruResultListener bpTruResultListener) {
    this.listener = bpTruResultListener;
  }

  private class ResultsTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    public ResultsTableModel() {
      super(new String[] { "#", "Start Time", "End Time", "Systolic", "Diastolic", "Pulse" }, 0);
    }

    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

  }

}
