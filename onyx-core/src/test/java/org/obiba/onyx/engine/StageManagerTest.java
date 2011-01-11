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
public class StageManagerTest {

  @Test
  public void testGetStages() throws Exception {
    StageManagerImpl stageManager = new StageManagerImpl();
    stageManager.setStageDescriptor(new ClassPathResource("testStages.xml", StageManagerTest.class));
    stageManager.afterPropertiesSet();

    List<Stage> stages = stageManager.getStages();
    Assert.assertNotNull(stages);
    Assert.assertEquals(6, stages.size());
    for(Stage stage : stages) {
      Assert.assertNotNull(stage.getName());
    }
  }

  @Test
  public void testGetStage() throws Exception {
    StageManagerImpl stageManager = new StageManagerImpl();
    stageManager.setStageDescriptor(new ClassPathResource("testStages.xml", StageManagerTest.class));
    stageManager.afterPropertiesSet();

    Stage stage = stageManager.getStage("Stage2");
    Assert.assertNotNull(stage);
    Assert.assertEquals(stage.getName(), "Stage2");
  }

  @Test
  public void testAddStage() throws Exception {
    StageManagerImpl stageManager = new StageManagerImpl();
    ClassPathResource stageDescriptor = new ClassPathResource("testStages.xml", StageManagerTest.class);
    stageManager.setStageDescriptor(stageDescriptor);
    stageManager.afterPropertiesSet();

    Stage stage = new Stage();
    stage.setName("NewStage");
    stage.setModule("Module1");
    ModuleDependencyCondition dependencyCondition = new ModuleDependencyCondition();
    dependencyCondition.setModuleName("Module2");
    dependencyCondition.setModuleRegistry(new ModuleRegistry());
    stage.setStageDependencyCondition(dependencyCondition);
    stageManager.addStage(stageManager.getStages().size(), stage);

    Assert.assertEquals(7, stageManager.getStages().size());
    Assert.assertNotNull(stageManager.getStage("NewStage"));

  }
}
