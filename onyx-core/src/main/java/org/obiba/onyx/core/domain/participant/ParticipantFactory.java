/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.onyx.core.domain.participant;

import org.obiba.onyx.core.domain.AttributeValue;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;

public class ParticipantFactory {

  private ParticipantMetadata participantMetadata;

  public Participant createWithDefaultEssentialAttributes() {
    Participant participant = new Participant();

    PropertyAccessor participantBean = new BeanWrapperImpl(participant);
    for(ParticipantAttribute attribute : participantMetadata.getEssentialAttributes()) {
      if(attribute.hasDefaultValues()) {
        for(AttributeValue defaultValue : attribute.getDefaultValues()) {
          String fieldName = Participant.essentialAttributeToFieldNameMap.get(attribute.getName());
          participantBean.setPropertyValue(fieldName, defaultValue.getValue());
        }
      }
    }
//    for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
//      if(attribute.hasDefaultValues()) {
//        for(AttributeValue defaultValue : attribute.getDefaultValues()) {
//          participant.getConfiguredAttributeValues().add(defaultValue);
//        }
//      }
//    }
    return participant;
  }

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;
  }

}
