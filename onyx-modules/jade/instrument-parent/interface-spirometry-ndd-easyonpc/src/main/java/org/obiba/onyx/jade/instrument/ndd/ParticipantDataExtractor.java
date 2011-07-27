/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
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

/**
 *
 */
public class ParticipantDataExtractor extends XMLDataExtractor<ParticipantData> {

  public ParticipantDataExtractor(XPath xpath, Document doc) {
    super();
    init(xpath, doc);
  }

  /**
   * Extracts participant information data
   */
  public ParticipantData extractData() throws XPathExpressionException {
    ParticipantData pData = new ParticipantData();
    pData.setIdentifier(extractAttributeValue("//Patient", "ID"));
    pData.setLastName(xpath.evaluate("//Patient/LastName/text()", doc, XPathConstants.STRING).toString());
    pData.setFirstName(xpath.evaluate("//Patient/FirstName/text()", doc, XPathConstants.STRING).toString());
    pData.setHeight(extractDoubleValue("//PatientDataAtTestTime/Height/text()"));
    pData.setWeight(extractLongValue("//PatientDataAtTestTime/Weight/text()"));
    pData.setEthnicity(xpath.evaluate("//PatientDataAtTestTime/Ethnicity/text()", doc, XPathConstants.STRING).toString());
    pData.setSmoker(xpath.evaluate("//PatientDataAtTestTime/Smoker/text()", doc, XPathConstants.STRING).toString());
    pData.setAsthma(xpath.evaluate("//PatientDataAtTestTime/Asthma/text()", doc, XPathConstants.STRING).toString());
    pData.setGender(xpath.evaluate("//PatientDataAtTestTime/Gender/text()", doc, XPathConstants.STRING).toString());
    pData.setDateOfBirth(xpath.evaluate("//PatientDataAtTestTime/DateOfBirth/text()", doc, XPathConstants.STRING).toString());

    return pData;
  }

}
