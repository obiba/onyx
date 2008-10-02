package org.obiba.onyx.jade.instrument.gehealthcare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Inspired from class Interface_CaG.EcgCollector by dbujold Parse ecg xml result file and return a object containing
 * the data
 * @author acarey
 */

public class CardiosoftInstrumentResultParser {

  // The whole XML document
  private String xmlDocument;

  private String time;

  private String date;

  private String diagnosis;

  private String filterSetting;

  // Resting ECG Measurements
  private String diagnosisVersion;

  private Long ventricularRate;

  private Long pQInterval;

  private Long pDuration;

  private Long qRsDuration;

  private Long qTInterval;

  private Long qTCInterval;

  private Long rRInterval;

  private Long pPInterval;

  private Long pAxis;

  private Long rAxis;

  private Long tAxis;

  private Long qRSNum;

  private Long pOnset;

  private Long pOffset;

  private Long qOnset;

  private Long qOffset;

  private Long tOffset;

  private XPath xpath;

  private Document doc;

  /**
   * Constructor parsing the xml file
   * @param pFileStream
   * @throws IOException
   */
  @SuppressWarnings("deprecation")
  public CardiosoftInstrumentResultParser(InputStream pFileStream) {
    try {
      // First read the whole file to keep a copy in the EcgCollector object.
      BufferedReader fileReader = new BufferedReader(new InputStreamReader(pFileStream));
      StringBuilder xmlFileContent = new StringBuilder();
      String wOneLine;
      int i = 0;
      while((wOneLine = fileReader.readLine()) != null) {
        xmlFileContent.append(wOneLine + "\n");
        i++;
      }
      xmlDocument = xmlFileContent.toString();

      // Now create a stream using the copy of the XML file for the XPath analysis
      InputStream xmlStream = new java.io.StringBufferInputStream(xmlDocument);
      DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
      domFactory.setNamespaceAware(true);
      DocumentBuilder builder = domFactory.newDocumentBuilder();
      doc = builder.parse(xmlStream);

      XPathFactory factory = XPathFactory.newInstance();
      xpath = factory.newXPath();

      extractDateTime();
      extractInterpretation();
      extractFilterSetting();
      extractRestingEcgMeasurements();
    } catch(XPathExpressionException e) {
      throw new RuntimeException("Invalid XPath expression. Stopping XML data extraction.", e);
    } catch(ParserConfigurationException e) {
      throw new RuntimeException("An error has occured while trying to initialize the XPath parser.", e);
    } catch(SAXException e) {
      throw new RuntimeException("An error has occured while trying to initialize the XPath parser.", e);
    } catch(IOException ioEx) {
      throw new RuntimeException("Error: CardiosoftInstrumentResultParser IOException ", ioEx);
    }
  }

  public String getXmlDocument() {
    return xmlDocument;
  }

  public String getTime() {
    return time;
  }

  public String getDate() {
    return date;
  }

  public String getDiagnosis() {
    return diagnosis;
  }

  public String getFilterSetting() {
    return filterSetting;
  }

  public String getDiagnosisVersion() {
    return diagnosisVersion;
  }

  public Long getVentricularRate() {
    return ventricularRate;
  }

  public Long getPQInterval() {
    return pQInterval;
  }

  public Long getPDuration() {
    return pDuration;
  }

  public Long getQRsDuration() {
    return qRsDuration;
  }

  public Long getQTInterval() {
    return qTInterval;
  }

  public Long getQTCInterval() {
    return qTCInterval;
  }

  public Long getRRInterval() {
    return rRInterval;
  }

  public Long getPPInterval() {
    return pPInterval;
  }

  public Long getPAxis() {
    return pAxis;
  }

  public Long getRAxis() {
    return rAxis;
  }

  public Long getTAxis() {
    return tAxis;
  }

  public Long getQRSNum() {
    return qRSNum;
  }

  public Long getPOnset() {
    return pOnset;
  }

  public Long getPOffset() {
    return pOffset;
  }

  public Long getQOnset() {
    return qOnset;
  }

  public Long getQOffset() {
    return qOffset;
  }

  public Long getTOffset() {
    return tOffset;
  }

  /**
   * Extracts date and time of measurement.
   * @throws XPathExpressionException
   */
  private void extractDateTime() throws XPathExpressionException {

    time = xpath.evaluate("concat(//ObservationDateTime/Hour/text(), ':', //ObservationDateTime/Minute/text(), ':', //ObservationDateTime/Second/text())", doc, XPathConstants.STRING).toString();

    date = xpath.evaluate("concat(//ObservationDateTime/Year/text(), '-', //ObservationDateTime/Month/text(), '-', //ObservationDateTime/Day/text())", doc, XPathConstants.STRING).toString();
  }

  /**
   * Extracts software's data interpretation.
   * @throws XPathExpressionException
   */
  private void extractInterpretation() throws XPathExpressionException {

    // Extracting Diagnosis
    Object diagnosisNode = xpath.evaluate("//Interpretation/Diagnosis/DiagnosisText/text()", doc, XPathConstants.NODESET);
    StringBuilder diagnosisSb = new StringBuilder();
    NodeList nodes = (NodeList) diagnosisNode;
    for(int i = 0; i < nodes.getLength(); i++) {
      diagnosisSb.append(nodes.item(i).getNodeValue());
      if(i + 1 < nodes.getLength()) {
        diagnosisSb.append("\n");
      }
    }
    diagnosis = diagnosisSb.toString();

    // Extracting Conclusion
    Object conclusionNode = xpath.evaluate("//Interpretation/Conclusion/ConclusionText/text()", doc, XPathConstants.NODESET);
    StringBuilder conclusionSb = new StringBuilder();
    nodes = (NodeList) conclusionNode;
    for(int i = 0; i < nodes.getLength(); i++) {
      conclusionSb.append(nodes.item(i).getNodeValue());
      if(i + 1 < nodes.getLength()) {
        conclusionSb.append("\n");
      }
    }

  }

  /**
   * Extracts filter data.
   */
  private void extractFilterSetting() throws XPathExpressionException {
    filterSetting = xpath.evaluate("concat('Cublic Spline: ', //FilterSetting/CubicSpline/text(), " + "     '\nFilter50Hz: ',    //FilterSetting/Filter50Hz/text(),  " + "     '\nFilter60Hz: ',      //FilterSetting/Filter60Hz/text(),  " + "     '\nLowPass (', //FilterSetting/LowPass/attribute::units, '): ',    //FilterSetting/LowPass/text(),     " + "     '\nHighPass (', //FilterSetting/HighPass/attribute::units, '): ',    //FilterSetting/HighPass/text())", doc, XPathConstants.STRING).toString();
  }

  /**
   * Extract all data located under the RestingEcgMeasurements tag.
   * @throws XPathExpressionException
   */
  private void extractRestingEcgMeasurements() throws XPathExpressionException {
    diagnosisVersion = xpath.evaluate("//RestingECGMeasurements/DiagnosisVersion/text()", doc, XPathConstants.STRING).toString();
    ventricularRate = extractMeasurement("VentricularRate");
    pQInterval = extractMeasurement("PQInterval");
    pDuration = extractMeasurement("PDuration");
    qRsDuration = extractMeasurement("QRSDuration");
    qTInterval = extractMeasurement("QTInterval");
    qTCInterval = extractMeasurement("QTCInterval");
    rRInterval = extractMeasurement("RRInterval");
    pPInterval = extractMeasurement("PPInterval");
    pAxis = extractMeasurement("PAxis");
    rAxis = extractMeasurement("RAxis");
    tAxis = extractMeasurement("TAxis");
    qRSNum = Long.valueOf(xpath.evaluate("//RestingECGMeasurements/QRSNum/text()", doc, XPathConstants.STRING).toString());
    pOnset = extractMeasurement("POnset");
    pOffset = extractMeasurement("POffset");
    qOnset = extractMeasurement("QOnset");
    qOffset = extractMeasurement("QOffset");
    tOffset = extractMeasurement("TOffset");
  }

  /**
   * Extracts data corresponding to the parameter
   * @param tagName
   * @return the result as a Long
   * @throws XPathExpressionException
   */
  private Long extractMeasurement(String tagName) throws XPathExpressionException {
    String measurement = xpath.evaluate("//RestingECGMeasurements/" + tagName + "/text()", doc, XPathConstants.STRING).toString();

    if(measurement.equals("")) {
      return null;
    } else {
      return Long.valueOf(measurement);
    }
  }
}
