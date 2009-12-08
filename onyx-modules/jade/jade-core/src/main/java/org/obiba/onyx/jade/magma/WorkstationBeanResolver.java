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
import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.magma.AbstractOnyxBeanResolver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ValueSetBeanResolver for Workstation-related beans.
 */
public class WorkstationBeanResolver extends AbstractOnyxBeanResolver {
  //
  // Instances
  //

  @Autowired(required = true)
  private ExperimentalConditionService experimentalConditionService;

  //
  // AbstractOnyxBeanResolver Methods
  //

  public boolean resolves(Class<?> type) {
    return ExperimentalCondition.class.equals(type) || ExperimentalConditionValue.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(ExperimentalCondition.class.equals(type)) {
      resolveExperimentalCondition(valueSet, variable);
    } else if(ExperimentalConditionValue.class.equals(type)) {
      resolveExperimentalConditionValue(valueSet, variable);
    }
    return null;
  }

  //
  // Methods
  //

  public void setExperimentalConditionService(ExperimentalConditionService experimentalConditionService) {
    this.experimentalConditionService = experimentalConditionService;
  }

  protected List<ExperimentalCondition> resolveExperimentalCondition(ValueSet valueSet, Variable variable) {
    String workstationName = valueSet.getVariableEntity().getIdentifier();
    String experimentalConditionLogName = extractVariableNameElement(variable.getName(), 0);

    ExperimentalCondition template = new ExperimentalCondition();
    template.setName(experimentalConditionLogName);
    template.setWorkstation(workstationName);

    return experimentalConditionService.getExperimentalConditions(template);
  }

  protected List<ExperimentalConditionValue> resolveExperimentalConditionValue(ValueSet valueSet, Variable variable) {
    List<ExperimentalConditionValue> experimentalConditionValues = new ArrayList<ExperimentalConditionValue>();

    List<ExperimentalCondition> experimentalConditions = resolveExperimentalCondition(valueSet, variable);
    if(!experimentalConditions.isEmpty()) {
      String experimentalConditionAttributeName = extractVariableNameElement(variable.getName(), 1);
      if(experimentalConditionAttributeName != null) {
        for(ExperimentalCondition experimentalCondition : experimentalConditions) {
          for(ExperimentalConditionValue value : experimentalCondition.getExperimentalConditionValues()) {
            if(value.getAttributeName().equals(experimentalConditionAttributeName)) {
              experimentalConditionValues.add(value);
            }
          }
        }
      }
    }
    return experimentalConditionValues;
  }

  private String extractVariableNameElement(String variableName, int index) {
    String[] variableNameElements = variableName.split("\\.");
    if(index < variableNameElements.length) {
      return variableNameElements[index];
    }
    return null;
  }
}
