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
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.crypt.KeyProviderSecurityException;
import org.obiba.magma.crypt.NoSuchKeyException;
import org.obiba.magma.filter.FilteredValueTable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.DatasourceCopier.DatasourceCopyValueSetEventListener;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.MultithreadedDatasourceCopier;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.crypt.IPublicKeyFactory;
import org.obiba.onyx.engine.variable.CaptureAndExportStrategy;
import org.obiba.onyx.engine.variable.export.format.DatasourceFactoryProvider;
import org.obiba.onyx.engine.variable.export.format.XmlDatasourceFactoryProvider;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.obiba.onyx.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class OnyxDataExport {

  private static final Logger log = LoggerFactory.getLogger(OnyxDataExport.class);

  private List<DatasourceFactoryProvider> exportDatasourceProviders;

  private ParticipantService participantService;

  private ExportLogService exportLogService;

  private UserSessionService userSessionService;

  private List<OnyxDataExportDestination> exportDestinations;

  private File outputRootDirectory;

  private Map<String, CaptureAndExportStrategy> captureAndExportStrategyMap;

  private IPublicKeyFactory publicKeyFactory;

  // ONYX-424: Required to set FlushMode
  private SessionFactory sessionFactory;

  private MagmaInstanceProvider magmaInstanceProvider;

  private ThreadFactory threadFactory;

  public void setExportDatasourceProviders(List<DatasourceFactoryProvider> exportDatasourceProviders) {
    this.exportDatasourceProviders = exportDatasourceProviders;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setThreadFactory(ThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
  }

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

  public void setMagmaInstanceProvider(MagmaInstanceProvider magmaInstanceProvider) {
    this.magmaInstanceProvider = magmaInstanceProvider;
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

    Datasource datasource = magmaInstanceProvider.getOnyxDatasource();
    for(final OnyxDataExportDestination destination : exportDestinations) {
      log.info("Exporting to {}", destination.getName());
      boolean exportFailed = false;

      Iterable<ValueTable> tables = getExportValueTables(datasource, destination);

      File outputFile = destination.createOutputFile(outputRootDirectory);

      DatasourceFactory factory = getDatasourceFactory(pkProvider, destination, tables, outputFile);

      Datasource outputDatasource = factory.create();
      if(destination.getOptions() != null && destination.getOptions().getUseEnrollmentId()) {
        outputDatasource = new EnrollmentIdDatasource(participantService, outputDatasource);
      }
      try {
        outputDatasource.initialise();
      } catch(DatasourceParsingException e) {
        e.printTree();
        throw e;
      }

      try {
        for(ValueTable table : tables) {
          // Export interviews for each destination
          long exportStartTime = System.currentTimeMillis();

          ExportListener listener = new ExportListener(destination);

          // Copy the filtered table to the destination datasource
          boolean copyNulls = destination.getOptions() != null ? destination.getOptions().getCopyNullValues() : false;
          MultithreadedDatasourceCopier.Builder.newCopier().withQueueSize(10).withThreads(threadFactory).withCopier(DatasourceCopier.Builder.newCopier().copyNullValues(copyNulls).withLoggingListener().withListener(listener)).from(table).to(outputDatasource).build().copy();

          long exportEndTime = System.currentTimeMillis();
          log.info("Exported [{}] entities of type [{}] in [{}ms] to destination [{}.{}].", new Object[] { listener.getValueSetCount(), table.getEntityType(), exportEndTime - exportStartTime, destination.getName(), table.getName() });
        }
      } catch(Exception e) {
        // Flag the export as failed so we delete the output file
        exportFailed = true;
        throw e;
      } finally {
        outputDatasource.dispose();
        if(exportFailed == true) {
          try {
            FileUtil.delete(outputFile);
          } catch(IOException e) {
            // ignore
          }
        }
      }
    }
  }

  private
      DatasourceFactory
      getDatasourceFactory(KeyProvider pkProvider, final OnyxDataExportDestination destination, Iterable<ValueTable> tables, File outputFile) {

    // Default export format
    String format = XmlDatasourceFactoryProvider.FORMAT;

    if(destination.getOptions() != null && destination.getOptions().getFormat() != null) {
      format = destination.getOptions().getFormat();
    }

    for(DatasourceFactoryProvider provider : exportDatasourceProviders) {
      if(provider.getFormat().equalsIgnoreCase(format)) {
        return provider.getDatasourceFactory(destination, outputFile, pkProvider, tables);
      }
    }
    throw new IllegalStateException("Unknown export format: " + format);
  }

  private Iterable<ValueTable> getExportValueTables(Datasource datasource, final OnyxDataExportDestination destination) {
    return Iterables.transform(Iterables.filter(datasource.getValueTables(), new Predicate<ValueTable>() {

      @Override
      public boolean apply(ValueTable input) {
        return destination.wantsTable(input);
      }

    }), new Function<ValueTable, ValueTable>() {

      @Override
      public ValueTable apply(ValueTable from) {
        return new FilteredValueTable(from, destination.getVariableFilterChainForTable(from), destination.getEntityFilterChainForTable(from));
      }
    });
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
    ExportLog exportLog = ExportLog.Builder.newLog().type(valueSet.getVariableEntity().getType()).identifier(valueSet.getVariableEntity().getIdentifier()).start(captureStartDate).end(captureEndDate).destination(destination.getName() + '.' + destinationTableName).exportDate(exportDate).user(userSessionService.getUser().getLogin()).build();
    exportLogService.save(exportLog);
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
