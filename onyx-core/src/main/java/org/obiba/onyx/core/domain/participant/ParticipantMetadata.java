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

  private List<ParticipantAttribute> attributes;

  //
  // Constructors
  //

  public ParticipantMetadata() {
    attributes = new ArrayList<ParticipantAttribute>();
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
   * Set the (configurable) participant attributes.
   * 
   * @param attributes participant attributes
   */
  public void setAttributes(List<ParticipantAttribute> attributes) {
    this.attributes.clear();

    if(attributes != null) {
      this.attributes.addAll(attributes);
    }
  }

  /**
   * Returns the (configurable) participant attributes.
   * 
   * @return participant attributes
   */
  public List<ParticipantAttribute> getAttributes() {
    return Collections.unmodifiableList(attributes);
  }

  /**
   * Returns the attribute with the specified name.
   * 
   * @param name attribute name
   * @return attribute with the specified name (or <code>null</code> if no such attribute exists)
   */
  public ParticipantAttribute getAttribute(String name) {
    for(ParticipantAttribute attribute : attributes) {
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

    Resource configPath = resolver.getResource(onyxConfigPath);

    Resource[] resources = null;

    if(configPath != null && configPath.exists()) {
      resources = resolver.getResources(onyxConfigPath + "/" + PARTICIPANT_ATTRIBUTES_FILENAME);
      initAttributes(resources);
    }
  }

  private void initAttributes(Resource[] resources) throws IOException {
    if(resources != null && resources.length > 0) {
      ParticipantAttributeReader reader = new ParticipantAttributeReader();
      reader.setResources(resources);
      List<ParticipantAttribute> attributes = reader.read();

      setAttributes(attributes);
    }
  }
}
