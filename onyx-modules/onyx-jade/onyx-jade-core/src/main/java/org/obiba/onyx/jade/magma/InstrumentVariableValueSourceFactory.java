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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.Variable.BuilderVisitor;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.beans.BeanPropertyVariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentMeasurementType;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.engine.variable.impl.InstrumentCaptureAndExportStrategy;
import org.obiba.onyx.magma.CategoryLocalizedAttributeVisitor;
import org.obiba.onyx.magma.CustomVariablesRegistry;
import org.obiba.onyx.magma.DataTypes;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * VariableValueSourceFactory for creating VariableValueSources for Instruments.
 */
public class InstrumentVariableValueSourceFactory extends BeanVariableValueSourceFactory<Instrument> {
  //
  // Constants
  //

  public static final String INSTRUMENT = "Instrument";

  public static final String EXPORT_LOG = "exportLog";

  //
  // Instance Variables
  //

  @Autowired
  private OnyxAttributeHelper attributeHelper;

  @Autowired
  private InstrumentService instrumentService;

  @Autowired
  private ExperimentalConditionService experimentalConditionService;

  @Autowired
  private InstrumentCaptureAndExportStrategy instrumentCaptureAndExportStrategy;

  @Autowired
  private CustomVariablesRegistry customVariablesRegistry;

  //
  // Constructors
  //

  public InstrumentVariableValueSourceFactory() {
    super(INSTRUMENT, Instrument.class);
  }

  //
  // BeanVariableValueSourceFactory Methods
  //

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = null;

    // Create Instrument sources.
    setProperties(ImmutableSet.of("name", "vendor", "model", "serialNumber", "barcode"));
    setPrefix(INSTRUMENT);
    sources = super.createSources();

    // Create sources for instrument types.
    sources.add(createInstrumentTypesSource());

    // Create Instrument captureStartDate and captureEndDate sources.
    sources.add(createInstrumentCaptureStartDateSource());
    sources.add(createInstrumentCaptureEndDateSource());

    // Create sources for instrument calibrations.
    sources.addAll(createInstrumentCalibrationSources());

    // Create sources for export logs.
    sources.addAll(createExportLogSources());

    // Create sources for custom variables.
    sources.addAll(customVariablesRegistry.getVariables("Instruments"));

    return sources;
  }

  //
  // Methods
  //

  public void setAttributeHelper(OnyxAttributeHelper attributeHelper) {
    this.attributeHelper = attributeHelper;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public void setExperimentalConditionService(ExperimentalConditionService experimentalConditionService) {
    this.experimentalConditionService = experimentalConditionService;
  }

  public void setInstrumentCaptureAndExportStrategy(InstrumentCaptureAndExportStrategy instrumentCaptureAndExportStrategy) {
    this.instrumentCaptureAndExportStrategy = instrumentCaptureAndExportStrategy;
  }

  public void setCustomVariablesRegistry(CustomVariablesRegistry customVariablesRegistry) {
    this.customVariablesRegistry = customVariablesRegistry;
  }

  private VariableValueSource createInstrumentTypesSource() {
    Variable.Builder builder = new Variable.Builder(INSTRUMENT + '.' + "type", TextType.get(), INSTRUMENT);
    builder.repeatable();

    List<InstrumentType> instrumentTypes = new ArrayList<InstrumentType>(instrumentService.getInstrumentTypes().values());
    Collections.sort(instrumentTypes, new Comparator<InstrumentType>() {

      public int compare(InstrumentType o1, InstrumentType o2) {
        return o1.getName().compareTo(o2.getName());
      }

    });

    for(InstrumentType type : instrumentTypes) {
      Category.Builder catBuilder = Category.Builder.newCategory(type.getName());
      attributeHelper.addLocalizedAttributes(catBuilder, type.getName() + ".description");
      builder.addCategory(catBuilder.build());
    }

    return new BeanPropertyVariableValueSource(builder.build(), InstrumentMeasurementType.class, "type");
  }

  private VariableValueSource createInstrumentCaptureStartDateSource() {
    return new VariableValueSource() {

      @Override
      public String getName() {
        return INSTRUMENT + '.' + "captureStartDate";
      }

      @Override
      public Variable getVariable() {
        Variable.Builder builder = new Variable.Builder(getName(), getValueType(), INSTRUMENT);
        return builder.build();
      }

      @Override
      public Value getValue(ValueSet valueSet) {
        return getValueType().valueOf(instrumentCaptureAndExportStrategy.getCaptureStartDate(valueSet.getVariableEntity().getIdentifier()));
      }

      @Override
      public boolean supportVectorSource() {
        return false;
      }

      @Override
      public ValueType getValueType() {
        return DateTimeType.get();
      }

      @Override
      public VectorSource asVectorSource() {
        return null;
      }

    };
  }

  private VariableValueSource createInstrumentCaptureEndDateSource() {
    return new VariableValueSource() {

      @Override
      public String getName() {
        return INSTRUMENT + '.' + "captureEndDate";
      }

      @Override
      public Variable getVariable() {
        Variable.Builder builder = new Variable.Builder(getName(), getValueType(), INSTRUMENT);
        return builder.build();
      }

      @Override
      public Value getValue(ValueSet valueSet) {
        return getValueType().valueOf(instrumentCaptureAndExportStrategy.getCaptureEndDate(valueSet.getVariableEntity().getIdentifier()));
      }

      @Override
      public boolean supportVectorSource() {
        return false;
      }

      @Override
      public ValueType getValueType() {
        return DateTimeType.get();
      }

      @Override
      public VectorSource asVectorSource() {
        return null;
      }
    };
  }

  private Collection<VariableValueSource> createInstrumentCalibrationSources() {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(Map.Entry<String, InstrumentType> entry : instrumentService.getInstrumentTypes().entrySet()) {
      InstrumentType instrumentType = entry.getValue();

      for(InstrumentCalibration instrumentCalibration : experimentalConditionService.getInstrumentCalibrationsByType(instrumentType.getName())) {
        // Create sources for calibration time, workstation and user.
        BeanVariableValueSourceFactory<ExperimentalCondition> factory = new BeanVariableValueSourceFactory<ExperimentalCondition>("Instrument", ExperimentalCondition.class);
        factory.setPrefix(instrumentCalibration.getName());
        factory.setProperties(ImmutableSet.of("time", "workstation", "userName"));
        factory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("userName", "user").build());
        factory.setOccurrenceGroup(instrumentCalibration.getName());

        sources.addAll(factory.createSources());

        // Create source for calibrated instrument's barcode variable.
        sources.add(createCalibratedInstrumentBarcodeSource(instrumentType.getName(), instrumentCalibration.getName()));

        // Create sources for calibration attributes.
        sources.addAll(createInstrumentCalibrationAttributeSources(instrumentType, instrumentCalibration));
      }
    }

    return sources;
  }

  private VariableValueSource createCalibratedInstrumentBarcodeSource(final String instrumentTypeName, final String instrumentCalibrationName) {
    return new VariableValueSource() {

      @Override
      public String getName() {
        return instrumentCalibrationName + '.' + "instrument";
      }

      @Override
      public Variable getVariable() {
        Variable.Builder builder = Variable.Builder.newVariable(getName(), getValueType(), "Instrument");
        attributeHelper.addLocalizedAttributes(builder, instrumentCalibrationName);
        OnyxAttributeHelper.addAttribute(builder, "instrumentType", instrumentTypeName);

        return builder.build();
      }

      @Override
      public Value getValue(ValueSet valueSet) {
        String instrumentBarcode = valueSet.getVariableEntity().getIdentifier();
        Instrument instrument = instrumentService.getInstrumentByBarcode(instrumentBarcode);
        if(instrument != null) {
          return instrument.getTypes().contains(instrumentTypeName)
              ? getValueType().valueOf(instrumentBarcode) : getValueType().nullValue();
        } else {
          return getValueType().nullValue();
        }
      }

      @Override
      public boolean supportVectorSource() {
        return false;
      }

      @Override
      public ValueType getValueType() {
        return TextType.get();
      }

      @Override
      public VectorSource asVectorSource() {
        return null;
      }
    };
  }

  private Collection<VariableValueSource> createInstrumentCalibrationAttributeSources(InstrumentType instrumentType, InstrumentCalibration instrumentCalibration) {
    Collection<VariableValueSource> sources = Sets.newHashSet();

    for(Attribute calibrationAttribute : instrumentCalibration.getAttributes()) {
      String propertyName = "data.value";
      Class<?> propertyType = DataTypes.valueTypeFor(calibrationAttribute.getType()).getJavaClass();

      InstrumentCalibrationAttributeVisitor calibrationAttributeVisitor = new InstrumentCalibrationAttributeVisitor(attributeHelper, instrumentType.getName(), instrumentCalibration.getName(), calibrationAttribute);

      BeanVariableValueSourceFactory<ExperimentalConditionValue> attributeSourceFactory = new BeanVariableValueSourceFactory<ExperimentalConditionValue>("Instrument", ExperimentalConditionValue.class);
      attributeSourceFactory.setPrefix(instrumentCalibration.getName());
      attributeSourceFactory.setProperties(ImmutableSet.of(propertyName));
      attributeSourceFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put(propertyName, calibrationAttribute.getName()).build());
      attributeSourceFactory.setPropertyNameToPropertyType(new ImmutableMap.Builder<String, Class<?>>().put(propertyName, propertyType).build());
      attributeSourceFactory.setOccurrenceGroup(instrumentCalibration.getName());
      attributeSourceFactory.setVariableBuilderVisitors(ImmutableSet.of(calibrationAttributeVisitor));

      sources.addAll(attributeSourceFactory.createSources());
    }

    return sources;
  }

  private Collection<VariableValueSource> createExportLogSources() {
    BeanVariableValueSourceFactory<ExportLog> exportLogFactory = new BeanVariableValueSourceFactory<>(INSTRUMENT, ExportLog.class);
    exportLogFactory.setPrefix(INSTRUMENT + '.' + EXPORT_LOG);
    exportLogFactory.setOccurrenceGroup(EXPORT_LOG);
    exportLogFactory.setProperties(ImmutableSet.of("type", "destination", "captureStartDate", "captureEndDate", "exportDate"));

    return exportLogFactory.createSources();
  }

  //
  // Inner Classes
  //

  static class InstrumentCalibrationAttributeVisitor implements BuilderVisitor {
    private final OnyxAttributeHelper attributeHelper;

    private final String instrumentTypeName;

    private final String instrumentCalibrationName;

    private final Attribute calibrationAttribute;

    InstrumentCalibrationAttributeVisitor(OnyxAttributeHelper attributeHelper, String instrumentTypeName, String instrumentCalibrationName, Attribute calibrationAttribute) {
      this.attributeHelper = attributeHelper;
      this.instrumentTypeName = instrumentTypeName;
      this.instrumentCalibrationName = instrumentCalibrationName;
      this.calibrationAttribute = calibrationAttribute;
    }

    @Override
    public void visit(Builder builder) {
      // Add variable's localized attributes.
      attributeHelper.addLocalizedAttributes(builder, instrumentCalibrationName);

      // Add instrumentType attribute.
      OnyxAttributeHelper.addAttribute(builder, "instrumentType", instrumentTypeName);

      // If a set of allowed values are declared for the attribute, add them as categories.
      for(String categoryName : calibrationAttribute.getAllowedValues()) {
        Category.BuilderVisitor localizedAttributeVisitor = new CategoryLocalizedAttributeVisitor(attributeHelper, categoryName);
        builder.addCategory(categoryName, null, ImmutableSet.of(localizedAttributeVisitor));
      }
    }
  }
}
