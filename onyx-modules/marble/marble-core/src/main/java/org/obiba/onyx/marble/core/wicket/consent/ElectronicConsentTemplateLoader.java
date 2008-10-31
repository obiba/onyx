package org.obiba.onyx.marble.core.wicket.consent;

import java.util.Locale;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class ElectronicConsentTemplateLoader implements ResourceLoaderAware {

  private ResourceLoader resourceLoader;

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  /**
   * Get the consent form template for specified Locale.
   * 
   * @param locale The Locale.
   * @return The consent form template.
   */
  public Resource getConsentTemplate(Locale locale) {
    Resource resource = resourceLoader.getResource("WEB-INF/consent/ConsentForm_" + locale.getLanguage() + ".pdf");

    // If template not found for specified Locale, return default (englist) template.
    if(resource == null) {
      resource = resourceLoader.getResource("WEB-INF/consent/ConsentForm_en.pdf");
    }
    return resource;
  }

}
