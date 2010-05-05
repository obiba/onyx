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

import org.obiba.onyx.core.service.ParticipantRegistry;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

/**
 * Factory bean to create a {@link ParticipantRegistry} based on a value set in the onyx-config.properties file.
 */
public class ParticipantRegistryFactoryBean implements FactoryBean<ParticipantRegistry> {

  private static final String FIXED_TYPE = "fixed";

  private static final String RESTFUL_TYPE = "restful";

  private String participantRegistryType;

  private ParticipantRegistry fixedBean;

  private ParticipantRegistry restfulBean;

  @Override
  public ParticipantRegistry getObject() throws Exception {
    if(participantRegistryType.equalsIgnoreCase(RESTFUL_TYPE)) {
      return restfulBean;
    } else {
      return fixedBean;
    }
  }

  @Override
  public Class<?> getObjectType() {
    return ParticipantRegistry.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void validateArgs() throws IllegalArgumentException {
    Assert.notNull(fixedBean, "fixedBean cannot be null");
    Assert.notNull(restfulBean, "restfulBean cannot be null");
    Assert.hasText(participantRegistryType, "participantRegistryType must not be null or empty");
    Assert.isTrue(participantRegistryType.equalsIgnoreCase(FIXED_TYPE) || participantRegistryType.equalsIgnoreCase(RESTFUL_TYPE), "participantRegistryType must contain the value [" + FIXED_TYPE + "] or [" + RESTFUL_TYPE + "].");
  }

  public void setParticipantRegistryType(String participantRegistryType) {
    this.participantRegistryType = participantRegistryType;
  }

  public void setFixedBean(ParticipantRegistry fixedBean) {
    this.fixedBean = fixedBean;
  }

  public void setRestfulBean(ParticipantRegistry restfulBean) {
    this.restfulBean = restfulBean;
  }
}
