/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.util;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class VariableFinderTest {

  private static final Logger log = LoggerFactory.getLogger(VariableFinderTest.class);

  private IVariablePathNamingStrategy variablePathNamingStrategy = DefaultVariablePathNamingStrategy.getInstance("Study");

  private Variable root;

  @Before
  public void setUp() {

    root = new Variable(variablePathNamingStrategy.getRootName());
    Variable parent;
    Variable variable;
    Variable subvariable;

    // users

    parent = root.addVariable("Admin/User", variablePathNamingStrategy.getPathSeparator());

    parent.addVariable(new Variable("login").setDataType(DataType.TEXT).setKey("user"));
    parent.addVariable(new Variable("name").setDataType(DataType.TEXT)).addReference("user");

    // participants

    parent = root.addVariable("Admin/Participant", variablePathNamingStrategy.getPathSeparator());

    variable = new Variable("barcode").setDataType(DataType.TEXT);
    parent.addVariable(variable);

    subvariable = parent.addVariable(new Variable("name").setDataType(DataType.TEXT));
    parent.addVariable(new Variable("gender").setDataType(DataType.TEXT));

    // questionnaire

    parent = root.addVariable("HealthQuestionnaire", variablePathNamingStrategy.getPathSeparator());

    variable = new Variable("Participant_AGE").addCategories("Participant_AGE", "PNA", "DK");
    parent.addVariable(variable);

    subvariable = new Variable("OPEN_AGE").setDataType(DataType.INTEGER).setUnit("year");
    variable.addVariable(subvariable);

    parent = root.addVariable("HealthQuestionnaire/DATE_OF_BIRTH", variablePathNamingStrategy.getPathSeparator());

    variable = new Variable("DOB_YEAR").addCategories("DOB_YEAR", "PNA", "DK");
    parent.addVariable(variable);

    subvariable = new Variable("OPEN_YEAR").setDataType(DataType.INTEGER);
    variable.addVariable(subvariable);

    variable = new Variable("DOB_MONTH").addCategories("DOB_MONTH", "PNA", "DK");
    parent.addVariable(variable);

    subvariable = new Variable("OPEN_MONTH").setDataType(DataType.INTEGER);
    variable.addVariable(subvariable);

    variable = new Variable("DOB_DAY").addCategories("DOB_DAY", "PNA", "DK");
    parent.addVariable(variable);

    subvariable = new Variable("OPEN_DAY").setDataType(DataType.INTEGER);
    variable.addVariable(subvariable);

    // instruments

    parent = root.addVariable("StandingHeight", variablePathNamingStrategy.getPathSeparator());

    parent.addVariable(new Variable("InstrumentRun")).addVariable(new Variable("user").setDataType(DataType.TEXT).setKey("user"));
    parent.addVariable(new Variable("First_Height_Measurement").setDataType(DataType.DECIMAL));
    parent.addVariable(new Variable("Second_Height_Measurement").setDataType(DataType.DECIMAL));
  }

  @Test
  public void testFilterAndFind() {

    // dumps

    log.debug("\n" + VariableStreamer.toXML(root));

    VariableFinder finder = VariableFinder.getInstance(root, variablePathNamingStrategy);
    Assert.assertEquals(12, finder.filterVariables(VariableFinder.ALL_VARIABLES_XPATH).size());
    log.debug("===============");
    Assert.assertEquals(5, finder.filterVariables(VariableFinder.ALL_ADMIN_VARIABLES_XPATH).size());
    log.debug("===============");
    Assert.assertEquals(2, finder.filterKeyVariables("user").size());
    log.debug("===============");
    Assert.assertEquals(1, finder.filterReferenceVariables("user").size());
    log.debug("===============");
    Assert.assertEquals(1, finder.filterReferenceVariables("name", "user").size());
    log.debug("===============");
    Assert.assertEquals(0, finder.filterReferenceVariables("email", "user").size());
    log.debug("===============");
    Assert.assertEquals(7, finder.filterComplementVariables(VariableFinder.ALL_ADMIN_VARIABLES_XPATH).size());
    log.info("===============");
    Assert.assertEquals(0, finder.filterIntersection(finder.filter(VariableFinder.ALL_ADMIN_VARIABLES_XPATH), finder.filterComplement(VariableFinder.ALL_ADMIN_VARIABLES_XPATH)).size());

    // search
    Variable searchedEntity = VariableFinder.getInstance(root, variablePathNamingStrategy).findVariable("/Study/HealthQuestionnaire/DATE_OF_BIRTH/DOB_MONTH/OPEN_MONTH");
    Assert.assertNotNull(searchedEntity);
    Assert.assertEquals("OPEN_MONTH", searchedEntity.getName());

    searchedEntity = VariableFinder.getInstance(root, variablePathNamingStrategy).findVariable("/Study/HealthQuestionnaire/DATE_OF_BIRTH/OPEN_MONTH");
    Assert.assertNull(searchedEntity);

    searchedEntity = VariableFinder.getInstance(root, variablePathNamingStrategy).findVariable("/AnotherStudy/HealthQuestionnaire/DATE_OF_BIRTH/DOB_MONTH/OPEN_MONTH");
    Assert.assertNull(searchedEntity);

  }

}
