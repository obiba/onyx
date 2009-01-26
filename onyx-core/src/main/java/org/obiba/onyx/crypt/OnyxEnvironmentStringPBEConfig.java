/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.crypt;

import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;

/**
 * 
 */
public class OnyxEnvironmentStringPBEConfig extends EnvironmentStringPBEConfig {

  /**
   * <p>
   * Creates a new <tt>OnyxEnvironmentStringPBEConfig</tt> instance.
   * </p>
   */
  public OnyxEnvironmentStringPBEConfig() {
    super();
  }

  /**
   * Set the configuration object to use the specified JVM system property, or if not found the specified environment
   * variable, to load the value for the password.
   * @param passwordKeyName
   */
  public void setOnyxPasswordKeyName(String passwordKeyName) {
    setPasswordSysPropertyName(passwordKeyName);
    if(getPasswordSysPropertyName() == null) {
      setPasswordEnvName(passwordKeyName);
    }
  }

}
