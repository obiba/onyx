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

import java.util.List;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultExportLogServiceImpl extends PersistenceManagerAwareService implements ExportLogService {
  //
  // ExportLogService Methods
  //

  public void save(ExportLog exportLog) {
    getPersistenceManager().save(exportLog);
  }

  public List<ExportLog> getExportLogs(String entityTypeName, String destination, boolean ascending) {
    return getExportLogs(entityTypeName, null, destination, ascending);
  }

  public List<ExportLog> getExportLogs(String entityTypeName, String identifier, String destination, boolean ascending) {
    ExportLog template = ExportLog.Builder.newLog().type(entityTypeName).identifier(identifier).destination(destination).build();
    return getPersistenceManager().match(template, new SortingClause("exportDate", ascending));
  }

  public ExportLog getLastExportLog(String entityTypeName, String identifier) {
    return getLastExportLog(entityTypeName, identifier, null);
  }

  public ExportLog getLastExportLog(String entityTypeName, String identifier, String destination) {
    List<ExportLog> exportLogs = getExportLogs(entityTypeName, identifier, destination, false);
    return !exportLogs.isEmpty() ? exportLogs.get(0) : null;
  }

}
