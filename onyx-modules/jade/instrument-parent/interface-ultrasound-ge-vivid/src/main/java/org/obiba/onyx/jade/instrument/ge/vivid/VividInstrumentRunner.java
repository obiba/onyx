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
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

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
      dcmDir = tmpDir;
      log.info("DICOM files stored to {}", dcmDir.getAbsolutePath());
    } catch(IOException e) {
      throw new RuntimeException(e);
    }

    server = new DicomServer(dcmDir, dicomSettings);
  }

  @Override
  public void run() {
    gui = new DicomStorageScp(server,
        new VividDicomStoragePredicate(instrumentExecutionService.getExpectedOutputParameterVendorNames()));

    try {
      server.start();
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
        JOptionPane
            .showMessageDialog(null, "Uploading data to Onyx. Please wait. This dialog will close automatically.",
                "Uploading...", JOptionPane.INFORMATION_MESSAGE);
      }
    });

    Set<String> output = instrumentExecutionService.getExpectedOutputParameterVendorNames();
    try {
      List<StoredDicomFile> listDicomFiles = server.listDicomFiles();

      for(Vector<Object> row : gui.getData()) {
        Map<String, Data> values = new HashMap<String, Data>();
        String suid = (String) row.get(DicomStorageScp.columns.indexOf(DicomStorageScp.STUDYINSTANCEUID));
        int cineLoopIdx = 1;
        boolean added = false;

        for(StoredDicomFile dcm : listDicomFiles) {
          try {
            DicomObject dicomObject = dcm.getDicomObject();
            String studyInstanceUid = dicomObject.getString(Tag.StudyInstanceUID);
            String mediaStorageSOPClassUID = dicomObject.contains(Tag.MediaStorageSOPClassUID) ? dicomObject
                .getString(Tag.MediaStorageSOPClassUID) : null;
            String modality = dicomObject.getString(Tag.Modality);
            // Allow garbage collection, as the instance may be quite large
            dicomObject = null;

            if(studyInstanceUid.equals(suid)) {
              String key = null;

              if(mediaStorageSOPClassUID != null && mediaStorageSOPClassUID.equals(UID.UltrasoundImageStorage)) {
                key = "STILL_IMAGE";
              } else if(mediaStorageSOPClassUID != null &&
                  mediaStorageSOPClassUID.equals(UID.UltrasoundMultiframeImageStorage)) {
                key = "CINELOOP_" + cineLoopIdx;
                cineLoopIdx++;
              } else if("SR".equals(modality)) {
                key = "SR";
              } else {
                // don't know what this file is.
                log.warn("Received unknown DICOM file. Ignoring.");
              }

              if(key != null && output.contains(key)) {
                // This will contain a large byte-array
                Data dicomData = DataBuilder.buildBinary(compress(dcm.getFile()));
                log.info(String.format("[%s] dicom file: %d bytes -- compressed file: %d bytes", key, dcm.getFile().length(),
                    ((byte[]) dicomData.getValue()).length));

                values.put(key, dicomData);
                added = true;
              }
            }
          } catch(IOException e) {
            log.error("Unexpected excepion while reading DICOM file.", e);
          }
        }
        // one or more dicom data were added, then report the SIDE it applies to as well
        if(added && output.contains("SIDE")) {
          String laterality = (String) row.get(DicomStorageScp.columns.indexOf(DicomStorageScp.LATERALITY));
          log.info("SIDE is {}", laterality);
          values.put("SIDE", DataBuilder.buildText(laterality));
        }
        // send data to server
        instrumentExecutionService.addOutputParameterValues(values);
      }
    } catch(Exception e) {
      log.error("Unexpected exception while processing DICOM files.", e);
    } finally {
      FileSystemUtils.deleteRecursively(dcmDir);
    }
  }

  private byte[] compress(File file) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream compressed = new GZIPOutputStream(baos);
    FileCopyUtils.copy(new FileInputStream(file), compressed);
    return baos.toByteArray();
  }

  private static class VividDicomStoragePredicate implements DicomStorageScp.DicomStoragePredicate {

    private final static Logger log = LoggerFactory.getLogger(VividDicomStoragePredicate.class);

    private final Set<String> output;

    private Map<String, Integer> cineLoopIdsMap = new HashMap<String, Integer>();

    private VividDicomStoragePredicate(Set<String> output) {
      this.output = output;
    }

    @Override
    public boolean apply(String siuid, DicomObject dicomObject) {
      if (!cineLoopIdsMap.containsKey(siuid)) {
        cineLoopIdsMap.put(siuid, 0);
      }
      int cineLoopIdx = cineLoopIdsMap.get(siuid);

      String mediaStorageSOPClassUID = dicomObject.contains(Tag.MediaStorageSOPClassUID) ? dicomObject
          .getString(Tag.MediaStorageSOPClassUID) : null;
      String modality = dicomObject.getString(Tag.Modality);
      log.info("StudyInstanceUID={}", siuid);
      log.info("  Expected outputs: {}", StringUtils.collectionToCommaDelimitedString(output));
      if(mediaStorageSOPClassUID != null && mediaStorageSOPClassUID.equals(UID.UltrasoundImageStorage)) {
        log.info("  STILL_IMAGE found");
        if(output.contains("STILL_IMAGE")) {
          return true;
        }
      } else if(mediaStorageSOPClassUID != null &&
          mediaStorageSOPClassUID.equals(UID.UltrasoundMultiframeImageStorage)) {
        cineLoopIdx++;
        cineLoopIdsMap.put(siuid, cineLoopIdx);
        log.info("  CINELOOP_{} found", cineLoopIdx);
        if(output.contains("CINELOOP_" + cineLoopIdx)) {
          return true;
        }
      } else if("SR".equals(modality)) {
        log.info("  SR found");
        if(output.contains("SR")) {
          return true;
        }
      }
      log.info("  File type does not apply");
      // else ignore
      return false;
    }

  }
}