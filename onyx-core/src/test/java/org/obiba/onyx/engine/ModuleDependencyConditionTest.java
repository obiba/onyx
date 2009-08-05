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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ModuleDependencyConditionTest {

  private ModuleDependencyCondition jadeModuleDependencyCondition;

  private Module jadeModule;

  private Stage jadeWeightStage;

  private Stage jadeConclusion;

  @Before
  public void setUp() throws Exception {

    jadeModule = createMock(Module.class);
    ModuleRegistry moduleRegistry = new ModuleRegistry();
    expect(jadeModule.getName()).andReturn("Jade").anyTimes();
    jadeWeightStage = createStage("weight", "Jade", false);
    jadeConclusion = createStage("conclusion", "Jade", true);
    expect(jadeModule.getStages()).andReturn(Arrays.asList(new Stage[] { jadeWeightStage, jadeConclusion })).anyTimes();
    EasyMock.replay(jadeModule);
    moduleRegistry.registerModule(jadeModule);
    jadeModuleDependencyCondition = new ModuleDependencyCondition();
    jadeModuleDependencyCondition.setModuleName("Jade");
    jadeModuleDependencyCondition.setModuleRegistry(moduleRegistry);
  }

  @Test
  public void testIsDependantOnForOtherStageInModule() {
    Assert.assertTrue(jadeModuleDependencyCondition.isDependentOn(jadeConclusion, "weight"));
  }

  @Test
  public void testIsNotDependantOnStageOnWhichItIsBeingApplied() {
    Assert.assertFalse(jadeModuleDependencyCondition.isDependentOn(jadeConclusion, "conclusion"));
  }

  private Stage createStage(String stageName, String moduleName, boolean conclusion) {
    Stage stage = new Stage();
    stage.setName(stageName);
    stage.setModule(moduleName);
    stage.setInterviewConclusion(conclusion);
    return stage;
  }

}
