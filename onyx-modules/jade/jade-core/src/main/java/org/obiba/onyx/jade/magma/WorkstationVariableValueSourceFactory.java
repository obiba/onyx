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
import java.util.Set;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.magma.DataTypes;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.obiba.onyx.magma.VariableLocalizedAttributeVisitor;

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

  private ExperimentalConditionService experimentalConditionService;

  private ValueSetBeanResolver beanResolver;

  private OnyxAttributeHelper attributeHelper;

  //
  // VariableValueSourceFactory Methods
  //

  public Set<VariableValueSource> createSources(String collection) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    // Create source for Workstation name variable.
    sources.add(createWorkstationSource(collection));

    // Create sources for (non-instrument) experimental conditions.
    sources.addAll(createExperimentalConditionSources(collection, beanResolver));

    return sources;
  }

  //
  // Methods
  //

  public void setExperimentalConditionService(ExperimentalConditionService experimentalConditionService) {
    this.experimentalConditionService = experimentalConditionService;
  }

  public void setBeanResolver(ValueSetBeanResolver beanResolver) {
    this.beanResolver = beanResolver;
  }

  public void setAttributeHelper(OnyxAttributeHelper attributeHelper) {
    this.attributeHelper = attributeHelper;
  }

  private VariableValueSource createWorkstationSource(final String collection) {
    return new VariableValueSource() {

      public Variable getVariable() {
        Variable.Builder builder = new Variable.Builder(collection, WORKSTATION + '.' + "name", getValueType(), WORKSTATION);
        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        return getValueType().valueOf(valueSet.getVariableEntity().getIdentifier());
      }

      public ValueType getValueType() {
        // TODO Auto-generated method stub
        return TextType.get();
      }
    };
  }

  private Set<VariableValueSource> createExperimentalConditionSources(final String collection, ValueSetBeanResolver resolver) {
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

      sources.addAll(factory.createSources(collection, resolver));

      // Create sources for ExperimentalCondition attributes.
      sources.addAll(createExperimentalConditionAttributeSources(collection, experimentalConditionLog, resolver));
    }

    return sources;
  }

  private Set<VariableValueSource> createExperimentalConditionAttributeSources(final String collection, ExperimentalConditionLog experimentalConditionLog, ValueSetBeanResolver resolver) {
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

      sources.addAll(conditionAttributeSourceFactory.createSources(collection, resolver));
    }

    return sources;
  }
}
