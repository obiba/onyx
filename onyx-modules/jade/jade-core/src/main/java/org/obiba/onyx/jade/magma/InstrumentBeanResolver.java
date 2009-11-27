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
    return Instrument.class.equals(type) || ExperimentalCondition.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(type.equals(Instrument.class)) {
      return resolveInstrument(valueSet);
    } else if(type.equals(ExperimentalCondition.class)) {
      return resolveExperimentalConditions(valueSet);
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
}
