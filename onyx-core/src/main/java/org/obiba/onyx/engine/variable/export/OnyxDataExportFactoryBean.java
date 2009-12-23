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

import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

public class OnyxDataExportFactoryBean implements FactoryBean {

  private Resource destinationsResource;

  public void setDestinationsResource(Resource destinationsResource) {
    this.destinationsResource = destinationsResource;
  }

  public Object getObject() throws Exception {
    OnyxDataExportReader onyxDataExportReader = new OnyxDataExportReader();
    onyxDataExportReader.setResources(new Resource[] { destinationsResource });
    return onyxDataExportReader.read();
  }

  public Class<?> getObjectType() {
    return List.class;
  }

  public boolean isSingleton() {
    return false;
  }
}
