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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.core.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnyxDataPurge {

  private static final Logger log = LoggerFactory.getLogger(OnyxDataPurge.class);

  private ExportLogService exportLogService;

  private UserSessionService userSessionService;

  private List<OnyxDataExportDestination> purgeConfiguration;

  public void setPurgeConfiguration(List<OnyxDataExportDestination> purgeConfiguration) {
    this.purgeConfiguration = purgeConfiguration;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public void setExportLogService(ExportLogService exportLogService) {
    this.exportLogService = exportLogService;
  }

  public List<Participant> getParticipantsToPurge() throws Exception {
    List<Participant> result = new ArrayList<Participant>();

    long exportStartTime = new Date().getTime();

    log.info("Starting export to configured destinations.");

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(OnyxDataExportDestination purge : purgeConfiguration) {
        for(ValueTable table : datasource.getValueTables()) {

          // Apply all filters to ValueTable for current OnyxDestination.
          // ValueTable filteredCollection = new FilteredValueTable(table, null, null);
          ValueTable filteredCollection = table;

        }

      }

    }

    long exportEndTime = new Date().getTime();

    log.info("Exported [{}] interview(s) in [{}ms] to [{}] destination(s).", new Object[] { 0, exportEndTime - exportStartTime, 0 });
    return result;
  }

}
