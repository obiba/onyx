/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.condition.ExperimentalCondition;
import org.obiba.onyx.core.domain.condition.ExperimentalConditionValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ExperimentalConditionServiceTest extends BaseDefaultSpringContextTestCase {

  @Autowired(required = true)
  PersistenceManager persistenceManager;

  @Autowired(required = true)
  ExperimentalConditionService experimentalConditionService;

  ExperimentalCondition experimentalCondition;

  @Before
  public void setUp() throws Exception {
    experimentalCondition = new ExperimentalCondition();
  }

  @Test
  @Dataset
  public void testSaveExperimentalConditionWithZeroValues() {

    experimentalConditionService.save(experimentalCondition);

    ExperimentalCondition retrievedExperimentalCondition = persistenceManager.get(ExperimentalCondition.class, 1L);

    assertThat(experimentalCondition, is(retrievedExperimentalCondition));

  }

  @Test
  public void testSaveExperimentalConditionwithOneValue() throws Exception {
    ExperimentalConditionValue value = new ExperimentalConditionValue();
    value.setExperimentalCondition(experimentalCondition);
    value.setAttributeType(DataType.TEXT);
    value.setAttributeName("TEST_VALUE_ONE");
    Data data = new Data(DataType.TEXT);
    data.setValue("test value");
    value.setData(data);

    List<ExperimentalConditionValue> values = new ArrayList<ExperimentalConditionValue>();
    values.add(value);

    experimentalCondition.setExperimentalConditionValues(values);
    experimentalConditionService.save(experimentalCondition);
    ExperimentalCondition retrievedExperimentalCondition = persistenceManager.get(ExperimentalCondition.class, 2L);
    getExperimentalConditionValue(retrievedExperimentalCondition, "TEST_VALUE_ONE");
    assertThat(getExperimentalConditionValue(retrievedExperimentalCondition, "TEST_VALUE_ONE"), is(value));

  }

  private ExperimentalConditionValue getExperimentalConditionValue(ExperimentalCondition experimentalCondition, String name) {
    for(ExperimentalConditionValue experimentalConditionValue : experimentalCondition.getExperimentalConditionValues()) {
      if(experimentalConditionValue.getAttributeName().equals(name)) {
        return experimentalConditionValue;
      }
    }
    throw new IllegalStateException("The ExperimentalConditionValue with the name [" + name + "] does not exist.");
  }
}
