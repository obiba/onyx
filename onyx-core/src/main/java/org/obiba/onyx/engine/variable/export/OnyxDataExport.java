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
import java.security.KeyPair;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.crypt.KeyProviderSecurityException;
import org.obiba.magma.crypt.NoSuchKeyException;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.filter.FilteredValueTable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.DatasourceCopier.DatasourceCopyValueSetEventListener;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.crypt.IPublicKeyFactory;
import org.obiba.onyx.engine.variable.CaptureAndExportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class OnyxDataExport {

  private static final Logger log = LoggerFactory.getLogger(OnyxDataExport.class);

  private ExportLogService exportLogService;

  private UserSessionService userSessionService;

  private List<OnyxDataExportDestination> exportDestinations;

  private File outputRootDirectory;

  private Map<String, CaptureAndExportStrategy> captureAndExportStrategyMap;

  private IPublicKeyFactory publicKeyFactory;

  // ONYX-424: Required to set FlushMode
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

  @Transactional(rollbackFor = Exception.class)
  public void exportInterviews() throws Exception {

    FlushMode originalExportMode = sessionFactory.getCurrentSession().getFlushMode();
    // Change the flushMode. We'll flush the session manually: see ExportListener below.
    sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
    try {
      log.info("Starting export to configured destinations.");
      internalExport();
      log.info("Export successfully completed.");
    } finally {
      // Reset the flushMode
      sessionFactory.getCurrentSession().setFlushMode(originalExportMode);
    }

  }

  /**
   * Private method for performing the complete export process
   */
  private void internalExport() throws Exception {
    KeyProvider pkProvider = new KeyProvider() {
      public KeyPair getKeyPair(PublicKey publicKey) throws NoSuchKeyException, KeyProviderSecurityException {
        throw new NoSuchKeyException("No KeyPair for publicKey.");
      }

      public KeyPair getKeyPair(String alias) throws NoSuchKeyException, KeyProviderSecurityException {
        throw new NoSuchKeyException("No KeyPair for alias '" + alias + "'");
      }

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

        boolean exportFailed = false;
        File outputFile = new File(outputRootDirectory, destination.getName() + "-" + getCurrentDateTimeString() + ".zip");
        FsDatasource outputDatasource = new FsDatasource(destination.getName(), outputFile, destination.getEncryptionStrategy(pkProvider));

        MagmaEngine.get().addDatasource(outputDatasource);
        try {
          for(ValueTable table : datasource.getValueTables()) {
            // Check whether the destination wants this type of entity
            if(destination.wantsTable(table)) {
              // Export interviews for each destination
              long exportStartTime = System.currentTimeMillis();

              // Apply all filters to ValueTable for current OnyxDestination.
              ValueTable filteredTable = new FilteredValueTable(table, destination.getVariableFilterChainForTable(table), destination.getEntityFilterChainForTable(table));

              ExportListener listener = new ExportListener(destination);
              DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withListener(listener).build();

              // Copy the filtered table to the destination datasource
              copier.copy(filteredTable, outputDatasource);

              long exportEndTime = System.currentTimeMillis();
              log.info("Exported [{}] entities of type [{}] in [{}ms] to destination [{}.{}].", new Object[] { listener.getValueSetCount(), table.getEntityType(), exportEndTime - exportStartTime, destination.getName(), table.getName() });
            }
          }
        } catch(Exception e) {
          // Flag the export as failed so we delete the output file
          exportFailed = true;
          throw e;
        } finally {
          MagmaEngine.get().removeDatasource(outputDatasource);
          if(exportFailed == true) {
            outputFile.delete();
          }
        }
      }
    }
  }

  /**
   * Creates an {@code ExportLog} entry for the specified {@code valueSet} and {@code destination}.
   * @param valueSet
   * @param destination
   */
  private void markAsExported(ValueSet valueSet, OnyxDataExportDestination destination, String destinationTableName) {

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
    ExportLog log = ExportLog.Builder.newLog().type(valueSet.getVariableEntity().getType()).identifier(valueSet.getVariableEntity().getIdentifier()).start(captureStartDate).end(captureEndDate).destination(destination.getName() + '.' + destinationTableName).exportDate(exportDate).user(userSessionService.getUser().getLogin()).build();
    exportLogService.save(log);
  }

  private String getCurrentDateTimeString() {
    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    return df.format(new Date());
  }

  private class ExportListener implements DatasourceCopyValueSetEventListener {
    long valueSetCount = 0;

    OnyxDataExportDestination destination;

    ExportListener(OnyxDataExportDestination destination) {
      this.destination = destination;
    }

    public long getValueSetCount() {
      return valueSetCount;
    }

    public void onValueSetCopy(ValueTable source, ValueSet valueSet) {
      // Clear the session: this empties the first-level cache which is currently filled with the entity's data.
      // Clearing the session also clears any pending write operations (INSERT or UPDATE). This is safe because
      // the copy operation is read-only.
      // We clear the session before we create the export log. It helps the flush call below (in onValueSetCopied)
      // run faster since the session will only contain the new export log.
      sessionFactory.getCurrentSession().clear();
    }

    public void onValueSetCopied(ValueTable source, ValueSet valueSet, String... tables) {
      valueSetCount++;

      // Create the export log
      for(String destinationTableName : tables) {
        markAsExported(valueSet, destination, destinationTableName);
      }

      // Flush the export log (this executes the underlying INSERT statement for the ExportLog within the transaction)
      sessionFactory.getCurrentSession().flush();
    }
  }

}
