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

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.magma.AbstractOnyxBeanResolver;
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

  //
  // AbstractOnyxBeanResolver Methods
  //

  public boolean resolves(Class<?> type) {
    return Instrument.class.equals(type) || InstrumentCalibration.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(type.equals(Instrument.class)) {
      return resolveInstrument(valueSet);
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
}
