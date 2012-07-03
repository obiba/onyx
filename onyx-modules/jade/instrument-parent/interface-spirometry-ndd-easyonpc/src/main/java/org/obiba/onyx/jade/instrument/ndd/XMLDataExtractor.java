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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 */
public abstract class XMLDataExtractor<T> {

  protected XPath xpath;

  protected Document doc;

  public XMLDataExtractor() {
    super();
  }

  void init(XPath xpath, Document doc) {
    this.doc = doc;
    this.xpath = xpath;
  }

  public abstract T extractData() throws XPathExpressionException;

  protected String extractAttributeValue(String path, String attr) throws XPathExpressionException {
    Node node = (Node) xpath.evaluate(path, doc, XPathConstants.NODE);
    if(node == null) {
      throw new IllegalStateException(String.format("node %s not found. Cannot extract %s attribute.", path, attr));
    }
    return extractAttributeValue(node, attr);
  }

  protected String extractAttributeValue(Node node, String attr) throws XPathExpressionException {
    if(node == null) throw new IllegalArgumentException();
    Node attrNode = node.getAttributes().getNamedItem(attr);
    if(attrNode == null) {
      throw new IllegalStateException(String.format("Node %s has no attribute %s", node.getNodeName(), attr));
    }
    return attrNode.getTextContent();
  }

  protected String extractStringValue(String path) throws XPathExpressionException {
    return xpath.evaluate(path, doc, XPathConstants.STRING).toString();
  }

  protected Long extractLongValue(String path) throws XPathExpressionException {
    String value = extractStringValue(path);
    if(!value.equals("") && !value.equals("NaN")) {
      return Long.valueOf(value);
    }
    return null;
  }

  protected Double extractDoubleValue(String path) throws XPathExpressionException {
    String value = extractStringValue(path);
    if(!value.equals("") && !value.equals("NaN")) {
      return Double.valueOf(value);
    }
    return null;
  }

  protected Double parseDouble(String str) {
    if(str != null && str.length() != 0 && !str.equals("NaN")) {
      return Double.parseDouble(str);
    }
    return null;
  }

}
