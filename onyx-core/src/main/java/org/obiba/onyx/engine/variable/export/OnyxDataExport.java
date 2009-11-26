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

import org.obiba.magma.Collection;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.filter.FilteredCollection;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.core.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class OnyxDataExport {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(OnyxDataExport.class);

  private Datasource onyxDataSource;

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

  // Set this method to be Transactional in order to have a single commit at the end of the export.
  @Transactional(rollbackFor = Exception.class)
  public void exportInterviews() throws Exception {
    for(Collection collection : onyxDataSource.getCollections()) {

      for(OnyxDataExportDestination destination : exportDestinations) {

        // Wrap collection in filtered collection
        Collection filteredCollection = new FilteredCollection(collection, destination.getVariableFilterChainMap(), destination.getEntityFilterChainMap());

        // Datasource xmlDataSource = new XmlDataSource(destination.getStrategies(), rootDirectory);

        // This may change. May not need to register when copying.
        // MagmaEngine.get().addDatasource(xmlDataSource);

        // MagmaUtil.copy(filteredCollection, xmlDataSource.getWritableCollection());

        // Instead of marking everything as exported in a sequential manner (as is done here), it may be
        // better to use the observer pattern and subscribe to changes as published by the XmlDataSource.
        // When the XmlDataSource publishes that it has written an entity, we can respond by marking that
        // entity as exported.
        markAsExported(filteredCollection, destination);
      }
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

  public File getOutputRootDirectory() {
    return outputRootDirectory;
  }

  public void setOutputRootDirectory(File outputRootDirectory) {
    this.outputRootDirectory = outputRootDirectory;
  }

  public void setOnyxDataSource(Datasource onyxDataSource) {
    this.onyxDataSource = onyxDataSource;
  }

}
