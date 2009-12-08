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
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.Variable.BuilderVisitor;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.magma.CategoryLocalizedAttributeVisitor;
import org.obiba.onyx.magma.DataTypes;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * VariableValueSourceFactory for creating VariableValueSources for Instruments.
 */
public class InstrumentVariableValueSourceFactory extends BeanVariableValueSourceFactory<Instrument> {
  //
  // Constants
  //

  public static final String INSTRUMENT = "Instrument";

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
  private ExportLogService exportLogService;

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
    setProperties(ImmutableSet.of("type", "name", "vendor", "model", "serialNumber", "barcode"));
    setPrefix(INSTRUMENT);
    sources = super.createSources();

    // Create Instrument captureStartDate and captureEndDate sources.
    sources.add(createInstrumentCaptureStartDateSource());
    sources.add(createInstrumentCaptureEndDateSource());

    // Create sources for instrument calibrations.
    sources.addAll(createInstrumentCalibrationSources());

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

  public void setExportLogService(ExportLogService exportLogService) {
    this.exportLogService = exportLogService;
  }

  private VariableValueSource createInstrumentCaptureStartDateSource() {
    return new VariableValueSource() {

      public Variable getVariable() {
        Variable.Builder builder = new Variable.Builder(INSTRUMENT + '.' + "captureStartDate", getValueType(), INSTRUMENT);
        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        String instrumentBarcode = valueSet.getVariableEntity().getIdentifier();
        ExportLog exportLog = exportLogService.getLastExportLog(INSTRUMENT, instrumentBarcode);
        if(exportLog != null) {
          List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getInstrumentCalibrationsRecordedAfter(instrumentBarcode, exportLog.getExportDate());
          return !experimentalConditions.isEmpty() ? getValueType().valueOf(experimentalConditions.get(0).getTime()) : getValueType().nullValue();
        } else {
          List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getInstrumentCalibrations(instrumentBarcode);
          return !experimentalConditions.isEmpty() ? getValueType().valueOf(experimentalConditions.get(0).getTime()) : getValueType().nullValue();
        }
      }

      public ValueType getValueType() {
        return DateType.get();
      }
    };
  }

  private VariableValueSource createInstrumentCaptureEndDateSource() {
    return new VariableValueSource() {

      public Variable getVariable() {
        Variable.Builder builder = new Variable.Builder(INSTRUMENT + '.' + "captureEndDate", getValueType(), INSTRUMENT);
        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        String instrumentBarcode = valueSet.getVariableEntity().getIdentifier();
        ExportLog exportLog = exportLogService.getLastExportLog(INSTRUMENT, instrumentBarcode);
        if(exportLog != null) {
          List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getInstrumentCalibrationsRecordedAfter(instrumentBarcode, exportLog.getExportDate());
          return !experimentalConditions.isEmpty() ? getValueType().valueOf(experimentalConditions.get(experimentalConditions.size() - 1).getTime()) : getValueType().nullValue();
        } else {
          List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getInstrumentCalibrations(instrumentBarcode);
          return !experimentalConditions.isEmpty() ? getValueType().valueOf(experimentalConditions.get(experimentalConditions.size() - 1).getTime()) : getValueType().nullValue();
        }
      }

      public ValueType getValueType() {
        return DateType.get();
      }
    };
  }

  private Set<VariableValueSource> createInstrumentCalibrationSources() {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(Map.Entry<String, InstrumentType> entry : instrumentService.getInstrumentTypes().entrySet()) {
      InstrumentType instrumentType = entry.getValue();

      for(InstrumentCalibration instrumentCalibration : experimentalConditionService.getInstrumentCalibrationsByType(instrumentType.getName())) {
        // Create sources for calibration time, workstation and user.
        BeanVariableValueSourceFactory<ExperimentalCondition> factory = new BeanVariableValueSourceFactory<ExperimentalCondition>("Instrument", ExperimentalCondition.class);
        factory.setPrefix(instrumentCalibration.getName());
        factory.setProperties(ImmutableSet.of("time", "workstation", "user.login"));
        factory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("user.login", "user").build());
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

      public Variable getVariable() {
        Variable.Builder builder = Variable.Builder.newVariable(instrumentCalibrationName + '.' + "instrument", getValueType(), "Instrument");
        attributeHelper.addLocalizedAttributes(builder, instrumentCalibrationName);
        OnyxAttributeHelper.addAttribute(builder, "instrumentType", instrumentTypeName);

        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        String instrumentBarcode = valueSet.getVariableEntity().getIdentifier();
        Instrument instrument = instrumentService.getInstrumentByBarcode(instrumentBarcode);
        if(instrument != null) {
          return (instrument.getType().equals(instrumentTypeName)) ? getValueType().valueOf(instrumentBarcode) : getValueType().nullValue();
        } else {
          return getValueType().nullValue();
        }
      }

      public ValueType getValueType() {
        return TextType.get();
      }

    };
  }

  private Set<VariableValueSource> createInstrumentCalibrationAttributeSources(InstrumentType instrumentType, InstrumentCalibration instrumentCalibration) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

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

  //
  // Inner Classes
  //

  static class InstrumentCalibrationAttributeVisitor implements BuilderVisitor {
    private OnyxAttributeHelper attributeHelper;

    private String instrumentTypeName;

    private String instrumentCalibrationName;

    private Attribute calibrationAttribute;

    public InstrumentCalibrationAttributeVisitor(OnyxAttributeHelper attributeHelper, String instrumentTypeName, String instrumentCalibrationName, Attribute calibrationAttribute) {
      this.attributeHelper = attributeHelper;
      this.instrumentTypeName = instrumentTypeName;
      this.instrumentCalibrationName = instrumentCalibrationName;
      this.calibrationAttribute = calibrationAttribute;
    }

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