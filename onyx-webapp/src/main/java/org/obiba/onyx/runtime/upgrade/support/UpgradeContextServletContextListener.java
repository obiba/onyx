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
import javax.sql.DataSource;

import org.obiba.onyx.webapp.OnyxApplicationPropertyPlaceholderConfigurer;
import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.obiba.runtime.upgrade.support.DatabaseMetadataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class UpgradeContextServletContextListener implements ServletContextListener {

  private final Logger log = LoggerFactory.getLogger(UpgradeContextServletContextListener.class);

  public void contextInitialized(ServletContextEvent sce) {
    XmlWebApplicationContext appContext = loadUpgradeContext(sce);

    log.info("Starting the upgrade procedure.");

    try {
      launchUpgrade(appContext);
    } catch(Exception couldNotCompleteUpdate) {
      log.error("The upgrade procedure could not be completed because the following error was encountered", couldNotCompleteUpdate);
    }

    appContext.close();
    log.info("Completed the upgrade procedure.");

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

  private void launchUpgrade(XmlWebApplicationContext appContext) throws Exception {

    DataSource dataSource = (DataSource) appContext.getBean("dataSource");
    DatabaseMetadataUtil dbMetadataUtil = new DatabaseMetadataUtil(dataSource);

    // Extract the database product name from the metadata.
    String databaseProductName = dbMetadataUtil.getDatabaseProductName();

    // TODO For now the only database supported by the upgrade manager is MySQL. We need to implement a generic solution
    // for database changes, so that they are not implemented using vendor specific DDL scripts.
    if(databaseProductName.equals("MySQL")) {
      UpgradeManager upgradeManager = (UpgradeManager) appContext.getBean("upgradeManager");
      try {
        upgradeManager.executeUpgrade();
      } catch(UpgradeException upgradeFailed) {
        throw new RuntimeException("The was error running the upgrade manager", upgradeFailed);
      }
    } else {
      throw new RuntimeException("The following database is not supported by the upgrade manager: " + databaseProductName);
    }
  }

  public void contextDestroyed(ServletContextEvent sce) {
  }
}
