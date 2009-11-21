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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.VariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Factory for creating VariableValueSources for InstrumentRun variables.
 */
public class InstrumentRunVariableValueSourceFactory extends BeanVariableValueSourceFactory<InstrumentRun> {
  //
  // Constants
  //

  public static final String INSTRUMENT_RUN = "InstrumentRun";

  public static final String CONTRAINDICATION = "Contraindication";

  public static final String MEASURE = "Measure";

  //
  // Instance Variables
  //

  @Autowired(required = true)
  private InstrumentService instrumentService;

  //
  // Constructors
  //

  public InstrumentRunVariableValueSourceFactory() {
    super("Participant", InstrumentRun.class);
  }

  //
  // BeanVariableValueSourceFactory Methods
  //

  @Override
  public Set<VariableValueSource> createSources(String collection, ValueSetBeanResolver resolver) {
    Set<VariableValueSource> sources = null;

    for(Map.Entry<String, InstrumentType> entry : instrumentService.getInstrumentTypes().entrySet()) {
      InstrumentType instrumentType = entry.getValue();

      // Call superclass method to create the sources for InstrumentRun variables.
      String instrumentTypePrefix = instrumentType.getName();
      setPrefix(instrumentTypePrefix + '.' + INSTRUMENT_RUN);
      setProperties(ImmutableSet.of("user", "timeStart", "timeEnd", "otherContraindication"));
      sources = super.createSources(collection, resolver);

      // Call superclass method again to create the source the InstrumentRun.Contraindication.code variable.
      String ciVariablePrefix = instrumentType.getName() + '.' + INSTRUMENT_RUN + '.' + CONTRAINDICATION;
      setPrefix(ciVariablePrefix);
      setProperties(ImmutableSet.of("contraindicationCode"));
      setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("contraindicationCode", "code").build());
      sources.addAll(super.createSources(collection, resolver));

      // Add source for InstrumentRun.Contraindication.type variable.
      sources.addAll(createContraindicationTypeSource(collection, ciVariablePrefix, resolver));

      // Add sources for instrument parameter variables.
      sources.addAll(createInstrumentParameterSources(collection, instrumentTypePrefix, resolver, instrumentType));
    }

    return sources;
  }

  //
  // Methods
  //

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  private Set<VariableValueSource> createContraindicationTypeSource(String collection, String prefix, ValueSetBeanResolver resolver) {
    BeanVariableValueSourceFactory<Contraindication> delegateFactory = new BeanVariableValueSourceFactory<Contraindication>("Participant", Contraindication.class);
    delegateFactory.setPrefix(prefix);
    delegateFactory.setProperties(ImmutableSet.of("type"));

    return delegateFactory.createSources(collection, resolver);
  }

  private Set<VariableValueSource> createInstrumentParameterSources(String collection, String instrumentTypePrefix, ValueSetBeanResolver resolver, InstrumentType instrumentType) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    List<InstrumentParameter> instrumentParameters = instrumentType.getInstrumentParameters();
    if(instrumentParameters.size() > 0) {
      for(InstrumentParameter instrumentParameter : instrumentParameters) {
        BeanVariableValueSourceFactory<InstrumentRunValue> delegateFactory = new BeanVariableValueSourceFactory<InstrumentRunValue>("Participant", InstrumentRunValue.class);
        delegateFactory.setProperties(ImmutableSet.of("data.value"));

        if(instrumentType.isRepeatable() && instrumentParameter instanceof InstrumentOutputParameter && !instrumentParameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED)) {
          // Add sources for the measure variables (user, time, instrumentBarcode).
          String measurePrefix = instrumentTypePrefix + '.' + MEASURE;
          sources.addAll(createMeasureSources(collection, measurePrefix, resolver));

          // Add source for the measure's instrument parameter variable.
          delegateFactory.setPrefix(instrumentTypePrefix + '.' + MEASURE + '.' + instrumentParameter.getCode());
          delegateFactory.setOccurrenceGroup(MEASURE);
          sources.addAll(delegateFactory.createSources(collection, resolver));
        } else {
          // Add source for the instrument parameter.
          delegateFactory.setPrefix(instrumentTypePrefix + '.' + instrumentParameter.getCode());
          sources.addAll(delegateFactory.createSources(collection, resolver));
        }
      }
    }

    return sources;
  }

  private Set<VariableValueSource> createMeasureSources(String collection, String measurePrefix, ValueSetBeanResolver resolver) {
    BeanVariableValueSourceFactory<Measure> delegateFactory = new BeanVariableValueSourceFactory<Measure>("Participant", Measure.class);
    delegateFactory.setPrefix(measurePrefix);
    delegateFactory.setOccurrenceGroup(MEASURE);
    delegateFactory.setProperties(ImmutableSet.of("user", "time", "instrumentBarcode"));

    return delegateFactory.createSources(collection, resolver);
  }
}
