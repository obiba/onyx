package org.obiba.onyx.jade.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.obiba.onyx.jade.remote.RemoteService;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class JnlpClient {

  public static void main(String[] args) throws InterruptedException, InvalidPropertiesFormatException, UnsupportedEncodingException, IOException {
    Properties config = new Properties();
    config.loadFromXML(new ByteArrayInputStream(args[0].getBytes("UTF-8")));

    GenericApplicationContext ctx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
    xmlReader.loadBeanDefinitions(new ClassPathResource("/META-INF/spring/remote-api-client.xml"));

    PropertyPlaceholderConfigurer c = new PropertyPlaceholderConfigurer();
    c.setProperties(config);
    ctx.addBeanFactoryPostProcessor(c);
    
    ctx.refresh();

    RemoteService service = (RemoteService) ctx.getBean("remoteService");
    System.out.println("RemoteService says: " + service.echo("Hello, World!"));

    Thread.sleep(3000);
    ctx.destroy();
    System.exit(0);
  }
}
