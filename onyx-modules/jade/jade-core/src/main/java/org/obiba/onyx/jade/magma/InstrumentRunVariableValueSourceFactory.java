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

import org.obiba.magma.Category;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.Variable.BuilderVisitor;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.obiba.onyx.magma.StageAttributeVisitor;
import org.obiba.onyx.util.data.Data;
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

  @Autowired(required = true)
  private OnyxAttributeHelper attributeHelper;

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
      setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName())));
      String instrumentTypePrefix = instrumentType.getName();

      // Call superclass method to create the sources for InstrumentRun variables.
      String instrumentRunPrefix = instrumentTypePrefix + '.' + INSTRUMENT_RUN;
      setPrefix(instrumentRunPrefix);
      setProperties(ImmutableSet.of("user", "timeStart", "timeEnd", "otherContraindication"));
      sources = super.createSources(collection, resolver);

      // For non-repeatable instrument types, add source for instrument barcode variable.
      if(!instrumentType.isRepeatable()) {
        sources.add(createBarcodeSource(collection, instrumentRunPrefix, instrumentType, resolver));
      }

      // Call superclass method again to create the source the InstrumentRun.Contraindication.code variable.
      String ciVariablePrefix = instrumentType.getName() + '.' + INSTRUMENT_RUN + '.' + CONTRAINDICATION;
      setPrefix(ciVariablePrefix);
      setProperties(ImmutableSet.of("contraindicationCode"));
      setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("contraindicationCode", "code").build());
      sources.addAll(super.createSources(collection, resolver));

      // Add source for InstrumentRun.Contraindication.type variable.
      sources.add(createContraindicationTypeSource(collection, ciVariablePrefix, instrumentType, resolver));

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

  private VariableValueSource createBarcodeSource(String collection, String prefix, InstrumentType instrumentType, ValueSetBeanResolver resolver) {
    BeanVariableValueSourceFactory<InstrumentRun> delegateFactory = new BeanVariableValueSourceFactory<InstrumentRun>("Participant", InstrumentRun.class);
    delegateFactory.setPrefix(prefix);
    delegateFactory.setProperties(ImmutableSet.of("instrument.barcode"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("instrument.barcode", "instrumentBarcode").build());
    setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName())));

    return delegateFactory.createSources(collection, resolver).iterator().next();
  }

  private VariableValueSource createContraindicationTypeSource(String collection, String prefix, InstrumentType instrumentType, ValueSetBeanResolver resolver) {
    BeanVariableValueSourceFactory<Contraindication> delegateFactory = new BeanVariableValueSourceFactory<Contraindication>("Participant", Contraindication.class);
    delegateFactory.setPrefix(prefix);
    delegateFactory.setProperties(ImmutableSet.of("type"));
    setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName())));

    return delegateFactory.createSources(collection, resolver).iterator().next();
  }

  private Set<VariableValueSource> createInstrumentParameterSources(String collection, String instrumentTypePrefix, ValueSetBeanResolver resolver, InstrumentType instrumentType) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    List<InstrumentParameter> instrumentParameters = instrumentType.getInstrumentParameters();
    if(instrumentParameters.size() > 0) {
      for(InstrumentParameter instrumentParameter : instrumentParameters) {
        BeanVariableValueSourceFactory<InstrumentRunValue> delegateFactory = new BeanVariableValueSourceFactory<InstrumentRunValue>("Participant", InstrumentRunValue.class);
        delegateFactory.setProperties(ImmutableSet.of("captureMethod", "data.value"));
        delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName()), new InstrumentParameterAttributeVisitor(attributeHelper, instrumentType, instrumentParameter)));

        if(instrumentType.isRepeatable() && instrumentParameter instanceof InstrumentOutputParameter && !instrumentParameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED)) {
          // Add sources for the measure variables (user, time, instrumentBarcode).
          String measurePrefix = instrumentTypePrefix + '.' + MEASURE;
          sources.addAll(createMeasureSources(collection, measurePrefix, instrumentType, resolver));

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

  private Set<VariableValueSource> createMeasureSources(String collection, String measurePrefix, InstrumentType instrumentType, ValueSetBeanResolver resolver) {
    BeanVariableValueSourceFactory<Measure> delegateFactory = new BeanVariableValueSourceFactory<Measure>("Participant", Measure.class);
    delegateFactory.setPrefix(measurePrefix);
    delegateFactory.setOccurrenceGroup(MEASURE);
    delegateFactory.setProperties(ImmutableSet.of("user", "time", "instrumentBarcode"));
    setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName())));

    return delegateFactory.createSources(collection, resolver);
  }

  //
  // Inner Classes
  //

  static class InstrumentParameterAttributeVisitor implements BuilderVisitor {
    private OnyxAttributeHelper attributeHelper;

    private InstrumentType instrumentType;

    private InstrumentParameter instrumentParameter;

    public InstrumentParameterAttributeVisitor(OnyxAttributeHelper attributeHelper, InstrumentType instrumentType, InstrumentParameter instrumentParameter) {
      this.attributeHelper = attributeHelper;
      this.instrumentParameter = instrumentParameter;
    }

    public void visit(Builder builder) {
      // Add variable's localized attributes.
      attributeHelper.addLocalizedAttributes(builder, instrumentParameter.getCode());

      // Add YES and NO categories for interpretative parameters.
      if(instrumentParameter instanceof InterpretativeParameter) {
        builder.addCategory(InterpretativeParameter.YES, "1");
        builder.addCategory(InterpretativeParameter.NO, "0");
      }

      // For parameters with declared allowed values, add categories for those values.
      // Use a visitor to add each category's localized attributes.
      if(instrumentParameter.getAllowedValues().size() > 0) {
        int pos = 1;
        for(Data allowedValue : instrumentParameter.getAllowedValues()) {
          String code = allowedValue.getValueAsString();
          Category.BuilderVisitor localizedAttributeVisitor = new CategoryLocalizedAttributeVisitor(attributeHelper, code);
          builder.addCategory(code, Integer.toString(pos++), ImmutableSet.of(localizedAttributeVisitor));
        }
      }

      // Add validation attributes for parameters with integrity checks.
      if(instrumentParameter.getIntegrityChecks().size() > 0) {
        OnyxAttributeHelper.addValidationAttribute(builder, instrumentParameter.getIntegrityChecks().toString());
      }

      // For parameters with a data source, add source attribute.
      if(instrumentParameter.getDataSource() != null) {
        OnyxAttributeHelper.addSourceAttribute(builder, instrumentParameter.getDataSource().toString());
      }

      // For parameters with a condition, add condition attribute.
      if(instrumentParameter.getCondition() != null) {
        OnyxAttributeHelper.addConditionAttribute(builder, instrumentParameter.getCondition().toString());
      }

      // For parameters with a capture method, add a default capture method attribute.
      if(instrumentParameter.getCaptureMethod() != null) {
        OnyxAttributeHelper.addDefaultCaptureMethodAttribute(builder, instrumentParameter.getCaptureMethod().toString());
        if(instrumentParameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.AUTOMATIC)) {
          OnyxAttributeHelper.addIsManualCaptureAllowedAttribute(builder, instrumentParameter.isManualCaptureAllowed());
        }
      }

      // For parameters that are part of a repeatable measure, add occurrence count attribute.
      if(instrumentType.isRepeatable() && instrumentParameter instanceof InstrumentOutputParameter && !instrumentParameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED)) {
        OnyxAttributeHelper.addOccurrenceCountAttribute(builder, instrumentType.getExpectedMeasureCount().toString());
      }
    }
  }

  private static class CategoryLocalizedAttributeVisitor implements Category.BuilderVisitor {
    private OnyxAttributeHelper attributeHelper;

    private String code;

    public CategoryLocalizedAttributeVisitor(OnyxAttributeHelper variableHelper, String code) {
      this.attributeHelper = variableHelper;
      this.code = code;
    }

    public void visit(Category.Builder builder) {
      attributeHelper.addLocalizedAttributes(builder, code);
    }
  }
}
