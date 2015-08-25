package org.obiba.onyx.jade.instrument.ge.vivid;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.swing.*;

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

public class VividInstrumentRunner implements InstrumentRunner {

  protected Logger log = LoggerFactory.getLogger(VividInstrumentRunner.class);

  private static final String STILL_IMAGE_KEY = "STILL_IMAGE";

  private static final String CINELOOP_KEY = "CINELOOP";

  private static final String SR_KEY = "SR";

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
        int stillImageIdx = 1;
        int srIdx = 1;
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
              StringBuilder key = new StringBuilder();

              if(UID.UltrasoundImageStorage.equals(mediaStorageSOPClassUID)) {
                key.append(STILL_IMAGE_KEY);
                if(stillImageIdx > 1 || stillImageIdx == 1 && output.contains(STILL_IMAGE_KEY + "_1")) {
                  key.append("_").append(stillImageIdx);
                }
                stillImageIdx++;
              } else if(UID.UltrasoundMultiframeImageStorage.equals(mediaStorageSOPClassUID)) {
                key.append(CINELOOP_KEY);
                if(cineLoopIdx > 1 || cineLoopIdx == 1 && output.contains(CINELOOP_KEY + "_1")) {
                  key.append("_").append(cineLoopIdx);
                }
                cineLoopIdx++;
              } else if(SR_KEY.equals(modality)) {
                key.append(SR_KEY);
                if(srIdx > 1 || srIdx == 1 && output.contains(SR_KEY + "_1")) {
                  key.append("_").append(srIdx);
                }
                srIdx++;
              } else {
                // don't know what this file is.
                log.warn("Received unknown DICOM file. Ignoring.");
              }

              String keyStr = key.toString();
              if(output.contains(keyStr)) {
                // This will contain a large byte-array
                Data dicomData = DataBuilder.buildBinary(compress(dcm.getFile()));
                log.info(String
                    .format("[%s] dicom file: %d bytes -- compressed file: %d bytes", keyStr, dcm.getFile().length(),
                        ((byte[]) dicomData.getValue()).length));

                values.put(keyStr, dicomData);
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

  public static class VividDicomStoragePredicate implements DicomStorageScp.DicomStoragePredicate {

    private final static Logger log = LoggerFactory.getLogger(VividDicomStoragePredicate.class);

    private final Set<String> output;

    private final Map<String, List<String>> stillIdsMap = new HashMap<>();

    private final Map<String, List<String>> cineLoopIdsMap = new HashMap<>();

    private final Map<String, List<String>> srIdsMap = new HashMap<>();

    public VividDicomStoragePredicate(Set<String> output) {
      this.output = output;
    }

    @Override
    public boolean apply(String siuid, File file, DicomObject dicomObject) {
      String mediaStorageSOPClassUID = dicomObject.contains(Tag.MediaStorageSOPClassUID) ? dicomObject
          .getString(Tag.MediaStorageSOPClassUID) : null;
      String modality = dicomObject.getString(Tag.Modality);
      log.info("StudyInstanceUID={}", siuid);
      StringBuffer outputs = new StringBuffer();
      for (String s : output) outputs.append(s).append(" ");
      log.info("  Expected outputs: {}", outputs);

      if(UID.UltrasoundImageStorage.equals(mediaStorageSOPClassUID)) {
        return checkOutput(STILL_IMAGE_KEY, getIndex(stillIdsMap, siuid, file.getName()));
      }

      if(UID.UltrasoundMultiframeImageStorage.equals(mediaStorageSOPClassUID)) {
        return checkOutput(CINELOOP_KEY, getIndex(cineLoopIdsMap, siuid, file.getName()));
      }

      if(SR_KEY.equals(modality)) {
        return checkOutput(SR_KEY, getIndex(srIdsMap, siuid, file.getName()));
      }

      log.info("  File type does not apply");
      // else ignore
      return false;
    }

    private boolean checkOutput(String key, int idx) {
      log.info("  {} ({}) found", key, idx);
      if(output.contains(key + "_" + idx) || idx == 1 && output.contains(key)) {
        log.info("  {} ({}) applies", key, idx);
        return true;
      }
      log.info("  {} ({}) does not apply", key, idx);
      return false;
    }

    /**
     * Get index of the dicom ID, add it if not found.
     *
     * @param idsMap
     * @param siuid
     * @return
     */
    private Integer getIndex(Map<String, List<String>> idsMap, String siuid, String fileName) {
      if(!idsMap.containsKey(siuid)) {
        idsMap.put(siuid, new ArrayList<String>());
      }
      if (!idsMap.get(siuid).contains(fileName))  {
        idsMap.get(siuid).add(fileName);
      }
      return idsMap.get(siuid).indexOf(fileName) + 1;
    }

  }

}