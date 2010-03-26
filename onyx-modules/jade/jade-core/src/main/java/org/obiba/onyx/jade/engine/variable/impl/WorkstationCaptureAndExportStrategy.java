/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.variable.impl;

import java.util.List;

import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;

/**
 * Strategy for determining a Workstation's capture date range.
 */
public class WorkstationCaptureAndExportStrategy extends AbstractJadeCaptureAndExportStrategy {
  //
  // AbstractJadeCaptureDateRangeStrategy Methods
  //

  public String getEntityType() {
    return "Workstation";
  }

  public boolean isExported(String entityIdentifier) {
    return isExported(entityIdentifier, null);
  }

  public boolean isExported(String entityIdentifier, String destinationName) {
    // Return TRUE if new experimental conditions have been recorded since the last export, FALSE
    // otherwise (return FALSE also if no ExportLog exists for the workstation).
    ExportLog exportLog = (destinationName != null) ? exportLogService.getLastExportLog("Workstation", entityIdentifier, destinationName) : exportLogService.getLastExportLog("Workstation", entityIdentifier);
    if(exportLog != null) {
      List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getNonInstrumentRelatedConditionsRecordedAfter(entityIdentifier, exportLog.getExportDate());
      return experimentalConditions.isEmpty();
    }
    return false;
  }

  protected List<ExperimentalCondition> getExperimentalConditions(String entityIdentifier) {
    return experimentalConditionService.getNonInstrumentRelatedConditions(entityIdentifier);
  }
}
