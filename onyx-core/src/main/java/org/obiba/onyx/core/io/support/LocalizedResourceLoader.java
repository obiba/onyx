package org.obiba.onyx.core.io.support;

import java.util.Locale;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.LocalizedResourceHelper;

/**
 * This class overrides LocalizedResourceHelper to replace the DefaultResourceLoader by a more complete implementation
 * provided by the ResourceLoaderAware interface.
 */
public class LocalizedResourceLoader extends LocalizedResourceHelper implements ResourceLoaderAware {

  private String resourcePath;

  private String resourceExtension;

  private ResourceLoader resourceLoader;

  /**
   * Get the resource for the specified Locale.
   * 
   * @param locale The Locale.
   * @return The resource.
   */
  public Resource getLocalizedResource(Locale locale) {
    LocalizedResourceHelper templateLoader = new LocalizedResourceHelper(resourceLoader);
    return templateLoader.findLocalizedResource(resourcePath, resourceExtension, locale);
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public String getResourcePath() {
    return resourcePath;
  }

  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  public String getResourceExtension() {
    return resourceExtension;
  }

  public void setResourceExtension(String resourceExtension) {
    this.resourceExtension = resourceExtension;
  }

}
