package org.obiba.onyx.jade.client;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class JnlpClient {

  public static void main(String[] args) {

    // Create application context.
    GenericApplicationContext appContext = loadAppContext(args);

    // Launch measurement process.
    InstrumentRunner instrumentRunner = (InstrumentRunner) appContext.getBean("instrumentRunner");
    try {

      instrumentRunner.initialize();

      try {
        instrumentRunner.run();

      } finally {

        try {
          instrumentRunner.shutdown();
        } catch(Exception wExDuringShutdown) {
          // Errors during shutdown are ignored.
        }

      }

    } catch(Exception wEx) {
      throw new RuntimeException("Error while running instrument!", wEx);

    } finally {
      
      // Make sure application context is destroyed.
      appContext.destroy();
    }

    System.exit(0);

  }

  protected static GenericApplicationContext loadAppContext(String[] requestParams) {

    // Load bootstrap context files.
    GenericApplicationContext appContext = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/bootstrap-context.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/instrument-context.xml"));

    // Load remote API context file and configure server parameters in context file.
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/remote-api-client.xml"));
    Properties paramsProp = loadServiceParams(requestParams);
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
