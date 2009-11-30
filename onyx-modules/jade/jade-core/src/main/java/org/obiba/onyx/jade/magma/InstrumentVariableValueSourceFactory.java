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
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.magma.CategoryLocalizedAttributeVisitor;
import org.obiba.onyx.magma.DataTypes;
import org.obiba.onyx.magma.OnyxAttributeHelper;

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

  private OnyxAttributeHelper attributeHelper;

  private InstrumentService instrumentService;

  private ExperimentalConditionService experimentalConditionService;

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
  public Set<VariableValueSource> createSources(String collection, ValueSetBeanResolver resolver) {
    Set<VariableValueSource> sources = null;

    // Create Instrument sources.
    setProperties(ImmutableSet.of("type", "name", "vendor", "model", "serialNumber", "barcode"));
    setPrefix(INSTRUMENT);
    sources = super.createSources(collection, resolver);

    // Create sources for instrument calibrations.
    sources.addAll(createInstrumentCalibrationSources(collection, resolver));

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

  private Set<VariableValueSource> createInstrumentCalibrationSources(String collection, ValueSetBeanResolver resolver) {
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

        sources.addAll(factory.createSources(collection, resolver));

        // Create source for calibrated instrument's barcode variable.
        sources.add(createCalibratedInstrumentBarcodeSource(collection, instrumentType.getName(), instrumentCalibration.getName()));

        // Create sources for calibration attributes.
        for(int i = 0; i < instrumentCalibration.getAttributes().size(); i++) {
          Attribute calibrationAttribute = instrumentCalibration.getAttributes().get(i);
          String propertyName = "attributes[" + i + "]";

          ImmutableSet.Builder<String> propertySetBuilder = new ImmutableSet.Builder<String>();
          ImmutableMap.Builder<String, String> nameMapBuilder = new ImmutableMap.Builder<String, String>();
          ImmutableMap.Builder<String, Class<?>> mappedPropertyTypeBuilder = new ImmutableMap.Builder<String, Class<?>>();

          propertySetBuilder.add(propertyName);
          nameMapBuilder.put(propertyName, calibrationAttribute.getName());
          mappedPropertyTypeBuilder.put("attributes", DataTypes.valueTypeFor(calibrationAttribute.getType()).getJavaClass());

          InstrumentCalibrationAttributeVisitor calibrationAttributeVisitor = new InstrumentCalibrationAttributeVisitor(attributeHelper, instrumentType.getName(), instrumentCalibration.getName(), calibrationAttribute);

          BeanVariableValueSourceFactory<ExperimentalCondition> attributeSourceFactory = new BeanVariableValueSourceFactory<ExperimentalCondition>("Instrument", ExperimentalCondition.class);
          attributeSourceFactory.setPrefix(instrumentCalibration.getName());
          attributeSourceFactory.setProperties(propertySetBuilder.build());
          attributeSourceFactory.setPropertyNameToVariableName(nameMapBuilder.build());
          attributeSourceFactory.setMappedPropertyType(mappedPropertyTypeBuilder.build());
          attributeSourceFactory.setOccurrenceGroup(instrumentCalibration.getName());
          attributeSourceFactory.setVariableBuilderVisitors(ImmutableSet.of(calibrationAttributeVisitor));

          sources.addAll(attributeSourceFactory.createSources(collection, resolver));
        }
      }
    }

    return sources;
  }

  private VariableValueSource createCalibratedInstrumentBarcodeSource(final String collection, final String instrumentTypeName, final String instrumentCalibrationName) {
    return new VariableValueSource() {

      public Variable getVariable() {
        Variable.Builder builder = Variable.Builder.newVariable(collection, instrumentCalibrationName + '.' + "instrument", getValueType(), "Instrument");
        attributeHelper.addLocalizedAttributes(builder, instrumentCalibrationName);
        OnyxAttributeHelper.addAttribute(builder, "instrumentType", instrumentTypeName);

        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        String instrumentBarcode = valueSet.getVariableEntity().getIdentifier();
        return getValueType().valueOf(instrumentBarcode);
      }

      public ValueType getValueType() {
        return TextType.get();
      }

    };
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
