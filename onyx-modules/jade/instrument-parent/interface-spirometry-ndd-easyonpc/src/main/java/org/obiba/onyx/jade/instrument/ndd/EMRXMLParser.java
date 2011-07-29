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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 */
public class EMRXMLParser<T extends TestData<?>> {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(EMRXMLParser.class);

  private XPath xpath;

  private Document doc;

  private ParticipantData participantData;

  private T testData;

  public EMRXMLParser() {
    super();
  }

  public void parse(InputStream in, TestDataExtractor<T> testExtractor) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

    // First read the whole file
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    domFactory.setNamespaceAware(true);
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    doc = builder.parse(in);

    XPathFactory factory = XPathFactory.newInstance();
    xpath = factory.newXPath();

    ParticipantDataExtractor pExtractor = new ParticipantDataExtractor(xpath, doc);
    participantData = pExtractor.extractData();
    testExtractor.init(xpath, doc);
    testData = testExtractor.extractData();

    in.close();
  }

  public ParticipantData getParticipantData() {
    return participantData;
  }

  public T getTestData() {
    return testData;
  }

}
