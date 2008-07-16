package org.obiba.onyx.jade.client;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import org.obiba.onyx.jade.instrument.AbstractInstrumentRunner;
import org.obiba.onyx.jade.remote.RemoteService;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class JnlpClient {
  
  public static void main(String[] args) throws Exception {

    GenericApplicationContext appContext = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/bootstrap-context.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/instrument-context.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/remote-api-client.xml"));

    Properties params = loadServiceParams(args);
    PropertyPlaceholderConfigurer c = new PropertyPlaceholderConfigurer();
    c.setProperties(params);
    appContext.addBeanFactoryPostProcessor(c);

    appContext.refresh();

    AbstractInstrumentRunner instrumentRunner = (AbstractInstrumentRunner) appContext.getBean("instrumentRunner");
    instrumentRunner.run();

    RemoteService instrumentService = instrumentRunner.getInstrRemoteService();
    
    System.out.println("RemoteService says: " + instrumentService.echo("Hello World test367!"));

    Thread.sleep(3000);
    appContext.destroy();

    System.exit(0);
  }

  private static Properties loadServiceParams(String[] args) {

    Properties params = new Properties();
    try {

      params.loadFromXML(new ByteArrayInputStream(args[0].getBytes("UTF-8")));
      return params;

    } catch(Exception wErrorLoadingXml) {
      throw new RuntimeException("Error! Client was unable to load application parameters.");
    }

  }

}
