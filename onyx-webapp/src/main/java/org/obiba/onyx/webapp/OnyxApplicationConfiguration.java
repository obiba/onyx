/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp;

import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Onyx web application configuration settings holder.
 * @see OnyxApplication
 */
public class OnyxApplicationConfiguration {

  private final Logger log = LoggerFactory.getLogger(OnyxApplicationConfiguration.class);

  private enum Type {
    deployment, development;
  };

  private String configurationType;

  private Version version;

  public String getConfigurationType() {
    return configurationType;
  }

  public void setConfigurationType(String configurationType) {
    if(configurationType != null) {
      if(configurationType.equals(Type.deployment.toString()) || configurationType.equals(Type.development.toString())) {
        this.configurationType = configurationType;
      } else {
        log.error("Wrong configuration type: {} found, deployment or development expected.", configurationType);
      }
    } else {
      this.configurationType = null;
    }
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return configurationType + " " + version;
  }
}
