/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl;

import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IPropertyKeyWriter;

/**
 * Write the questionnaire properties into a properties object.
 * @author Yannick Marcon
 * 
 */
public class PropertiesPropertyKeyWriterImpl implements IPropertyKeyWriter {

  private Properties language;

  private Properties properties = new Properties();

  /**
   * Constructor without properties reference.
   */
  public PropertiesPropertyKeyWriterImpl() {
  }

  /**
   * Constructor with a property reference.
   * @param language
   */
  public PropertiesPropertyKeyWriterImpl(Properties language) {
    this.language = language;
  }

  /**
   * Get the questionnaire properties object.
   * @return
   */
  public Properties getProperties() {
    return properties;
  }

  public void endBloc() {
  }

  public void write(String key, String value) {
    properties.put(key, value);
  }

  public void end() {
  }

  public Properties getReference() {
    return language;
  }

  public void writeComment(String... comments) {
  }

}
