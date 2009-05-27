/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import org.obiba.onyx.core.io.support.XStreamDataConverter;

import com.thoughtworks.xstream.XStream;

/**
 * 
 */
public class DataSourceStreamer {

  private XStream xstream;

  private DataSourceStreamer() {
    initializeXstream();
  }

  public static String toXML(IDataSource dataSource) {
    DataSourceStreamer dsStreamer = new DataSourceStreamer();
    return dsStreamer.xstream.toXML(dataSource);
  }

  private void initializeXstream() {
    xstream = new XStream();

    xstream.registerConverter(new XStreamDataConverter());
  }

}
