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

import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.impl.DefaultEntityPathNamingStrategy;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thoughtworks.xstream.XStream;

/**
 * 
 */
public class VariableTest {

  private XStream xstream;

  private IEntityPathNamingStrategy pathNamingStrategy = DefaultEntityPathNamingStrategy.getInstance("STUDY_NAME");

  @Test
  public void testXStream() {
    initializeXStream();

    Entity root = new Entity(pathNamingStrategy.getRootName());
    Entity parent;
    Variable variable;
    Variable subvariable;

    VariableDataSet dataSet = new VariableDataSet();

    // participants

    parent = root.addEntity("ADMIN/PARTICIPANT", DefaultEntityPathNamingStrategy.PATH_SEPARATOR);

    variable = new Variable("BARCODE").setDataType(DataType.TEXT);
    parent.addEntity(variable);

    VariableData[] participantBarcodes = new VariableData[3];
    for(int i = 1; i <= participantBarcodes.length; i++) {
      participantBarcodes[i - 1] = dataSet.addVariableData(new VariableData(pathNamingStrategy.getPath(variable), DataBuilder.buildText(Integer.toString(i))));
    }

    subvariable = new Variable("NAME").setDataType(DataType.TEXT);
    parent.addEntity(subvariable);
    for(int i = 0; i < participantBarcodes.length; i++) {
      dataSet.addVariableData(new VariableData(pathNamingStrategy.getPath(variable), DataBuilder.buildText("Name " + (i + 1))).addReference(participantBarcodes[i]));
    }

    // questionnaire

    parent = root.addEntity("HealthQuestionnaire", DefaultEntityPathNamingStrategy.PATH_SEPARATOR);

    variable = new Variable("PARTICIPANT_AGE").addCategories("PARTICIPANT_AGE", "PNA", "DK");
    parent.addEntity(variable);

    subvariable = new Variable("OPEN_AGE").setDataType(DataType.INTEGER).setUnit("year");
    variable.addEntity(subvariable);

    for(int i = 0; i < participantBarcodes.length; i++) {
      dataSet.addVariableData(new VariableData(pathNamingStrategy.getPath(variable), DataBuilder.buildInteger(45 + i)).addReference(participantBarcodes[i]));
    }

    parent = root.addEntity("HealthQuestionnaire/DATE_OF_BIRTH", DefaultEntityPathNamingStrategy.PATH_SEPARATOR);

    variable = new Variable("DOB_YEAR").addCategories("DOB_YEAR", "PNA", "DK");
    parent.addEntity(variable);

    subvariable = new Variable("OPEN_YEAR").setDataType(DataType.INTEGER).setUnit("year");
    variable.addEntity(subvariable);

    System.out.println("\n**** Variables directory ****\n");
    System.out.println(xstream.toXML(root));

    System.out.println("\n**** Variables XPath ****\n");
    xquery(root, "//variable");
    System.out.println();
    xquery(root, "//entity[@name='PARTICIPANT']/variable[@name='BARCODE']");
    System.out.println();
    xquery(root, "//entity[@name='PARTICIPANT']/variable[@name='BARCODE'] | //entity[not(@name='PARTICIPANT')]/variable");

    System.out.println("\n**** Variables paths ****\n");
    writeVariables(root);

    System.out.println("\n**** Variables dataSet ****\n");
    System.out.println(xstream.toXML(dataSet));

    System.out.println("\n**** Variables data paths ****\n");
    writeVariablesData(dataSet);

  }

  private void xquery(Entity parent, String query) {
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

  private void writeVariables(Entity parent) {
    for(Entity child : parent.getEntities()) {
      if(child instanceof Variable) {
        System.out.println(pathNamingStrategy.getPath(child));
      }
      writeVariables(child);
    }
  }

  private void writeVariablesData(VariableDataSet dataSet) {
    for(VariableData data : dataSet.getVariableDatas()) {
      System.out.println(data.getPath());
    }
  }

  private void initializeXStream() {
    xstream = new XStream();
    xstream.setMode(XStream.ID_REFERENCES);
    xstream.autodetectAnnotations(true);

    xstream.alias("participant", Participant.class);
    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }

}
