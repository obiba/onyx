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
import org.obiba.onyx.magma.CategoryLocalizedAttributeVisitor;
import org.obiba.onyx.magma.DataTypes;
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

  @Autowired
  private InstrumentService instrumentService;

  @Autowired
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
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = null;

    for(Map.Entry<String, InstrumentType> entry : instrumentService.getInstrumentTypes().entrySet()) {
      InstrumentType instrumentType = entry.getValue();
      setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName())));
      String instrumentTypePrefix = instrumentType.getName();

      // Call superclass method to create the sources for InstrumentRun variables.
      String instrumentRunPrefix = instrumentTypePrefix + '.' + INSTRUMENT_RUN;
      setPrefix(instrumentRunPrefix);
      setProperties(ImmutableSet.of("user.login", "timeStart", "timeEnd", "otherContraindication"));
      setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("user.login", "user").build());
      sources = super.createSources();

      // For non-repeatable instrument types, add source for instrument barcode variable.
      if(!instrumentType.isRepeatable()) {
        sources.add(createBarcodeSource(instrumentRunPrefix, instrumentType));
      }

      // Call superclass method again to create the source the InstrumentRun.Contraindication.code variable.
      String ciVariablePrefix = instrumentType.getName() + '.' + INSTRUMENT_RUN + '.' + CONTRAINDICATION;
      setPrefix(ciVariablePrefix);
      setProperties(ImmutableSet.of("contraindicationCode"));
      setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("contraindicationCode", "code").build());
      sources.addAll(super.createSources());

      // Add source for InstrumentRun.Contraindication.type variable.
      sources.add(createContraindicationTypeSource(ciVariablePrefix, instrumentType));

      // Add sources for instrument parameter variables.
      sources.addAll(createInstrumentParameterSources(instrumentTypePrefix, instrumentType));
    }

    return sources;
  }

  //
  // Methods
  //

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public void setAttributeHelper(OnyxAttributeHelper attributeHelper) {
    this.attributeHelper = attributeHelper;
  }

  private VariableValueSource createBarcodeSource(String prefix, InstrumentType instrumentType) {
    BeanVariableValueSourceFactory<InstrumentRun> delegateFactory = new BeanVariableValueSourceFactory<InstrumentRun>("Participant", InstrumentRun.class);
    delegateFactory.setPrefix(prefix);
    delegateFactory.setProperties(ImmutableSet.of("instrument.barcode"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("instrument.barcode", "instrumentBarcode").build());
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName())));

    return delegateFactory.createSources().iterator().next();
  }

  private VariableValueSource createContraindicationTypeSource(String prefix, InstrumentType instrumentType) {
    BeanVariableValueSourceFactory<Contraindication> delegateFactory = new BeanVariableValueSourceFactory<Contraindication>("Participant", Contraindication.class);
    delegateFactory.setPrefix(prefix);
    delegateFactory.setProperties(ImmutableSet.of("type"));
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName())));

    return delegateFactory.createSources().iterator().next();
  }

  private Set<VariableValueSource> createInstrumentParameterSources(String instrumentTypePrefix, InstrumentType instrumentType) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    List<InstrumentParameter> instrumentParameters = instrumentType.getInstrumentParameters();
    if(instrumentParameters.size() > 0) {
      for(InstrumentParameter instrumentParameter : instrumentParameters) {
        BeanVariableValueSourceFactory<InstrumentRunValue> delegateFactory = new BeanVariableValueSourceFactory<InstrumentRunValue>("Participant", InstrumentRunValue.class);
        delegateFactory.setProperties(ImmutableSet.of("data.value"));
        delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("data.value", instrumentParameter.getCode()).build());
        delegateFactory.setPropertyNameToPropertyType(getInstrumentParameterMappedPropertyType(instrumentParameter));
        delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName()), new InstrumentParameterVisitor(attributeHelper, instrumentType, instrumentParameter)));

        String captureMethodPrefix = null;
        if(instrumentType.isRepeatable() && instrumentParameter instanceof InstrumentOutputParameter && !instrumentParameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED)) {
          // Add sources for the measure's user, time and instrumentBarcode variables.
          String measurePrefix = instrumentTypePrefix + '.' + MEASURE;
          sources.addAll(createMeasureSources(measurePrefix, instrumentType));

          // Configure factory for an instrument parameter that is part of a measure.
          delegateFactory.setPrefix(measurePrefix);
          delegateFactory.setOccurrenceGroup(MEASURE);

          captureMethodPrefix = measurePrefix + '.' + instrumentParameter.getCode();
        } else {
          // Configure factory for an instrument parameter that is not part of a measure.
          delegateFactory.setPrefix(instrumentTypePrefix);

          captureMethodPrefix = instrumentTypePrefix + '.' + instrumentParameter.getCode();
        }

        // Add source for the instrument parameter's data.
        sources.addAll(delegateFactory.createSources());

        // Add source for the instrument parameter's capture method.
        sources.add(createCaptureMethodSource(captureMethodPrefix, instrumentType, instrumentParameter));
      }
    }

    return sources;
  }

  private Set<VariableValueSource> createMeasureSources(String measurePrefix, InstrumentType instrumentType) {
    BeanVariableValueSourceFactory<Measure> delegateFactory = new BeanVariableValueSourceFactory<Measure>("Participant", Measure.class);
    delegateFactory.setPrefix(measurePrefix);
    delegateFactory.setOccurrenceGroup(MEASURE);
    delegateFactory.setProperties(ImmutableSet.of("user.login", "time", "instrumentBarcode"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("user.login", "user").build());
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName())));

    return delegateFactory.createSources();
  }

  private VariableValueSource createCaptureMethodSource(String captureMethodPrefix, InstrumentType instrumentType, InstrumentParameter instrumentParameter) {
    BeanVariableValueSourceFactory<InstrumentRunValue> delegateFactory = new BeanVariableValueSourceFactory<InstrumentRunValue>("Participant", InstrumentRunValue.class);
    delegateFactory.setPrefix(captureMethodPrefix);
    delegateFactory.setProperties(ImmutableSet.of("captureMethod"));
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(instrumentType.getName())));

    return delegateFactory.createSources().iterator().next();
  }

  private Map<String, Class<?>> getInstrumentParameterMappedPropertyType(InstrumentParameter instrumentParameter) {
    Map<String, Class<?>> propertyType = new ImmutableMap.Builder<String, Class<?>>().put("data.value", DataTypes.valueTypeFor(instrumentParameter.getDataType()).getJavaClass()).build();
    return propertyType;
  }

  //
  // Inner Classes
  //

  static class InstrumentParameterVisitor implements BuilderVisitor {
    private OnyxAttributeHelper attributeHelper;

    private InstrumentType instrumentType;

    private InstrumentParameter instrumentParameter;

    public InstrumentParameterVisitor(OnyxAttributeHelper attributeHelper, InstrumentType instrumentType, InstrumentParameter instrumentParameter) {
      this.attributeHelper = attributeHelper;
      this.instrumentType = instrumentType;
      this.instrumentParameter = instrumentParameter;
    }

    public void visit(Builder builder) {
      // Add variable's localized attributes.
      attributeHelper.addLocalizedAttributes(builder, instrumentParameter.getCode());

      // Add variable's unit and mime type.
      builder.unit(instrumentParameter.getMeasurementUnit()).mimeType(instrumentParameter.getMimeType());

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
}
