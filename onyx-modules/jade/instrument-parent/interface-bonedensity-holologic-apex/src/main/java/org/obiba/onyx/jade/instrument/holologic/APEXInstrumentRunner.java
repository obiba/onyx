/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.holologic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dcm4che2.tool.dcmrcv.DicomServer;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.holologic.IVAImagingScanDataExtractor.Energy;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileSystemUtils;

public class APEXInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(APEXInstrumentRunner.class);

  protected InstrumentExecutionService instrumentExecutionService;

  private JdbcTemplate patScanDb;

  private File scanDataDir;

  private DicomSettings dicomSettings;

  private DicomServer server;

  private File dcmDir;

  private List<String> participantFiles = new ArrayList<String>();

  private Set<String> outVendorNames;

  private Locale locale;

  private List<String> sentVariables = new ArrayList<String>();

  private ApexReceiver apexReceiver = new ApexReceiver();

  private String participantKey;

  private String participantID;

  private boolean isRepeatable;

  private List<Map<String, Data>> retrieveDeviceData() {

    List<Map<String, Data>> dataList = new ArrayList<Map<String, Data>>();

    log.info("participantId: " + participantID);
    participantKey = patScanDb
        .queryForObject("select p.PATIENT_KEY from PATIENT p where p.IDENTIFIER1=?", String.class, participantID);
    log.info("participantKey: " + participantKey);
    if(instrumentExecutionService.hasInputParameter("HipSide")) {
      String hipSide = instrumentExecutionService.getInputParameterValue("HipSide").getValue();
      log.info("hipSide: " + hipSide);
      log.info("expected: " + instrumentExecutionService.getExpectedMeasureCount());
      if(hipSide != null) {
        if(hipSide.toUpperCase().startsWith("L")) {
          extractLeftHip(dataList);
        } else if(hipSide.toUpperCase().startsWith("R")) {
          extractRightHip(dataList);
        } else if(hipSide.toUpperCase().startsWith("B")) {
          if(instrumentExecutionService.getExpectedMeasureCount() > 1) {
            extractLeftHip(dataList);
            extractRightHip(dataList);
          } else {
            extractScanData(dataList,
                new HipScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.LEFT, server, apexReceiver));
            extractScanData(dataList,
                new HipScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.RIGHT, server, apexReceiver));
          }
        }
      }
    } else if(instrumentExecutionService.getExpectedMeasureCount() > 1) {
      extractLeftHip(dataList);
      extractRightHip(dataList);
    } else {
      extractScanData(dataList,
          new HipScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.LEFT, server, apexReceiver));
      extractScanData(dataList,
          new HipScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.RIGHT, server, apexReceiver));
    }
    if(instrumentExecutionService.hasInputParameter("ForearmSide")) {
      String forearmSide = instrumentExecutionService.getInputParameterValue("ForearmSide").getValue();
      if(forearmSide != null) {
        if(forearmSide.toUpperCase().startsWith("L")) {
          extractScanData(dataList,
              new ForearmScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.LEFT, server, apexReceiver) {
                @Override
                public String getName() {
                  return "FA";
                }
              });
        } else if(forearmSide.toUpperCase().startsWith("R")) {
          extractScanData(dataList,
              new ForearmScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.RIGHT, server, apexReceiver) {
                @Override
                public String getName() {
                  return "FA";
                }
              });
        }
      }
    } else {
      extractScanData(dataList,
          new ForearmScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.LEFT, server, apexReceiver));
      extractScanData(dataList,
          new ForearmScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.RIGHT, server, apexReceiver));
    }
    extractScanData(dataList,
        new WholeBodyScanDataExtractor(patScanDb, scanDataDir, participantKey, server, apexReceiver));
    extractScanData(dataList,
        new IVAImagingScanDataExtractor(patScanDb, scanDataDir, participantKey, Energy.CLSA_DXA, server, apexReceiver));

    return dataList;

  }

  private void extractRightHip(List<Map<String, Data>> dataList) {
    extractScanData(dataList,
        new HipScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.RIGHT, server, apexReceiver) {
          @Override
          public String getName() {
            return "HIP";
          }
        });
  }

  private void extractLeftHip(List<Map<String, Data>> dataList) {
    extractScanData(dataList,
        new HipScanDataExtractor(patScanDb, scanDataDir, participantKey, Side.LEFT, server, apexReceiver) {
          @Override
          public String getName() {
            return "HIP";
          }
        });
  }

  private void extractScanData(List<Map<String, Data>> dataList, APEXScanDataExtractor extractor) {
    log.info("extractScanData");
    // filter the values to output
    Map<String, Data> extractedData = extractor.extractData();
    Map<String, Data> outputData = new HashMap<String, Data>();

    for(Entry<String, Data> entry : extractedData.entrySet()) {
      if(outVendorNames.contains(entry.getKey())) {
        outputData.put(entry.getKey(), entry.getValue());
      }
    }
    log.info(extractedData + "");
    log.info(outputData + "");
    dataList.add(outputData);

    participantFiles.addAll(extractor.getFileNames());
  }

  public void sendDataToServer(Map<String, Data> data) {
    instrumentExecutionService.addOutputParameterValues(data);
  }

  /**
   * Implements parent method initialize from InstrumentRunner Delete results from previous measurement and initiate the
   * input file to be read by the external application
   */
  public void initialize() {
    participantID = instrumentExecutionService.getParticipantID();
    isRepeatable = instrumentExecutionService.isRepeatableMeasure();
    initApexReceiverStatus();
    outVendorNames = instrumentExecutionService.getExpectedOutputParameterVendorNames();

    try {
      File tmpDir = File.createTempFile("dcm", "");
      if(tmpDir.delete() == false || tmpDir.mkdir() == false) {
        throw new RuntimeException("Cannot create temp directory");
      }
      this.dcmDir = tmpDir;
      log.info("DICOM files stored to {}", dcmDir.getAbsolutePath());
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    this.server = new DicomServer(dcmDir, dicomSettings);
  }

  /**
   * Implements parent method run from InstrumentRunner Launch the external application, retrieve and send the data
   */
  public void run() {
    log.info("Start Dicom server");
    try {
      server.start();
    } catch(IOException e) {
      log.error("Error start server");
    }
    apexReceiver.waitForExit();
  }

  private void retrieveMeasurements() {
    log.info("Retrieving measurements");
    List<Map<String, Data>> dataList = retrieveDeviceData();

    log.info("Sending data to server");
    sentVariables.clear();
    for(Map<String, Data> dataMap : dataList) {
      for(Map.Entry<String, Data> entry : dataMap.entrySet()) {
        sentVariables.add(entry.getKey());
      }
      // send only if measure is complete (all variable assigned)
      // because repeatable measure accept partial variable set
      if(isRepeatable) {
        if(outVendorNames.equals(dataMap.keySet())) {
          sendDataToServer(dataMap);
        }
      } else {
        sendDataToServer(dataMap);
      }
    }
  }

  public void shutdown() {
    log.info("Shutdown Dicom server");
    server.stop();
    deleteTemporaryDicomFiles();
  }

  /**
   * Return true if you sent all required variable, false otherwise
   *
   * @return
   */
  private boolean isCompleteVariable() {
    List<String> missing = new ArrayList<String>();
    List<String> sentVariablesCopy = new ArrayList<String>(sentVariables);
    boolean retValue = true;

    // if repeatable measure check if all variables in measure has been sent
    for(int i = 0; i < instrumentExecutionService.getExpectedMeasureCount(); i++) {
      for(String out : outVendorNames) {
        if(sentVariablesCopy.contains(out) == false) {
          missing.add(out);
          retValue = false;
        } else {
          sentVariablesCopy.remove(out);
        }
      }
    }
    log.info("Missing variables: " + missing);
    return retValue;
  }

  private void deleteTemporaryDicomFiles() {
    log.info("Delete temporary dicom files");
    FileSystemUtils.deleteRecursively(dcmDir);
  }

  public void initApexReceiverStatus() {
    apexReceiver.setParticipantId(participantID);
    apexReceiver.setCheckActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        retrieveMeasurements();
        if(isCompleteVariable()) {
          if(isRepeatable) {
            apexReceiver.setVariableStatusOKButCheck();
          } else {
            apexReceiver.setVariableStatusOK();
          }
          if(apexReceiver.isCompleteRawInDicom()) {
            apexReceiver.setDicomStatusOK();
            apexReceiver.setSaveEnable();
          } else {
            apexReceiver.setDicomStatusNotOK();
          }
        } else {
          apexReceiver.setVariableStatusNotOK();
          apexReceiver.setDicomStatusNotReady();
        }
        apexReceiver.validate();
        apexReceiver.repaint();
      }
    });
    apexReceiver.setVisible(true);
  }

  public enum Side {
    LEFT, RIGHT
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public void setPatScanDb(JdbcTemplate patScanDb) {
    this.patScanDb = patScanDb;
  }

  public void setDicomSettings(DicomSettings dicomSettings) {
    this.dicomSettings = dicomSettings;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

}
