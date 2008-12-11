/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.client;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.logging.LogManager;

import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.logging.RemoteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class JnlpClient {

  private static final Logger log = LoggerFactory.getLogger(JnlpClient.class);

  public static void main(String[] args) throws Exception {

    LogManager.getLogManager().readConfiguration(JnlpClient.class.getResourceAsStream("/logging.properties"));

    // Create application context.
    GenericApplicationContext appContext = loadAppContext(args);

    addRemoteLoggingHandler(appContext);

    InstrumentExecutionService instrumentExecutionService = (InstrumentExecutionService) appContext.getBean("instrumentExecutionService");
    try {
      // Fetch the InstrumentRunner implementation from the application context and run it.
      InstrumentRunner instrumentRunner = (InstrumentRunner) appContext.getBean("instrumentRunner");
      if(instrumentRunner == null) {
        log.error("Bootstrap was launched without an instrument runner. Make sure the instrument-context.xml file declares a bean named 'instrumentRunner'. This bean must implement the {} interface.", InstrumentRunner.class.getName());
      } else {
        log.info("Initializing Instrument Runner of type {}", instrumentRunner.getClass().getName());
        instrumentRunner.initialize();

        try {
          log.info("Executing runner");
          instrumentRunner.run();
        } catch(Exception e) {
          log.error("Unexpected error while executing runner", e);
          instrumentExecutionService.instrumentRunnerError(e);
        } finally {

          try {
            log.info("Shutting down runner");
            instrumentRunner.shutdown();
          } catch(Exception e) {
            // Errors during shutdown are ignored.
            log.warn("An exception was caught during shutdown, but will be ignored: {}", e.getMessage());
            log.debug("Exception caught:", e);
          }

        }
      }
    } catch(Exception ex) {
      log.error("Unexpected error while initializing runner {}", ex);
      instrumentExecutionService.instrumentRunnerError(ex);
    } finally {
      // Make sure application context is destroyed.
      log.info("Destroying application context");
      appContext.destroy();
    }

    log.info("Exiting VM");
    System.exit(0);

  }

  private static void addRemoteLoggingHandler(GenericApplicationContext appContext) {
    // Add remoteHandler into logger manually
    RemoteHandler remoteHandler = (RemoteHandler) appContext.getBean("remoteHandler");
    if(remoteHandler != null) {
      log.debug("Remote Handler is {}", remoteHandler.getClass().getName());
      LogManager.getLogManager().getLogger("").addHandler(remoteHandler);
    }
  }

  protected static GenericApplicationContext loadAppContext(String[] requestParams) {
    Properties paramsProp = loadServiceParams(requestParams);

    // Load bootstrap context files.
    GenericApplicationContext appContext = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/bootstrap-context.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/instrument-context.xml"));

    // Load remote API context file and configure server parameters in context file.
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/remote-api-client.xml"));
    PropertyPlaceholderConfigurer c = new PropertyPlaceholderConfigurer();
    c.setProperties(paramsProp);
    appContext.addBeanFactoryPostProcessor(c);

    appContext.refresh();

    return appContext;

  }

  protected static Properties loadServiceParams(String[] args) {

    Properties params = new Properties();
    try {

      params.loadFromXML(new ByteArrayInputStream(args[0].getBytes("UTF-8")));
      return params;

    } catch(Exception wErrorLoadingXml) {
      throw new RuntimeException("Error! Client was unable to load application parameters.");
    }

  }

}
