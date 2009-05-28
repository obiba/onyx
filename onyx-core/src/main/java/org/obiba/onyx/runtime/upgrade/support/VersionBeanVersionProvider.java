/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.runtime.upgrade.support;

import org.obiba.runtime.Version;
import org.springframework.beans.factory.FactoryBean;

public class VersionBeanVersionProvider implements FactoryBean {

  private String versionString;

  public Object getObject() throws Exception {
    Version version = new Version(versionString);
    return version;
  }

  public Class<Version> getObjectType() {
    return Version.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setVersion(String versionString) {
    this.versionString = versionString;
  }

}
