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
import java.util.Set;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.magma.DataTypes;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.obiba.onyx.magma.VariableLocalizedAttributeVisitor;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Factory for creating VariableValueSources for Workstations.
 */
public class WorkstationVariableValueSourceFactory implements VariableValueSourceFactory {
  //
  // Constants
  //

  public static final String WORKSTATION = "Workstation";

  //
  // Instances
  //

  @Autowired
  private ExperimentalConditionService experimentalConditionService;

  @Autowired
  private ExportLogService exportLogService;

  @Autowired
  private OnyxAttributeHelper attributeHelper;

  //
  // VariableValueSourceFactory Methods
  //

  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    // Create Workstation sources (name, captureStartDate, captureEndDate).
    sources.addAll(createWorkstationSources());

    // Create sources for (non-instrument) experimental conditions.
    sources.addAll(createExperimentalConditionSources());

    return sources;
  }

  //
  // Methods
  //

  public void setExperimentalConditionService(ExperimentalConditionService experimentalConditionService) {
    this.experimentalConditionService = experimentalConditionService;
  }

  public void setExportLogService(ExportLogService exportLogService) {
    this.exportLogService = exportLogService;
  }

  public void setAttributeHelper(OnyxAttributeHelper attributeHelper) {
    this.attributeHelper = attributeHelper;
  }

  private Set<VariableValueSource> createWorkstationSources() {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    sources.add(createWorkstationNameSource());
    sources.add(createWorkstationCaptureStartDateSource());
    sources.add(createWorkstationCaptureEndDateSource());

    return sources;
  }

  private VariableValueSource createWorkstationNameSource() {
    return new VariableValueSource() {

      public Variable getVariable() {
        Variable.Builder builder = new Variable.Builder(WORKSTATION + '.' + "name", getValueType(), WORKSTATION);
        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        return getValueType().valueOf(valueSet.getVariableEntity().getIdentifier());
      }

      public ValueType getValueType() {
        return TextType.get();
      }
    };
  }

  private VariableValueSource createWorkstationCaptureStartDateSource() {
    return new VariableValueSource() {

      public Variable getVariable() {
        Variable.Builder builder = new Variable.Builder(WORKSTATION + '.' + "captureStartDate", getValueType(), WORKSTATION);
        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        String workstationId = valueSet.getVariableEntity().getIdentifier();
        ExportLog exportLog = exportLogService.getLastExportLog(WORKSTATION, workstationId);
        if(exportLog != null) {
          List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getNonInstrumentRelatedConditionsRecordedAfter(workstationId, exportLog.getExportDate());
          return !experimentalConditions.isEmpty() ? getValueType().valueOf(experimentalConditions.get(0).getTime()) : null;
        } else {
          List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getNonInstrumentRelatedConditions(workstationId);
          return !experimentalConditions.isEmpty() ? getValueType().valueOf(experimentalConditions.get(0).getTime()) : null;
        }
      }

      public ValueType getValueType() {
        return DateType.get();
      }
    };
  }

  private VariableValueSource createWorkstationCaptureEndDateSource() {
    return new VariableValueSource() {

      public Variable getVariable() {
        Variable.Builder builder = new Variable.Builder(WORKSTATION + '.' + "captureEndDate", getValueType(), WORKSTATION);
        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        String workstationId = valueSet.getVariableEntity().getIdentifier();
        ExportLog exportLog = exportLogService.getLastExportLog(WORKSTATION, workstationId);
        if(exportLog != null) {
          List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getNonInstrumentRelatedConditionsRecordedAfter(workstationId, exportLog.getExportDate());
          return !experimentalConditions.isEmpty() ? getValueType().valueOf(experimentalConditions.get(experimentalConditions.size() - 1).getTime()) : null;
        } else {
          List<ExperimentalCondition> experimentalConditions = experimentalConditionService.getNonInstrumentRelatedConditions(workstationId);
          return !experimentalConditions.isEmpty() ? getValueType().valueOf(experimentalConditions.get(experimentalConditions.size() - 1).getTime()) : null;
        }
      }

      public ValueType getValueType() {
        return DateType.get();
      }
    };
  }

  private Set<VariableValueSource> createExperimentalConditionSources() {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(ExperimentalConditionLog experimentalConditionLog : experimentalConditionService.getExperimentalConditionLog()) {
      // Skip InstrumentCalibrations.
      if(experimentalConditionLog instanceof InstrumentCalibration) { // OR: isInstrumentedRelated()?
        continue;
      }

      // Create sources for ExperimentalCondition time and user.
      BeanVariableValueSourceFactory<ExperimentalCondition> factory = new BeanVariableValueSourceFactory<ExperimentalCondition>(WORKSTATION, ExperimentalCondition.class);
      factory.setPrefix(experimentalConditionLog.getName());
      factory.setProperties(ImmutableSet.of("time", "user.login"));
      factory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("user.login", "user").build());
      factory.setOccurrenceGroup(experimentalConditionLog.getName());

      sources.addAll(factory.createSources());

      // Create sources for ExperimentalCondition attributes.
      sources.addAll(createExperimentalConditionAttributeSources(experimentalConditionLog));
    }

    return sources;
  }

  private Set<VariableValueSource> createExperimentalConditionAttributeSources(ExperimentalConditionLog experimentalConditionLog) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(Attribute conditionAttribute : experimentalConditionLog.getAttributes()) {
      String propertyName = "data.value";
      Class<?> propertyType = DataTypes.valueTypeFor(conditionAttribute.getType()).getJavaClass();

      BeanVariableValueSourceFactory<ExperimentalConditionValue> conditionAttributeSourceFactory = new BeanVariableValueSourceFactory<ExperimentalConditionValue>(WORKSTATION, ExperimentalConditionValue.class);
      conditionAttributeSourceFactory.setPrefix(experimentalConditionLog.getName());
      conditionAttributeSourceFactory.setProperties(ImmutableSet.of(propertyName));
      conditionAttributeSourceFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put(propertyName, conditionAttribute.getName()).build());
      conditionAttributeSourceFactory.setPropertyNameToPropertyType(new ImmutableMap.Builder<String, Class<?>>().put(propertyName, propertyType).build());
      conditionAttributeSourceFactory.setOccurrenceGroup(experimentalConditionLog.getName());
      conditionAttributeSourceFactory.setVariableBuilderVisitors(ImmutableSet.of(new VariableLocalizedAttributeVisitor(attributeHelper, conditionAttribute.getName())));

      sources.addAll(conditionAttributeSourceFactory.createSources());
    }

    return sources;
  }
}
