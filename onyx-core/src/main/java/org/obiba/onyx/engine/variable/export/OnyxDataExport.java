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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

public class OnyxDataExport {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(OnyxDataExport.class);

  private EntityQueryService queryService;

  private VariableDirectory variableDirectory;

  private UserSessionService userSessionService;

  private IOnyxDataExportStrategy exportStrategy;

  private Resource configDirectory;

  // ONYX-424: Required to set FlushMode to COMMIT
  private SessionFactory sessionFactory;

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

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  // Set this method to be Transactional in order to have a single commit at the end of the export.
  @Transactional(rollbackFor = Exception.class)
  public void exportInterviews() throws Exception {

    // ONYX-424: Set FlushMode to COMMIT so that Hibernate only flushes the entities after the export has completed.
    // This prevents Hibernate from flushing BEFORE every time we read from the database.
    if(sessionFactory != null) {
      sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
    }

    long exportAllStartTime = new Date().getTime();

    log.info("Starting export interviews for configured destinations.");

    // same export date for every participant and every destinations: kind of system snapshot.
    Date exportDate = new Date();

    // Get a list of potential exportable interviews (participant not marked as exported)
    Participant template = new Participant();
    template.setExported(false);
    List<Participant> participantsToCheck = queryService.match(template);
    List<Participant> participants = new ArrayList<Participant>();
    for(Participant participant : participantsToCheck) {
      if(participant.isExportable()) {
        participants.add(participant);
      }
    }

    // This is to keep a list of exported interviews for all destinations.
    Set<String> exportedBarcodes = new HashSet<String>();

    Map<String, List<Participant>> participantForEachDestinationMap = null;

    // If exportable interviews were found
    if(participants.size() > 0) {

      // Create a list of interviews to be exported for each destination
      participantForEachDestinationMap = getParticipantsForEachDestination(exportDate, participants);

      // Export interviews for each destination
      for(OnyxDataExportDestination destination : this.exportDestinations) {

        List<Participant> participantForThisDestination = participantForEachDestinationMap.get(destination.getName());

        if(participantForThisDestination != null && participantForThisDestination.size() > 0) {
          exportDestination(participantForThisDestination, exportDate, destination, exportedBarcodes);
        }

      }

    }

    long exportAllEndTime = new Date().getTime();
    log.info("Exported [{}] interview(s) in [{}ms] to [{}] destination(s).", new Object[] { exportedBarcodes.size(), exportAllEndTime - exportAllStartTime, participantForEachDestinationMap == null ? 0 : participantForEachDestinationMap.size() });
  }

  /**
   * Creates a List of Participants to be exported for each export destination. The List is added to a Map to facilitate
   * the retrieval of the List for a specific destination (the key is the destination name).
   * 
   * Also marks the participant as exported and sets the export date.
   * 
   * @param exportDate Export date which will be set on each participant to be exported.
   * @param participants The initial list of participant from which the destinations specific lists will be created.
   * 
   * @return A Map containing a list of participants for each destination.
   */
  protected Map<String, List<Participant>> getParticipantsForEachDestination(Date exportDate, List<Participant> participants) {

    Map<String, List<Participant>> participantForEachDestinationMap = new HashMap<String, List<Participant>>();

    for(OnyxDataExportDestination destination : this.exportDestinations) {

      List<Participant> participantsForDestination = getParticipantsToBeExportedForDestination(destination, participants);

      if(participantsForDestination.size() > 0) {
        participantForEachDestinationMap.put(destination.getName(), participantsForDestination);
        for(Participant participant : participantsForDestination) {
          participant.setExported(true);
          participant.setExportDate(exportDate);
        }
      }

    }

    if(sessionFactory != null) {

      // Flushing the session will write the pending modifications (exported flag)
      sessionFactory.getCurrentSession().flush();
    }

    return participantForEachDestinationMap;
  }

  /**
   * Creates a list of participants to be exported for a specific destination.
   * 
   * @param destination The destination.
   * @param participants The initial list of participant from which the destination specific list will be created.
   * @return The list of participant to be exported.
   */
  protected List<Participant> getParticipantsToBeExportedForDestination(OnyxDataExportDestination destination, List<Participant> participants) {
    Set<InterviewStatus> exportedInterviewStatuses = destination.getExportedInterviewStatuses();
    List<Participant> participantsToBeExported = new ArrayList<Participant>();
    for(Participant participant : participants) {
      if(exportedInterviewStatuses.contains(participant.getInterview().getStatus())) {
        participantsToBeExported.add(participant);
      }
    }
    return participantsToBeExported;
  }

  /**
   * Export a list of participant to a specific destination.
   * 
   * @param participants The participants to export.
   * @param exportDate The timestamp for the export.
   * @param destination The target destination.
   * @param exportedBarcodes A Set of participant's barcode to keep track of the exported participant.
   * 
   */
  private void exportDestination(List<Participant> participants, Date exportDate, OnyxDataExportDestination destination, Set<String> exportedBarcodes) throws IOException {
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
        exportedBarcodes.add(participant.getBarcode());
        String entryName = participant.getBarcode() + ".xml";
        OutputStream os = exportStrategy.newEntry(entryName);
        VariableDataSet participantData = variableDirectory.getParticipantData(participant, destination);
        participantData.setExportDate(exportDate);
        VariableStreamer.toXML(participantData, os);
        os.flush();

        if(sessionFactory != null) {
          // Clearing the session will empty the cache, freeing memory.
          // It will also delete any pending updates, which is why we already marked all participants as exported.
          sessionFactory.getCurrentSession().clear();
        }
      }

    } catch(RuntimeException e) {
      context.fail();
      log.error("Error exporting data to destination " + destination.getName() + ":" + e.getMessage(), e);
      throw e;
    } finally {
      context.endExport();
      exportStrategy.terminate(context);
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
