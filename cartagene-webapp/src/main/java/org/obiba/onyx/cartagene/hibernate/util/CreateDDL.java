/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.cartagene.hibernate.util;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.Set;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class CreateDDL {

  // System constants for the current platform directory token
  static String fileSep = System.getProperty("file.separator");

  private String path = "";

  private static GenericApplicationContext appContext;

  public static void main(String args[]) {

    // Create application context.
    appContext = loadAppContext();

    CreateDDL c = new CreateDDL();
    c.createDDL();
    System.err.println("Finish");
  }

  protected static GenericApplicationContext loadAppContext() {

    // Load bootstrap context files.
    GenericApplicationContext appContext = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
    xmlReader.loadBeanDefinitions(new FileSystemResource("src/main/webapp/WEB-INF/spring/context.xml"));
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

  public CreateDDL() {
  }

  private AnnotationConfiguration getConfiguration() {
    try {
      AnnotationConfiguration conf = new AnnotationConfiguration();

      Set<Class<?>> annotatedClasses = (Set<Class<?>>) appContext.getBean("annotatedHibernateClasses");
      for(Class cl : annotatedClasses) {
        conf.addAnnotatedClass(cl);
      }

      // naming strategie
      conf.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);

      Properties props = new Properties();
      props.put("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");

      conf.addProperties(props);
      return conf;
    } catch(Throwable ex) {
      // Log exception!
      throw new ExceptionInInitializerError(ex);
    }
  }

  /**
   * Loads the Hibernate configuration information, sets up the database and the Hibernate session factory.
   */
  public void createDDL() {
    System.out.println("testCreateDDL");
    try {
      Configuration conf = getConfiguration();
      String dialect_file = "mysql.sql";

      System.out.println("Generating: " + dialect_file);
      SchemaExport mySchemaExport = new SchemaExport(conf, conf.buildSettings());
      mySchemaExport.setDelimiter(";");

      // Despite the name, the generated create
      // scripts WILL include drop statements at
      // the top of the script!
      mySchemaExport.setOutputFile(path + "create_" + dialect_file);
      mySchemaExport.create(false, false);

      // Generates DROP statements only
      mySchemaExport.setOutputFile(path + "drop_" + dialect_file);
      mySchemaExport.drop(false, false);

      System.out.println(dialect_file + " OK.");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

}
