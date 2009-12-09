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

import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.engine.variable.CaptureAndExportStrategy;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for Jade CaptureDateRangeStrategies.
 */
public abstract class AbstractJadeCaptureAndExportStrategy implements CaptureAndExportStrategy {
  //
  // Instance Variables
  //

  @Autowired(required = true)
  protected ExperimentalConditionService experimentalConditionService;

  @Autowired(required = true)
  protected ExportLogService exportLogService;

  //
  // CaptureDateRangeStrategy Methods
  //

  public abstract String getEntityType();

  public Date getCaptureStartDate(String entityIdentifier) {
    List<ExperimentalCondition> experimentalConditions = getExperimentalConditions(entityIdentifier);
    return !experimentalConditions.isEmpty() ? experimentalConditions.get(0).getTime() : null;
  }

  public Date getCaptureEndDate(String entityIdentifier) {
    List<ExperimentalCondition> experimentalConditions = getExperimentalConditions(entityIdentifier);
    return !experimentalConditions.isEmpty() ? experimentalConditions.get(experimentalConditions.size() - 1).getTime() : null;
  }

  public abstract boolean isExported(String entityIdentifier);

  //
  // Methods
  //

  public void setExperimentalConditionService(ExperimentalConditionService experimentalConditionService) {
    this.experimentalConditionService = experimentalConditionService;
  }

  public void setExportLogService(ExportLogService exportLogService) {
    this.exportLogService = exportLogService;
  }

  protected abstract List<ExperimentalCondition> getExperimentalConditions(String entityIdentifier);
}
