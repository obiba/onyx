/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.test.AbstractMagmaTest;
import org.springframework.core.io.ClassPathResource;

/**
 * Unit tests for {@link DefaultCustomVariableRegistry}.
 */
public class DefaultCustomVariablesRegistryTest extends AbstractMagmaTest {
  //
  // Instance Variables
  //

  private DefaultCustomVariablesRegistry sut;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    sut = new DefaultCustomVariablesRegistry();
    sut.setResource(new ClassPathResource("DefaultCustomVariablesRegistryTest/custom-variables.xml"));
    sut.initSourceMap();
  }

  //
  // Test Methods
  //

  @Test
  public void testInitSourceMap_InitializesAllVariables() {
    Set<VariableValueSource> customParticipantVariables = sut.getVariables("Participants");
    assertNotNull(customParticipantVariables);
    assertEquals(1, customParticipantVariables.size());
    assertContainsVariable(customParticipantVariables, "Admin.Participant.PostalCodePrefix");

    Set<VariableValueSource> customCiVariables = sut.getVariables("CIPreliminaryQuestionnaire");
    assertNotNull(customCiVariables);
    assertEquals(10, customCiVariables.size());
    assertContainsVariable(customCiVariables, "BP_CI");
    assertContainsVariable(customCiVariables, "BD_CI");
    assertContainsVariable(customCiVariables, "GS_CI");
    assertContainsVariable(customCiVariables, "SH_CI");
    assertContainsVariable(customCiVariables, "WH_CI");
    assertContainsVariable(customCiVariables, "SP_CI");
    assertContainsVariable(customCiVariables, "BI_CI");
    assertContainsVariable(customCiVariables, "WT_CI");
    assertContainsVariable(customCiVariables, "AS_CI");
    assertContainsVariable(customCiVariables, "BSC_CI");
  }

  @Test
  public void testGetVariables_ReturnsEmptySetForTableWithNoCustomVariables() {
    Set<VariableValueSource> sources = sut.getVariables("TableWithNoCustomVariables");
    assertNotNull(sources);
    assertEquals(0, sources.size());
  }

  //
  // Helper Methods
  //

  private void assertContainsVariable(Set<VariableValueSource> sources, String variableName) {
    for(VariableValueSource source : sources) {
      if(source.getVariable().getName().equals(variableName)) {
        return;
      }
    }
    fail("does not contain variable " + variableName);
  }
}
