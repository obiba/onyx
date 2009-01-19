/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

/**
 * 
 */
public class OnyxDataExportFactoryBean implements FactoryBean, InitializingBean {

  private XStream destinationXstream;

  private Resource destinationsResource;

  public void setDestinationsResource(Resource destinationsResource) {
    this.destinationsResource = destinationsResource;
  }

  public Object getObject() throws Exception {
    InputStream is = destinationsResource.getInputStream();
    try {
      return destinationXstream.fromXML(is);
    } finally {
      if(is != null) {
        try {
          is.close();
        } catch(IOException e) {
          // Ignore
        }
      }
    }
  }

  public Class<?> getObjectType() {
    return List.class;
  }

  public boolean isSingleton() {
    return false;
  }

  public void afterPropertiesSet() throws Exception {
    destinationXstream = new XStream();
    destinationXstream.alias("destinations", LinkedList.class);
    destinationXstream.alias("destination", OnyxDataExportDestination.class);
  }

}
