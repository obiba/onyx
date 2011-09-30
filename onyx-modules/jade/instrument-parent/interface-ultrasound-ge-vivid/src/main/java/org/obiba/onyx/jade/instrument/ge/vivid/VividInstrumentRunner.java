package org.obiba.onyx.jade.instrument.ge.vivid;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
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
    DicomStorageScp gui = new DicomStorageScp(server);

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
    Set<String> output = instrumentExecutionService.getExpectedOutputParameterVendorNames();
    try {
      Map<String, Data> values = new HashMap<String, Data>();
      for(StoredDicomFile dcm : server.listDicomFiles()) {
        try {
          DicomObject d = dcm.getDicomObject();
          String modality = d.getString(Tag.Modality);
          if(output.contains(modality)) {
            values.put(modality, DataBuilder.buildBinary(dcm.getFile()));
          }
        } catch(IOException e) {
          // ignore
        }
      }
      instrumentExecutionService.addOutputParameterValues(values);
    } finally {
      FileSystemUtils.deleteRecursively(dcmDir);
    }
  }
}