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
    return templateLoader.findLocalizedResource(getResourceBasename(), resourceExtension, locale);
  }

  /**
   * Returns the full basename of the localized resource.
   * <p>
   * This is the concatenation of {@code resourcePath}, {@code File.separator} and {@code resourceName}.
   * @return
   */
  public String getResourceBasename() {
    return resourcePath + File.separator + resourceName;
  }

  /**
   * Get the available Locales by crawling the resource files.
   * 
   * @return A list of Locales. Null if none are available.
   */
  public List<Locale> getAvailableLocales() {
    final List<Locale> languages = new ArrayList<Locale>();

    Object[] resourceMessageVar = new Object[] { resourcePath, resourceName, resourceExtension };
    log.debug("Searching for available locales for resource (path={}, name={}, extension={})", resourceMessageVar);

    // Get the resource "real" path in the file system.
    File dir;
    try {
      Resource path = resourceLoader.getResource(resourcePath);
      // Doesn't support resource that are not on the file system. Sorry.
      dir = path.getFile();
    } catch(IOException e) {
      log.error("The resource path '{}' cannot be used because it cannot be converted to a File on the filesystem.", resourcePath);
      log.error("LocalizedResourceLoader configuration is [path={}, name={}, extension={}]", resourceMessageVar);
      return languages;
    }

    // Iterate over all language files.
    dir.listFiles(new FileFilter() {

      public boolean accept(File file) {
        String fileName = file.getName();
        log.debug("Filename being filtered is {}", fileName);
        if(file.isFile() && fileName.startsWith(resourceName + '_') && fileName.endsWith(resourceExtension)) {
          String localeString = extractLocaleString(fileName);

          Locale locale = null;
          if(localeString.contains("_")) {
            String[] parts = localeString.split("_");
            if(parts.length == 3) {
              locale = new Locale(parts[0], parts[1], parts[2]);
            } else if(parts.length == 2) {
              locale = new Locale(parts[0], parts[1]);
            } else {
              log.warn("Cannot decode localeString {} to a Locale proper for resource {}.", localeString, resourceName);
            }
          } else {
            locale = new Locale(localeString);
          }

          if(locale != null) {
            log.debug("Found the following locale \"{}\" for resource \"{}\"", locale, resourceName);
            languages.add(locale);
            return true;
          }
        }

        return false;
      }

    });

    if(languages.isEmpty()) {
      log.warn("No locales available for resource (path={}, name={}, extension={})", resourceMessageVar);
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
