package org.obiba.onyx.jade.instrument.ge.vivid;

import java.awt.EventQueue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.tool.dcmrcv.DicomServer;
import org.dcm4che2.tool.dcmrcv.DicomServer.StoredDicomFile;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

public class VividInstrumentRunner implements InstrumentRunner {

  protected Logger log = LoggerFactory.getLogger(VividInstrumentRunner.class);

  private InstrumentExecutionService instrumentExecutionService;

  private DicomSettings dicomSettings;

  private File dcmDir;

  private DicomServer server;

  private DicomStorageScp gui;

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public void setDicomSettings(DicomSettings dicomSettings) {
    this.dicomSettings = dicomSettings;
  }

  @Override
  public void initialize() {
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

  @Override
  public void run() {
    gui = new DicomStorageScp(server);

    try {
      this.server.start();
    } catch(IOException e) {
      // ignore
    }

    gui.show();
    gui.waitForExit();
  }

  @Override
  public void shutdown() {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(null, "Uploading data to Onyx. Please wait. This dialog will close automatically.", "Uploading...", JOptionPane.INFORMATION_MESSAGE);
      }
    });

    Set<String> output = instrumentExecutionService.getExpectedOutputParameterVendorNames();
    try {
      Map<String, Data> values = new HashMap<String, Data>();

      Vector<Vector<Object>> data = gui.getData();
      for(int i = 0; i < data.size(); i++) {
        Vector<Object> row = data.get(i);
        List<String> columns = DicomStorageScp.columns;
        String studyInstanceUID = (String) row.get(columns.indexOf(DicomStorageScp.STUDYINSTANCEUID));

        List<StoredDicomFile> listDicomFiles = server.listDicomFiles();

        String laterality = (String) row.get(columns.indexOf(DicomStorageScp.LATERALITY));
        if(output.contains("SIDE")) {
          values.put("SIDE", DataBuilder.buildText(laterality));
        }

        int idx = 1;
        for(StoredDicomFile dcm : listDicomFiles) {
          try {
            DicomObject dicomObject = dcm.getDicomObject();
            String studyInstanceUid = dicomObject.getString(Tag.StudyInstanceUID);
            String mediaStorageSOPClassUID = dicomObject.contains(Tag.MediaStorageSOPClassUID) ? dicomObject.getString(Tag.MediaStorageSOPClassUID) : null;
            String modality = dicomObject.getString(Tag.Modality);
            // Allow garbage collection, as the instance may be quite large
            dicomObject = null;
            if(studyInstanceUid.equals(studyInstanceUID)) {
              // This will contain a large byte-array
              Data dicomData = DataBuilder.buildBinary(compress(dcm.getFile()));
              log.info(String.format("dicom file: %d bytes -- compressed file: %d bytes", dcm.getFile().length(), ((byte[]) dicomData.getValue()).length));

              if(mediaStorageSOPClassUID != null && mediaStorageSOPClassUID.equals(UID.UltrasoundImageStorage)) {
                if(output.contains("STILL_IMAGE")) {
                  values.put("STILL_IMAGE", dicomData);
                }
              } else if(mediaStorageSOPClassUID != null && mediaStorageSOPClassUID.equals(UID.UltrasoundMultiframeImageStorage)) {
                if(output.contains("CINELOOP_" + idx)) {
                  values.put("CINELOOP_" + idx, dicomData);
                }
                idx++;
              } else if("SR".equals(modality)) {
                if(output.contains("SR")) {
                  values.put("SR", dicomData);
                }
              } else {
                // don't know what this file is.
                log.warn("Received unknown DICOM file. Ignoring.");
              }
            }
          } catch(IOException e) {
            log.error("Unexpected excepion while reading DICOM file.", e);
          }
        }
        instrumentExecutionService.addOutputParameterValues(values);
      }
    } catch(Exception e) {
      log.error("Unexpected excepion while processing DICOM files.", e);
    } finally {
      FileSystemUtils.deleteRecursively(dcmDir);
    }
  }

  private byte[] compress(File file) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream compressed = new GZIPOutputStream(baos);

    byte[] buffer = new byte[4096];
    FileInputStream fis = new FileInputStream(file);
    try {
      long byteCount = 0;
      int bytesRead;
      while((bytesRead = fis.read(buffer)) != -1) {
        compressed.write(buffer, 0, bytesRead);
        byteCount += bytesRead;
      }
      compressed.flush();
    } finally {
      try {
        fis.close();
      } catch(IOException ex) {
        // do nothing
      }
      try {
        compressed.close();
      } catch(IOException ex) {
        // do nothing
      }
    }

    return baos.toByteArray();
  }
}