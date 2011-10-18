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
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class LateralScanDataExtractor extends APEXScanDataExtractor {

  private static final Logger log = LoggerFactory.getLogger(LateralScanDataExtractor.class);

  private Energy energy;

  protected LateralScanDataExtractor(JdbcTemplate patScanDb, File scanDataDir, String participantKey, Energy energy, DicomServer server) {
    super(patScanDb, scanDataDir, participantKey, server);
    this.energy = energy;
  }

  @Override
  public String getName() {
    switch(energy) {
    case SINGLE:
      return "SEL";
    default:
      return "DEL";
    }
  }

  @Override
  public String getDicomBodyPartName() {
    return "LSPINE";
  }

  @Override
  protected long getScanType() {
    switch(energy) {
    case SINGLE:
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
    SINGLE, DUAL
  }

}
