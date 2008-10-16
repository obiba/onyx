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

import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * <p>This class is for automatic searching annotated classes (i.e. Hibernate entity classes with <code>Entity</code>
 * annotation). It is mostly for use with Hibernate's <code>SessionFactory</code> in the Spring application context,
 * but can be used to find classes that match any annotations. This code is based on William Mo's
 * <code>EntityBeanFinderFactoryBean</code>.
 * </p>
 * 
 * <p>Example bean definition:
 * <pre>
 * &lt;bean id="sessionFactory" class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean"&gt;
 *   &lt;property name="dataSource"&gt;
 *     &lt;ref bean="dataSource"/&gt;
 *   &lt;/property&gt;
 *   &lt;property name="annotatedClasses"&gt;
 *     &lt;bean class="org.obiba.onyx.spring.AnnotatedBeanFinderFactoryBean"&gt;
 *     &lt;!-- Use Apache Ant Pattern --&gt;
 *     &lt;property name="searchPatterns"&gt;
 *       &lt;set&gt;
 *         &lt;value&gt;classpath*:org&#47;obiba&#47;**&#47;*&#47;*.class&lt;/value&gt;
 *         &lt;value&gt;**&#47;foo-core-*.jar&lt;/value&gt;
 *       &lt;/set&gt;
 *     &lt;/property&gt;
 *
 *     &lt;!-- Use Java regular expression to find all domain classes, default is .* --&gt;
 *     &lt;property name="qualifiedClassNamePatterns"&gt;
 *       &lt;set&gt;
 *         &lt;value&gt;^org\.obiba\..*\.domain\..*&lt;/value&gt;
 *       &lt;/set&gt;
 *     &lt;/property&gt;
 *
 *     &lt;!-- Specify annotations to look for in classes --&gt;
 *     &lt;property name="annotationClasses"&gt;
 *       &lt;set&gt;
 *         &lt;value&gt;javax.persistence.Entity&lt;/value&gt;
 *         &lt;value&gt;javax.persistence.Embeddable&lt;/value&gt;
 *         &lt;value&gt;javax.persistence.MappedSuperclass&lt;/value&gt;
 *       &lt;/set&gt;
 *     &lt;/property&gt;
 *
 *       &lt;value&gt;test.package.Foo&lt;/value&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 *   &lt;property name="annotatedPackages"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;test.package&lt;/value&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * </p>
 * 
 * <p>This class is for automatic searching annotated entity classes (i.e. classes with <code>Entity</code> annotation)
 * for Hibernate's <code>SessionFacotry</code> in the Spring application context.</p>
 * 
 * @author William Mo
 * @version Original version downloaded, Jan 29, 2008
 * @see http://forum.springframework.org/showthread.php?t=46630
 * 
 * @author Oren E. Livne
 * @version added supported for other Hibernate annotations, Jan 29, 2008
 */
public class AnnotatedBeanFinderFactoryBean implements ResourceLoaderAware, FactoryBean {
  
  /**
   * A logger that helps identify this class' printouts.
   */
  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Resource resolver.
   */
  private ResourcePatternResolver resolver;

  /**
   * A collection of resource search patterns.
   */
  private final Set<String> searchPatterns = new HashSet<String>();

  /**
   * A collection of qualified class name patterns to find in the selected resources.
   */
  private final Set<String> qualifiedClassNamePatterns = new HashSet<String>();

  /**
   * List of annotation types to match in classes.
   */
  private final Set<Class<? extends Annotation>> annotationClasses = new HashSet<Class<? extends Annotation>>();

  /**
   * The output set of annotated classes.
   */
  private final Set<Class<?>> annotatedClasses = new HashSet<Class<?>>();

  public AnnotatedBeanFinderFactoryBean() {
    // default accepted class name pattern is all
    qualifiedClassNamePatterns.add(".*");
  }

  /**
   * Inject the resource loader.
   * 
   * @param resourceLoader
   * @see org.springframework.context.ResourceLoaderAware#setResourceLoader(org.springframework.core.io.ResourceLoader)
   */
  public void setResourceLoader(ResourceLoader resourceLoader) {
    resolver = (ResourcePatternResolver) resourceLoader;
  }

  /**
   * Return an instance (possibly shared or independent) of the object managed by this factory.
   * <p>
   * As with a BeanFactory, this allows support for both the Singleton and Prototype design pattern.
   * 
   * @return instance of the object managed by this factory
   * @throws Exception in case of creation errors
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  public Object getObject() throws Exception {
    if(annotatedClasses.isEmpty()) {
      searchAnnotatedEntityClasses();
    }

    return annotatedClasses;
  }

  /**
   * Return the type of product made by this factory. In this cases, a class.
   * 
   * @return he type of object that this FactoryBean creates, in this case, a class
   * @see org.springframework.beans.factory.FactoryBean#getObjectType()
   */
  public Class<?> getObjectType() {
    return annotatedClasses.getClass();
  }

  /**
   * Indicates that this bean is a singleton.
   * 
   * @return true
   * @see org.springframework.beans.factory.FactoryBean#isSingleton()
   */
  public boolean isSingleton() {
    return true;
  }

  /**
   * The main method that searches for annotated classes in classpath resources.
   */
  private void searchAnnotatedEntityClasses() {
    // Search resources by every search pattern.
    log.info("searchAnnotatedEntityClasses in " + searchPatterns);
    for(String searchPattern : searchPatterns) {
      try {
        Resource[] resources = resolver.getResources(searchPattern);

        if(resources != null) {
          // Parse every resource.
          for(Resource res : resources) {
            String path = res.getURL().getPath();
            // Path name string should not be empty.
            if(!path.equals("")) {
              if(path.endsWith(".class")) {
                dealWithClasses(path);
              } else if(path.endsWith(".jar")) {
                dealWithJars(res);
              }
            }
          }
        }
      } catch(Exception ignore) {
        log.warn("Resource resolving failed", ignore);
      }
    }
    if(log.isInfoEnabled()) {
      log.info("Annotations to look for: " + annotationClasses);
      log.info("Annotated classes found: " + annotatedClasses);
    }
  }

  /**
   * @param path
   */
  private void dealWithClasses(String path) {
    Set<String> qClassNames = listAllPossibleQualifiedClassNames(path);

    for(String qName : qClassNames) {
      // Apply the qualified class name pattern to improve the searching
      // performance.
      if(matchQualifiedClassNamePatterns(qName)) {
        addPossibleClasses(qName);
      }
    }
  }

  /**
   * @param qName
   */
  private void addPossibleClasses(String qName) {
    Class<?> clazz;
    try {
      clazz = Class.forName(qName);

      // Add the class to the annotatedEntityClasses property.
      if(checkEntityAnnotation(clazz)) {
        annotatedClasses.add(clazz);
      }
    } catch(Exception ignore) {
    } catch(NoClassDefFoundError ignore) {
    }
  }

  /**
   * @param res
   * @throws Exception
   */
  private void dealWithJars(Resource res) throws Exception {
    // Enumerate all entries in this JAR file.
    Enumeration<JarEntry> jarEntries = new JarFile(res.getFile()).entries();
    while(jarEntries.hasMoreElements()) {
      String name = jarEntries.nextElement().getName();

      // If the entry is a class, deal with it.
      if(name.endsWith(".class") && !name.equals("")) {
        // Format the path first.
        name = pathToQualifiedClassName(name);

        // Apply the qualified class name pattern to improve the
        // searching performance.
        if(matchQualifiedClassNamePatterns(name))
        // This is the qualified class name, so add it.
        addPossibleClasses(name);
      }
    }
  }

  /**
   * @param classPath
   * @return
   */
  private Set<String> listAllPossibleQualifiedClassNames(String classPath) {
    Set<String> qualifiedClassNames = new HashSet<String>();

    // Format the path first.
    String path = pathToQualifiedClassName(classPath);

    // Split the QName by the dot (i.e. '.') character.
    String[] pathParts = path.split("\\.");

    // Add the path parts one by one from the end of the array to the
    // beginning.
    StringBuffer qName = new StringBuffer();
    for(int i = pathParts.length - 1; i >= 0; i--) {
      qName.insert(0, pathParts[i]);
      qualifiedClassNames.add(qName.toString());
      qName.insert(0, ".");
    }

    return qualifiedClassNames;
  }

  /**
   * @param path
   * @return
   */
  private String pathToQualifiedClassName(String path) {
    return path.replaceAll("/", ".").replaceAll("\\\\", ".").substring(0, path.length() - ".class".length());
  }

  /**
   * Match a path against a set of qualified class name patterns.
   * 
   * @param path path to match
   * @return result of matching
   */
  private boolean matchQualifiedClassNamePatterns(String path) {
    for(String pattern : qualifiedClassNamePatterns) {
      if(path.matches(pattern)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check whether the class implements at least one of the specified annotation types.
   * 
   * @param clazz class to check
   * @return <code>true</code> if and only if the class implements at least one of the specified annotation types
   */
  private boolean checkEntityAnnotation(Class<?> clazz) {
    for(Class<? extends Annotation> annotationClass : annotationClasses) {
      if(clazz.getAnnotation(annotationClass) != null) {
        if(log.isDebugEnabled()) {
          log.debug("Found class " + clazz.getSimpleName() + " annotation @" + annotationClass.getSimpleName());
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Clean and trim a string read from the context file.
   * 
   * @param string string to clean
   * @return cleaned string
   */
  private String cleanString(String string) {
    return StringUtils.trimAllWhitespace(string.replaceAll("[\t\n]", ""));
  }

  /**
   * Returns the set of resource search patterns.
   * 
   * @return the set of resource search patterns
   */
  public Set<String> getSearchPatterns() {
    return searchPatterns;
  }

  /**
   * Returns the the set of qualified class name patterns.
   * 
   * @return the the set of qualified class name patterns
   */
  public Set<String> getQualifiedClassNamePatterns() {
    return qualifiedClassNamePatterns;
  }

  /**
   * Returns the the set of annotation types.
   * 
   * @return the the set of annotation types
   */
  public Set<Class<? extends Annotation>> getAnnotationClasses() {
    return annotationClasses;
  }

  /**
   * Inject the set of resource search patterns.
   * 
   * @param searchPatterns the set of resource search patterns to set
   */
  public void setSearchPatterns(Set<String> searchPatterns) {
    // Regular expression are sensitive with special characters.
    for(String pattern : searchPatterns) {
      this.searchPatterns.add(cleanString(pattern));
    }
  }

  /**
   * Inject the set of qualified class name patterns.
   * 
   * @param qualifiedClassNamePatterns the set of qualified class name patterns to set
   */
  public void setQualifiedClassNamePatterns(Set<String> qualifiedClassNamePatterns) {
    // Regular expression are sensitive with special characters.
    // Clear default value
    if(qualifiedClassNamePatterns.size() > 0) this.qualifiedClassNamePatterns.clear();
    for(String pattern : qualifiedClassNamePatterns) {
      this.qualifiedClassNamePatterns.add(cleanString(pattern));
    }
  }

  /**
   * Injects the set of annotation types.
   * 
   * @param annotationClasses the set of qualified annotation class names to set.
   */
  @SuppressWarnings("unchecked")
  public void setAnnotationClasses(Set<String> annotationClasses) {
    // Filter tabs and new lines
    for(String annotationClass : annotationClasses) {
      Class<? extends Annotation> clazz;
      try {
        clazz = (Class<? extends Annotation>) Class.forName(cleanString(annotationClass));
        this.annotationClasses.add(clazz);
      } catch(NoClassDefFoundError ignore) {
        throw new FatalBeanException("The class " + annotationClass + " in the annotatedClasses property of the sessionFactory declaration is not an annotation type.");
      } catch(ClassCastException e) {
        throw new FatalBeanException("Could not find annotation class " + annotationClass + " in the annotatedClasses property of the sessionFactory declaration.");
      } catch(Throwable throwable) {
        throw new FatalBeanException("Could not add annotation class " + annotationClass + " to the list of annotations in the annotatedClasses property of the sessionFactory declaration: " + throwable);
      }
    }
  }
}
