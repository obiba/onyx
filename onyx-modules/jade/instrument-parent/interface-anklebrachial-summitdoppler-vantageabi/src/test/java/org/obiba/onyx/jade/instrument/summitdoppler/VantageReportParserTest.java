/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.summitdoppler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

/**
 *
 */
public class VantageReportParserTest {

  @Test
  public void testParse() throws URISyntaxException, IOException {
    VantageReportParser parser = new VantageReportParser();

    File file = new File(getClass().getResource("/VAN00303.ABI").toURI());

    parser.parse(file);
  }

}
