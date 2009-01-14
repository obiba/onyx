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

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class VariableFinderTest {

  private static final Logger log = LoggerFactory.getLogger(VariableFinderTest.class);

  private IVariablePathNamingStrategy variablePathNamingStrategy = DefaultVariablePathNamingStrategy.getInstance("STUDY_NAME");

  private Variable root;

  @Before
  public void setUp() {

    root = new Variable(variablePathNamingStrategy.getRootName());
    Variable parent;
    Variable variable;
    Variable subvariable;

    // users

    parent = root.addVariable("ADMIN/USER", variablePathNamingStrategy.getPathSeparator());

    variable = new Variable("LOGIN").setDataType(DataType.TEXT);
    parent.addVariable(variable);

    VariableData[] userLogins = new VariableData[2];

    // participants

    parent = root.addVariable("ADMIN/PARTICIPANT", variablePathNamingStrategy.getPathSeparator());

    variable = new Variable("BARCODE").setDataType(DataType.TEXT);
    parent.addVariable(variable);

    subvariable = new Variable("NAME").setDataType(DataType.TEXT);
    Variable subvariable2 = new Variable("CONSENT").setDataType(DataType.BOOLEAN);
    parent.addVariable(subvariable);
    parent.addVariable(subvariable2);

    // questionnaire

    parent = root.addVariable("HealthQuestionnaire", variablePathNamingStrategy.getPathSeparator());

    variable = new Variable("PARTICIPANT_AGE").addCategories("PARTICIPANT_AGE", "PNA", "DK");
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

    variable = new Variable("First_Height_Measurement").setDataType(DataType.DECIMAL);
    parent.addVariable(variable);

    variable = new Variable("Second_Height_Measurement").setDataType(DataType.DECIMAL);
    parent.addVariable(variable);
  }

  @Test
  public void testXStream() {

    // dumps

    log.info("\n" + VariableStreamer.toXML(root));

    Assert.assertEquals(10, xquery(root, VariableFinder.ALL_VARIABLES_XPATH).size());
    log.info("===============");
    xquery(root, "//variable[@name='PARTICIPANT']/descendant::*");// | " + VariableFinder.ALL_VARIABLES_XPATH);
    log.info("===============");
    xquery(root, "//variable[@name='PARTICIPANT']/variable[@name='BARCODE'] | //variable[not(@name='PARTICIPANT')]//variable");

    // search
    Variable searchedEntity = VariableFinder.getInstance(root, variablePathNamingStrategy).findVariable("/STUDY_NAME/HealthQuestionnaire/DATE_OF_BIRTH/DOB_MONTH/OPEN_MONTH");
    Assert.assertNotNull(searchedEntity);
    Assert.assertEquals("OPEN_MONTH", searchedEntity.getName());

    searchedEntity = VariableFinder.getInstance(root, variablePathNamingStrategy).findVariable("/STUDY_NAME/HealthQuestionnaire/DATE_OF_BIRTH/OPEN_MONTH");
    Assert.assertNull(searchedEntity);

    searchedEntity = VariableFinder.getInstance(root, variablePathNamingStrategy).findVariable("/ANOTHER_STUDY_NAME/HealthQuestionnaire/DATE_OF_BIRTH/DOB_MONTH/OPEN_MONTH");
    Assert.assertNull(searchedEntity);

  }

  private List<Variable> xquery(Variable parent, String query) {
    return VariableFinder.getInstance(parent, variablePathNamingStrategy).filter(query);
  }

}
