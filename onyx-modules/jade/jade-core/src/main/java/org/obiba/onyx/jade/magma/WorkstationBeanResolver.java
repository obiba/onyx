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

import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
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
    return ExperimentalCondition.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(ExperimentalCondition.class.equals(type)) {
      resolveExperimentalCondition(valueSet);
    }
    return null;
  }

  //
  // Methods
  //

  public void setExperimentalConditionService(ExperimentalConditionService experimentalConditionService) {
    this.experimentalConditionService = experimentalConditionService;
  }

  protected List<ExperimentalCondition> resolveExperimentalCondition(ValueSet valueSet) {
    String workstationName = valueSet.getVariableEntity().getIdentifier();

    ExperimentalCondition template = new ExperimentalCondition();
    template.setWorkstation(workstationName);

    return experimentalConditionService.getExperimentalConditions(template);
  }
}
