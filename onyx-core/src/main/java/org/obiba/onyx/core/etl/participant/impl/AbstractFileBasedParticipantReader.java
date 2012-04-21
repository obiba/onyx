/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.obiba.core.util.StreamUtil;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.core.io.Resource;

/**
 * 
 */
public abstract class AbstractFileBasedParticipantReader extends AbstractParticipantReader {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private Resource inputDirectory;

  private FileInputStream fileInputStream = null;

  @Override
  public boolean isFileBased() {
    return true;
  }

  @Override
  public void addFileForProcessing(InputStream is) throws IOException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
    String fileName = formatter.format(new Date()) + "_upload" + getFilePattern();
    File inputFile = new File(getInputDirectory().getFile(), fileName);
    if(inputFile.createNewFile()) {
      FileOutputStream fos = new FileOutputStream(inputFile);
      try {
        StreamUtil.copy(is, fos);
      } finally {
        StreamUtil.silentSafeClose(fos);
      }
    }
  }

  @Override
  public boolean isUpdateAvailable() {
    try {
      return (getInputDirectory().getFile().listFiles(getFilter()).length > 0);
    } catch(IOException e) {
      log.error("Error listing files in appointment list directory.", e);
      return false;
    }
  }

  @Override
  public void open(ExecutionContext context) throws ItemStreamException {
    File currentFile = null;

    try {
      if(getInputDirectory() == null || getInputDirectory().getFile() == null) return;

      AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, "Start updating appointments"));
      File[] appointmentFiles = getInputDirectory().getFile().listFiles(this.getFilter());

      if(appointmentFiles.length > 1) AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, "Found " + appointmentFiles.length + " appointment lists. Will process the most recent one only and archive the others."));
      sortFilesOnDateAsc(appointmentFiles);

      currentFile = appointmentFiles[appointmentFiles.length - 1];
      AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, "Processing appointment list file " + currentFile.getName()));

      context.put("fileName", currentFile.getName());
      fileInputStream = new FileInputStream(currentFile);

    } catch(IOException e) {
      String message = "Abort updating appointments: Reading file error: " + ((currentFile == null) ? "unknown file" : currentFile.getName()) + " - " + e.getMessage();
      AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR, message));

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("ParticipantsListFileReadingError", new String[] { e.getMessage() }, "Reading file error: " + e.getMessage());
      throw vex;
    }
  }

  @Override
  public void update(ExecutionContext context) throws ItemStreamException {
  }

  @Override
  public void close() throws ItemStreamException {
    if(fileInputStream != null) {
      try {
        fileInputStream.close();
      } catch(IOException e) {
        // Ignored
      }
    }
  }

  public FilenameFilter getFilter() {
    return (new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return AbstractFileBasedParticipantReader.this.accept(dir, name);
      }
    });
  }

  public void sortFilesOnDateAsc(File[] appointmentFiles) {
    Arrays.sort(appointmentFiles, new Comparator<File>() {
      @Override
      public int compare(File f1, File f2) {
        return (Long.valueOf(f1.lastModified()).compareTo(Long.valueOf(f2.lastModified())));
      }
    });
  }

  protected boolean accept(File dir, String name) {
    return (name.toLowerCase().endsWith(getFilePattern()));
  }

  public void setInputDirectory(Resource inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  public Resource getInputDirectory() {
    return inputDirectory;
  }

  public FileInputStream getFileInputStream() {
    return fileInputStream;
  }

  public void setFileInputStream(FileInputStream fileInputStream) {
    this.fileInputStream = fileInputStream;
  }

}
