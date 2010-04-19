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

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Provides custom error handling for the {@link RestfulParticipantRegistry} {@link RestTemplate}.
 */
public class RestfulParticipantRegistryErrorHandler extends DefaultResponseErrorHandler {

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    if(response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
      throw new NoSuchParticipantException(response.getStatusText());
    }
    throw new ParticipantRegistryLookupException(response.getStatusText());
  }

}
