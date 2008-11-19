package org.obiba.onyx.core.io.support;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.LocalizedResourceHelper;

/**
 * This class overrides LocalizedResourceHelper to replace the DefaultResourceLoader by a more complete implementation
 * provided by the ResourceLoaderAware interface. Adds also a method to get a list of locales available for a resource.
 */
public class LocalizedResourceLoader extends LocalizedResourceHelper implements ResourceLoaderAware {

  private static final Logger log = LoggerFactory.getLogger(LocalizedResourceLoader.class);

  // The resource base name.
  private String resourceName;

  // The resource file extension.
  private String resourceExtension;

  // The resource base path (see org.springframework.core.io.ResourceLoader for more details).
  private String resourcePath;

  private ResourceLoader resourceLoader;

  /**
   * Get the resource for the specified Locale.
   * 
   * @param locale The Locale.
   * @return The resource.
   */
  public Resource getLocalizedResource(Locale locale) {
    LocalizedResourceHelper templateLoader = new LocalizedResourceHelper(resourceLoader);
    return templateLoader.findLocalizedResource(resourcePath + resourceName, resourceExtension, locale);
  }

  /**
   * Get the available Locales by crawling the resource files.
   * 
   * @return A list of Locales. Null if none are available.
   */
  public List<Locale> getAvailableLocales() {
    final List<Locale> languages = new ArrayList<Locale>();

    Object[] resourceMessageVar = new Object[] { resourcePath, resourceName, resourceExtension };
    log.info("Searching for available locales for resource (path={}, name={}, extension={})", resourceMessageVar);

    // Get the resource "real" path in the file system.
    File dir;
    try {
      dir = new File(resourceLoader.getResource(resourcePath).getURI());

      // If that path does not exist.
    } catch(IOException e) {
      log.error("The resource path was not found (path={}, name={}, extension={})", resourceMessageVar);
      return languages;
    }

    // Iterate over all language files.
    dir.listFiles(new FileFilter() {

      public boolean accept(File file) {
        String fileName = file.getName();
        log.debug("Filename being filtered is {}", fileName);
        if(file.isFile() && fileName.startsWith(resourceName + '_') && fileName.endsWith(resourceExtension)) {
          String localeString = extractLocaleString(fileName);

          log.info("Found the following locale \"{}\" for resource \"{}\"", localeString, resourceName);

          languages.add(new Locale(localeString));
          return true;
        }

        return false;
      }

    });

    if(languages.isEmpty()) {
      log.info("No locales available for resource (path={}, name={}, extension={})", resourceMessageVar);
    }

    return languages;
  }

  private String extractLocaleString(String fileName) {
    String localeString = null;

    int startIndex = fileName.indexOf('_');
    int endIndex = fileName.lastIndexOf(resourceExtension);

    localeString = fileName.substring(startIndex + 1, endIndex);

    return localeString;
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public void setResourcePath(File resourcePath) {
    this.resourcePath = resourcePath.toURI().toString();
  }

  public void setResourcePath(String resourcePath) {
    if(!resourcePath.endsWith("/")) {
      this.resourcePath = resourcePath + "/";
    } else {
      this.resourcePath = resourcePath;
    }
  }

  public void setResourceExtension(String resourceExtension) {
    this.resourceExtension = resourceExtension;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

}
