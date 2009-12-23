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

import java.util.Collections;
import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.StringUtil;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ValueSetBeanResolver for Instrument beans.
 */
public class InstrumentBeanResolver extends ExperimentalConditionBeanResolver {
  //
  // Instance Variables
  //

  @Autowired
  private InstrumentService instrumentService;

  @Autowired
  private ExportLogService exportLogService;

  //
  // ExperimentalConditionBeanResolver Methods
  //

  public boolean resolves(Class<?> type) {
    return super.resolves(type) || Instrument.class.equals(type) || ExportLog.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(Instrument.class.equals(type)) {
      return resolveInstrument(valueSet);
    } else if(ExperimentalCondition.class.equals(type)) {
      return resolveExperimentalCondition(valueSet, variable);
    } else if(ExperimentalConditionValue.class.equals(type)) {
      return resolveExperimentalConditionValue(valueSet, variable);
    } else if(ExportLog.class.equals(type)) {
      return resolveExportLog(valueSet, variable);
    }

    return null;
  }

  protected List<ExperimentalCondition> resolveExperimentalCondition(ValueSet valueSet, Variable variable) {
    Instrument instrument = resolveInstrument(valueSet);
    if(instrument != null) {
      String experimentalConditionName = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 0);
      if(experimentalConditionName != null) {
        return getExperimentalConditions(experimentalConditionName, instrument.getBarcode());
      }
    }
    return Collections.emptyList();
  }

  //
  // Methods
  //

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public void setExportLogService(ExportLogService exportLogService) {
    this.exportLogService = exportLogService;
  }

  protected Instrument resolveInstrument(ValueSet valueSet) {
    String instrumentBarcode = valueSet.getVariableEntity().getIdentifier();
    return instrumentService.getInstrumentByBarcode(instrumentBarcode);
  }

  protected List<ExportLog> resolveExportLog(ValueSet valueSet, Variable variable) {
    return exportLogService.getExportLogs("Instrument", valueSet.getVariableEntity().getIdentifier(), null, true);
  }

  private List<ExperimentalCondition> getExperimentalConditions(String experimentalConditionName, String instrumentBarcode) {
    ExperimentalCondition template = new ExperimentalCondition();
    template.setName(experimentalConditionName);

    ExperimentalConditionValue experimentalConditionValue = new ExperimentalConditionValue();
    experimentalConditionValue.setAttributeName(ExperimentalConditionService.INSTRUMENT_BARCODE);
    experimentalConditionValue.setAttributeType(DataType.TEXT);
    experimentalConditionValue.setData(DataBuilder.buildText(instrumentBarcode));
    template.addExperimentalConditionValue(experimentalConditionValue);

    return experimentalConditionService.getExperimentalConditions(template);
  }
}
