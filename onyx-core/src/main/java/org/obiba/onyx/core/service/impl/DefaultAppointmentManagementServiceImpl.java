/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.obiba.core.util.FileUtil;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.etl.participant.IParticipantReadListener;
import org.obiba.onyx.core.etl.participant.IParticipantReader;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultAppointmentManagementServiceImpl implements AppointmentManagementService, ResourceLoaderAware {

  private static final Logger appointmentListUpdatelog = LoggerFactory.getLogger("appointmentListUpdate");

  private String inputDirectory;

  private String outputDirectory;

  private File inputDir;

  private File outputDir;

  private ApplicationConfigurationService applicationConfigurationService;

  private UserSessionService userSessionService;

  private ParticipantService participantService;

  private ResourceLoader resourceLoader;

  private IParticipantReader participantReader;

  public void initialize() {
    if(inputDirectory == null || inputDirectory.isEmpty()) throw new IllegalArgumentException("DefaultAppointmentManagementServiceImpl: InputDirectory should not be null");

    try {
      setInputDir(resourceLoader.getResource(inputDirectory).getFile());
      if(!getInputDir().exists()) {
        getInputDir().mkdirs();
      }
      if(!getInputDir().isDirectory()) {
        throw new IllegalArgumentException("DefaultAppointmentManagementServiceImpl: InputDirectory " + getInputDir().getAbsolutePath() + " is not a directory");
      }

      if(outputDirectory != null && !outputDirectory.isEmpty()) {
        setOutputDir(resourceLoader.getResource(outputDirectory).getFile());
        if(!getOutputDir().exists()) {
          getOutputDir().mkdirs();
        }
        if(!getOutputDir().isDirectory()) {
          throw new IllegalArgumentException("DefaultAppointmentManagementServiceImpl: OutputDirectory " + getOutputDir().getAbsolutePath() + " is not a directory");
        }
      }
    } catch(IOException ex) {
      throw new RuntimeException("DefaultAppointmentManagementServiceImpl: Failed to access directory - " + ex);
    }
  }

  
  public boolean isUpdateAvailable() {
    if(getInputDir().listFiles(getFilter()).length > 0) {
      return true;
    }
    return false;
  }

  synchronized public void updateAppointments() {
    if(!isUpdateAvailable()) {
      return;
    }

    appointmentListUpdatelog.info("Start updating appointments");
    File[] appointmentFiles = getInputDir().listFiles(getFilter());
    if(appointmentFiles.length > 1) {
      appointmentListUpdatelog.info("Found {} appointment lists. Will process the most recent one only.");
    }
    sortFilesOnDateAsc(appointmentFiles);

    File currentFile = appointmentFiles[appointmentFiles.length - 1];
    // Archive all other files
    for(int i = 0; i < appointmentFiles.length - 1; i++) {
      archiveFile(appointmentFiles[i]);
    }

    String siteCode = applicationConfigurationService.getApplicationConfiguration().getSiteNo();
    User user = userSessionService.getUser();
    participantService.cleanUpAppointment();

    appointmentListUpdatelog.info("Processing appointment list file {}", currentFile.getName());
    List<IParticipantReadListener> listeners = new ArrayList<IParticipantReadListener>();
    listeners.add(new UpdateParticipantListener(siteCode, user, participantService));
    FileInputStream inputStream = null;

    try {
      participantReader.process(inputStream = new FileInputStream(currentFile), listeners);
    } catch(FileNotFoundException e) {
      // should not happen cause we found it by exploring the directory
      appointmentListUpdatelog.error("Abort updating appointments: No participants list file found in directory {} ", inputDirectory);

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("NoParticipantsListFileFound", new String[] { inputDirectory }, "No participants list file found in: " + inputDirectory);
      throw vex;
    } catch(IOException e) {
      appointmentListUpdatelog.error("Abort updating appointments: Reading file error: {} - {}", currentFile.getName(), e.getMessage());

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("ParticipantsListFileReadingError", new String[] { e.getMessage() }, "Reading file error: " + e.getMessage());
      throw vex;
    } catch(IllegalArgumentException e) {
      appointmentListUpdatelog.error("Abort updating appointments: Validation error: {} - {}", currentFile.getName(), e.getMessage());

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("ParticipantsListFileValidationError", new String[] { e.getMessage() }, "Validation error in file: " + e.getMessage());
      throw vex;
    } finally {
      if(inputStream != null) {
        try {
          inputStream.close();
        } catch(IOException e) {
          // Ignored
        }
      }
    }
    archiveFile(currentFile);

    appointmentListUpdatelog.info("End updating appointments");
  }

  public void archiveFiles(File... appointmentFiles) {
    for(File currentFile : appointmentFiles) {
      archiveFile(currentFile);
    }
  }

  public void archiveFile(File file) {
    if(outputDir != null) {
      try {
        appointmentListUpdatelog.info("Moving file {} to output directory {}.", file.getName(), getOutputDir().getAbsolutePath());

        // Re-create output directory, in case it was deleted at runtime.
        if(!getOutputDir().exists()) {
          getOutputDir().mkdirs();
        }

        FileUtil.moveFile(file, getOutputDir());
      } catch(IOException e) {
        appointmentListUpdatelog.error("Abort updating appointments: Archiving file error {} - {}", file.getName(), e.getMessage());

        ValidationRuntimeException vex = new ValidationRuntimeException();
        vex.reject("ParticipantsListFileArchivingError", new String[] { e.getMessage() }, "Archiving file error: " + e.getMessage());
        throw vex;
      }
    } else {
      // If no output directory has been configured, just delete the file.
      file.delete();
    }

  }

  public void sortFilesOnDateAsc(File[] appointmentFiles) {
    Arrays.sort(appointmentFiles, new Comparator<File>() {
      public int compare(File f1, File f2) {
        return (Long.valueOf(f1.lastModified()).compareTo(Long.valueOf(f2.lastModified())));
      }
    });
  }

  
  public FilenameFilter getFilter() {
    return (new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (name.toLowerCase().endsWith(".xls"));
      }
    });
  }

  public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
    this.applicationConfigurationService = applicationConfigurationService;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public void setInputDirectory(String inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public void setParticipantReader(IParticipantReader participantReader) {
    this.participantReader = participantReader;
  }

  
  public File getInputDir() {
    return inputDir;
  }

  public void setInputDir(File inputDir) {
    this.inputDir = inputDir;
  }

  public void setOutputDir(File outputDir) {
    this.outputDir = outputDir;
  }

  
  public File getOutputDir() {
    return outputDir;
  }

}
