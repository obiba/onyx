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

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.magma.AbstractOnyxBeanResolver;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ValueSetBeanResolver for Instrument beans.
 */
public class InstrumentBeanResolver extends AbstractOnyxBeanResolver {
  //
  // Instance Variables
  //

  @Autowired(required = true)
  private InstrumentService instrumentService;

  @Autowired(required = true)
  private ExperimentalConditionService experimentalConditionService;

  //
  // AbstractOnyxBeanResolver Methods
  //

  public boolean resolves(Class<?> type) {
    return Instrument.class.equals(type) || ExperimentalCondition.class.equals(type) || ExperimentalConditionValue.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(type.equals(Instrument.class)) {
      return resolveInstrument(valueSet);
    } else if(type.equals(ExperimentalCondition.class)) {
      return resolveExperimentalConditions(valueSet);
    } else if(type.equals(ExperimentalConditionValue.class)) {
      return resolveExperimentalConditionValue(valueSet, variable);
    }

    return null;
  }

  //
  // Methods
  //

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  protected Instrument resolveInstrument(ValueSet valueSet) {
    String instrumentBarcode = valueSet.getVariableEntity().getIdentifier();
    return instrumentService.getInstrumentByBarcode(instrumentBarcode);
  }

  protected List<ExperimentalCondition> resolveExperimentalConditions(ValueSet valueSet) {
    Instrument instrument = resolveInstrument(valueSet);
    if(instrument != null) {
      ExperimentalCondition template = new ExperimentalCondition();
      ExperimentalConditionValue instrumentBarcode = new ExperimentalConditionValue();
      instrumentBarcode.setAttributeName(ExperimentalConditionService.INSTRUMENT_BARCODE);
      instrumentBarcode.setAttributeType(DataType.TEXT);
      instrumentBarcode.setData(DataBuilder.buildText(instrument.getBarcode()));
      template.addExperimentalConditionValue(instrumentBarcode);

      return experimentalConditionService.getExperimentalConditions(template);
    }
    return null;
  }

  protected List<ExperimentalConditionValue> resolveExperimentalConditionValue(ValueSet valueSet, Variable variable) {
    String experimentalConditionName = extractExperimentalConditionName(variable.getName());
    String experimentalConditionAttributeName = extractExperimentalConditionAttributeName(variable.getName());

    if(experimentalConditionName != null && experimentalConditionAttributeName != null) {
      Instrument instrument = resolveInstrument(valueSet);
      if(instrument != null) {
        ExperimentalCondition template = new ExperimentalCondition();
        template.setName(experimentalConditionName);
        ExperimentalConditionValue instrumentBarcode = new ExperimentalConditionValue();
        instrumentBarcode.setAttributeName(ExperimentalConditionService.INSTRUMENT_BARCODE);
        instrumentBarcode.setAttributeType(DataType.TEXT);
        instrumentBarcode.setData(DataBuilder.buildText(instrument.getBarcode()));
        template.addExperimentalConditionValue(instrumentBarcode);

        List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getExperimentalConditions(template);

        List<ExperimentalConditionValue> experimentalConditionValues = new ArrayList<ExperimentalConditionValue>();
        for(ExperimentalCondition experimentalCondition : experimentalConditions) {
          for(ExperimentalConditionValue value : experimentalCondition.getExperimentalConditionValues()) {
            if(value.getAttributeName().equals(experimentalConditionAttributeName)) {
              experimentalConditionValues.add(value);
            }
          }
        }
        return experimentalConditionValues;
      }
    }
    return null;
  }

  private String extractExperimentalConditionName(String variableName) {
    // Variable name format: Instrument.<CalibrationName>.(...)
    String[] variableNameParts = variableName.split("\\.");
    if(variableNameParts.length >= 2) {
      return variableNameParts[1];
    }
    return null;
  }

  private String extractExperimentalConditionAttributeName(String variableName) {
    // Variable name format: Instrument.<CalibrationName>.<CalibrationAttributeName>.(...)
    String[] variableNameParts = variableName.split("\\.");
    if(variableNameParts.length >= 3) {
      return variableNameParts[2];
    }
    return null;
  }
}
