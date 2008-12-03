/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Participant metadata.
 */
public class ParticipantMetadata implements ResourceLoaderAware, InitializingBean {
  //
  // Constants
  //

  private static final String PARTICIPANT_ATTRIBUTES_FILENAME = "participant-attributes.xml";

  //
  // Instance Variables
  //

  private ResourceLoader resourceLoader;

  private String onyxConfigPath;

  private List<ParticipantAttribute> essentialAttributes;

  private List<ParticipantAttribute> configuredAttributes;

  //
  // Constructors
  //

  public ParticipantMetadata() {
    essentialAttributes = new ArrayList<ParticipantAttribute>();
    configuredAttributes = new ArrayList<ParticipantAttribute>();
  }

  //
  // ResourceLoaderAware Methods
  //

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  //
  // InitializingBean Methods
  //

  public void afterPropertiesSet() throws Exception {
    initConfig();
  }

  //
  // Methods
  //

  /**
   * Sets the path to the Onyx configuration folder.
   * 
   * @param onyxConfigPath path to the Onyx configuration folder (e.g., "WEB-INF/config").
   */
  public void setOnyxConfigPath(String onyxConfigPath) {
    this.onyxConfigPath = onyxConfigPath;
  }

  /**
   * Set the essential participant attributes.
   * 
   * @param attributes participant attributes
   */
  public void setEssentialAttributes(List<ParticipantAttribute> attributes) {
    this.essentialAttributes.clear();

    if(attributes != null) {
      this.essentialAttributes.addAll(attributes);
    }
  }

  /**
   * Returns the essential participant attributes.
   * 
   * @return participant attributes
   */
  public List<ParticipantAttribute> getEssentialAttributes() {
    return Collections.unmodifiableList(essentialAttributes);
  }

  /**
   * Returns the essential attribute with the specified name.
   * 
   * @param name attribute name
   * @return attribute with the specified name (or <code>null</code> if no such attribute exists)
   */
  public ParticipantAttribute getEssentialAttribute(String name) {
    for(ParticipantAttribute attribute : essentialAttributes) {
      if(attribute.getName().equals(name)) {
        return attribute;
      }
    }

    return null;
  }

  /**
   * Set the configured participant attributes.
   * 
   * @param attributes participant attributes
   */
  public void setConfiguredAttributes(List<ParticipantAttribute> attributes) {
    this.configuredAttributes.clear();

    if(attributes != null) {
      this.configuredAttributes.addAll(attributes);
    }
  }

  /**
   * Returns the (configurable) participant attributes.
   * 
   * @return participant attributes
   */
  public List<ParticipantAttribute> getConfiguredAttributes() {
    return Collections.unmodifiableList(configuredAttributes);
  }

  /**
   * Returns the configured attribute with the specified name.
   * 
   * @param name attribute name
   * @return attribute with the specified name (or <code>null</code> if no such attribute exists)
   */
  public ParticipantAttribute getConfiguredAttribute(String name) {
    for(ParticipantAttribute attribute : configuredAttributes) {
      if(attribute.getName().equals(name)) {
        return attribute;
      }
    }

    return null;
  }

  /**
   * Loads (or re-loads) configuration information stored in the configuration file.
   * 
   * @throws IOException on an I/O error
   */
  public void initConfig() throws IOException {
    ResourcePatternResolver resolver = (ResourcePatternResolver) this.resourceLoader;
    Resource[] resources = null;

    // Load essential participant attributes.
    resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "META-INF/" + PARTICIPANT_ATTRIBUTES_FILENAME);
    initEssentialAttributes(resources);

    // Load configured participant attributes.
    Resource configPath = resolver.getResource(onyxConfigPath);

    if(configPath != null && configPath.exists()) {
      resources = resolver.getResources(onyxConfigPath + "/" + PARTICIPANT_ATTRIBUTES_FILENAME);
      initConfiguredAttributes(resources);
    }
  }

  private void initEssentialAttributes(Resource[] resources) throws IOException {
    if(resources != null && resources.length > 0) {
      ParticipantAttributeReader reader = new ParticipantAttributeReader();
      reader.setResources(resources);
      List<ParticipantAttribute> attributes = reader.read();

      setEssentialAttributes(attributes);
    }
  }

  private void initConfiguredAttributes(Resource[] resources) throws IOException {
    if(resources != null && resources.length > 0) {
      ParticipantAttributeReader reader = new ParticipantAttributeReader();
      reader.setResources(resources);
      List<ParticipantAttribute> attributes = reader.read();

      setConfiguredAttributes(attributes);
    }
  }
}
