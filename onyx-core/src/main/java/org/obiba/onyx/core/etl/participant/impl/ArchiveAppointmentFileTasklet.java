/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import org.obiba.core.util.FileUtil;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

/**
 * Spring Batch Tasklet called in ArchiveAppointmentFileStep to archive the participant files contained in the
 * inputDirectory
 */
public class ArchiveAppointmentFileTasklet implements Tasklet {

  private Resource inputDirectory;

  private Resource outputDirectory;

  private AbstractParticipantReader participantReader;

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) throws Exception {

    if(getInputDirectory() != null && getInputDirectory().getFile() != null) {
      for(File file : getInputDirectory().getFile().listFiles(getFilter())) {
        archiveFile(file, context.getStepContext().getStepExecution().getExecutionContext());
      }
    }

    return RepeatStatus.FINISHED;
  }

  private void archiveFile(File file, ExecutionContext context) {
    if(getOutputDirectory() != null) {
      try {

        File outputDir = getOutputDirectory().getFile();
        String message = "Moving file " + file.getName() + " to output directory " + outputDir.getAbsolutePath();
        AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, message));

        // Re-create output directory, in case it was deleted at runtime.
        if(!outputDir.exists()) {
          outputDir.mkdirs();
        }

        FileUtil.moveFile(file, outputDir);
      } catch(IOException e) {
        String message = "Abort updating appointments: Archiving file error " + file.getName() + " - " + e.getMessage();
        AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR, message));

        ValidationRuntimeException vex = new ValidationRuntimeException();
        vex.reject("ParticipantsListFileArchivingError", new String[] { e.getMessage() }, "Archiving file error: " + e.getMessage());
        throw vex;
      }
    } else {
      // If no output directory has been configured, just delete the file.
      file.delete();
    }

  }

  private FilenameFilter getFilter() {
    return (new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return participantReader.accept(dir, name);
      }
    });
  }

  public void setInputDirectory(Resource inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  public Resource getInputDirectory() {
    return inputDirectory;
  }

  public void setOutputDirectory(Resource outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public Resource getOutputDirectory() {
    return outputDirectory;
  }

  public void setParticipantReader(AbstractParticipantReader participantReader) {
    this.participantReader = participantReader;
  }

}
