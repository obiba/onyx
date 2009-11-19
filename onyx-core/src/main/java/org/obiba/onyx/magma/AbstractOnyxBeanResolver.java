/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import org.obiba.magma.ValueSet;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for ValueSetBeanResolvers that resolve Onyx beans.
 */
public abstract class AbstractOnyxBeanResolver implements ValueSetBeanResolver {

  private ParticipantService participantService;

  @Autowired
  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public ParticipantService getParticipantService() {
    return participantService;
  }

  protected Participant getParticipant(ValueSet valueSet) {
    Participant template = new Participant();
    template.setBarcode(valueSet.getVariableEntity().getIdentifier());
    return participantService.getParticipant(template);
  }
}
