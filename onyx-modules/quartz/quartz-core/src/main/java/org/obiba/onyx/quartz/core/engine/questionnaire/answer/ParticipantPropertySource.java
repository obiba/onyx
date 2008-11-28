/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;

import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get a {@link Participant} property in a {@link Data}.
 */
public class ParticipantPropertySource extends DataSource {

  private static final long serialVersionUID = 5625713001098059689L;

  private static final Logger log = LoggerFactory.getLogger(ParticipantPropertySource.class);

  private String property;

  private String unit;

  public ParticipantPropertySource(String property) {
    super();
    this.property = property;
  }

  public ParticipantPropertySource(String property, String unit) {
    super();
    this.property = property;
    this.unit = unit;
  }

  public String getProperty() {
    return property;
  }

  public Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    Participant participant = activeQuestionnaireAdministrationService.getQuestionnaireParticipant().getParticipant();
    try {
      for(PropertyDescriptor pd : Introspector.getBeanInfo(Participant.class).getPropertyDescriptors()) {
        if(property.equals(pd.getName())) {
          Object propertyValue = pd.getReadMethod().invoke(participant);
          // log.info("source.participant.property." + property + "=" + propertyValue + " " +
          // propertyValue.getClass().getSimpleName());
          if(propertyValue instanceof Gender) {
            propertyValue = propertyValue.toString();
          }
          return DataBuilder.build((Serializable) propertyValue);
        }
      }
    } catch(Exception e) {
      log.error("Could not resolve participant property: " + property, e);
    }
    throw new IllegalArgumentException("Could not resolve participant property: " + property);
  }

  public String getUnit() {
    return unit;
  }
}
