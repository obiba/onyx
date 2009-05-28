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

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.web.context.support.ServletContextResource;

public class OnyxApplicationPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

  private String configPath;

  public OnyxApplicationPropertyPlaceholderConfigurer(ServletContext servletContext) {
    super();

    PropertiesFactoryBean pfb = new PropertiesFactoryBean();
    pfb.setLocation(new ServletContextResource(servletContext, "WEB-INF/onyx.properties"));
    pfb.setSingleton(false);
    Properties onyxProperties;
    try {
      onyxProperties = (Properties) pfb.getObject();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }

    configPath = onyxProperties.getProperty("org.obiba.onyx.config.path");
    if(configPath == null) {
      throw new IllegalStateException("Onyx config path not set.");
    }

    this.setProperties(onyxProperties);
    // This must be set to true in order to let another PropertyPlaceholderConfigurer replace the unresolved entries.
    this.setIgnoreUnresolvablePlaceholders(true);
  }

  public String getConfigPath() {
    return configPath;
  }
}
