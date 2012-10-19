/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.spring;

import java.io.IOException;
import java.util.Properties;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.spring.properties.EncryptablePropertyPlaceholderConfigurer;
import org.jasypt.util.text.TextEncryptor;

/**
 *
 */
public class OnyxPropertyPlaceholderConfigurer extends EncryptablePropertyPlaceholderConfigurer {

  /**
   * @param stringEncryptor
   */
  public OnyxPropertyPlaceholderConfigurer(StringEncryptor stringEncryptor) {
    super(stringEncryptor);
  }

  /**
   * @param textEncryptor
   */
  public OnyxPropertyPlaceholderConfigurer(TextEncryptor textEncryptor) {
    super(textEncryptor);
  }

  public Properties getProperties() throws IOException {
    return mergeProperties();
  }

}
