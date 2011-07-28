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

import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.obiba.onyx.jade.instrument.ndd.FVCDataExtractor.FVCData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

/**
 *
 */
public class FVCDataExtractor extends TestDataExtractor<FVCData> {

  private static final Logger log = LoggerFactory.getLogger(FVCDataExtractor.class);

  public FVCDataExtractor() {
    super();
  }

  @Override
  protected String getName() {
    return "FVC";
  }

  @Override
  protected FVCData extractDataImpl() throws XPathExpressionException {
    FVCData data = new FVCData();

    data.setBestResults(extractResultParametersData((NodeList) xpath.evaluate(getTestRoot() + "/BestValues/ResultParameter", doc, XPathConstants.NODESET)));

    NodeList trials = getTrialNodes();
    for(int i = 0; i < trials.getLength(); i++) {
      if(extractTrialStringValue(i + 1, "/Accepted").equals("true")) {
        FVCTrialData tData = new FVCTrialData();
        data.getTrials().add(tData);
        tData.setResults(extractResultParametersData(getTrialResultParameterNodes(i + 1)));
        tData.setDate(extractTrialStringValue(i + 1, "/Date"));
        tData.flowInterval = parseDouble(extractTrialStringValue(i + 1, "/ChannelFlow/SamplingInterval"));
        tData.flowValues = extractTrialStringValue(i + 1, "/ChannelFlow/SamplingValues");
        tData.volumeInterval = parseDouble(extractTrialStringValue(i + 1, "/ChannelVolume/SamplingInterval"));
        tData.volumeValues = extractTrialStringValue(i + 1, "/ChannelVolume/SamplingValues");
      }
    }

    // log.info(data.toString());

    return data;
  }

  public class FVCData extends TestData<FVCTrialData> {

    private Map<String, Number> bestResults;

    public FVCData() {
      super();
    }

    public void setBestResults(Map<String, Number> bestResults) {
      this.bestResults = bestResults;
    }

    public Map<String, Number> getBestResults() {
      return bestResults;
    }

  }

  public class FVCTrialData extends TrialData {

    private Double flowInterval;

    private String flowValues;

    private Double volumeInterval;

    private String volumeValues;

    public FVCTrialData() {
      super();
    }

    public Double getFlowInterval() {
      return flowInterval;
    }

    public String getFlowValues() {
      return flowValues;
    }

    public Double getVolumeInterval() {
      return volumeInterval;
    }

    public String getVolumeValues() {
      return volumeValues;
    }
  }

}
