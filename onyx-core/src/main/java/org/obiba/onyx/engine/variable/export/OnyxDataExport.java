/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * 
 */
public class OnyxDataExport {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(OnyxDataExport.class);

  private EntityQueryService queryService;

  private VariableDirectory variableDirectory;

  private UserSessionService userSessionService;

  private IOnyxDataExportStrategy exportStrategy;

  private Resource configDirectory;

  private List<OnyxDataExportDestination> exportDestinations;

  private File outputRootDirectory;

  public void setExportDestinations(List<OnyxDataExportDestination> exportDestinations) {
    this.exportDestinations = exportDestinations;
  }

  public void setExportStrategy(IOnyxDataExportStrategy exportStrategy) {
    this.exportStrategy = exportStrategy;
  }

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public void setVariableDirectory(VariableDirectory variableDirectory) {
    this.variableDirectory = variableDirectory;
  }

  public void setConfigDirectory(Resource configDirectory) {
    this.configDirectory = configDirectory;
  }

  public void exportCompletedInterviews() throws Exception {
    Participant template = new Participant();
    // template.setExported(false);
    List<Participant> participants = queryService.match(template);
    for(Iterator<Participant> iterator = participants.iterator(); iterator.hasNext();) {
      Participant participant = iterator.next();
      // Export completed interviews only
      Interview interview = participant.getInterview();
      if(interview == null || interview.getStatus() != InterviewStatus.COMPLETED) {
        iterator.remove();
      }
    }

    if(participants.size() > 0) {
      for(OnyxDataExportDestination destination : this.exportDestinations) {
        log.info("Exporting to destination {}", destination.getName());
        OnyxDataExportContext context = new OnyxDataExportContext(destination.getName(), userSessionService.getUser());
        try {
          exportStrategy.prepare(context);

          // variables file
          OutputStream osVariables = exportStrategy.newEntry("variables.xml");
          VariableStreamer.toXML(variableDirectory.getVariableRoot(), osVariables);
          osVariables.flush();

          // system configuration in a zip file
          if(configDirectory != null) {
            File configDir = configDirectory.getFile();
            if(configDir != null && configDir.exists() && configDir.isDirectory()) {
              OutputStream osConfigZip = exportStrategy.newEntry(configDir.getName() + ".zip");
              zipDir(configDir, osConfigZip);
            }
          }

          // participants files
          for(Participant participant : participants) {
            String entryName = participant.getBarcode() + ".xml";
            OutputStream os = exportStrategy.newEntry(entryName);
            VariableDataSet participantData = variableDirectory.getParticipantData(participant, destination);
            VariableStreamer.toXML(participantData, os);
            os.flush();
          }
        } catch(RuntimeException e) {
          context.fail();
          log.error("Error exporting data to destination " + destination.getName() + ":" + e.getMessage(), e);
        } finally {
          context.endExport();
          exportStrategy.terminate(context);
        }
      }
    }
  }

  /**
   * Zip recursively the directory into the given output stream.
   * @param dir
   * @param os
   */
  private void zipDir(File dir, OutputStream os) {
    try {
      ZipOutputStream out = new ZipOutputStream(os);
      addDir(dir, out, dir);
      // Complete the ZIP file
      os.flush();
      out.finish();
    } catch(IOException e) {
      log.error("Failed zipping directory " + dir.getAbsolutePath(), e);
    }
  }

  /**
   * Add a directory content to zip output stream.
   * @param dir
   * @param out
   * @throws IOException
   */
  private void addDir(File dir, ZipOutputStream out, File sourceDir) throws IOException {
    File[] files = dir.listFiles();

    if(files != null) {
      byte[] tmpBuf = new byte[2048];
      for(File file : files) {
        if(file.isDirectory()) {
          addDir(file, out, sourceDir);
          continue;
        }

        // get a relative path for the zip
        String path = getFileRelativePath(file, sourceDir);
        FileInputStream in = new FileInputStream(file);
        log.debug("adding={}", path);
        out.putNextEntry(new ZipEntry(path));

        // Transfer from the file to the ZIP file
        int len;
        while((len = in.read(tmpBuf)) > 0) {
          out.write(tmpBuf, 0, len);
        }

        // Complete the entry
        out.closeEntry();
        in.close();
      }
    }
  }

  /**
   * Get the file path relative to the given source directory file.
   * @param f
   * @param sourceDir
   * @return
   */
  private String getFileRelativePath(File f, File sourceDir) {
    String path = f.getName();
    while(!f.getParentFile().equals(sourceDir)) {
      f = f.getParentFile();
      path = f.getName() + File.separator + path;
    }
    return path;
  }

  public File getOutputRootDirectory() {
    return outputRootDirectory;
  }

  public void setOutputRootDirectory(File outputRootDirectory) {
    this.outputRootDirectory = outputRootDirectory;
  }

}
