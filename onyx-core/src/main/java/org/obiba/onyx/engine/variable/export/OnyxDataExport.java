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
import java.util.Date;
import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.fs.DatasourceCopier;
import org.obiba.magma.datasource.fs.FsDatasource;
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

    DatasourceCopier copier = new DatasourceCopier();

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(OnyxDataExportDestination destination : exportDestinations) {
        FsDatasource outputDatasource = new FsDatasource(destination.getName(), outputRootDirectory + "/" + destination.getName() + ".zip");

        MagmaEngine.get().addDatasource(outputDatasource);
        try {
          for(ValueTable table : datasource.getValueTables()) {

            // Export interviews for each destination

            // Apply all filters to ValueTable for current OnyxDestination.
            // ValueTable filteredCollection = new FilteredValueTable(table, null, null);
            ValueTable filteredCollection = table;

            // Save FilteredCollection to disk.
            copier.copy(table, outputDatasource);

            // Mark the data of the FilteredCollection as exported for current destination (log entry).
            // markAsExported(filteredCollection, destination);
            markAsExported(filteredCollection, destination);

          }
        } finally {
          MagmaEngine.get().removeDatasource(outputDatasource);
        }
      }

    }

    long exportEndTime = new Date().getTime();

    log.info("Exported [{}] interview(s) in [{}ms] to [{}] destination(s).", new Object[] { 0, exportEndTime - exportStartTime, 0 });
  }

  private void markAsExported(ValueTable table, OnyxDataExportDestination destination) {
    for(ValueSet valueSet : table.getValueSets()) {
      // Find the earliest and latest entity capture date-time
      // Write an entry in ExportLog to flag the set of entities as exported.
      ExportLog log = ExportLog.Builder.newLog().type(valueSet.getVariableEntity().getType()).identifier(valueSet.getVariableEntity().getIdentifier()).start(new Date()).end(new Date()).destination(destination.getName()).exportDate(new Date()).user(userSessionService.getUser().getLogin()).build();
      exportLogService.save(log);
    }
  }

}
