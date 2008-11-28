/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.ruby.core.wicket.model.RegisteredParticipantTubeModel;

public class RegisteredParticipantTubeProvider extends SortableDataProvider {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  //
  // Constructors
  //

  public RegisteredParticipantTubeProvider() {
    InjectorHolder.getInjector().inject(this);
  }

  //
  // SortableDataProvider Methods
  //

  public Iterator<RegisteredParticipantTube> iterator(int first, int count) {
    return activeTubeRegistrationService.getParticipantTubeRegistration().getRegisteredParticipantTubes().iterator();
  }

  public IModel model(Object object) {
    return new RegisteredParticipantTubeModel((RegisteredParticipantTube) object);
  }

  public int size() {
    return activeTubeRegistrationService.getParticipantTubeRegistration().getRegisteredParticipantTubes().size();
  }
}