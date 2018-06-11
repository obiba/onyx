/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.model;

import java.util.List;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.model.SpringDetachableModel;

public class RegisteredParticipantTubeModel extends SpringDetachableModel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean(name = "activeTubeRegistrationService")
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  private String barcode;

  //
  // Constructors
  //

  public RegisteredParticipantTubeModel(RegisteredParticipantTube registeredParticipantTube) {
    this.barcode = registeredParticipantTube.getBarcode();
  }

  //
  // SpringDetachableModel Methods
  //

  @Override
  protected Object load() {
    RegisteredParticipantTube registeredParticipantTube = null;

    ParticipantTubeRegistration participantTubeRegistration = activeTubeRegistrationService.getParticipantTubeRegistration();

    List<RegisteredParticipantTube> registeredParticipantTubes = participantTubeRegistration.getRegisteredParticipantTubes();

    for(RegisteredParticipantTube aRegisteredParticipantTube : registeredParticipantTubes) {
      if(aRegisteredParticipantTube.getBarcode().equals(barcode)) {
        registeredParticipantTube = aRegisteredParticipantTube;
        break;
      }
    }

    return registeredParticipantTube;
  }
}