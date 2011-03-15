/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.spring.context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.google.common.collect.Maps;

/**
 * A {@code FactoryBean} that creates a special {@code MessageSource} compatible with Onyx's module structure.
 * <p>
 * A {@code MessageSource} is created by finding all bundles that respect Onyx's naming convention:
 * 
 * <pre>
 * onyxConfigPath/&lt;module&gt;/messages_&lt;locale&gt;.{properties,xml}
 * </pre>
 * 
 * <p>
 * Another {@code MessageSource} is created by finding all the bundles named "META-INF/messages" on the classpath. This
 * allows loading bundles from the module's jar files. This new {@code MessageSource} is set as the first's
 * {@code parent}. This allows overriding the default messages by re-defining the same kay in the bundles in the
 * configuration files.
 * <p>
 * Extra bundles may be configured using the {@code extraBasenames} property.
 */
public class OnyxMessageSourceFactoryBean implements FactoryBean, ResourceLoaderAware {

  private static final Logger log = LoggerFactory.getLogger(OnyxMessageSourceFactoryBean.class);

  private static final String MESSAGES_BUNDLENAME = "messages";

  private static final String MESSAGES_PROPERTIES_SUFFIX = ".properties";

  private static final String MESSAGES_XML_SUFFIX = ".xml";

  private ResourceLoader resourceLoader;

  private String onyxConfigPath;

  private Set<String> extraBasenames;

  public void setOnyxConfigPath(String onyxConfigPath) {
    this.onyxConfigPath = onyxConfigPath;
    if(this.onyxConfigPath.endsWith("/") == false) {
      this.onyxConfigPath = this.onyxConfigPath + "/";
    }
  }

  public void setExtraBasenames(Set<String> extraBasenames) {
    this.extraBasenames = extraBasenames;
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public Object getObject() throws Exception {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setResourceLoader(resourceLoader);

    Set<String> basenames = new TreeSet<String>();
    if(this.resourceLoader instanceof ResourcePatternResolver) {
      findBasenames(basenames, MESSAGES_PROPERTIES_SUFFIX);
      findBasenames(basenames, MESSAGES_XML_SUFFIX);
    }
    if(extraBasenames != null) {
      basenames.addAll(extraBasenames);
    }
    String[] basenamesArray = basenames.toArray(new String[] {});
    log.debug("MessageSource contains the following basenames: {}", Arrays.toString(basenamesArray));
    messageSource.setBasenames(basenamesArray);

    MessageSource moduleMessageSource = loadJarBundles();
    messageSource.setParentMessageSource(moduleMessageSource);

    return new StringReferenceFormatingMessageSource(messageSource);
  }

  public Class<?> getObjectType() {
    return MessageSource.class;
  }

  public boolean isSingleton() {
    return true;
  }

  protected void findBasenames(Set<String> basenames, String suffix) throws IOException {
    ResourcePatternResolver resolver = (ResourcePatternResolver) this.resourceLoader;

    // Find all files that match "pathPrefix/**/MESSAGES_BASENAME*suffix"
    // For example, this will find "WEB-INF/config/myModule/messages_en.properties"

    String resourcePattern = onyxConfigPath + "**/" + MESSAGES_BUNDLENAME + "*" + suffix;
    String resourcePatternParent = onyxConfigPath + MESSAGES_BUNDLENAME + "*" + suffix;

    log.debug("Finding resources that match pattern {}", resourcePattern);
    List<Resource> messageResources = new ArrayList<Resource>();
    messageResources.addAll(Arrays.asList(resolver.getResources(resourcePattern)));
    messageResources.addAll(Arrays.asList(resolver.getResources(resourcePatternParent)));

    for(Resource resource : messageResources) {
      String basename = resolveBasename(resource);
      log.debug("Basename for bundle resource {} resolved as {}", resource.getDescription(), basename);
      if(basename != null) {
        basenames.add(basename);
      }
    }
  }

  protected String resolveBasename(Resource bundleResource) {

    // Resource#getFilename() does not return the full path. As such, we must get the underlying File instance.
    // Since all Resource instances are not File instances, this will only work with resources on the filesystem.

    File bundleFile;
    try {
      bundleFile = bundleResource.getFile();
    } catch(IOException e) {
      log.info("Cannot add resource {} to MessageSource because it is not on the filesystem.", bundleResource.getDescription());
      return null;
    }

    String filename = bundleFile.getAbsolutePath();
    // Make file pathname compatible with Spring's pathnames (if necessary)
    if(File.separatorChar != '/') {
      filename = filename.replace(File.separatorChar, '/');
    }
    StringBuilder basename = new StringBuilder(filename);

    // Find the last part that fits the bundle's name
    int basenameIndex = basename.lastIndexOf(MESSAGES_BUNDLENAME);
    int length = basename.length();

    // Delete anything appearing after the bundle's name
    basename.delete(basenameIndex + MESSAGES_BUNDLENAME.length(), length);

    return "file:" + basename.toString();
  }

  protected MessageSource loadJarBundles() throws IOException {
    ResourcePatternResolver resolver = (ResourcePatternResolver) this.resourceLoader;

    String bundlePattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "META-INF/" + MESSAGES_BUNDLENAME + "*" + MESSAGES_PROPERTIES_SUFFIX;

    Resource[] messageResources = resolver.getResources(bundlePattern);

    StaticMessageSource sms = new StaticMessageSource();
    for(int i = 0; i < messageResources.length; i++) {
      Resource resource = messageResources[i];
      Locale locale = extractLocale(resource, MESSAGES_PROPERTIES_SUFFIX);
      log.debug("Found module bundle resource {} with locale {}", resource.getDescription(), locale);

      Properties props = new Properties();
      props.load(resource.getInputStream());
      sms.addMessages(Maps.fromProperties(props), locale);
    }
    return sms;
  }

  protected Locale extractLocale(Resource resource, String suffix) {
    String filename = resource.getFilename();

    StringBuilder locale = new StringBuilder(filename);
    // Find the last part that fits the bundle's name
    int basenameIndex = filename.lastIndexOf(MESSAGES_BUNDLENAME);
    int length = MESSAGES_BUNDLENAME.length();

    // Remove everything before the basename, the basename and one more char to eat the '_'
    locale.delete(0, basenameIndex + length + 1);
    int suffixIndex = locale.lastIndexOf(suffix);
    locale.delete(suffixIndex, locale.length());

    return new Locale(locale.toString());
  }
}
