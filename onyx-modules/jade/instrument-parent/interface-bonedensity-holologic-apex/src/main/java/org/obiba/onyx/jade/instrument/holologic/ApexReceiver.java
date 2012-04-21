/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.holologic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ApexReceiver extends JFrame {

  /**
   * 
   */
  private static final String OK_BUT_CHECK = "Ok, click refresh in onyx";

  private static final String HOLOGIC_APEX_RECEIVER = "Hologic Apex Receiver";

  private static final String APEX_DATABASE_DICOM_FILES = "Apex database + DICOM files:";

  private static final String RAW_DATA_IN_DICOM_FILES = "Raw data in DICOM files:";

  private static final String CAPTURE_DATA_WAIT = "Waiting capture...";

  private static final String MISSING_DATA = "Missing Data (variable database or Dicom Files)...";

  private static final String OK = "OK";

  private static final String P_AND_R_NOT_INCLUDED = "<html>P and R not included:<br>Configure Apex, close this window and restart</html>";

  private static final String CAPTURE_DICOM = "Need to capture DICOM files";

  private static final String CAPTURE_DATA = "Capture";

  private static final long serialVersionUID = 1L;

  private final CountDownLatch exitLatch = new CountDownLatch(1);

  private JButton check;

  private JPanel panelVariablesStatus;

  private JPanel panelDicomStatus;

  private JLabel lblParticipantId;

  private JLabel lblWaitingClickingCheckVariable;

  private JLabel lblWaitingClickingCheckDicom;

  private JButton btnSaveButton;

  private boolean completeRawInDicom = true;

  public ApexReceiver() {
    init();
    initPlus();
  }

  public void init() {
    setTitle(HOLOGIC_APEX_RECEIVER);
    getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel panel = new JPanel();
    panel.setBackground(Color.LIGHT_GRAY);
    getContentPane().add(panel, BorderLayout.CENTER);
    panel.setLayout(new GridLayout(3, 2, 0, 5));

    JPanel panelParticipantId = new JPanel();
    panel.add(panelParticipantId);

    lblParticipantId = new JLabel();
    panelParticipantId.add(lblParticipantId);

    JPanel panelCheck = new JPanel();
    panel.add(panelCheck);

    check = new JButton(CAPTURE_DATA);
    panelCheck.add(check);

    JPanel panelVariablesLabel = new JPanel();
    FlowLayout flowLayoutVariableLabel = (FlowLayout) panelVariablesLabel.getLayout();
    flowLayoutVariableLabel.setAlignment(FlowLayout.RIGHT);
    panel.add(panelVariablesLabel);

    JLabel lblApexDatabase = new JLabel(APEX_DATABASE_DICOM_FILES);
    panelVariablesLabel.add(lblApexDatabase);

    panelVariablesStatus = new JPanel();
    FlowLayout flowLayoutVariableStatus = (FlowLayout) panelVariablesStatus.getLayout();
    flowLayoutVariableStatus.setAlignment(FlowLayout.LEFT);
    panel.add(panelVariablesStatus);

    lblWaitingClickingCheckVariable = new JLabel(CAPTURE_DATA_WAIT);
    panelVariablesStatus.add(lblWaitingClickingCheckVariable);

    JPanel panelDicomLabel = new JPanel();
    FlowLayout flowLayoutDicom = (FlowLayout) panelDicomLabel.getLayout();
    flowLayoutDicom.setAlignment(FlowLayout.RIGHT);
    panel.add(panelDicomLabel);

    JLabel lblRawDataIn = new JLabel(RAW_DATA_IN_DICOM_FILES);
    panelDicomLabel.add(lblRawDataIn);

    panelDicomStatus = new JPanel();
    FlowLayout flowLayoutDicomStatus = (FlowLayout) panelDicomStatus.getLayout();
    flowLayoutDicomStatus.setAlignment(FlowLayout.LEFT);
    panel.add(panelDicomStatus);

    lblWaitingClickingCheckDicom = new JLabel(CAPTURE_DATA_WAIT);
    panelDicomStatus.add(lblWaitingClickingCheckDicom);

    JPanel panelSaveButton = new JPanel();
    getContentPane().add(panelSaveButton, BorderLayout.SOUTH);

    btnSaveButton = new JButton(OK);
    panelSaveButton.add(btnSaveButton);
    setSaveDisable();
    btnSaveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ApexReceiver.this.dispose();
      }
    });
  }

  public void initPlus() {
    setBounds(1, 1, 570, 185);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((screenSize.width - getWidth()) / 2, screenSize.height - getHeight() - 70);
    setResizable(false);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        exitLatch.countDown();
      }
    });
  }

  public void setCheckActionListener(ActionListener actionListener) {
    check.addActionListener(actionListener);
  }

  public void setVariableStatusOK() {
    lblWaitingClickingCheckVariable.setText(OK);
    panelVariablesStatus.setBackground(Color.GREEN);
  }

  public void setVariableStatusNotOK() {
    lblWaitingClickingCheckVariable.setText(MISSING_DATA);
    panelVariablesStatus.setBackground(Color.RED);
    setSaveDisable();
  }

  public void setVariableStatusOKButCheck() {
    lblWaitingClickingCheckVariable.setText(OK_BUT_CHECK);
    panelVariablesStatus.setBackground(Color.GREEN);
  }

  public void setDicomStatusOK() {
    lblWaitingClickingCheckDicom.setText(OK);
    panelDicomStatus.setBackground(Color.GREEN);
  }

  public void setDicomStatusNotOK() {
    lblWaitingClickingCheckDicom.setText(P_AND_R_NOT_INCLUDED);
    panelDicomStatus.setBackground(Color.RED);
    setSaveDisable();
  }

  private void setSaveDisable() {
    btnSaveButton.setEnabled(false);
  }

  public void setSaveEnable() {
    btnSaveButton.setEnabled(true);
  }

  public void setDicomStatusNotReady() {
    lblWaitingClickingCheckDicom.setText(CAPTURE_DICOM);
    panelDicomStatus.setBackground(Color.RED);
  }

  public void setParticipantId(String id) {
    lblParticipantId.setText("Participant Id: " + id);
  }

  public void waitForExit() {
    try {
      exitLatch.await();
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void missingRawInDicomFile(boolean completeDicom) {
    completeRawInDicom &= completeDicom;
  }

  public boolean isCompleteRawInDicom() {
    return completeRawInDicom;
  }

}
