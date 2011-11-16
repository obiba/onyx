/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.ndd;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public abstract class TestDataExtractor<T extends TestData<?>> extends XMLDataExtractor<T> {

  static final Logger log = LoggerFactory.getLogger(TestDataExtractor.class);

  public TestDataExtractor() {
    super();
  }

  public T extractData() throws XPathExpressionException {
    checkTestName();
    T tData = extractDataImpl();
    tData.setType(getName());
    tData.setDate(xpath.evaluate("//Test/TestDate/text()", doc, XPathConstants.STRING).toString());
    tData.setQualitygrade(xpath.evaluate("//Test/QualityGrade/text()", doc, XPathConstants.STRING).toString());

    return tData;
  }

  private void checkTestName() {
    try {
      Node node = (Node) xpath.evaluate(getTestRoot(), doc, XPathConstants.NODE);
      if(node == null) {
        throw new IllegalArgumentException("Unable to find one test of type: " + getName());
      }
    } catch(XPathExpressionException e) {
      throw new IllegalArgumentException("Unable to identify the type of test", e);
    }
  }

  protected abstract String getName();

  protected abstract T extractDataImpl() throws XPathExpressionException;

  protected String getTestRoot() {
    return "//Test[@TypeOfTest='" + getName() + "']";
  }

  protected NodeList getTrialNodes() throws XPathExpressionException {
    return (NodeList) xpath.evaluate(getTestRoot() + "/Trials/Trial", doc, XPathConstants.NODESET);
  }

  protected NodeList getTrialResultParameterNodes(int index) throws XPathExpressionException {
    return (NodeList) xpath.evaluate(getTrialPath(index) + "/ResultParameters/ResultParameter", doc, XPathConstants.NODESET);
  }

  protected String getTrialPath(int index) {
    return getTestRoot() + "/Trials/Trial[" + index + "]";
  }

  protected String extractTrialStringValue(int index, String path) throws XPathExpressionException {
    return extractStringValue(getTrialPath(index) + path);
  }

  protected Long extractTrialLongValue(int index, String path) throws XPathExpressionException {
    return extractLongValue(getTrialPath(index) + path);
  }

  protected Map<String, Number> extractResultParametersData(NodeList resultParams) throws XPathExpressionException {
    Map<String, Number> results = new HashMap<String, Number>();
    for(int i = 0; i < resultParams.getLength(); i++) {
      extractResultParametersData(results, resultParams.item(i));
    }
    return results;
  }

  protected void extractResultParametersData(Map<String, Number> results, Node resultParam) throws XPathExpressionException {
    String name = extractAttributeValue(resultParam, "ID");

    NodeList children = resultParam.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Node node = children.item(i);
      if(node.getLocalName() != null) {
        if(node.getLocalName().equals("DataValue")) {
          putResult(results, name, parseDouble(node.getTextContent()));
        } else if(node.getLocalName().equals("PredictedValue")) {
          putResult(results, name + "_PRED", parseDouble(node.getTextContent()));
        } else if(node.getLocalName().equals("LLNormalValue")) {
          putResult(results, name + "_LLNORMAL", parseDouble(node.getTextContent()));
        }
      }
    }
  }

  private void putResult(Map<String, Number> results, String name, Double value) {
    if(value != null) {
      results.put(name, value);
    }
  }

}
