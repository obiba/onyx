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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.engine.variable.Attribute;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class VariableStreamerTest {

  private static final Logger log = LoggerFactory.getLogger(VariableStreamerTest.class);

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  private Variable root;

  @Before
  public void setUp() {
    variablePathNamingStrategy = DefaultVariablePathNamingStrategy.getInstance("Study");

    root = new Variable(variablePathNamingStrategy.getRootName());
    Variable parent;
    Variable variable;
    Variable subvariable;

    // users

    parent = root.addVariable("Admin.User", variablePathNamingStrategy);
    parent.setDataType(DataType.TEXT).setRepeatable(true);
    parent.addVariable(new Variable("login").setDataType(DataType.TEXT));
    parent.addVariable(new Variable("name").setDataType(DataType.TEXT));

    // participants

    parent = root.addVariable("Admin.Participant", variablePathNamingStrategy);

    variable = new Variable("barcode").setDataType(DataType.TEXT);
    parent.addVariable(variable);

    subvariable = parent.addVariable(new Variable("name").setDataType(DataType.TEXT));
    parent.addVariable(new Variable("gender").setDataType(DataType.TEXT));

    // questionnaire

    parent = root.addVariable("HealthQuestionnaire", variablePathNamingStrategy);

    variable = new Variable("Participant_AGE").addCategories("Participant_AGE", "PNA", "DK");
    parent.addVariable(variable);

    subvariable = new Variable("OPEN_AGE").setDataType(DataType.INTEGER).setUnit("year");
    variable.addVariable(subvariable);

    parent = root.addVariable("HealthQuestionnaire.DATE_OF_BIRTH", variablePathNamingStrategy);
    parent.addAttributes(new Attribute("label", Locale.ENGLISH, "What is your date of birth ?"), new Attribute("label", Locale.FRENCH, "Quelle est votre date de naissance ?"));

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

    parent = root.addVariable("StandingHeight", variablePathNamingStrategy);

    parent.addVariable(new Variable("InstrumentRun")).addVariable(new Variable("user").setDataType(DataType.TEXT));
    parent.addVariable(new Variable("First_Height_Measurement").setDataType(DataType.DECIMAL));
    parent.addVariable(new Variable("Second_Height_Measurement").setDataType(DataType.DECIMAL));
  }

  @Test
  public void testXMLStream() {
    String xml = VariableStreamer.toXML(root);
    log.info(xml);

    Variable variable = VariableStreamer.fromXML(new ByteArrayInputStream(xml.getBytes()));

    String xml2 = VariableStreamer.toXML(variable);
    // log.info(xml2);
    Assert.assertEquals(xml, xml2);
  }

  @Test
  public void testCsvOutputStream() {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    VariableStreamer.toCSV(root, os, variablePathNamingStrategy);
    Assert.assertEquals(true, os.size() > 0);
    log.info(os.toString());
  }

  @Test
  public void testVariableDataStreaming() {
    VariableDataSet variableDataSet = new VariableDataSet();
    Calendar cal = new GregorianCalendar();
    cal.set(Calendar.YEAR, 2009);
    cal.set(Calendar.MONTH, 6);
    cal.set(Calendar.DAY_OF_MONTH, 15);
    variableDataSet.setExportDate(cal.getTime());
    variableDataSet.addVariableData(new VariableData("Onyx.blabla", DataBuilder.buildBoolean(true)));

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    VariableStreamer.toXML(variableDataSet, os);
    Assert.assertEquals(true, os.size() > 0);
    log.info(os.toString());

    variableDataSet = VariableStreamer.fromXML(new ByteArrayInputStream(os.toByteArray()));
    Assert.assertNotNull(variableDataSet);
    Assert.assertEquals(cal.getTime(), variableDataSet.getExportDate());
    Assert.assertEquals(1, variableDataSet.getVariableDatas().size());
    Assert.assertEquals("Onyx.blabla", variableDataSet.getVariableDatas().get(0).getVariablePath());
    Assert.assertEquals(1, variableDataSet.getVariableDatas().get(0).getDatas().size());
    Assert.assertEquals(DataBuilder.buildBoolean(true), variableDataSet.getVariableDatas().get(0).getDatas().get(0));

  }

  @Test
  public void testVariableDataWithNullValueStreaming() {
    VariableDataSet variableDataSet = new VariableDataSet();
    Calendar cal = new GregorianCalendar();
    cal.set(Calendar.YEAR, 2009);
    cal.set(Calendar.MONTH, 6);
    cal.set(Calendar.DAY_OF_MONTH, 15);
    variableDataSet.setExportDate(cal.getTime());
    variableDataSet.addVariableData(new VariableData("Onyx.blabla", new Data(DataType.TEXT, null)));

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    VariableStreamer.toXML(variableDataSet, os);
    Assert.assertEquals(true, os.size() > 0);
    log.info(os.toString());

    variableDataSet = VariableStreamer.fromXML(new ByteArrayInputStream(os.toByteArray()));
    Assert.assertNotNull(variableDataSet);
    Assert.assertEquals(cal.getTime(), variableDataSet.getExportDate());
    Assert.assertEquals(1, variableDataSet.getVariableDatas().size());
    Assert.assertEquals("Onyx.blabla", variableDataSet.getVariableDatas().get(0).getVariablePath());
    Assert.assertEquals(0, variableDataSet.getVariableDatas().get(0).getDatas().size());

  }

  @Test
  public void testCsvString() {
    log.info(VariableStreamer.toCSV(root, variablePathNamingStrategy));

  }

}
