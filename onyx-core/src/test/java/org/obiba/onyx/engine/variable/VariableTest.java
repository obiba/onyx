/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thoughtworks.xstream.XStream;

/**
 * 
 */
public class VariableTest {

  private static final Logger log = LoggerFactory.getLogger(VariableTest.class);

  private XStream xstream;

  private IVariablePathNamingStrategy pathNamingStrategy = DefaultVariablePathNamingStrategy.getInstance("STUDY_NAME");

  private Variable root;

  private VariableDataSet dataSet;

  @Before
  public void setUp() {
    initializeXStream();

    root = new Variable(pathNamingStrategy.getRootName());
    Variable parent;
    Variable variable;
    Variable subvariable;

    dataSet = new VariableDataSet();

    // users

    parent = root.addVariable("ADMIN/USER", pathNamingStrategy.getPathSeparator());

    variable = new Variable("LOGIN").setDataType(DataType.TEXT);
    parent.addVariable(variable);

    VariableData[] userLogins = new VariableData[2];
    userLogins[0] = dataSet.addVariableData(new VariableData(pathNamingStrategy.getPath(variable), DataBuilder.buildText("gina")));
    userLogins[1] = dataSet.addVariableData(new VariableData(pathNamingStrategy.getPath(variable), DataBuilder.buildText("alexander")));

    // participants

    parent = root.addVariable("ADMIN/PARTICIPANT", pathNamingStrategy.getPathSeparator());

    variable = new Variable("BARCODE").setDataType(DataType.TEXT);
    parent.addVariable(variable);

    VariableData[] participantBarcodes = new VariableData[3];
    for(int i = 1; i <= participantBarcodes.length; i++) {
      participantBarcodes[i - 1] = dataSet.addVariableData(new VariableData(pathNamingStrategy.getPath(variable), DataBuilder.buildText(Integer.toString(i))));
    }

    subvariable = new Variable("NAME").setDataType(DataType.TEXT);
    Variable subvariable2 = new Variable("CONSENT").setDataType(DataType.BOOLEAN);
    parent.addVariable(subvariable);
    parent.addVariable(subvariable2);
    for(int i = 0; i < participantBarcodes.length; i++) {
      participantBarcodes[i].addReference(new VariableData(pathNamingStrategy.getPath(subvariable), DataBuilder.buildText("Name " + (i + 1))));
      participantBarcodes[i].addReference(new VariableData(pathNamingStrategy.getPath(subvariable2), DataBuilder.buildBoolean(Boolean.TRUE)));
    }

    // questionnaire

    parent = root.addVariable("HealthQuestionnaire", pathNamingStrategy.getPathSeparator());

    variable = new Variable("PARTICIPANT_AGE").addCategories("PARTICIPANT_AGE", "PNA", "DK");
    parent.addVariable(variable);

    subvariable = new Variable("OPEN_AGE").setDataType(DataType.INTEGER).setUnit("year");
    variable.addVariable(subvariable);

    for(int i = 0; i < participantBarcodes.length; i++) {
      participantBarcodes[i].addReference(new VariableData(pathNamingStrategy.getPath(subvariable), DataBuilder.buildInteger(45 + i)));
    }

    parent = root.addVariable("HealthQuestionnaire/DATE_OF_BIRTH", pathNamingStrategy.getPathSeparator());

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

    parent = root.addVariable("StandingHeight", pathNamingStrategy.getPathSeparator());

    variable = new Variable("First_Height_Measurement").setDataType(DataType.DECIMAL);
    parent.addVariable(variable);

    for(int i = 0; i < participantBarcodes.length; i++) {
      VariableData data = new VariableData(pathNamingStrategy.getPath(variable), DataBuilder.buildDecimal(170.5 + i));
      participantBarcodes[i].addReference(data);
      userLogins[0].addReference(data);
    }

    variable = new Variable("Second_Height_Measurement").setDataType(DataType.DECIMAL);
    parent.addVariable(variable);

    for(int i = 0; i < participantBarcodes.length; i++) {
      VariableData data = new VariableData(pathNamingStrategy.getPath(variable), DataBuilder.buildDecimal(170.0 + i));
      participantBarcodes[i].addReference(data);
      userLogins[0].addReference(data);
    }

    String str = "Hello World !";
    dataSet.addVariableData(new VariableData("Binary_Data", DataBuilder.buildBinary(new ByteArrayInputStream(str.getBytes()))));

  }

  @Test
  public void testXStream() {

    // dumps

    System.out.println("\n**** Variables directory ****\n");
    System.out.println(xstream.toXML(root));

    System.out.println("\n**** Variables XPath ****\n");
    xquery(root, "//variable");
    System.out.println();
    xquery(root, "//variable[@name='PARTICIPANT']/variable[@name='BARCODE']");
    System.out.println();
    xquery(root, "//variable[@name='PARTICIPANT']/variable[@name='BARCODE'] | //variable[not(@name='PARTICIPANT')]//variable");

    System.out.println("\n**** Variables paths ****\n");
    writeVariables(root);

    // search
    Variable searchedEntity = pathNamingStrategy.getVariable(root, "/STUDY_NAME/HealthQuestionnaire/DATE_OF_BIRTH/DOB_MONTH/OPEN_MONTH");
    Assert.assertNotNull(searchedEntity);
    Assert.assertEquals("OPEN_MONTH", searchedEntity.getName());

    searchedEntity = pathNamingStrategy.getVariable(root, "/STUDY_NAME/HealthQuestionnaire/DATE_OF_BIRTH/OPEN_MONTH");
    Assert.assertNull(searchedEntity);

    searchedEntity = pathNamingStrategy.getVariable(root, "/ANOTHER_STUDY_NAME/HealthQuestionnaire/DATE_OF_BIRTH/DOB_MONTH/OPEN_MONTH");
    Assert.assertNull(searchedEntity);

    System.out.println("\n**** Variables dataSet ****\n");
    System.out.println(xstream.toXML(dataSet));

    // System.out.println("\n**** Variables data paths ****\n");
    // writeVariablesData(dataSet);

  }

  private void xquery(Variable parent, String query) {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true); // never forget this!
    try {
      DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
      Document doc = builder.parse(new ByteArrayInputStream(xstream.toXML(parent).getBytes()));

      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();
      XPathExpression expr = xpath.compile(query);
      Object result = expr.evaluate(doc, XPathConstants.NODESET);
      NodeList nodes = (NodeList) result;
      for(int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        System.out.println(pathNamingStrategy.getPath(node));
      }

    } catch(Exception e) {
      e.printStackTrace();
    }

  }

  private void writeVariables(Variable parent) {
    for(Variable child : parent.getVariables()) {
      System.out.println(pathNamingStrategy.getPath(child));
      writeVariables(child);
    }
  }

  // private void writeVariablesData(VariableDataSet dataSet) {
  // for(VariableData data : dataSet.getVariableDatas()) {
  // System.out.println(data.getPath());
  // }
  // }

  private void initializeXStream() {
    xstream = new XStream();
    xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    xstream.autodetectAnnotations(true);

    xstream.alias("participant", Participant.class);
    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }

}
