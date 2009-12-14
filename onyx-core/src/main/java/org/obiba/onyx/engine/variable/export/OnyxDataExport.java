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
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.crypt.NoSuchKeyException;
import org.obiba.magma.crypt.PublicKeyProvider;
import org.obiba.magma.datasource.fs.DatasourceCopier;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.fs.DatasourceCopier.DatasourceCopyEventListener;
import org.obiba.magma.filter.FilteredValueTable;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.crypt.IPublicKeyFactory;
import org.obiba.onyx.engine.variable.CaptureAndExportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnyxDataExport {

  private static final Logger log = LoggerFactory.getLogger(OnyxDataExport.class);

  private ExportLogService exportLogService;

  private UserSessionService userSessionService;

  private List<OnyxDataExportDestination> exportDestinations;

  private File outputRootDirectory;

  private Map<String, CaptureAndExportStrategy> captureAndExportStrategyMap;

  private IPublicKeyFactory publicKeyFactory;

  // ONYX-424: Required to set FlushMode to COMMIT
  private SessionFactory sessionFactory;

  public void setExportDestinations(List<OnyxDataExportDestination> exportDestinations) {
    this.exportDestinations = exportDestinations;
  }

  public List<OnyxDataExportDestination> getExportDestinations() {
    return exportDestinations;
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

  public void setCaptureAndExportStrategyMap(Map<String, CaptureAndExportStrategy> captureAndExportStrategyMap) {
    this.captureAndExportStrategyMap = captureAndExportStrategyMap;
  }

  public void setPublicKeyFactory(IPublicKeyFactory publicKeyFactory) {
    this.publicKeyFactory = publicKeyFactory;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void exportInterviews() throws Exception {

    long exportStartTime = new Date().getTime();

    log.info("Starting export to configured destinations.");

    sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);

    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withListener(new DatasourceCopyEventListener() {

      public void onVariableCopy(Variable variable) {
      }

      public void onVariableCopied(Variable variable) {
      }

      public void onValueSetCopy(ValueSet valueSet) {
      }

      public void onValueSetCopied(ValueSet valueSet) {
        sessionFactory.getCurrentSession().clear();
      }
    }).build();

    PublicKeyProvider pkProvider = new PublicKeyProvider() {
      public PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException {
        PublicKey key = publicKeyFactory.getPublicKey(datasource.getName());
        if(key == null) {
          throw new NoSuchKeyException(datasource.getName(), "No PublicKey for destination '" + datasource.getName() + "'");
        }
        return key;
      }
    };

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(OnyxDataExportDestination destination : exportDestinations) {

        File outputFile = new File(outputRootDirectory, destination.getName() + ".zip");
        FsDatasource outputDatasource = new FsDatasource(destination.getName(), outputFile, destination.getEncryptionStrategy(pkProvider));

        MagmaEngine.get().addDatasource(outputDatasource);
        try {
          for(ValueTable table : datasource.getValueTables()) {
            // Check whether the destination wants this type of entity
            if(destination.wantsEntityType(table.getEntityType())) {
              // Export interviews for each destination

              // Apply all filters to ValueTable for current OnyxDestination.
              ValueTable filteredTable = new FilteredValueTable(table, destination.getVariableFilterChainForEntityName(table.getEntityType()), destination.getEntityFilterChainForEntityName(table.getEntityType()));

              // Save FilteredCollection to disk.
              copier.copy(filteredTable, outputDatasource);

              // Mark the data of the FilteredCollection as exported for current destination (log entry).
              // markAsExported(filteredCollection, destination);
              markAsExported(filteredTable, destination);
              sessionFactory.getCurrentSession().flush();
            }
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
      Date exportDate = new Date();

      // Find the earliest and latest entity capture date-time
      Date captureStartDate = null;
      Date captureEndDate = null;
      CaptureAndExportStrategy captureAndExportStrategy = captureAndExportStrategyMap.get(valueSet.getVariableEntity().getType());
      if(captureAndExportStrategy != null) {
        captureStartDate = captureAndExportStrategy.getCaptureStartDate(valueSet.getVariableEntity().getIdentifier());
        captureEndDate = captureAndExportStrategy.getCaptureEndDate(valueSet.getVariableEntity().getIdentifier());
      }

      // If capture dates null, default to export date (could happen for instruments and workstations).
      captureStartDate = (captureStartDate != null) ? captureStartDate : exportDate;
      captureEndDate = (captureEndDate != null) ? captureEndDate : exportDate;

      // Write an entry in ExportLog to flag the set of entities as exported.
      ExportLog log = ExportLog.Builder.newLog().type(valueSet.getVariableEntity().getType()).identifier(valueSet.getVariableEntity().getIdentifier()).start(captureStartDate).end(captureEndDate).destination(destination.getName()).exportDate(exportDate).user(userSessionService.getUser().getLogin()).build();
      exportLogService.save(log);
    }
  }

}
