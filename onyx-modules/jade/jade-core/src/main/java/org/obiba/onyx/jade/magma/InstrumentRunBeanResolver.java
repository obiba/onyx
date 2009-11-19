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
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.magma.AbstractOnyxBeanResolver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ValueSetBeanResolver for InstrumentRun beans.
 */
public class InstrumentRunBeanResolver extends AbstractOnyxBeanResolver {
  //
  // Constants
  //

  // TODO: This constant is also defined in InstrumentRunVariableValueSourceFactory.
  // Should define it once somewhere.
  public static final String INSTRUMENT_RUN = "InstrumentRun";

  //
  // Instance Variables
  //

  @Autowired(required = true)
  private InstrumentService instrumentService;

  @Autowired(required = true)
  private InstrumentRunService instrumentRunService;

  //
  // AbstractOnyxBeanResolver Methods
  //

  public boolean resolves(Class<?> type) {
    return InstrumentRun.class.equals(type) || Contraindication.class.equals(type) || Measure.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(type.equals(InstrumentRun.class)) {
      String instrumentTypeName = extractInstrumentTypeName(variable.getName());
      if(instrumentTypeName != null) {
        return getInstrumentRun(valueSet, instrumentTypeName);
      }
    } else if(type.equals(Contraindication.class)) {
      String instrumentTypeName = extractInstrumentTypeName(variable.getName());
      if(instrumentTypeName != null) {
        return getContraindication(valueSet, instrumentTypeName);
      }
    } else if(type.equals(Measure.class)) {
      // TODO: Implement this later, when "occurrence" related code stabilizes.
    }

    return null;
  }

  //
  // Methods
  //

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  protected InstrumentService getInstrumentService() {
    return instrumentService;
  }

  protected Contraindication getContraindication(ValueSet valueSet, String instrumentTypeName) {
    InstrumentType instrumentType = instrumentService.getInstrumentType(instrumentTypeName);
    if(instrumentType != null) {
      InstrumentRun instrumentRun = getInstrumentRun(valueSet, instrumentTypeName);
      if(instrumentRun != null) {
        return instrumentType.getContraindication(instrumentRun.getContraindication());
      }
    }
    return null;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  protected InstrumentRunService getInstrumentRunService() {
    return instrumentRunService;
  }

  protected InstrumentRun getInstrumentRun(ValueSet valueSet, String instrumentTypeName) {
    Participant participant = getParticipant(valueSet);
    return instrumentRunService.getInstrumentRun(participant, instrumentTypeName);
  }

  private String extractInstrumentTypeName(String variableName) {
    String[] elements = variableName.split("\\.");
    for(int i = 0; i < elements.length - 1; i++) {
      if(elements[i].equals(INSTRUMENT_RUN)) {
        return elements[i + 1];
      }
    }

    return null;
  }
}
