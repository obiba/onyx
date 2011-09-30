package org.obiba.onyx.jade.instrument.ge.vivid;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.tool.dcmrcv.DicomServer;
import org.dcm4che2.tool.dcmrcv.DicomServer.State;
import org.dcm4che2.tool.dcmrcv.DicomServer.StateListener;
import org.dcm4che2.tool.dcmrcv.DicomServer.StorageListener;

public class DicomStorageScp {

  private final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  private final CountDownLatch exitLatch = new CountDownLatch(1);

  private final DicomServer server;

  private JFrame frmDicomServer;

  private JTextField aeTitle;

  private JComboBox hostname;

  private JSpinner port;

  private JButton startStop;

  private JTable table;

  /**
   * Create the application.
   */
  public DicomStorageScp(DicomServer server) {
    if(server == null) throw new IllegalArgumentException();
    this.server = server;

    this.server.addStorageListener(new StorageListener() {

      @Override
      public void onStored(File file, DicomObject dicomObject) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rows = model.getRowCount();

        Date date = dicomObject.getDate(Tag.AcquisitionDateTime) != null ? dicomObject.getDate(Tag.AcquisitionDateTime) : dicomObject.getDate(Tag.StudyDate, Tag.StudyTime);

        model.addRow(new Object[] { "" + (rows + 1), file.getName(), dicomObject.getString(Tag.PatientID), df.format(date), dicomObject.getString(Tag.Modality) });
      }
    });

    this.server.addStateListener(new StateListener() {

      @Override
      public void onStateChange(State newState) {
        if(newState == State.STARTED) {
          startStop.setText("Stop");
        } else {
          startStop.setText("Start");
        }

        boolean editable = newState == State.STOPPED;
        aeTitle.setEditable(editable);
        hostname.setEditable(editable);
        port.setEnabled(editable);
      }
    });
    initialize();
    bind();
  }

  public void show() {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          frmDicomServer.setVisible(true);
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
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
   */
  private void initialize() {
    frmDicomServer = new JFrame();
    frmDicomServer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frmDicomServer.setBounds(100, 100, 700, 315);
    frmDicomServer.setTitle("DICOM Server");
    frmDicomServer.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel panel_1 = new JPanel();
    panel_1.setBorder(new TitledBorder(null, "DICOM Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    frmDicomServer.getContentPane().add(panel_1, BorderLayout.NORTH);
    panel_1.setLayout(new BorderLayout(0, 0));

    JPanel panel = new JPanel();
    panel_1.add(panel);
    panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

    JLabel lblAeTitle = new JLabel("AE Title");
    panel.add(lblAeTitle);

    aeTitle = new JTextField();
    panel.add(aeTitle);
    aeTitle.setColumns(15);

    JLabel lblHostnameip = new JLabel("Hostname/IP");
    panel.add(lblHostnameip);

    hostname = new JComboBox(new DefaultComboBoxModel());
    panel.add(hostname);

    JLabel lblPort = new JLabel("Port");
    panel.add(lblPort);

    port = new JSpinner();
    panel.add(port);
    port.setModel(new SpinnerNumberModel(1100, 100, 65535, 1));

    JPanel panel_2 = new JPanel();
    FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
    panel_1.add(panel_2, BorderLayout.SOUTH);

    startStop = new JButton("Start");
    startStop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if(server.isRunning() == false) {
          getSettings().setAeTitle(aeTitle.getText());
          DefaultComboBoxModel c = ((DefaultComboBoxModel) hostname.getModel());
          getSettings().setHostname((String) c.getSelectedItem());
          getSettings().setPort((Integer) port.getValue());
          try {
            server.start();
          } catch(IOException e) {
            JOptionPane.showMessageDialog(frmDicomServer, e);
          }
        } else {
          server.stop();
        }
      }
    });
    panel_2.add(startStop);

    JPanel panel_3 = new JPanel();
    panel_3.setBorder(new TitledBorder(null, "Files Received", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    frmDicomServer.getContentPane().add(panel_3, BorderLayout.CENTER);
    panel_3.setLayout(new BorderLayout(0, 0));

    table = new JTable();
    table.setModel(new DefaultTableModel(new String[] { "#", "Filename", "Patient ID", "Aquisition Date/time", "Modality" }, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    });
    table.getColumnModel().getColumn(0).setResizable(false);
    table.getColumnModel().getColumn(0).setPreferredWidth(15);
    table.getColumnModel().getColumn(0).setMaxWidth(15);
    panel_3.add(table, BorderLayout.CENTER);
    panel_3.add(table.getTableHeader(), BorderLayout.NORTH);

    JPanel panel_4 = new JPanel();
    frmDicomServer.getContentPane().add(panel_4, BorderLayout.SOUTH);

    JButton btnNewButton = new JButton("Save");
    btnNewButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        frmDicomServer.dispose();
      }
    });
    panel_4.add(btnNewButton);

    frmDicomServer.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosed(WindowEvent e) {
        try {
          server.stop();
        } finally {
          exitLatch.countDown();
        }
      }

    });
  }

  private DicomSettings getSettings() {
    return server.getSettings();
  }

  private void bind() {
    aeTitle.setText(getSettings().getAeTitle());
    Vector<String> interfaces = getIPs();
    hostname.setModel(new DefaultComboBoxModel(interfaces));
    if(interfaces.contains(getSettings().getHostname())) {
      hostname.setSelectedItem(getSettings().getHostname());
    } else if(interfaces.size() > 0) {
      hostname.setSelectedIndex(0);
    }
    port.setValue(Integer.valueOf(getSettings().getPort()));
  }

  private Vector<String> getIPs() {
    Vector<String> ifaces = new Vector<String>();
    Enumeration<NetworkInterface> ni;
    try {
      ni = NetworkInterface.getNetworkInterfaces();
    } catch(SocketException e1) {
      return ifaces;
    }

    while(ni.hasMoreElements()) {
      NetworkInterface networkInterface = ni.nextElement();
      try {
        if(networkInterface.isUp()) {
          Enumeration<InetAddress> ias = networkInterface.getInetAddresses();
          while(ias.hasMoreElements()) {
            InetAddress inetAddress = (InetAddress) ias.nextElement();
            if(inetAddress instanceof Inet4Address) {
              ifaces.add(inetAddress.getHostAddress());
            }
          }
        }
      } catch(Exception e) {
        // ignore
      }
    }
    return ifaces;
  }

  public static void main(String[] args) throws IOException {
    File f = File.createTempFile("dcm", "");
    f.delete();
    f.mkdir();
    DicomStorageScp scp = new DicomStorageScp(new DicomServer(f, new DicomSettings()));
    scp.show();
    scp.waitForExit();
  }
}
