/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 *
 */
public class StageDescriptorFactoryBeanTest {

  @Test
  public void testStageLoad() throws Exception {
    StageDescriptorFactoryBean bean = new StageDescriptorFactoryBean();
    bean.setStageDescriptor(new ClassPathResource("testStages.xml", StageDescriptorFactoryBeanTest.class));

    List<Stage> stages = (List<Stage>) bean.getObject();
    Assert.assertNotNull(stages);
    Assert.assertEquals(6, stages.size());
    for(Stage stage : stages) {
      Assert.assertNotNull(stage.getName());
    }
  }
}
