/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantRegistry;
import org.springframework.web.client.RestTemplate;

/**
 * This implementation of the {@link ParticipantRegistry} retrieves {@link Participant}s from a RESTful web service.
 */
public class RestfulParticipantRegistry implements ParticipantRegistry {

  private String urlTemplate;

  private RestTemplate restTemplate;

  public void setUrlTemplate(String urlTemplate) {
    this.urlTemplate = urlTemplate;
  }

  public RestTemplate getRestTemplate() {
    return restTemplate;
  }

  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public Participant lookupParticipant(String uniqueId) throws NoSuchParticipantException, ParticipantRegistryLookupException {
    if(uniqueId == null) uniqueId = "";
    return restTemplate.getForObject(urlTemplate, Participant.class, uniqueId);
  }
}
