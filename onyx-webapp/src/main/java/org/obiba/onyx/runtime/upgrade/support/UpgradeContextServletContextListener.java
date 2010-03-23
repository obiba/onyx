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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.obiba.onyx.webapp.OnyxApplicationPropertyPlaceholderConfigurer;
import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class UpgradeContextServletContextListener implements ServletContextListener {

  private final Logger log = LoggerFactory.getLogger(UpgradeContextServletContextListener.class);

  public void contextInitialized(ServletContextEvent sce) {
    XmlWebApplicationContext appContext = loadUpgradeContext(sce);

    try {
      log.info("Starting the upgrade procedure.");
      launchUpgrade(appContext);
      log.info("Successfully completed the upgrade procedure.");
    } catch(UpgradeException e) {
      log.error("The upgrade procedure could not be completed because the following error was encountered", e);
      throw new RuntimeException(e);
    } finally {
      try {
        appContext.close();
      } catch(RuntimeException e) {
        // ignore
      }
    }

  }

  private XmlWebApplicationContext loadUpgradeContext(ServletContextEvent sce) {
    ServletContext servletContext = sce.getServletContext();

    OnyxApplicationPropertyPlaceholderConfigurer configurer = new OnyxApplicationPropertyPlaceholderConfigurer(servletContext);

    XmlWebApplicationContext appContext = new XmlWebApplicationContext();
    appContext.setServletContext(servletContext);
    appContext.addBeanFactoryPostProcessor(configurer);
    appContext.setConfigLocation(servletContext.getInitParameter("upgradeContextLocation"));
    appContext.refresh();

    return appContext;
  }

  private void launchUpgrade(XmlWebApplicationContext appContext) throws UpgradeException {
    UpgradeManager upgradeManager = (UpgradeManager) appContext.getBean("upgradeManager");
    upgradeManager.executeUpgrade();
  }

  public void contextDestroyed(ServletContextEvent sce) {
  }
}
