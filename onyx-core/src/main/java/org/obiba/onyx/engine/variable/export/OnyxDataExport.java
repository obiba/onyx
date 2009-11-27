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
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Collection;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.engine.output.Strategies;
import org.obiba.magma.filter.FilteredCollection;
import org.obiba.magma.xstream.Io;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.core.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnyxDataExport {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(OnyxDataExport.class);

  private ExportLogService exportLogService;

  private UserSessionService userSessionService;

  private List<OnyxDataExportDestination> exportDestinations;

  private File outputRootDirectory;

  public void setExportDestinations(List<OnyxDataExportDestination> exportDestinations) {
    this.exportDestinations = exportDestinations;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public void setExportLogService(ExportLogService exportLogService) {
    this.exportLogService = exportLogService;
  }

  public void setOutputRootDirectory(File outputRootDirectory) {
    this.outputRootDirectory = outputRootDirectory;
  }

  public File getOutputRootDirectory() {
    return outputRootDirectory;
  }

  public void exportInterviews() throws Exception {

    long exportStartTime = new Date().getTime();

    log.info("Starting export to configured destinations.");

    ArrayList<Collection> collections = new ArrayList<Collection>();
    collections.add(MagmaEngine.get().lookupCollection("onyx-baseline"));

    for(Collection collection : collections) {

      // Export interviews for each destination
      for(OnyxDataExportDestination destination : exportDestinations) {

        // Apply all filters to Collection for current OnyxDestination.
        Collection filteredCollection = new FilteredCollection(collection, destination.getVariableFilterChainMap(), destination.getEntityFilterChainMap());

        // Save FilteredCollection to disk.
        saveToDisk(filteredCollection, destination.getName(), outputRootDirectory, destination.getStrategies());

        // Mark the data of the FilteredCollection as exported for current destination (log entry).
        // markAsExported(filteredCollection, destination);

      }

    }

    long exportEndTime = new Date().getTime();

    log.info("Exported [{}] interview(s) in [{}ms] to [{}] destination(s).", new Object[] { 0, exportEndTime - exportStartTime, 0 });
  }

  private void saveToDisk(Collection collection, String destinationName, File outputDirectory, Strategies outputStrategies) {
    // Datasource exportDatasource = new FilesystemDatasource(destinationName, outputRootDirectory,
    // destination.getStrategies());
    // exportDatasource.createCollection(destinationName);
    // MagmaUtil.copy(collection, exportDatasource);

    displayCollectionDebugInformation(collection, destinationName, outputDirectory, outputStrategies);
  }

  private void displayCollectionDebugInformation(Collection collection, String destinationName, File outputDirectory, Strategies outputStrategies) {
    log.info("Exporting the following destination: {}", destinationName);
    log.info("Export output directory : {}", outputDirectory);

    StringWriter strategies = new StringWriter();
    for(String strategy : outputStrategies.getStrategies()) {
      strategies.append(strategy + ", ");
    }

    log.info("Export strategies are : {}", strategies);

    Io io = new Io();
    FileOutputStream os = null;
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      File temp = new File(System.getProperty("java.io.tmpdir"), "onyx-export.xml_" + dateFormat.format(new Date(System.currentTimeMillis())));
      log.info("Exporting to {}", temp.getPath());
      os = new FileOutputStream(temp);
      io.writeEntities(collection, os);
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      StreamUtil.silentSafeClose(os);
    }

  }

  private void markAsExported(Collection collection, OnyxDataExportDestination destination) {

    for(String entityType : collection.getEntityTypes()) {
      for(VariableEntity entity : collection.getEntities(entityType)) {
        ValueSet valueSet = collection.loadValueSet(entity);
        for(VariableValueSource source : collection.getVariableValueSources(entityType)) {

          // Value value = source.getValue(valueSet);

        }
        // Find the earliest and latest entity capture date-time
        // Write an entry in ExportLog to flag the set of entities as exported.

        ExportLog log = ExportLog.Builder.newLog().type(entityType).identifier(entity.getIdentifier()).start(new Date()).end(new Date()).destination(destination.getName()).exportDate(new Date()).user(userSessionService.getUser()).build();
        exportLogService.save(log);
      }
    }
  }

}
