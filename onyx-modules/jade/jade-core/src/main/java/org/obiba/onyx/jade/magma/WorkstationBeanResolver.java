/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.magma;

import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ValueSetBeanResolver for Workstation-related beans.
 */
public class WorkstationBeanResolver extends ExperimentalConditionBeanResolver {
  //
  // Instance Variables
  //

  @Autowired
  private ExportLogService exportLogService;

  //
  // ExperimentalConditionBeanResolver Methods
  //

  public boolean resolves(Class<?> type) {
    return super.resolves(type) || ExportLog.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(ExperimentalCondition.class.equals(type)) {
      return resolveExperimentalCondition(valueSet, variable);
    } else if(ExperimentalConditionValue.class.equals(type)) {
      return resolveExperimentalConditionValue(valueSet, variable);
    } else if(ExportLog.class.equals(type)) {
      return resolveExportLog(valueSet, variable);
    }
    return null;
  }

  protected List<ExperimentalCondition> resolveExperimentalCondition(ValueSet valueSet, Variable variable) {
    String workstationName = valueSet.getVariableEntity().getIdentifier();
    String experimentalConditionLogName = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 0);

    ExperimentalCondition template = new ExperimentalCondition();
    template.setName(experimentalConditionLogName);
    template.setWorkstation(workstationName);

    return experimentalConditionService.getExperimentalConditions(template);
  }

  //
  // Methods
  //

  public void setExportLogService(ExportLogService exportLogService) {
    this.exportLogService = exportLogService;
  }

  protected List<ExportLog> resolveExportLog(ValueSet valueSet, Variable variable) {
    return exportLogService.getExportLogs("Workstation", valueSet.getVariableEntity().getIdentifier(), null, true);
  }
}
