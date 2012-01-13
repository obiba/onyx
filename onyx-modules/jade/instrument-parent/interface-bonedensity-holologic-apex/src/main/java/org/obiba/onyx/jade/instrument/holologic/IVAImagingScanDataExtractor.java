/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.holologic;

import java.io.File;
import java.util.Map;

import org.dcm4che2.tool.dcmrcv.DicomServer;
import org.obiba.onyx.jade.instrument.holologic.APEXInstrumentRunner.Side;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class IVAImagingScanDataExtractor extends APEXScanDataExtractor {

  private static final Logger log = LoggerFactory.getLogger(IVAImagingScanDataExtractor.class);

  private Energy energy;

  protected IVAImagingScanDataExtractor(JdbcTemplate patScanDb, File scanDataDir, String participantKey, Energy energy, DicomServer server, ApexReceiver apexReceiver) {
    super(patScanDb, scanDataDir, participantKey, server, apexReceiver);
    this.energy = energy;
  }

  @Override
  public String getName() {
    switch(energy) {
    case DUAL_LATERAL:
      return "DEL";
    default:
      return "SEL";
    }
  }

  @Override
  public String getDicomBodyPartName() {
    return "LSPINE";
  }

  @Override
  protected long getScanType() {
    switch(energy) {
    case CLSA_DXA:
      return 29l;
    case SINGLE_AP:
      return 35l;
    case SINGLE_LATERAL:
      return 36l;
    default:
      return 37l;
    }
  }

  @Override
  protected void extractDataImpl(Map<String, Data> data) {
    log.warn("no additional data can be extracted for this scan");
  }

  public enum Energy {
    SINGLE_LATERAL, DUAL_LATERAL, SINGLE_AP,
    // clsa c-arm (different of simulation mode)
    CLSA_DXA
  }

  @Override
  public Side getSide() {
    return null;
  }

}
